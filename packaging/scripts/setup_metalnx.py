#!/usr/bin/python
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
import sys
from argparse import ArgumentParser
from os import mkdir, getcwd, chdir, remove
from shutil import rmtree, copyfile
from tempfile import mkdtemp
from xml.etree import ElementTree as ET

from lib.utils import *


class MetalnxContext(DBConnectionTestMixin, IRODSConnectionTestMixin, FileManipulationMixin):
    def __init__(self, conf):
        self.jar_path = '/usr/bin/jar'
        self.keytool_path = '/usr/bin/keytool'

        # Tomcat config params
        self.tomcat_home = ''
        self.tomcat_webapps_dir = ''
        self.tomcat_conf_dir = ''
        self.classes_path = ''
        self.db_type = MYSQL

        # Metalnx config params
        self.metalnx_war_path = '/tmp/emc-tmp/emc-metalnx-web.war'
        self.metalnx_db_properties_path = ''
        self.metalnx_irods_properties_path = ''
        self.tmp_dir = None
        self.is_https = False
        self.override_conf = conf.override

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

        conf = 'no'
        dirs = self._find_valid_tomcat_dirs()
        if dirs:
            # Asking user if he wants to keep the current configuration
            conf = read_input(
                'A Tomcat installation was detected in your system. Do you want to use Tomcat located at: [{}]?'.format(dirs['home']),
                choices=['yes', 'no'],
                default='yes'
            )
        else:
            print 'A Tomcat installation could not be automatically found in your system.'

        if conf == 'yes':
            self.tomcat_home = dirs['home']
            self.tomcat_webapps_dir = dirs['webapps']
            self.tomcat_conf_dir = dirs['conf']
            return True

        default_tomcat_home = config('TOMCAT_HOME_DIR', default='/usr/share/tomcat')
        self.tomcat_home = read_input('Enter your Tomcat directory', default=default_tomcat_home)
        config.set('TOMCAT_HOME_DIR', self.tomcat_home)

        # Check if Tomcat home path is valid
        if not self._is_dir_valid(self.tomcat_home):
            raise Exception('Tomcat directory is not valid. Please check the path and try again.')

        # Getting webapps/ dir for current installation of Tomcat
        self.tomcat_webapps_dir = path.join(self.tomcat_home, 'webapps')

        # Check if the webapps directory exists inside Tomcat home dir, if not, we ask the user to type it in
        if not self._is_dir_valid(self.tomcat_webapps_dir):
            default_webapps_dir = config('TOMCAT_WEBAPPS_DIR')
            self.tomcat_webapps_dir = read_input('Enter your Tomcat webapps directory', default=default_webapps_dir)
            config.set('TOMCAT_WEBAPPS_DIR', self.tomcat_webapps_dir)

            if not self._is_dir_valid(self.tomcat_webapps_dir):
                raise Exception('Tomcat webapps directory [{}] is not valid. Please check the path and try again.'.format(self.tomcat_webapps_dir))

        # Check if the conf/server.xml directory exists inside Tomcat home dir, if not, we ask the user to type it in
        self.tomcat_conf_dir = path.join(self.tomcat_home, 'conf')

        if not self._is_dir_valid(self.tomcat_conf_dir):
            default_conf_dir = config('TOMCAT_CONF_DIR')
            self.tomcat_conf_dir = read_input('Enter your Tomcat conf directory', default=default_conf_dir)
            config.set('TOMCAT_CONF_DIR', self.tomcat_conf_dir)

            if not self._is_dir_valid(self.tomcat_conf_dir):
                raise Exception(
                    'Tomcat conf directory [{}] is not valid. Please check the path and try again.'.format(
                        self.tomcat_conf_dir))

        return True

    def config_tomcat_shutdown(self):
        """Shuts tomcat down before configuration"""
        tomcat = self.tomcat_home[self.tomcat_home.rfind('/')+1:]
        subprocess.call(['service', tomcat, 'stop'])

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
            log('Detected current installation of Metalnx.')

            conf = config('CREATE_NEW_SETUP', default='yes')

            if not self.override_conf:
                # Asking user if he wants to keep the current configuration
                conf = read_input(
                    'Do you wish to use the current setup instead of creating a new one?',
                    choices=['yes', 'no'],
                    default=conf
                )

                config.set('CREATE_NEW_SETUP', conf)

            if self.override_conf or conf == 'no':
                rmtree(path.join(self.tomcat_webapps_dir, 'emc-metalnx-web'))
                return True

            self.existing_conf = True

            # Creating temporary directory for backup
            self.tmp_dir = mkdtemp()
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
            for key, spec in sorted(IRODS_PROPS_SPEC.items(), key=lambda e: e[1]['order']):
                curr_default = config(spec['env_param'], default=spec.get('default', None)(None))

                self.irods_props[spec['name']] = read_input(
                    'Enter {}'.format(spec['desc']),
                    default=curr_default,
                    hidden='password' == key,
                    choices=spec.get('values', None),
                    allow_empty='password' == key
                )

                if spec['env_param']:
                    config.set(spec['env_param'], self.irods_props[spec['name']])

            self._test_irods_connection()

    def config_database(self):
        """It will configure database access"""

        if not self.existing_conf:
            default_db_type = config('DB_TYPE', default='mysql')
            self.db_type = read_input('Enter the Metalnx Database type', default=default_db_type, choices=['mysql', 'postgresql'])
            config.set('DB_TYPE', self.db_type)

            for key, spec in sorted(DB_PROPS_SPEC.items(), key=lambda e: e[1]['order']):
                curr_default = config(spec['env_param'], default=spec.get('default', None)(self.db_type))

                self.db_props[spec['name']] = read_input(
                    'Enter {}'.format(spec['desc']),
                    default=curr_default,
                    hidden='password' == key,
                    choices=spec.get('values', None),
                    allow_empty='password' == key
                )

                if spec['env_param']:
                    config.set(spec['env_param'], self.db_props[spec['name']])

            # Database URL connection
            db_url = DB_TYPE_SPEC['url']['cast'](
                self.db_type,
                self.db_props[DB_PROPS_SPEC['host']['name']],
                self.db_props[DB_PROPS_SPEC['port']['name']],
                self.db_props[DB_PROPS_SPEC['db_name']['name']]
            )

            self.db_props[DB_TYPE_SPEC['url']['name']] = db_url
            self.db_props.update(DB_TYPE_SPEC[self.db_type])

            # Hibernate config params
            self.db_props.update(HIBERNATE_CONFIG[self.db_type])
            self.db_props.update(HIBERNATE_CONFIG['options'])

            self._test_database_connection(self.db_type)

    def config_restore_conf(self):
        """Restoring existing Metalnx configuration"""
        if self.existing_conf:
            self._move_properties_files(self.tmp_dir, self.classes_path)

            # Removing temp directory
            rmtree(self.tmp_dir)

    def config_set_https(self):
        """Sets HTTPS protocol in Tomcat and Metalnx"""
        metalnx_web_xml = path.join(self.tomcat_webapps_dir, 'emc-metalnx-web', 'WEB-INF', 'web.xml')
        tomcat_server_xml = path.join(self.tomcat_conf_dir, 'server.xml')

        server_conf = ET.parse(tomcat_server_xml).getroot()

        for c in server_conf.find('Service').findall('Connector'):
            if c.get('scheme') is not None and c.get('scheme') == 'https':
                self.is_https = True

        if not self.is_https:
            log('No HTTPS configuration (Connector) found on Tomcat.')

            default_use_https = config('HTTPS_ENABLED', default='no')
            use_https = read_input('Set HTTPS on your Tomcat server', default=default_use_https, choices=['yes', 'no'])
            config.set('HTTPS_ENABLED', use_https)

            if use_https == 'yes':

                if not self._is_file_valid(self.keytool_path):
                    raise Exception(
                        'Java devel package is not installed correctly. '
                        'Metalnx could not find the \'keytool\' binary.'
                    )

                log('Creating keystore for Metalnx...')

                log('A new self-signed certificate will be created in order to enable HTTPS on Tomcat.')

                default_keystore_path = config('KEYSTORE_PATH',
                                               default=path.join(self.tomcat_webapps_dir, 'metalnx.keystore'))

                keystore_path = read_input('Enter the path for the keystore', default_keystore_path)

                config.set('KEYSTORE_PATH', keystore_path)

                keystore_password = read_input('Enter the password for the keystore', hidden=True, allow_empty=False)

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

                new_path = path.join(self.tomcat_conf_dir, 'server.xml')
                self._backup_files(new_path)
                subprocess.check_call([
                    'sed', '-i',
                    's|<Connector.*port=\"8443\".*|-->{}<\!--|'.format(connector_spec.replace('\n', ' ')),
                    new_path
                ])

                self.is_https = True

        if self.is_https:
            log('Setting <security-contraint> in Metalnx...')
            self._backup_files(metalnx_web_xml)
            subprocess.check_call([
                'sed', '-i',
                's|>NONE<|>CONFIDENTIAL<|', metalnx_web_xml
            ])

    def config_confirm_props(self):
        """Confirm configuration parameters"""

        if self.existing_conf:
            return True

        print '\n\033[1mMetalnx Database Parameters:\033[0m'
        log('db.type = {}'.format(self.db_type))

        for prop_name, prop_val in self.db_props.items():
            if prop_name != DB_PROPS_SPEC['password']['name'] \
                    and prop_name != DB_TYPE_SPEC['url']['name'] \
                    and prop_name not in DB_TYPE_SPEC[self.db_type].keys() \
                    and prop_name not in HIBERNATE_CONFIG['options'].keys():
                log('{} = {}'.format(prop_name, prop_val))

        print

        print '\033[1miRODS Paramaters:\033[0m'

        for prop_name, prop_val in self.irods_props.items():
            if prop_name != IRODS_PROPS_SPEC['password']['name']:
                log('{} = {}'.format(prop_name, prop_val))

        setup_conf = read_input(
            '\n\033[1mDo you accept these configuration paramaters?\033[0m',
            default='yes',
            choices=['yes', 'no']
        )

        if setup_conf == 'no':
            raise Exception('Metalnx Configuration Setup Aborted.')

        # props used to build the DB url connection but do not exist in the database properties file
        del self.db_props[DB_PROPS_SPEC['host']['name']]
        del self.db_props[DB_PROPS_SPEC['port']['name']]
        del self.db_props[DB_PROPS_SPEC['db_name']['name']]

        self.db_props[DB_PROPS_SPEC['password']['name']] = DB_PROPS_SPEC['password']['cast'](
            self.db_props[DB_PROPS_SPEC['password']['name']])

        self.irods_props[IRODS_PROPS_SPEC['password']['name']] = IRODS_PROPS_SPEC['password']['cast'](
            self.irods_props[IRODS_PROPS_SPEC['password']['name']])

        self._write_db_properties_to_file()
        self._write_irods_properties_to_file()

    def config_tomcat_startup(self):
        """Starting tomcat back again"""
        tomcat = self.tomcat_home[self.tomcat_home.rfind('/')+1:]
        subprocess.call(['service', tomcat, 'start'])

    def config_displays_summary(self):
        """Metalnx configuration finished"""
        print
        print '\033[92mMetalnx configuration finished successfully!\033[0m'
        #print '\033[4m\nPlease, restart your Tomcat for the changes to be applied\n\033[0m\n'
        print 'You can access your Metalnx instance at:\n    \033[4m{}\033[0m\n'.format(get_mlx_url(self.is_https))
        print 'For further information and help, refer to:\n    \033[4m{}\033[0m\n'.format(GITHUB_URL)
        print

    def run(self):
        """Runs Metalnx configuration"""

        print banner()

        for step, method in enumerate(INSTALL_STEPS):
            invokable = getattr(self, method)
            print '\n\033[1m[*] Executing \033[32m{}\033[0m\033[1m ({}/{})\033[0m'.format(method, step + 1,
                                                                                          len(INSTALL_STEPS))
            print '   - {}'.format(invokable.__doc__)

            try:
                invokable()
                config.save()  # saving config params entered into a file
            except Exception as e:
                print '\033[31m[ERROR]: {}\n\033[0m'.format(e)
                sys.exit(-1)
        sys.exit(0)


def main(args):
    MetalnxContext(args).run()


if __name__ == '__main__':
    parser = ArgumentParser(description='Installs and configures Metalnx on Tomcat')
    parser.add_argument(
        '--override-conf', dest='override', action='store_true', default=False,
        help='Overrides existing configuration and setup a new profile based on user inputs'
    )
    args = parser.parse_args()
    main(args)
