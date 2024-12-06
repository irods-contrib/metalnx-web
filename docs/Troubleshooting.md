# Setup Tomcat for Remote Connections

Follow the steps below to allow remote connections to the Tomcat server:

Ensure that you have installed the [Tomcat](https://github.com/Metalnx/metalnx-web/wiki/Dependencies#web-server) package. Package installation can be verified with the `rpm` command on CentOS systems and `dpkg` on Debian based systems.  For example, on CentOS 7 the following command will list all tomcat packages:

    # rpm –qa | grep tomcat

On Ubuntu the command looks like:

    # dpkg –l | grep tomcat

You can read through the resulting list to identify packages.

*Depending on the repository structure you may find that you also need to install the package `tomcat-admin-webapps` in order to obtain the Tomcat administrative GUI.*

The Tomcat Manager is a web application that can be used interactively (via an HTML GUI) or programmatically (via a URL-based API) to deploy and manage web applications. In order to connect to the Tomcat server remotely, it is necessary to edit parameters in the Linux firewall to allow this. This is best done by modifying the firewall configuration.  

## Firewall-cmd (CentOS 7)

If you are using CentOS 7+ you should be using `firewall-cmd` to configure firewall policies on your server. The configuration changes can be achieved by typing the following commands in your terminal:
    
    firewall-cmd --zone=public --add-port=8080/tcp --permanent          # Tomcat configuration
    firewall-cmd --zone=public --add-port=1247/tcp --permanent          # iRODS configuration
    firewall-cmd --zone=public --add-port=1248/tcp --permanent          # iRODS configuration
    firewall-cmd --zone=public --permanent --add-port=20000-20199/tcp   # iRODS configuration
    firewall-cmd --zone=public --permanent --add-port=20000-20199/udp   # iRODS configuration
    firewall-cmd --zone=public --add-service=http --permanent           # Web server
    firewall-cmd --zone=public --add-service=https --permanent          # Web server
    firewall-cmd --reload

*If your environment has special network security needs please consult your Network Administrator prior to making firewall changes to ensure the necessary ports are allowed for use on your network and will NOT intefer with other critical network traffic or security policies.*

## Iptables

If `firewall-cmd` is not available, you can use iptables to configure your firewall. Using an editor, such as `vi`, edit the file `/etc/sysconfig/iptables`.  Comment out the line: 

	-A INPUT -j REJECT --reject-with icmp-host-prohibited 

By adding the character `#` to the first column position.

Next, add the following line below the one commented out: 

	-A INPUT -m state --state NEW -m tcp -p tcp --dport 8080 -j ACCEPT

If you plan to allow `https` also add the line:

	-A INPUT -m state --state NEW -m tcp -p tcp --dport 8443 -j ACCEPT

*If you will use ONLY SSL encryption on your Web connections to Metalnx (`https`) on the first of these added lines substitute port number 8443 where you see port 8080 above. This operation will open the secure https port for use under Tomcat. There is no need for the second line.*

*If you use a graphical firewall editor make your changes with this tool and do not modify the `iptables` file directly.  The GUI changes will override manual edits.*

Now, restart the iptables service:

    $ sudo systemctl restart iptables

## Configure Tomcat roles

In the Tomcat configuration directory (likely in `/usr/share/tomcat/conf`) and add the following lines to the file `tomcat-users.xml` between the tags `<tomcat-users> </tomcat-users>`. This will create a manager user in Tomcat to support remote application management. **NOTE: Take care not to add these lines in sections that are commented out.**

    <role rolename="manager"/>
    <user username="your_username_here" password="your_password_here" roles="manager"/>

    <role rolename="admin-gui"/>
    <user username="admin" password="metalnx" roles="manager-gui,admin-gui"/>

Pick any username / password combination you want.

In the same directory open the file `server.xml`. The easiest method is to add the following line between the lines: `<Connector port = “8080” protocol=”HTTP/1.1”` and `connectionTimeout=”20000”`:

    address="0.0.0.0"

This line will open access to any system which connects to Tomcat via port 8080.

*Please consult with your Network Administrator prior to this step if your organization has security policies related to network access.  This line opens Tomcat to any and all connection requests.  It is possible to restrict access to Tomcat.  Please consult the Tomcat documentation for information on how to implement security.*

Restart the tomcat service using the commands:

	systemctl restart tomcat

It should be enough for you to have access to Metalnx at `http://<hostname>:8080/metalnx/login/`.

# Migrating from Metalnx 1.0.X to Metalnx 1.1.X

If you already have an instance of Metalnx up and running on your environment and want to upgrade it to the latest version make sure you read this note. There are some configuration parameters that have been added and are required by the application to work properly with microservices.

After downloading the Metalnx package and installing it, run `python /opt/emc/setup_metalnx.py`. This script will set up the environment for you. Well, in the particular case of migration, everything is already set up and your configuration files already have the correct values for your **current** environment.

However, there is a very important step in the script that deserves special attention when migrating Metalnx. The fifth step of the set up process, the script will ask whether or not you want to use your current set up. Make sure you answer ***no***.

```
[*] Executing config_existing_setup (5/13)
   - It will save your current installed of metalnx and will restore them after update
    * Detected current installation of Metalnx.
Do you wish to use the current setup instead of creating a new one? (yes, no) [no]:
```

By answering ***no***, the script will be forced to ask you the credentials once again, but most importantly, it will ***update*** your configuration files and they will now be compatible with the latest Metalnx version. 

***Do not worry about having to retype everything again, Metalnx keeps the values you last typed in, so you do not have to write things that you already have defined before.***


## Setup iRODS Negotiation
Before running the Metalnx set up script, you need to make sure your iRODS negotiation paramaters are correct.

By default, iRODS is configured as `CS_NEG_DONT_CARE` in the `core.re` file, which means that the server can use SSL or not to communicate with the client. `CS_NEG_REQUIRE` and `CS_NEG_REFUSE` can be also used. `CS_NEG_REQUIRE` means that iRODS will always use SSL communication while `CS_NEG_REFUSE` tells iRODS not to use SSL at all.

If you do not want to use any SSL communication, before installing Metalnx we need to set the negotiation type on the grid to `CS_NEG_REFUSE`. If you want to use SSL, you can leave iRODS set to `CS_NEG_DONT_CARE` or `CS_NEG_REQUIRE`. Metalnx is always set to `CS_NEG_DONT_CARE`, so it will use SSL when required by iRODS.

You can change the negotiation paramater in iRODS by either 1) changing the core.re file directly or 2) creating a new file and then adding this file to the rule base set section in the server_config file.

1. Changing `core.re`

Open the `core.re` file under `/etc/irods/` and replace `acPreConnect(*OUT) { *OUT="CS_NEG_DONT_CARE"; }` with `acPreConnect(*OUT) { *OUT="CS_NEG_REFUSE"; }`.

2. Creating a new rule file

Go to `/etc/irods/` and create a new file named `ssl_negotiate.re`. Open the `ssl_negotiate.re` file, type `acPreConnect(*OUT) { *OUT="CS_NEG_REFUSE"; }` and save it. Now, open the `server_config.json` file and add a new entry to the `rule_base_set` section. It should look like:

```javascript
"re_rulebase_set": [
	{
		"filename": "ssl_negotiate"
	},
	{
		"filename": "core"
	}
]
```

**Just remember that once you modify this negotiation parameter to `CS_NEG_REFUSE` iRODS will never use SSL. If this is not the desired behaviour, check out the iRODS documentation to set up a grid with SSL.**
