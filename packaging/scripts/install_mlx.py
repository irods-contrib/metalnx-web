#!/usr/bin/python
import platform
import sys
import getpass
from os import path, stat, system

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

        self.db_type = ''
        self.db_host = ''
        self.db_name = ''
        self.db_user = ''
        self.db_pwd = ''

        self.irods_host = ''
        self.irods_port = ''
        self.irods_db_name = 'ICAT'
        self.irods_zone = ''
        self.irods_user = ''
        self.irods_pwd = ''

        pass

    @Ignore
    def config_java_devel(self):
        '''It will make sure the java-devel package is correctly installed'''
        stat(self.jar_path)
        return True

    @Ignore
    def config_tomcat_home(self):
        '''It will ask for your tomcat home directory and checks if it is a valid one'''
        self.tomcat_home = raw_input('Enter your Tomcat home directory [{}]: '.format(self.tomcat_home))

        # Getting bin/ and webapps/ dirs for current installation of Tomcat
        self.tomcat_bin_dir = path.join(self.tomcat_home, 'bin')
        self.tomcat_webapps_dir = path.join(self.tomcat_home, 'webapps')

        # If all paths are valid, then this is a valid tomcat directory
        if path.exists(self.tomcat_home) and path.isdir(self.tomcat_home) \
                and path.exists(self.tomcat_bin_dir) and path.isdir(self.tomcat_bin_dir) \
                and path.exists(self.tomcat_webapps_dir) and path.isdir(self.tomcat_webapps_dir):
            return True

        raise Exception('Tomcat directory is not valid. Please check the path and try again.')

    def config_irods(self):
        """It will configure iRODS access"""
        self.irods_host = raw_input('Enter the iRODS Host [{}]: '.format(self.irods_host))
        self.irods_port = raw_input('Enter the iRODS Port [{}]: '.format(self.irods_port))
        self.irods_zone = raw_input('Enter the iRODS Zone [{}]: '.format(self.irods_zone))
        self.irods_user = raw_input('Enter the iRODS Admin User [{}]: '.format(self.irods_user))
        self.irods_pwd = getpass.getpass('Enter the iRODS Admin Password (it will not be displayed): ')

        print 'Testing iRODS connection...'
        self._test_database_connection('postgres', self.irods_host, self.irods_user, self.irods_pwd, self.irods_db_name)
        print 'iRODS connection successful.'

        print 'iRODS Configuration done.'

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

    def run(self):
        '''
        Runs Metalnx configuration
        '''

        print self._banner()

        # Filtering out method that does not start with 'config_'
        methods_to_run = [m for m in dir(self) if m.startswith('config_')]

        for step, method in enumerate(methods_to_run):
            invokable = getattr(self, method)
            print '[*] Executing {} ({}/{})\n   - {}'.format(method, step + 1, len(methods_to_run), invokable.__doc__)

            try:
                invokable()
            except Exception as e:
                print '[ERROR]: {}'.format(e)
                sys.exit(-1)

        sys.exit(0)

    def _banner(self):
        main_line = '#          Metalnx Installation Script        #'
        return '#' * len(main_line) + '\n' + main_line + '\n' + '#' * len(main_line)


def main():
    MetalnxContext().run()


if __name__ == '__main__':
    main()
