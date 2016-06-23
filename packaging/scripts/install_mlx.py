#!/usr/bin/python
import getpass
import subprocess
import sys
from base64 import b64encode
from os import listdir, rename, path, devnull
from tempfile import mkdtemp

import MySQLdb as mysql
import psycopg2 as postgres

IRODS_CONNECTION_MSG = "PING\n"
TEST_CONNECTION_JAR = "test-connection.jar"
IRODS_VERSION_PING_RESPONSE_REGEX = r'<relVersion>(.*)</relVersion>'


def log(msg):
    print '   - {}'.format(msg)

class MetalnxContext:
    def __init__(self):
        self.jar_path = '/usr/bin/jar'
        self.tomcat_home = '/usr/share/tomcat'
        self.metalnx_war_path = '/tmp/emc-tmp/emc-metalnx-web.war'

        self.db_type = ''
        self.db_host = ''
        self.db_port = ''
        self.db_name = ''
        self.db_user = ''
        self.db_pwd = ''

        self.irods_host = ''
        self.irods_port = 1247
        self.irods_auth_schema = 'STANDARD'
        self.irods_db_name = 'ICAT'
        self.irods_zone = ''
        self.irods_user = ''
        self.irods_pwd = ''

        pass

    def config_java_devel(self):
        '''It will make sure the java-devel package is correctly installed'''
        if self._is_file_valid(self.jar_path):
            return True

        raise Exception('Could not find java-devel package.')

    def config_tomcat_home(self):
        '''It will ask for your tomcat home directory and checks if it is a valid one'''
        self.tomcat_home = raw_input('Enter your Tomcat home directory [{}]: '.format(self.tomcat_home))

        # Getting bin/ and webapps/ dirs for current installation of Tomcat
        self.tomcat_bin_dir = path.join(self.tomcat_home, 'bin')
        self.tomcat_webapps_dir = path.join(self.tomcat_home, 'webapps')

        # If all paths are valid, then this is a valid tomcat directory
        if self._is_dir_valid(self.tomcat_home) and self._is_dir_valid(self.tomcat_bin_dir) \
                and self._is_dir_valid(self.tomcat_webapps_dir):
            return True

        raise Exception('Tomcat directory is not valid. Please check the path and try again.')

    def config_metalnx_package(self):
        '''It will check if the Metalnx package has been correctly installed'''
        if self._is_file_valid(self.metalnx_war_path):
            return True

        raise Exception('Could not find Metalnx WAR file. Check if emc-metalnx-web package is installed.')

    def config_exisiting_setup(self):
        '''Looks for existing Metalnx installation on your environment'''

        metalnx_path = path.join(self.tomcat_webapps_dir, 'emc-metalnx-web')
        classes_path = path.join(metalnx_path, 'WEB-INF', 'classes')

        if self._is_dir_valid(metalnx_path) and self._is_dir_valid(classes_path):
            log('Detected current installation of Metalnx. Saving current configuration for further restoring.')

            # Creating temporary directory for backup
            self.tmp_dir = mkdtemp()

            # Listing properties files on current Metalnx installation directory
            files_in_dir = listdir(classes_path)

            for file in files_in_dir:
                if file.endswith('.properties'):
                    rename(path.join(classes_path, file), path.join(self.tmp_dir, file))
        else:
            log('No environment detected. Setting up a new Metalnx instance.')

        return True

    def config_irods(self):
        """It will configure iRODS access"""
        self.irods_host = raw_input('Enter the iRODS Host [{}]: '.format(self.irods_host))
        self.irods_port = raw_input('Enter the iRODS Port [{}]: '.format(self.irods_port))
        self.irods_auth_schema = raw_input(
            'Enter the iRODS Authentication Schema (STANDARD, PAM, GSI or KERBEROS) [{}]: '.format(
                self.irods_auth_schema))
        self.irods_zone = raw_input('Enter the iRODS Zone [{}]: '.format(self.irods_zone))
        self.irods_user = raw_input('Enter the iRODS Admin User [{}]: '.format(self.irods_user))
        self.irods_pwd = getpass.getpass('Enter the iRODS Admin Password (it will not be displayed): ')

        log('Testing iRODS connection...')
        self._test_irods_connection()
        log('iRODS connection successful.')

    def config_database(self):
        """It will configure database access"""

        self.db_host = raw_input('Enter the Metalnx Database Host [{}]: '.format(self.db_host))
        self.db_type = raw_input('Enter the Metalnx Database type (mysql or postgres) [{}]: '.format(self.db_type))
        self.db_port = raw_input('Enter the Metalnx Database port [{}]: '.format(self.db_port))
        self.db_name = raw_input('Enter the Metalnx Database Name [{}]: '.format(self.db_name))
        self.db_user = raw_input('Enter the Metalnx Database User [{}]: '.format(self.db_user))
        self.db_pwd = getpass.getpass('Enter the Metalnx Database Password (it will not be displayed): ')

        log('Testing {} database connection...'.format(self.db_type))
        self._test_database_connection()
        log('Database connection successful.')

    def run_order(self):
        """Defines configuration steps order"""
        return [
            "config_java_devel",
            "config_tomcat_home",
            "config_metalnx_package",
            "config_exisiting_setup",
            "config_database",
            "config_irods"
        ]

    def run(self):
        '''
        Runs Metalnx configuration
        '''

        print self._banner()

        for step, method in enumerate(self.run_order()):
            invokable = getattr(self, method)
            print '[*] Executing {} ({}/{})\n   - {}' \
                .format(method, step + 1, len(self.run_order()), invokable.__doc__)

            try:
                invokable()
            except Exception as e:
                print '[ERROR]: {}'.format(e)
                sys.exit(-1)

        sys.exit(0)

    def _banner(self):
        '''
        Returns banner string for the configuration script
        '''
        main_line = '#          Metalnx Installation Script        #'
        return '#' * len(main_line) + '\n' + main_line + '\n' + '#' * len(main_line)

    def _is_dir_valid(self, d):
        '''
        Checks if a path is a valid directory
        '''
        return path.exists(d) and path.isdir(d)

    def _is_file_valid(self, f):
        '''
        Checks if a path is a valid file
        '''
        return path.exists(f) and path.isfile(f)

    def _test_database_connection(self):
        """Tests database connectivity based on the database type"""

        db_connect_dict = {
            'mysql': self._connect_mysql,
            'postgres': self._connect_postgres
        }

        db_connect_dict[self.db_type]()
        return True

    def _connect_mysql(self):
        """Connects to a MySQL database"""
        log('Attempting to connect to MySQL server running on {}:{}'.format(self.db_host, self.db_port))
        mysql.connect(self.db_host, self.db_user, self.db_pwd, self.db_name).close()

    def _connect_postgres(self):
        """Connects to a PostgreSQL database"""
        log('Attempting to connect to PostgreSQL server running on {}:{}'.format(self.db_host, self.db_port))
        postgres.connect(host=self.db_host, port=self.db_port, user=self.db_user, password=self.db_pwd,
                         database=self.db_name).close()

    def _test_irods_connection(self):
        """Authenticates against iRODS"""
        os_devnull = open(devnull, 'w')
        irods_auth_params = ['java', '-jar', TEST_CONNECTION_JAR, self.irods_host, self.irods_port, self.irods_user,
                             self.irods_pwd, self.irods_zone, self.irods_auth_schema]
        subprocess.check_call(irods_auth_params, stdout=os_devnull)
        return True

    def _encode_password(self, pwd):
        """Encodes the given password"""
        return b64encode(pwd)

    def _write_db_properties_to_file(self):
        """Write database properties into a file"""
        pass

    def _write_irods_properties_to_file(self):
        """Write iRODS properties into a file"""
        pass


def main():
    MetalnxContext().run()


if __name__ == '__main__':
    main()
