EMC METALNX WEB WITH PAM AUTHENTICATION
=======================================

----------------------------------

Copyright © 2015-16 EMC Corporation.

This software is provided under the Software license provided in the <a href="LICENSE"> LICENSE </a> file.

The information in this file is provided “as is.” EMC Corporation makes no representations or warranties of any kind with respect to the information in this publication, and specifically disclaims implied warranties of merchantability or fitness for a particular purpose. 

-------------------------------- 

# PAM Authentication on Metalnx

## iRODS configuration

It is not very straightforward to set up PAM authentication in iRODS. Make sure your iRODS grid is operational with PAM before you proceed. Here are some greate resources you can check out to set up PAM in iRODS:

[SSL and PAM - UGM 2016](http://slides.com/irods/ugm2016-ssl-and-pam#/)
[Setting up iRODS - PAM Authentication](http://slides.com/irods/irods-pam#/)
[iRODS PAM Documentation](https://docs.irods.org/4.1.10/manual/authentication/#pam)

## Java

Assuming that your iRODS grid is running. Since Metalnx is developed in Java, we need to tell the JVM to trust our certifcate (if you follow the links above the certification file will be named `chain.pem`).

To do so, run the following command:

	keytool -import -file /etc/irods/ssl/chain.pem -keystore irodskeystore

The command above will create a keystore called *irodskeystore*.

## Tomcat

* Make sure at this point you are able to authenticate in iRODS using PAM, otherwise the authentication will not work through Metalnx.

Now that you already have the irodskeystore file, you need to add that to Tomcat for Metalnx to work with PAM.

The file you need to modify is `tomcat.conf` and it is normally under `TOMCAT/conf` where `TOMCAT` is your Tomcat home directory. `tomcat.conf` is an environment file that loaded when the Tomcat service starts. It is used 
to change environment configuration of your Tomcat instance.

Add the following line in your `tomcat.conf`:

```
JAVA_OPTS="-Djavax.net.ssl.trustStore=/etc/irods/ssl/irodskeystore"
```

Restart your Tomcat service (`systemctl restart tomcat` or `service tomcat restart`).

## Metalnx 

After setting up both iRODS and Tomcat properly, you are able to deploy Metalnx. Please, follow the Metalnx <a href="INSTALL"> INSTALL </a> guide to install it.


### Issues related to PAM in Metalnx, Jargon and iRODS

https://github.com/DICE-UNC/jargon/issues/215

https://github.com/Metalnx/metalnx-web/issues/2


