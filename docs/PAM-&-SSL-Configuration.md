This post explains how to configure Metalnx to work with an iRODS grid set up to use SSL and PAM.

There are few different ways to use SSL with our without PAM in your system:

![](https://github.com/Metalnx/metalnx-web/blob/master/docs/IMAGES/mlx-ssl-config.png)

## iRODS

In this section, we provide a quick guide on how to configure iRODS with SSL. If you want more information about it, we encourage you to check out this [SSL and PAM presentation](http://slides.com/irods/ugm2016-ssl-and-pam#/) by iRODS.

To configure SSL in iRODS you need create a directory on the icat server to hold the certificates. For the purpose of this Wiki post, we use self-signed certificates, but the process is similar if you have a certificate issued by a certificate authority. 

Run the following commands to create the private key, certificate and Diffie-Hellman parameters in `/etc/irods/ssl/`:
        
    $ mkdir /etc/irods/ssl
    $ cd /etc/irods/ssl
    $ openssl genrsa -out irods.key                                     # generate an RSA key
    $ openssl req -new -x509 -key irods.key -out irods.crt -days 365    # Generate self-signed certificate
    $ openssl dhparam -2 -out dhparams.pem 2048                         # Generate some Diffie-Hellman parameters

Make sure your */etc/irods/ssl* looks like:

```
$ ls -l /etc/irods/ssl
-rw-rw-r-- 1 irods irods 1277 Dec 13 01:48 irods.crt
-rw-rw-r-- 1 irods irods  424 Dec 13 01:50 dhparams.pem
-rw-rw-r-- 1 irods irods 1675 Dec 13 01:46 server.key
```

In addition, change the iCAT server to require SSL for all connections. Update the `/etc/irods/core.re` file to use `CS_NEG_REQUIRE` as follows:

    acPreConnect(*OUT) { *OUT="CS_NEG_REQUIRE"; }

Now, update the clients to use SSL. The first client we will update is the iRODS service account. Your `irods_environment.json` shoud be similar to this:

```
{
    "irods_host": "icat.com",
    "irods_port": 1247,
    "irods_default_resource": "demoResc",
    "irods_home": "/tempZone/home/rods",
    "irods_cwd": "/tempZone/home/rods",
    "irods_user_name": "rods",
    "irods_zone_name": "tempZone",
    "irods_client_server_negotiation": "request_server_negotiation",
    "irods_client_server_policy": "CS_NEG_REQUIRE",
    "irods_encryption_key_size": 32,
    "irods_encryption_salt_size": 8,
    "irods_encryption_num_hash_rounds": 16,
    "irods_encryption_algorithm": "AES-256-CBC",
    "irods_default_hash_scheme": "SHA256",
    "irods_match_hash_policy": "compatible",
    "irods_server_control_plane_port": 1248,
    "irods_server_control_plane_key": "TEMPORARY__32byte_ctrl_plane_key",
    "irods_server_control_plane_encryption_num_hash_rounds": 16,
    "irods_server_control_plane_encryption_algorithm": "AES-256-CBC",
    "irods_maximum_size_for_single_buffer_in_megabytes": 32,
    "irods_default_number_of_transfer_threads": 4,
    "irods_transfer_buffer_size_for_parallel_transfer_in_megabytes": 4,
    "irods_ssl_certificate_chain_file": "/etc/irods/ssl/irods.crt",
    "irods_ssl_certificate_key_file": "/etc/irods/ssl/server.key",
    "irods_ssl_dh_params_file": "/etc/irods/ssl/dhparams.pem",
    "irods_ssl_ca_certificate_file": "/etc/irods/ssl/irods.crt"
}
```
Notice that we changed the `irods_client_server_policy` to `CS_NEG_REQUIRE` and added SSL configuration parameters:

    "irods_client_server_policy": "CS_NEG_REQUIRE",
    "irods_ssl_certificate_chain_file": "/etc/irods/ssl/irods.crt",
    "irods_ssl_certificate_key_file": "/etc/irods/ssl/server.key",
    "irods_ssl_dh_params_file": "/etc/irods/ssl/dhparams.pem",
    "irods_ssl_ca_certificate_file": "/etc/irods/ssl/irods.crt"

Restart the iRODS service:

    ./iRODS/irodsctl restart

### Log in iRODS

After all that configuration, you should be able to log in iRODS without any problems.

	$ iinit 
	Enter your current iRODS password:
	$ ils
	/tempZone/home/rods
	
### PAM Configuration

This section provides you a quick explanation on how to set up PAM authentication in iRODS. If you do not want use PAM in your system, go to the Metalnx Configuration section.

### iRODS PAM file

First, we need to configure the iRODS PAM file: 

    sudo su - root -c 'echo "auth sufficient pam_unix.so" > /etc/pam.d/irods' 

In this case, the `pam_unix.so` module is used for traditional password authentication.

### Unix User creation

Next, if you want to use the `pam_unix.so` module, every iRODS user must be a Unix user. So, before adding anybody to iRODS make sure you create the corresponding user in unix. For example, with the following lines, we create the user *bob* and set his password by running:

    $ sudo useradd bob
    $ sudo passwd bob

*bob* will be the user used to authenticate in iRODS via PAM.

### iRODS User Creation

This *bob* user still needs to be added to the iRODS database (no need to set a password in iRODS). Do that by running:

    $ iadmin mkuser bob rodsuser

The following instructions are only necessary **if you want to use a command line to interact with iRODS. Metalnx does not require any of the following changes**.

Modify the `irods_environment.json` file on the client to use PAM as the authentication scheme:

    "irods_authentication_scheme": "PAM"

iRODS comes with a tool used for testing basic PAM authentication. You can run it to check if you are able to authenticate:

    iRODS/server/bin/PamAuthCheck metalnx

*Note that this program waits for you to type the password without any prompt. So, after running the command above ensure that you type your password correctly. This tool returns `Authenticated` or `Not Authenticated`*.

Now, update the `irods_environment.json` file on the client, set the `irods_user_name` to *bob* and the `irods_authentication_scheme` to PAM:

```
{
    "irods_host": "icat.renci.org",
    "irods_zone_name": "tempZone",
    "irods_port": 1247,
    "irods_user_name": "bob",
    "irods_authentication_scheme": "PAM",
    "irods_client_server_negotiation": "request_server_negotiation",
    "irods_client_server_policy": "CS_NEG_REQUIRE",
    "irods_ssl_ca_certificate_file": "/etc/irods/ssl/irods.crt",
    "irods_encryption_key_size": 16,
    "irods_encryption_salt_size": 8,
    "irods_encryption_num_hash_rounds": 16,
    "irods_encryption_algorithm": "AES-256-CBC"
}
```

## Metalnx Configuration

The next step is to configure the Metalnx application. Before going any further, make sure you have Java 8 or higher installed. You can check your current java version by running:

    $ java -version
    java version "1.8.0_121"
    Java(TM) SE Runtime Environment (build 1.8.0_121-b13)
    Java HotSpot(TM) 64-Bit Server VM (build 25.121-b13, mixed mode)

Now, we need to tell the Java Virtual Machine to trust the irods certificate created earlier. This can be done by running the following command:

    cd /etc/irods/ssl
    keytool -import -alias irodscertificate -file /etc/irods/ssl/irods.crt -keystore irodskeystore

The command above will create a keystore called *irodskeystore* in `/etc/irods/ssl`. This keystore is necessary for the `setup_script.py` to check whether or not Metalnx can securely connect to iRODS.

After creating the irods keystore file, you can run `python /opt/emc/setup_script.py` to configure Metalnx. At this point, you do not have to provide any additional information about keystores, the script takes care of the setup automatically.

Finally, there is only one configuration to be done. Specify to Tomcat where the `irodskeystore` file is, so the Metalnx Web server is able to communicate with iRODS using SSL. To do so, modify the `JAVA_OPTS` option in the file `/etc/default/tomcat7`:

    JAVA_OPTS="-Djavax.net.ssl.trustStore=/etc/irods/ssl/irodskeystore -Djavax.net.ssl.trustStorePassword=<keystore-password>"

### HTTPS

If you want to use HTTPS protocol for secure communication, you can set it up with the Metalnx setup script. 

During the setup process (`python /opt/emc/setup_script.py`), one of the last steps is to configure HTTPS. The script asks following question:

    Do you want to enable HTTPS on Tomcat and Metalnx? [Y/n]:  

If you answer ***n*** Tomcat will respond to the HTTP protocol on port 8080. If you press ***Y*** or ***y***, it will configure Tomcat to use SSL encryption (HTTPS) on port 8443.

If you encounter a message in the script that reads:

    Starting Tomcat ... Failed. Tomcat could not be started. Please do it manually.

This message means that the installation script could not restart Tomcat on its own.  You will need to take this action manually using the systemctl command (see below).

     # systemctl start tomcat