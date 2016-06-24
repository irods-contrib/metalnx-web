import re
import subprocess
from base64 import b64encode
from os import path, listdir, rename
from os.path import devnull

import MySQLdb as mysql
import psycopg2 as postgres

from config import *


def log(msg):
    print '    * {}'.format(msg)


def banner():
    """Returns banner string for the configuration script"""
    main_line = '#          Metalnx Installation Script        #'
    return '#' * len(main_line) + '\n' + main_line + '\n' + '#' * len(main_line)


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


class DBConnectionTestMixin:
    def _test_database_connection(self):
        """Tests database connectivity based on the database type"""

        log('Testing database connection...')
        getattr(self, '_connect_{}'.format(self.db_type))()
        log('Database connection successful.')
        return True

    def _connect_mysql(self):
        """Connects to a MySQL database"""
        mysql.connect(host=self.db_host, port=int(self.db_port), user=self.db_user, passwd=self.db_pwd,
                      db=self.db_name).close()

    def _connect_postgresql(self):
        """Connects to a PostgreSQL database"""
        postgres.connect(host=self.db_host, port=self.db_port, user=self.db_user, password=self.db_pwd,
                         database=self.db_name).close()


class IRODSConnectionTestMixin:
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

    @staticmethod
    def _encode_password(pwd):
        """Encodes the given password"""
        return b64encode(pwd)


class FileManipulationMixin:
    def _is_dir_valid(self, d):
        """Checks if a path is a valid directory"""
        return path.exists(d) and path.isdir(d)

    def _is_file_valid(self, f):
        """Checks if a path is a valid file"""
        return path.exists(f) and path.isfile(f)

    def _move_properties_files(self, origin, to):
        files_in_dir = listdir(origin)
        for f in files_in_dir:
            if f.endswith('.properties'):
                rename(path.join(origin, f), path.join(to, f))

    def _write_db_properties_to_file(self):
        """Write database properties into a file"""

        log('Creating Database properties file...')

        with open(self.metalnx_db_properties_path, 'r+') as dbpf:
            mcp = MetalnxConfigParser(self.db_type, dbpf)
            mcp.set('db.username', self.db_user)
            mcp.set('db.password', IRODSConnectionTestMixin._encode_password(self.db_pwd))
            mcp.set('db.url', 'jdbc:{}://{}:{}/{}'.format(self.db_type, self.db_host, self.db_port, self.db_name))
            mcp.write()

        log('Database properties file created.')

    def _write_irods_properties_to_file(self):
        """Write iRODS properties into a file"""
        pass
