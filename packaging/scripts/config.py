import getpass
from base64 import b64encode


def encode_password(pwd):
    """Encodes the given password"""
    return b64encode(pwd)


TEST_CONNECTION_JAR = 'test-connection.jar'

IRODS_PROPS_FILENAME = 'irods.environment.properties'
DATABASE_PROPS_FILENAME = 'database.properties'

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
    'config_metalnx_package',
    'config_existing_setup',
    'config_war_file',
    'config_database',
    'config_irods',
    'config_restore_conf',
    'config_set_https'
]
