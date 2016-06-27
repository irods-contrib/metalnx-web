import base64
import re
import subprocess
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


def encode_password(pwd):
    return base64.b64encode(pwd)


class MetalnxConfigParser:
    def __init__(self, fp):
        self.fp = fp
        self.prop_file_content = self.fp.read()
        self.fp.seek(0)
        self.options_dict = {}

    def set(self, option, value):
        self.options_dict[option] = value

    def write(self):
        for option, value in self.options_dict.iteritems():
            find_option_regex = re.escape(option) + r'\s*=\s*\S*'
            self.prop_file_content = re.sub(find_option_regex, '{}={}'.format(option, value), self.prop_file_content)

        self.fp.write(self.prop_file_content)


class DBConnectionTestMixin:
    def _test_database_connection(self, db_type):
        """Tests database connectivity based on the database type"""

        log('Testing database connection...')

        getattr(self, '_connect_{}'.format(db_type))()

        log('Database connection successful.')

        return True

    def _connect_mysql(self):
        """Connects to a MySQL database"""
        mysql.connect(
            host=self.db_props[DB_PROPS_SPEC['host']['name']],
            port=int(self.db_props[DB_PROPS_SPEC['port']['name']]),
            user=self.db_props[DB_PROPS_SPEC['user']['name']],
            passwd=self.db_props[DB_PROPS_SPEC['password']['name']],
            db=self.db_props[DB_PROPS_SPEC['db_name']['name']]
        ).close()

    def _connect_postgresql(self):
        """Connects to a PostgreSQL database"""

        postgres.connect(
            host=self.db_props[DB_PROPS_SPEC['host']['name']],
            port=self.db_props[DB_PROPS_SPEC['port']['name']],
            user=self.db_props[DB_PROPS_SPEC['user']['name']],
            password=self.db_props[DB_PROPS_SPEC['password']['name']],
            database=self.db_props[DB_PROPS_SPEC['db_name']['name']]
        ).close()


class IRODSConnectionTestMixin:
    def _test_irods_connection(self):
        """Authenticates against iRODS"""
        log('Testing iRODS connection...')

        os_devnull = open(devnull, 'w')

        irods_auth_params = [
            'java', '-jar', TEST_CONNECTION_JAR,
            self.irods_props[IRODS_PROPS_SPEC['host']['name']],
            self.irods_props[IRODS_PROPS_SPEC['port']['name']],
            self.irods_props[IRODS_PROPS_SPEC['user']['name']],
            self.irods_props[IRODS_PROPS_SPEC['password']['name']],
            self.irods_props[IRODS_PROPS_SPEC['zone']['name']],
            self.irods_props[IRODS_PROPS_SPEC['auth_scheme']['name']]
        ]

        subprocess.check_call(irods_auth_params, stdout=os_devnull)

        log('iRODS connection successful.')

        return True


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

    def _write_properties_to_file(self, path, props):
        """Write properties into a file"""

        log('Creating properties file...')

        with open(path, 'r+') as pf:
            mcp = MetalnxConfigParser(self.db_type, pf)

        log('Properties file created.')

    def _write_db_properties_to_file(self):
        """Write database properties into a file"""

        log('Creating Database properties file...')

        with open(path.join(self.classes_path, DATABASE_PROPS_FILENAME), 'r+') as dbpf:
            mcp = MetalnxConfigParser(dbpf)

            for prop_name, prop_val in self.db_props.items():
                mcp.set(prop_name, prop_val)
                log('\tProp {} = {} written.'.format(prop_name, prop_val))

            mcp.write()

        log('Database properties file created.')

    def _write_irods_properties_to_file(self):
        """Write iRODS properties into a file"""
        log('Creating iRODS properties file...')

        with open(path.join(self.classes_path, IRODS_PROPS_FILENAME), 'r+') as ipf:
            mcp = MetalnxConfigParser(ipf)

            for prop_name, prop_val in self.irods_props.items():
                mcp.set(prop_name, prop_val)
                log('\tProp {} = {} written.'.format(prop_name, prop_val))

            mcp.write()

        log('iRODS properties file created.')
