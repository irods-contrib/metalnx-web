#!/usr/bin/python
import getpass
import sys
from os import mkdir, getcwd, chdir, remove
from shutil import rmtree, copyfile
from tempfile import mkdtemp
from xml.etree import ElementTree as ET

from utils import *


class MetalnxContext(DBConnectionTestMixin, IRODSConnectionTestMixin, FileManipulationMixin):
    def __init__(self):
        self.jar_path = '/usr/bin/jar'
        self.tomcat_home = '/usr/share/tomcat'

        self.existing_conf = False

        self.metalnx_war_path = '/tmp/emc-tmp/emc-metalnx-web.war'
        self.metalnx_db_properties_path = 'database.properties'
        self.metalnx_irods_properties_path = 'irods.environment.properties'

        self.db_type = ''
        self.db_host = ''
        self.db_port = ''
        self.db_name = ''
        self.db_user = ''
        self.db_pwd = ''

        self.irods_host = ''
        self.irods_port = 1247
        self.irods_auth_schema = 'STANDARD'
        self.irods_db_name = 'ICAT'
        self.irods_zone = ''
        self.irods_user = ''
        self.irods_pwd = ''

    def config_java_devel(self):
        """It will make sure the java-devel package is correctly installed"""
        if self._is_file_valid(self.jar_path):
            return True

        raise Exception('Could not find java-devel package.')

    def config_tomcat_home(self):
        """It will ask for your tomcat home directory and checks if it is a valid one"""
        self.tomcat_home = raw_input('Enter your Tomcat home directory [{}]: '.format(self.tomcat_home))

        # Getting bin/ and webapps/ dirs for current installation of Tomcat
        self.tomcat_bin_dir = path.join(self.tomcat_home, 'bin')
        self.tomcat_webapps_dir = path.join(self.tomcat_home, 'webapps')

        # If all paths are valid, then this is a valid tomcat directory
        if self._is_dir_valid(self.tomcat_home) and self._is_dir_valid(self.tomcat_bin_dir) \
                and self._is_dir_valid(self.tomcat_webapps_dir):
            return True

        raise Exception('Tomcat directory is not valid. Please check the path and try again.')

    def config_metalnx_package(self):
        """It will check if the Metalnx package has been correctly installed"""
        if self._is_file_valid(self.metalnx_war_path):
            return True

        raise Exception('Could not find Metalnx WAR file. Check if emc-metalnx-web package is installed.')

    def config_existing_setup(self):
        """It will save your current installed of metalnx and will restore them after update"""

        metalnx_path = path.join(self.tomcat_webapps_dir, 'emc-metalnx-web')
        self.classes_path = path.join(metalnx_path, 'WEB-INF', 'classes')

        if self._is_dir_valid(metalnx_path) and self._is_dir_valid(self.classes_path):
            log('Detected current installation of Metalnx. Saving current configuration for further restoring.')

            # Creating temporary directory for backup
            self.tmp_dir = mkdtemp()
            self.existing_conf = True
            self._move_properties_files(self.classes_path, self.tmp_dir)
        else:
            log('No environment detected. Setting up a new Metalnx instance.')

        return True

    def config_war_file(self):
        """The installation process will now handle your new WAR file"""
        metalnx_web_dir = path.join(self.tomcat_webapps_dir, 'emc-metalnx-web')

        log('Removing current Metalnx installation directory')
        rmtree(metalnx_web_dir)

        log('Creating new Metalnx directory')
        mkdir(metalnx_web_dir)

        log('Copying WAR file to the new destination')
        copyfile(self.metalnx_war_path, path.join(metalnx_web_dir, 'emc-metalnx-web.war'))

        log('Entering WAR file directory')
        curr_path = getcwd()
        chdir(metalnx_web_dir)

        log('Extracting new WAR file on the target destination')
        war_path = path.join(metalnx_web_dir, 'emc-metalnx-web.war')
        irods_auth_params = ['jar', '-xf', war_path]
        subprocess.check_call(irods_auth_params)

        log('Going back to the previous working directory')
        chdir(curr_path)

        log('Removing temporary WAR file')
        remove(war_path)

    def config_irods(self):
        """It will configure iRODS access"""

        if not self.existing_conf:
            self.irods_host = raw_input('Enter the iRODS Host [{}]: '.format(self.irods_host))
            self.irods_port = raw_input('Enter the iRODS Port [{}]: '.format(self.irods_port))
            self.irods_auth_schema = raw_input(
                'Enter the iRODS Authentication Schema (STANDARD, PAM, GSI or KERBEROS) [{}]: '.format(
                    self.irods_auth_schema))
            self.irods_zone = raw_input('Enter the iRODS Zone [{}]: '.format(self.irods_zone))
            self.irods_user = raw_input('Enter the iRODS Admin User [{}]: '.format(self.irods_user))
            self.irods_pwd = getpass.getpass('Enter the iRODS Admin Password (it will not be displayed): ')

        print 'Testing iRODS connection...'
        self._test_irods_connection()
        log('iRODS connection successful.')

    def config_database(self):
        """It will configure database access"""

        if not self.existing_conf:
            self.db_host = raw_input('Enter the Metalnx Database Host [{}]: '.format(self.db_host))
            self.db_type = raw_input(
                'Enter the Metalnx Database type (mysql or postgresql) [{}]: '.format(self.db_type))
            self.db_port = raw_input('Enter the Metalnx Database port [{}]: '.format(self.db_port))
            self.db_name = raw_input('Enter the Metalnx Database Name [{}]: '.format(self.db_name))
            self.db_user = raw_input('Enter the Metalnx Database User [{}]: '.format(self.db_user))
            self.db_pwd = getpass.getpass('Enter the Metalnx Database Password (it will not be displayed): ')

            log('Testing {} database connection...'.format(self.db_type))
            self._test_database_connection()
            log('Database connection successful.')

            self._write_db_properties_to_file()

    def config_restore_conf(self):
        """Restoring existing Metalnx configuration"""
        if self.existing_conf:
            self._move_properties_files(self.tmp_dir, self.classes_path)

            # Removign temp directory
            rmtree(self.tmp_dir)

    def config_set_https(self):
        """Sets HTTPS protocol in Tomcat and Metanlx"""
        metalnx_web_xml = path.join(self.tomcat_webapps_dir, 'emc-metalnx-web', 'WEB-INF', 'classes', 'web.xml')
        tomcat_server_xml = path.join(self.tomcat_home, 'conf', 'server.xml')

        server_conf = ET.parse(tomcat_server_xml).getroot()

        is_https = False
        for c in server_conf.getroot().find('Service').findall('Connector'):
            if c.get('scheme') is not None and c.get('scheme') == 'https':
                is_https = True

        if not is_https:
            log('No HTTPS configuration (Connector) found on Tomcat.')
            use_https = raw_input('Would you like Metalnx to configure HTTPS on your Tomcat server? [y/n]').lower()

            if use_https == 'y':
                log('Creating keystore for Metalnx...')

                keystore_path = path.join(self.tomcat_webapps_dir, '.metlanx.keystore.')
                keystore_password = 'abcde1234'

                subprocess.check_call([
                    "keytool",
                    "-genkey",
                    "-keysize", "2048",
                    "-noprompt",
                    "-alias", "metalnx-tomcat",
                    "-dname", "CN=MetaLnx Tester, OU=home, O=home, L=Campinas, ST=SP, C=BR",
                    "-keyalg", "RSA",
                    "-keystore", keystore_path,
                    "-storepass", keystore_password,
                    "-keypass", keystore_password
                ])

                log('Changing server.xml in Tomcat configuration files...')
                connector_spec = """
                    <Connector
                        port=\"8443\"
                        accceptCount=\"100\"
                        protocol=\"org.apache.coyote.http11.Http11Protocol\"
                        disableUploadTiemout=\"true\"
                        enableLookups=\"true\"
                        keystoreFile=\"{}\"
                        maxThreads=\"150\"
                        SSLEnabled=\"true\"
                        scheme=\"https\"
                        secure=\"true\"
                        keystorePass=\"{}\"
                        clientAuth=\"false\"
                        sslProtocol=\"TLS\"
                        ciphers=\"TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256,TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384,TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,TLS_RSA_WITH_AES_128_CBC_SHA256,TLS_RSA_WITH_AES_128_CBC_SHA,TLS_RSA_WITH_AES_256_CBC_SHA256,TLS_RSA_WITH_AES_256_CBC_SHA\" />
                """.format(keystore_path, keystore_password)

                subprocess.check_call([
                    'sed', '-i',
                    's|<Connector.*port=\"8443\".*|-->{}<\!--|'.format(connector_spec.replace('\n', ' ')),
                    tomcat_server_xml
                ])

                is_https = True

        if is_https:
            log('Setting <security-contraint> in Metalnx...')
            mlx_web = ET.parse(metalnx_web_xml)
            r = mlx_web.getroot()
            r.find('security-constraint').find('user-data-constraint') \
                .find('transport-guarantee').text = 'CONFIDENTIAL'
            mlx_web.write(metalnx_web_xml)

    def run(self):
        """Runs Metalnx configuration"""

        print banner()

        for step, method in enumerate(INSTALL_STEPS):
            invokable = getattr(self, method)
            print '[*] Executing {} ({}/{})'.format(method, step + 1, len(INSTALL_STEPS))
            print '   - {}'.format(invokable.__doc__)

            try:
                invokable()
            except Exception as e:
                print '[ERROR]: {}'.format(e)
                sys.exit(-1)

        sys.exit(0)


def main():
    MetalnxContext().run()


if __name__ == '__main__':
    main()
