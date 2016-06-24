#!/usr/bin/python
import getpass
import re
import subprocess
import sys
from base64 import b64encode
from os import listdir, rename, path, devnull, mkdir, getcwd, chdir, remove
from shutil import rmtree, copyfile
from tempfile import mkdtemp

import MySQLdb as mysql
import psycopg2 as postgres

TEST_CONNECTION_JAR = 'test-connection.jar'

POSTGRESQL = 'postgresql'
MYSQL = 'mysql'

HIBERNATE_CONFIG = {
    MYSQL: {
        'db.driverClassName': 'com.mysql.jdbc.Driver',
        'hibernate.dialect': 'org.hibernate.dialect.MySQL5Dialect',
    },
    POSTGRESQL: {
        'db.driverClassName': 'org.postgresql.Driver',
        'hibernate.dialect': 'org.hibernate.dialect.PostgreSQLDialect'
    },
    'options': {
        'hibernate.show_sql': 'false',
        'hibernate.format_sql': 'false',
        'hibernate.hbm2ddl.auto': 'update',
        'connection.pool_size': '10'
    }
}


class MetalnxConfigParser(object):
    def __init__(self, db_type, fp):
        self.fp = fp
        self.prop_file_content = self.fp.read()
        self.fp.seek(0)

        self.options_dict = HIBERNATE_CONFIG[db_type]
        self.options_dict.update(HIBERNATE_CONFIG['options'])

    def set(self, option, value):
        self.options_dict[option] = value

    def write(self):
        for option, value in self.options_dict.iteritems():
            find_option_regex = re.escape(option) + r'\s*=\s*\S*'
            self.prop_file_content = re.sub(find_option_regex, '{}={}'.format(option, value), self.prop_file_content)

        self.fp.write(self.prop_file_content)


def log(msg):
    print '    * {}'.format(msg)


