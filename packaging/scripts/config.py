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

INSTALL_STEPS = [
    "config_java_devel",
    "config_tomcat_home",
    "config_metalnx_package",
    "config_existing_setup",
    "config_war_file",
    "config_database",
    "config_irods",
    "config_restore_conf"
]
