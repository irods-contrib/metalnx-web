import os
import platform
import psycopg2 as postgres
import MySQLdb as mysql


# MySQLdb lib
# Windows: exe
# yum install mysql-python
# apt-get install python-mysqldb

class MetalnxContext:
    def __init__(self):
        self.db_type = ''
        self.db_host = ''
        self.db_name = ''
        self.db_user = ''
        self.db_pwd = ''
        pass

    def banner(self):
        main_line = '#          Metalnx Installation Script        #'
        return '#' * len(main_line) + '\n' + main_line + '\n' + '#' * len(main_line)

    def config_java_devel(self):
        '''The installation process will make sure the java-devel package is correctly installed'''
        os.stat('/usr/bin/jar')
        return True

    def config_database(self):
        """It will configure database access"""

        self._is_host_reachable(self.db_host)
        self._test_database_connection(self, self.db_type, self.db_host, self.db_user, self.db_pwd, self.db_name)

    def _is_host_reachable(self, host):
        ping_str = "-n 1" if platform.system().lower() == "windows" else "-c 1"
        return os.system("ping " + ping_str + " " + host) == 0

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
        print self.banner()

        # Filtering out method that does not start with 'config_'
        methods_to_run = [m for m in dir(self) if m.startswith('config_')]

        for step, method in enumerate(methods_to_run):
            invokable = getattr(self, method)
            print '[*] Executing {} ({}/{})\n   - {}'.format(method, step + 1, len(methods_to_run), invokable.__doc__)

            try:
                invokable()
            except Exception as e:
                print '[ERROR]: {}'.format(e)


def main():
    MetalnxContext().run()


if __name__ == '__main__':
    main()