class MetalnxContext:
    def __init__(self):
        self.jar_path = '/usr/bin/jar'
        self.tomcat_home = '/usr/share/tomcat'

        self.existing_conf = False

        self.metalnx_war_path = '/tmp/emc-tmp/emc-metalnx-web.war'
        self.metalnx_db_properties_path = 'database.properties'
        self.metalnx_irods_properties_path = 'irods.environment.properties'

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

    def config_java_devel(self):
        """It will make sure the java-devel package is correctly installed"""
        if self._is_file_valid(self.jar_path):
            return True

        raise Exception('Could not find java-devel package.')

    def config_tomcat_home(self):
        """It will ask for your tomcat home directory and checks if it is a valid one"""
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
        """It will check if the Metalnx package has been correctly installed"""
        if self._is_file_valid(self.metalnx_war_path):
            return True

        raise Exception('Could not find Metalnx WAR file. Check if emc-metalnx-web package is installed.')

    def config_existing_setup(self):
        """It will save your current installed of metalnx and will restore them after update"""

        metalnx_path = path.join(self.tomcat_webapps_dir, 'emc-metalnx-web')
        self.classes_path = path.join(metalnx_path, 'WEB-INF', 'classes')

        if self._is_dir_valid(metalnx_path) and self._is_dir_valid(self.classes_path):
            log('Detected current installation of Metalnx. Saving current configuration for further restoring.')

            # Creating temporary directory for backup
            self.tmp_dir = mkdtemp()
            self.existing_conf = True
            self._move_properties_files(self.classes_path, self.tmp_dir)
        else:
            log('No environment detected. Setting up a new Metalnx instance.')

        return True

    def config_war_file(self):
        """The installation process will now handle your new WAR file"""
        metalnx_web_dir = path.join(self.tomcat_webapps_dir, 'emc-metalnx-web')

        log('Removing current Metalnx installation directory')
        rmtree(metalnx_web_dir)

        log('Creating new Metalnx directory')
        mkdir(metalnx_web_dir)

        log('Copying WAR file to the new destination')
        copyfile(self.metalnx_war_path, path.join(metalnx_web_dir, 'emc-metalnx-web.war'))

        log('Entering WAR file directory')
        curr_path = getcwd()
        chdir(metalnx_web_dir)

        log('Extracting new WAR file on the target destination')
        war_path = path.join(metalnx_web_dir, 'emc-metalnx-web.war')
        irods_auth_params = ['jar', '-xf', war_path]
        subprocess.check_call(irods_auth_params)

        log('Going back to the previous working directory')
        chdir(curr_path)

        log('Removing temporary WAR file')
        remove(war_path)

    def config_irods(self):
        """It will configure iRODS access"""

        if not self.existing_conf:
            self.irods_host = raw_input('Enter the iRODS Host [{}]: '.format(self.irods_host))
            self.irods_port = raw_input('Enter the iRODS Port [{}]: '.format(self.irods_port))
            self.irods_auth_schema = raw_input(
                'Enter the iRODS Authentication Schema (STANDARD, PAM, GSI or KERBEROS) [{}]: '.format(
                    self.irods_auth_schema))
            self.irods_zone = raw_input('Enter the iRODS Zone [{}]: '.format(self.irods_zone))
            self.irods_user = raw_input('Enter the iRODS Admin User [{}]: '.format(self.irods_user))
            self.irods_pwd = getpass.getpass('Enter the iRODS Admin Password (it will not be displayed): ')

        print 'Testing iRODS connection...'
        self._test_irods_connection()
        print 'iRODS connection successful.'

    def config_database(self):
        """It will configure database access"""

        if not self.existing_conf:
            self.db_host = raw_input('Enter the Metalnx Database Host [{}]: '.format(self.db_host))
            self.db_type = raw_input('Enter the Metalnx Database type (mysql or postgres) [{}]: '.format(self.db_type))
            self.db_port = raw_input('Enter the Metalnx Database port [{}]: '.format(self.db_port))
            self.db_name = raw_input('Enter the Metalnx Database Name [{}]: '.format(self.db_name))
            self.db_user = raw_input('Enter the Metalnx Database User [{}]: '.format(self.db_user))
            self.db_pwd = getpass.getpass('Enter the Metalnx Database Password (it will not be displayed): ')

            log('Testing {} database connection...'.format(self.db_type))
            self._test_database_connection()
            log('Database connection successful.')

            self._write_db_properties_to_file()

    def config_restore_conf(self):
        """Restoring existing Metalnx configuration"""
        if self.existing_conf:
            self._move_properties_files(self.tmp_dir, self.classes_path)

            # Removign temp directory
            rmtree(self.tmp_dir)

    def run_order(self):
        """Defines configuration steps order"""
        return [
            "config_java_devel",
            "config_tomcat_home",
            "config_metalnx_package",
            "config_existing_setup",
            "config_war_file",
            "config_database",
            "config_irods",
            "config_restore_conf"
        ]

    def run(self):
        """Runs Metalnx configuration"""

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
        """Returns banner string for the configuration script"""
        main_line = '#          Metalnx Installation Script        #'
        return '#' * len(main_line) + '\n' + main_line + '\n' + '#' * len(main_line)

    def _is_dir_valid(self, d):
        """Checks if a path is a valid directory"""
        return path.exists(d) and path.isdir(d)

    def _is_file_valid(self, f):
        """Checks if a path is a valid file"""
        return path.exists(f) and path.isfile(f)

    def _test_database_connection(self):
        """Tests database connectivity based on the database type"""

        print 'Testing database connection...'
        getattr(self, '_connect_{}'.format(self.db_type))()
        print 'Database connection successful.'
        return True

    def _connect_mysql(self):
        """Connects to a MySQL database"""
        mysql.connect(host=self.db_host, port=int(self.db_port), user=self.db_user, passwd=self.db_pwd,
                      db=self.db_name).close()

    def _connect_postgresql(self):
        """Connects to a PostgreSQL database"""
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

    def _move_properties_files(self, origin, to):
        files_in_dir = listdir(origin)
        for f in files_in_dir:
            if f.endswith('.properties'):
                rename(path.join(origin, f), path.join(to, f))

    def _write_db_properties_to_file(self):
        """Write database properties into a file"""

        print 'Creating Database properties file...'

        with open(self.metalnx_db_properties_path, 'r+') as dbpf:
            mcp = MetalnxConfigParser(self.db_type, dbpf)
            mcp.set('db.username', self.db_user)
            mcp.set('db.password', MetalnxContext._encode_password(self.db_pwd))
            mcp.set('db.url', 'jdbc:{}://{}:{}/{}'.format(self.db_type, self.db_host, self.db_port, self.db_name))
            mcp.write()

        print 'Database properties file created.'

    def _write_irods_properties_to_file(self):
        """Write iRODS properties into a file"""
        pass

    @staticmethod
    def _encode_password(pwd):
        """Encodes the given password"""
        return b64encode(pwd)


def main():
    MetalnxContext().run()


if __name__ == '__main__':
    main()
