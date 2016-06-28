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
from socket import gethostname


def encode_password(pwd):
    """Encodes the given password"""
    return b64encode(pwd)


RELEASE_VERSION = '1.0'
BUILD_NUMBER = '100'

TEST_CONNECTION_JAR = 'test-connection.jar'

IRODS_PROPS_FILENAME = 'irods.environment.properties'
DATABASE_PROPS_FILENAME = 'database.properties'

POSTGRESQL = 'postgresql'
MYSQL = 'mysql'

GITHUB_URL = 'https://github.com/sgworth/metalnx-web'
MLX_URL = 'http://{}:8080/emc-metalnx-web/login/'.format(gethostname())

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
        'default': 'localhost',
        'order': 1
    },
    'port': {
        'name': 'irods.port',
        'desc': 'iRODS Port',
        'default': '1247',
        'order': 2
    },
    'zone': {
        'name': 'irods.zoneName',
        'desc': 'iRODS Zone',
        'default': 'tempZone',
        'order': 3
    },
    'auth_scheme': {
        'name': 'irods.auth.scheme',
        'values': ['STANDARD', 'GSI', 'PAM', 'KERBEROS'],
        'desc': 'Authentication Schema',
        'default': 'STANDARD',
        'order': 4
    },
    'user': {
        'name': 'jobs.irods.username',
        'desc': 'iRODS Admin User',
        'default': 'rods',
        'order': 5
    },
    'password': {
        'name': 'jobs.irods.password',
        'desc': 'iRODS Admin Password',
        'default': '',
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
        'default': 'localhost',
        'order': 1
    },
    'port': {
        'name': 'db.port',
        'desc': 'Metalnx Database Port',
        'default': '3306',
        'order': 2
    },
    'db_name': {
        'name': 'db.db_name',
        'desc': 'Metalnx Database Name',
        'default': 'metalnx',
        'order': 3
    },
    'user': {
        'name': 'db.username',
        'desc': 'Metalnx Database User',
        'default': 'metalnx',
        'order': 4
    },
    'password': {
        'name': 'db.password',
        'desc': 'Metalnx Database Password',
        'default': '',
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
    'config_tomcat_startup',
    'config_displays_summary',
]
