import getpass
from base64 import b64encode
from socket import gethostname


def encode_password(pwd):
    """Encodes the given password"""
    return b64encode(pwd)


TEST_CONNECTION_JAR = 'test-connection.jar'

IRODS_PROPS_FILENAME = 'irods.environment.properties'
DATABASE_PROPS_FILENAME = 'database.properties'

POSTGRESQL = 'postgresql'
MYSQL = 'mysql'

GITHUB_URL = 'https://github.com/sgworth/metalnx-web'
MLX_URL = 'http://{}:8080/emc-metalnx-web/login'.format(gethostname())

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
        'default': 'localhost'
    },
    'port': {
        'name': 'irods.port',
        'desc': 'iRODS Port',
        'default': '1247'
    },
    'zone': {
        'name': 'irods.zoneName',
        'desc': 'iRODS Zone',
        'default': 'tempZone'
    },
    'auth_scheme': {
        'name': 'irods.auth.scheme',
        'values': ['STANDARD', 'GSI', 'PAM', 'KERBEROS'],
        'desc': 'Authentication Schema',
        'default': 'STANDARD'
    },
    'user': {
        'name': 'jobs.irods.username',
        'desc': 'iRODS Admin User',
        'default': 'rods'
    },
    'password': {
        'name': 'jobs.irods.password',
        'desc': 'iRODS Admin Password (it will not be displayed)',
        'default': '',
        'input_method': getpass.getpass,
        'cast': encode_password
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
        'default': 'localhost'
    },
    'port': {
        'name': 'db.port',
        'desc': 'Metalnx Database Port',
        'default': '3306'
    },
    'db_name': {
        'name': 'db.db_name',
        'desc': 'Metalnx Database Name',
        'default': 'metalnx'
    },
    'user': {
        'name': 'db.username',
        'desc': 'Metalnx Database User',
        'default': 'metalnx'
    },
    'password': {
        'name': 'db.password',
        'desc': 'Metalnx Database Password (it will not be displayed)',
        'default': '',
        'input_method': getpass.getpass,
        'cast': encode_password
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
