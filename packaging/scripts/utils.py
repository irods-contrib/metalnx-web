import base64
import re
import subprocess
from datetime import datetime
from os import path, listdir, rename
from os.path import devnull
from shutil import copy

import MySQLdb as mysql
import psycopg2 as postgres

from config import *


def log(msg):
    print '    * {}'.format(msg)


def banner():
    """Returns banner string for the configuration script"""
    main_line = ' ' * 21 + 'Metalnx Installation Script v{}-{}'.format(RELEASE_VERSION, BUILD_NUMBER) + ' ' * 21
    return '\033[44m' + ' ' * len(main_line) + '\n' + main_line + '\n' + ' ' * len(main_line) + '\033[0m'


def encode_password(pwd):
    return base64.b64encode(pwd)


def read_input(question, default=None, hidden=False, choices=None, allow_empty=False, max_iterations=2):
    """
    Auxiliary function to wrap the input capture and validate it against
    specified parameters.
    """

    if hidden and choices:
        raise Exception('Cannot set choices with hidden inputs.')

    if choices is not None and not isinstance(choices, list):
        raise Exception('The \'choices\' parameter must be a list.')

    if default and allow_empty:
        raise Exception('Cannot allow empty and set a default value.')

    if default and choices and default not in choices:
        raise Exception('Default value must be among choices')

    if choices:
        question = '{} ({})'.format(question, ', '.join(choices))

    if default:
        question = '{} [{}]'.format(question, default)

    read_method = raw_input
    if hidden:
        read_method = getpass.getpass
        hidden_alert = '(it will not be displayed)'
        question = '{} {}'.format(question, hidden_alert)

    question += ': '

    iter = 0
    while iter < max_iterations:
        user_input = read_method(question)
        if not user_input:
            if default:
                return default
            elif allow_empty:
                return user_input
            else:
                log('Invalid input: There is no default value defined for this parameter. Try again.')
        else:
            if choices and user_input not in choices:
                log('Invalid input: unknown option. Check the options and try again.')
            else:
                return user_input
        max_iterations += 1

    raise Exception('Too many tries. Please restart the configuration script.')


class MetalnxConfigParser(object):
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

        try:
            subprocess.check_call(irods_auth_params, stdout=os_devnull)
        except:
            raise Exception('Metalnx was not able to contact iRODS server. Check your parameters and try again.')

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
        """Move properties files to specified dir"""
        files_in_dir = listdir(origin)
        for f in files_in_dir:
            if f.endswith('.properties'):
                rename(path.join(origin, f), path.join(to, f))

    def _backup_files(self, files):
        """Adds a timestamp to the file name"""

        def backup_file(file_path):
            """
            Auxiliary function that creates a new copy of the fiven file
            with the timestamp appended to it.
            """
            log('Backing up file [{}]'.format(file_path))
            d = path.dirname(file_path)
            file_name = path.basename(file_path)
            new_file_name = '{}.{}'.format(file_name, datetime.now().strftime('%Y%m%d-%H%M%S'))
            new_path = path.join(d, new_file_name)
            copy(file_path, new_path)

        if isinstance(files, list):
            for f in files:
                backup_file(f)
        else:
            backup_file(files)

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

            mcp.write()

        log('Database properties file created.')

    def _write_irods_properties_to_file(self):
        """Write iRODS properties into a file"""
        log('Creating iRODS properties file...')

        with open(path.join(self.classes_path, IRODS_PROPS_FILENAME), 'r+') as ipf:
            mcp = MetalnxConfigParser(ipf)

            for prop_name, prop_val in self.irods_props.items():
                mcp.set(prop_name, prop_val)

            mcp.write()

        log('iRODS properties file created.')
