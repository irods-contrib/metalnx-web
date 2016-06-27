#!/usr/bin/python
import sys
from os import mkdir, getcwd, chdir, remove
from shutil import rmtree, copyfile
from tempfile import mkdtemp
from xml.etree import ElementTree as ET

from utils import *


class MetalnxContext(DBConnectionTestMixin, IRODSConnectionTestMixin, FileManipulationMixin):
    def __init__(self):
        self.jar_path = '/usr/bin/jar'

        # Tomcat config params
        self.tomcat_home = '/usr/share/tomcat'
        self.tomcat_bin_dir = ''
        self.tomcat_webapps_dir = ''
        self.classes_path = ''

        # Metalnx config params
        self.metalnx_war_path = '/tmp/emc-tmp/emc-metalnx-web.war'
        self.metalnx_db_properties_path = ''
        self.metalnx_irods_properties_path = ''
        self.tmp_dir = None

        # Metalnx properties
        self.irods_props = {}
        self.db_props = {}

        # Existing Metalnx configuration flag
        self.existing_conf = False

    def config_java_devel(self):
        """It will make sure the java-devel package is correctly installed"""
        if self._is_file_valid(self.jar_path):
            return True

        raise Exception('Could not find java-devel package.')

    def config_tomcat_home(self):
        """It will ask for your tomcat home directory and checks if it is a valid one"""
        self.tomcat_home = read_input('Enter your Tomcat directory', default=self.tomcat_home)

        # Getting bin/ and webapps/ dirs for current installation of Tomcat
        self.tomcat_bin_dir = path.join(self.tomcat_home, 'bin')
        self.tomcat_webapps_dir = path.join(self.tomcat_home, 'webapps')

        # If all paths are valid, then this is a valid tomcat directory
        if not self._is_dir_valid(self.tomcat_home) or not self._is_dir_valid(self.tomcat_bin_dir) \
                or not self._is_dir_valid(self.tomcat_webapps_dir):
            raise Exception('Tomcat directory is not valid. Please check the path and try again.')

        return True

    def config_metalnx_package(self):
        """It will check if the Metalnx package has been correctly installed"""
        if not self._is_file_valid(self.metalnx_war_path):
            raise Exception('Could not find Metalnx WAR file. Check if emc-metalnx-web package is installed.')

        return True

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

        if self.existing_conf:
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
            for _, spec in IRODS_PROPS_SPEC.items():
                input_method = spec.get('input_method', raw_input)
                response = input_method('Enter {} [{}]: '.format(spec['desc'], spec['default']))
                response = response if response else spec['default']

                if 'values' in spec and response not in spec['values']:
                    raise Exception('The {} must be {}'.format(spec['desc'], ', '.join(spec['values'])))

                self.irods_props[spec['name']] = response

            self._test_irods_connection()

            self.irods_props[IRODS_PROPS_SPEC['password']['name']] = IRODS_PROPS_SPEC['password']['cast'](
                self.irods_props[IRODS_PROPS_SPEC['password']['name']])

            self._write_irods_properties_to_file()

    def config_database(self):
        """It will configure database access"""

        if not self.existing_conf:
            db_type = read_input('Enter the Metalnx Database type', default='mysql', choices=['mysql', 'postgresql'])

            for key, spec in DB_PROPS_SPEC.items():
                self.db_props[spec['name']] = read_input(
                    'Enter {}'.format(spec['desc']),
                    default=spec['default'],
                    hidden=True if 'password' == key else False,
                    choices=spec['values'] if 'values' in spec else None,
                    allow_empty=True if 'password' == key else False
                )

                # input_method = spec.get('input_method', raw_input)
                # response = input_method('Enter {} [{}]: '.format(spec['desc'], spec['default']))
                # response = response if response else spec['default']

                # if 'values' in spec and response not in spec['values']:
                # raise Exception('The {} must be {}'.format(spec['desc'], ', '.join(spec['values'])))

                #self.db_props[spec['name']] = response

            # Database URL connection
            db_url = DB_TYPE_SPEC['url']['cast'](
                db_type,
                self.db_props[DB_PROPS_SPEC['host']['name']],
                self.db_props[DB_PROPS_SPEC['port']['name']],
                self.db_props[DB_PROPS_SPEC['db_name']['name']]
            )

            self.db_props[DB_TYPE_SPEC['url']['name']] = db_url
            self.db_props.update(DB_TYPE_SPEC[db_type])

            # Hibernate config params
            self.db_props.update(HIBERNATE_CONFIG[db_type])
            self.db_props.update(HIBERNATE_CONFIG['options'])

            self._test_database_connection(db_type)

            # props used to build the DB url connection but do not exist in the database properties file
            del self.db_props[DB_PROPS_SPEC['host']['name']]
            del self.db_props[DB_PROPS_SPEC['port']['name']]
            del self.db_props[DB_PROPS_SPEC['db_name']['name']]

            self.db_props[DB_PROPS_SPEC['password']['name']] = DB_PROPS_SPEC['password']['cast'](
                self.db_props[DB_PROPS_SPEC['password']['name']])

            self._write_db_properties_to_file()

    def config_restore_conf(self):
        """Restoring existing Metalnx configuration"""
        if self.existing_conf:
            self._move_properties_files(self.tmp_dir, self.classes_path)

            # Removing temp directory
            rmtree(self.tmp_dir)

    def config_set_https(self):
        """Sets HTTPS protocol in Tomcat and Metalnx"""
        metalnx_web_xml = path.join(self.classes_path, 'web.xml')
        tomcat_server_xml = path.join(self.tomcat_home, 'conf', 'server.xml')

        log('[DEBUG] Metalnx WEB file: {}'.format(metalnx_web_xml))
        log('[DEBUG] Tomcat server.xml file: {}'.format(tomcat_server_xml))

        server_conf = ET.parse(tomcat_server_xml).getroot()

        is_https = False
        for c in server_conf.find('Service').findall('Connector'):
            if c.get('scheme') is not None and c.get('scheme') == 'https':
                is_https = True
                log('[DEBUG] Found HTTPS connector!')

        if not is_https:
            log('No HTTPS configuration (Connector) found on Tomcat.')
            use_https = read_input('Set HTTPS on your Tomcat server', default='no', choices=['yes', 'no'])

            if use_https == 'yes':
                log('[DEBUG] User wants to setup HTTPS')
                log('Creating keystore for Metalnx...')

                log('A new self-signed certificate will be created in order to enable HTTPS on Tomcat.')
                keystore_path = read_input(
                    'Enter the path for the keystore', default=path.join(self.tomcat_webapps_dir, '.metlanx.keystore.'))
                keystore_password = read_input('Enter the password for the keystore', hidden=True, allow_empty=False)

                log('[DEBUG] Path: [{}] Password: [{}]'.format(keystore_path, keystore_password))

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

                self._backup_files(tomcat_server_xml)
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

            self._backup_files(metalnx_web_xml)
            mlx_web.write(metalnx_web_xml)

    def run(self):
        """Runs Metalnx configuration"""

        print banner()

        for step, method in enumerate(INSTALL_STEPS):
            invokable = getattr(self, method)
            print '\n[*] Executing {} ({}/{})'.format(method, step + 1, len(INSTALL_STEPS))
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
