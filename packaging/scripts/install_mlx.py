#!/usr/bin/python
import getpass
import sys
from os import listdir, rename
from os import path
from tempfile import mkdtemp

import MySQLdb as mysql
import psycopg2 as postgres


# MySQLdb lib
# Windows: exe
# yum install mysql-python
# apt-get install python-mysqldb

class Ignore:
    '''
    Annotation for ignoring method execution
    Usage:

    ...
        @Ignore
        def my_method(self):
            pass
    ...
    '''

    def __init__(self, m):
        self._method = m

    def __call__(self):
        print 'Ignoring {}'.format(self._method.__name__)


class MetalnxContext:
    def __init__(self):
        self.jar_path = '/usr/bin/jar'
        self.tomcat_home = '/usr/share/tomcat'
        self.metalnx_war_path = '/tmp/emc-temp/emc-metalnx-web.war'
        self.db_type = ''
        self.db_host = ''
        self.db_name = ''
        self.db_user = ''
        self.db_pwd = ''
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
        '''It will save your current installed of metalnx and will restore them after update'''

        metalnx_path = path.join(self.tomcat_webapps_dir, 'emc-metalnx-web')

        if self._is_dir_valid(metalnx_path):
            print '  Detected current installation of Metalnx. Saving current configuration for further restoring.'

            # Listing properties files on current Metalnx installation directory
            properties_path = path.join(metalnx_path, 'WEB-INF', 'classes')
            files_in_dir = listdir(properties_path)

            # Creating temporary directory for backup
            self.tmp_dir = mkdtemp()

            for file in files_in_dir:
                if file.endswith('.properties'):
                    rename(path.join(properties_path, file), path.join(self.tmp_dir, file))

        return True

    def config_database(self):
        """It will configure database access"""

        self.db_host = raw_input('Enter the Metalnx Database Host [{}]: '.format(self.db_host))
        self.db_type = raw_input('Enter the Metalnx Database type (mysql or postgres) [{}]: '.format(self.db_type))
        self.db_name = raw_input('Enter the Metalnx Database Name [{}]: '.format(self.db_name))
        self.db_user = raw_input('Enter the Metalnx Database User [{}]: '.format(self.db_user))
        self.db_pwd = getpass.getpass('Enter the Metalnx Database Password (it will not be displayed): ')

        print 'Testing database connection...'
        self._test_database_connection(self.db_type, self.db_host, self.db_user, self.db_pwd, self.db_name)
        print 'Database connection successful.'

        print 'Metalnx Database configuration done.'

    def run_order(self):
        return [
            "config_java_devel",
            "config_tomcat_home",
            "config_metalnx_package",
            "config_exisiting_setup",
            "config_database"
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

    def _test_database_connection(self, db_type, db_host, db_user, db_pwd, db_name):
        db_connect_dict = {
            'mysql': self._connect_mysql,
            'postgres': self._connect_postgres
        }

        db_connect_dict[db_type](db_host, db_user, db_pwd, db_name)
        return True

    def _connect_mysql(self, db_host, db_user, db_pwd, db_name):
        mysql.connect(db_host, db_user, db_pwd, db_name).close()

    def _connect_postgres(self, db_host, db_user, db_pwd, db_name):
        postgres.connect(host=db_host, user=db_user, password=db_pwd, database=db_name).close()


def main():
    MetalnxContext().run()


if __name__ == '__main__':
    main()
