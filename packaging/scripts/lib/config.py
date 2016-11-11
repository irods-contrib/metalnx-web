#	Copyright (c) 2015-2016, EMC Corporation
#
#	Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
import getpass
from base64 import b64encode
from hashlib import md5
from socket import gethostname
from os import path, getenv

KEY_VAL_SEPARATOR = '='

SALT = '!M3t4Lnx@1234'

SETUP_FILE = '.env'


def read_env_file(file_path):
    """
    Function that parses a .env file and return a key-value dictionary
    :param file_path:
    :return: dictionary
    """
    if not file_path or not path.isfile(file_path):
        return None

    data = {}

    with open(file_path, 'r') as f:
        for line in f:
            c = line.split(KEY_VAL_SEPARATOR)
            if len(c) < 2:
                return
            else:
                data[c[0]] = c[1].strip()

    return data


class MetalnxConfigEnv(object):
    def __init__(self, source):
        self.data = read_env_file(source)
        self.data_cache = {}

    def get(self, key):
        return self.data[key] if self.data and key in self.data else ''

    def set(self, option, value):
        """
        Saves all key, values params into memory to be dumped into a file later
        :param option: config param name
        :param value: value for config param
        :return: None
        """
        if not option or not value:
            return

        self.data_cache[option] = value

    def dump(self):
        """
        Dumps new configuration params into the setup file
        :return: None
        """
        if not self.data_cache:
            return

        # keeping existing params that were not set this time
        if self.data:
            for k, v in self.data.iteritems():
                if k not in self.data_cache and v != '':
                    self.data_cache[k] = v

        # writing all params to file
        with open(SETUP_FILE, 'w+') as f:
            for k, v in self.data_cache.iteritems():
                param = '{}={}\n'.format(k, v)
                f.write(param)


class MetalnxConfig(object):

    def __init__(self):
        self.setup = MetalnxConfigEnv(SETUP_FILE)

    def __call__(self, *args, **kwargs):
        """
        Shortcut to get
        """
        return self.get(*args, **kwargs)

    def get(self, option, default=None):
        if not self.setup:
            return None

        value = self.setup.get(option) or default
        return value if value is not None else ''

    def set(self, option, value):
        """
        Sets a new value for an option
        :param option: config param to be kept
        :param value: config param value to be kept
        :return: None
        """
        self.setup.set(option, value)

    def save(self):
        """
        Saves all config params to .env file
        :return:
        """
        self.setup.dump()

config = MetalnxConfig()


def encode_password(pwd):
    """Encodes password"""

    def pick_key(hostname):
        hostname = hostname.split('.')[0]
        s = md5(u'{}{}'.format(SALT, hostname)).digest()
        return sum([ord(c) for c in s])

    def encode(s, k):
        return ''.join([chr((ord(c) ^ k) % 256) for c in s])

    return b64encode(encode(pwd, pick_key(gethostname())))


def get_mlx_url(is_https):
    if is_https:
        return MLX_URL_FORMAT('https', gethostname(), '8443')
    return MLX_URL_FORMAT('http', gethostname(), '8080')

RELEASE_VERSION = '1.0'
BUILD_NUMBER = '257'

TEST_CONNECTION_JAR = '/opt/emc/test-connection.jar'

IRODS_PROPS_FILENAME = 'irods.environment.properties'
DATABASE_PROPS_FILENAME = 'database.properties'

POSTGRESQL = 'postgresql'
MYSQL = 'mysql'

GITHUB_URL = 'https://github.com/Metalnx/metalnx-web'
MLX_URL_FORMAT = '{}://{}:{}/emc-metalnx-web/login/'.format

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

IRODS_PROPS_SPEC = {
    'host': {
        'name': 'irods.host',
        'desc': 'iRODS Host',
        'env_param': 'IRODS_HOST',
        'default': lambda c: config('IRODS_HOST', default='localhost'),
        'order': 1
    },
    'port': {
        'name': 'irods.port',
        'desc': 'iRODS Port',
        'env_param': 'IRODS_PORT',
        'default': lambda c: config('IRODS_PORT', default='1247'),
        'order': 2
    },
    'zone': {
        'name': 'irods.zoneName',
        'desc': 'iRODS Zone',
        'env_param': 'IRODS_ZONE',
        'default': lambda c: config('IRODS_ZONE', default='tempZone'),
        'order': 3
    },
    'auth_scheme': {
        'name': 'irods.auth.scheme',
        'values': ['STANDARD', 'GSI', 'PAM', 'KERBEROS'],
        'desc': 'Authentication Schema',
        'env_param': 'IRODS_AUTH_SCHEME',
        'default': lambda c: config('IRODS_AUTH_SCHEME', default='STANDARD'),
        'order': 4
    },
    'user': {
        'name': 'jobs.irods.username',
        'desc': 'iRODS Admin User',
        'env_param': 'IRODS_USER',
        'default': lambda c: config('IRODS_USER', default='rods'),
        'order': 5
    },
    'password': {
        'name': 'jobs.irods.password',
        'desc': 'iRODS Admin Password',
        'env_param': None,
        'default': lambda c: '',
        'input_method': getpass.getpass,
        'cast': encode_password,
        'order': 6
    }
}

DB_TYPE_SPEC = {
    MYSQL: {
        'db.driverClassName': 'com.mysql.jdbc.Driver',
        'hibernate.dialect': 'org.hibernate.dialect.MySQL5Dialect'
    },
    POSTGRESQL: {
        'db.driverClassName': 'org.postgresql.Driver',
        'hibernate.dialect': 'org.hibernate.dialect.PostgreSQLDialect'
    },
    'url': {
        'name': 'db.url',
        'cast': 'jdbc:{}://{}:{}/{}'.format
    }
}

DB_PROPS_SPEC = {
    'host': {
        'name': 'db.host',
        'desc': 'Metalnx Database Host',
        'env_param': 'DB_HOST',
        'default': lambda c: config('DB_HOST', default='localhost'),
        'order': 1
    },
    'port': {
        'name': 'db.port',
        'desc': 'Metalnx Database Port',
        'env_param': 'DB_PORT',
        'default': lambda c: config('DB_PORT', default='3306' if c == 'mysql' else '5432'),
        'order': 2
    },
    'db_name': {
        'name': 'db.db_name',
        'desc': 'Metalnx Database Name',
        'env_param': 'DB_NAME',
        'default': lambda c: config('DB_NAME', default='metalnx'),
        'order': 3
    },
    'user': {
        'name': 'db.username',
        'desc': 'Metalnx Database User',
        'env_param': 'DB_USER',
        'default': lambda c: config('DB_USER', default='metalnx'),
        'order': 4
    },
    'password': {
        'name': 'db.password',
        'desc': 'Metalnx Database Password',
        'env_param': None,
        'default': lambda c: '',
        'input_method': getpass.getpass,
        'cast': encode_password,
        'order': 5
    }
}

INSTALL_STEPS = [
    'config_java_devel',
    'config_tomcat_home',
    'config_tomcat_shutdown',
    'config_metalnx_package',
    'config_existing_setup',
    'config_war_file',
    'config_database',
    'config_irods',
    'config_restore_conf',
    'config_set_https',
    'config_confirm_props',
    'config_tomcat_startup',
    'config_displays_summary',
]

tomcat_dirs = {
    'versions': ['', '6', '7', '8', '9'],
    'home': '/usr/share/tomcat',
    'conf': '/etc/tomcat',
    'webapps': '/var/lib/tomcat'
}
