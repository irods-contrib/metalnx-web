# Before Installation
Make sure your system has all dependencies necessary for the Metalnx Web application to work. For more information about it, check out the [Dependencies](https://github.com/Metalnx/metalnx-web/wiki/Dependencies) section on our Wiki.

# Download Binary Packages
**You do not need to build Metalnx from scratch** unless you want to. We already provide all packages needed to run Metalnx in your environment. The packages are available on our [Bintray repository](https://bintray.com/metalnx):

- [RPM](https://bintray.com/metalnx/rpm/download_file?file_path=emc-metalnx-webapp-1.1.1-3.noarch.rpm)
- [DEB](https://bintray.com/metalnx/deb/download_file?file_path=pool%2Fe%2Femc-metalnx-web%2Femc-metalnx-webapp-1.1.1-3.deb)

In case you want older versions of the microservices, all binary packages are hosted on the [Metalnx page](https://bintray.com/metalnx) at Bintray. Click on the links to find older versions of [RPM](https://bintray.com/metalnx/rpm/emc-metalnx-web) and [DEB](https://bintray.com/metalnx/deb/emc-metalnx-web) packages.

# Releases
If you want to know what is new in each release, check out all releases of this project [here](https://github.com/Metalnx/metalnx-web/releases).

# Install Metalnx

## Configure Database 
The command to setup the database will vary between whether you will use MySQL or PostgreSQL as the Metalnx database. The steps are logically similar.

### PostgreSQL

Become the user `postgres` using the command:
```
# su – postgres
```

Start the utility `psql` using the command:
```
# psql
```

Create the database user `metalnx` for communication with the database.

    Postgres=# CREATE USER metalnx WITH PASSWORD 'metalnx';

Create the database called `metalnx` for the Metalnx application to use.

    Postgres=# CREATE DATABASE "metalnx";

Grant the user `metalnx` access rights on the `metalnx` database:

    Postgres=# GRANT ALL PRIVILEGES ON DATABASE "metalnx" TO metalnx;

Exit the `psql` utility with the command:

    Postgres=# \q
    $ exit

Some extra configuration may be needed for Metalnx to be able to authenticate correctly against Postgres. Make sure you configured PostgreSQL as described in the [Database Dependency](https://github.com/Metalnx/metalnx-web/wiki/Dependencies#postgresql) section.

### MySQL

Become the mysql `root` user using the command:
```
# mysql – u root -p
```

Create the database user `metalnx` for communication with the database.

    mysql> CREATE USER ‘metalnx’@’<HOSTNAME>’ IDENTIFIED BY ‘metalnx’;

Create the database called `metalnx` for the Metalnx application to use.

    mysql> CREATE DATABASE metalnx;

Grant the user `metalnx` access rights on the `metalnx` database:

    mysql> GRANT ALL PRIVILEGES ON * . * TO ‘metalnx’@’<HOSTNAME>’;

Now, reload the privileges so they take effect:

    mysql> FLUSH PRIVILIGES:

Exit the `mysql` utility with the command:

    mysql> quit;

## Setup iRODS Negotiation

Before running the Metalnx setup script, make sure your iRODS negotiation parameters are correct.

By default, iRODS is configured as `CS_NEG_DONT_CARE` in the `core.re` file, which means that the server can use SSL or not to communicate with the client. `CS_NEG_REQUIRE` and `CS_NEG_REFUSE` can be also
used. `CS_NEG_REQUIRE` means that iRODS will always use SSL communication while `CS_NEG_REFUSE` tells iRODS not to use SSL at all.

### Using SSL

If you want to use SSL, you can leave iRODS set to `CS_NEG_DONT_CARE` or 
`CS_NEG_REQUIRE`. Metalnx is always set to `CS_NEG_DONT_CARE`, so it will use SSL when required by iRODS.

In other words, your `core.re` file under `/etc/irods` must be either:

    acPreConnect(*OUT) { *OUT="CS_NEG_DONT_CARE"; } 

or

    acPreConnect(*OUT) { *OUT="CS_NEG_REQUIRE"; } 

For more information about SSL configuration in Metalnx, check out the [PAM & SSL Configuration](https://github.com/Metalnx/metalnx-web/wiki/PAM-&-SSL-Configuration) post on this Wiki.

### No SSL

If you **do not** want to use any SSL communication, before installing Metalnx we need to set the negotiation type on the grid to `CS_NEG_REFUSE`. 

You can change the negotiation parameter in iRODS by either 1) changing the `core.re` file directly or 2) creating a new file and then adding this file to the *rule base set* section in the `server_config` file.

1) Changing `core.re`

Open the `core.re` file under `/etc/irods/` and replace 

    acPreConnect(*OUT) { *OUT="CS_NEG_DONT_CARE"; } 

with 

    acPreConnect(*OUT) { *OUT="CS_NEG_REFUSE"; }

2) Creating a new rule file

Go to `/etc/irods/` and create a new file named `ssl_negotiate.re`. Open the `ssl_negotiate.re` file, type `acPreConnect(*OUT) { *OUT="CS_NEG_REFUSE"; }` and save it. Now, open the `server_config.json` file and add a 
new entry to the `rule_base_set` section. It should look like:

```
"re_rulebase_set": [
	{
		"filename": "ssl_negotiate"
	},
	{
		"filename": "core"
	}
]
```

**Just remember that once you modify this negotiation parameter to `CS_NEG_REFUSE` iRODS will never use SSL. If this is not the desired behaviour, check out the iRODS [documentation](https://docs.irods.org) to set up a grid with SSL.**

## Package installation
As `root`, install the Metalnx application from either the `rpm` or `deb` packages.  These packages will have names similar to:

    emc-metalnx-webapp-X.X.X-X.noarch.rpm	# (CentOS / RHEL like systems)
    emc-metalnx-webapp-X.X.X-X.noarch.deb	# (Debian like systems)

On CentOS, install the Metalnx application package using the command:

```
# rpm –ivh emc-metalnx-webapp-1.X.X-X.noarch.rpm
```

On a Debian-like systems, install the Metalnx application using the command:
```
# dpkg –i emc-metalnx-webapp-1.X.X-X.noarch.deb
```

## Setup Metalnx 
The Metalnx installation package comes with a setup script. The script will help to setup the Metalnx in according to your environment.

Once the `rpm` or `deb` package is installed, run the Metalnx setup script, as `root`:

    # python /opt/emc/setup_metalnx.py

The script is organized in steps, so you can easily identify what is changed during its execution. The script will request several pieces of information and it will make sure all dependencies required by Metalnx are met. Details below:

     Executing config_java_devel (1/13)   

The first step checks if the `java_devel` package exists in your system. If it does, the script goes to step two:

     Executing config_tomcat_home (2/13)   

It asks for the tomcat home directory. Provide the full pathname to the Tomcat home directory. The steps 3 to 6 are automatic, there is no input required. You can follow what is happening by reading the output messages.

     Executing config_tomcat_shutdown (3/13)   # Shutdown the tomcat service
     Executing config_metalnx_package (4/13)   # Checks if any Metalnx package already exists in your environment
     Executing config_existing_setup (5/13)    # Saves current installed Metalnx configuration, if any
     Executing config_war_file (6/13)          # Installs the Metalnx WAR file into Tomcat

The step 7 asks the Metalnx Database configuration parameters, as follows:

     Executing config_database (7/13)                                      # Configures database access
     Enter the Metalnx Database type (mysql, postgresql) [mysql]: mysql    # Metalnx database type. By default, it is MySQL
     Enter Metalnx Database Host [localhost]:                              # Database hostname. By default, it is localhost.
     Enter Metalnx Database Port [3306]:                                   # Database port. The default port is 3306 for MySQL and 5432 for PostgreSQL
     Enter Metalnx Database Name [metalnx]:                                # Database name. By default, it is "metalnx".
     Enter Metalnx Database User [metalnx]:                                # Database user. By default, it is "metalnx"
     Enter Metalnx Database Password (it will not be displayed):           # Database password. We do not provide any default value for that.     
            
After the Metalnx database configuration, the script tests whether or not the credentials are valid by connecting to the database. If it is successful, the following message is shown:

    * Testing database connection...
    * Database connection successful.

*If you do not have `psycopg2` or `mysqldb` python modules installed in your system, the Metalnx setup script cannot test your database connection. In this case, warning messages will be displayed, but the setup will continue:*

    No DB connection test modules detected. Skipping DB connection test.
    Notice that if your DB params are incorrect, Metalnx will not work as expected.
    To change these parameters, execute the configuration script again.

Now, you will be configuring the iRODS parameters to allow Metalnx connect to the grid.

    Enter iRODS Host [localhost]:                                             # iRODS machine's hostname. By default, it is localhost
    Enter iRODS Port [1247]:                                                  # port number used to communicate with the iCAT. By default, it is 1247
    Enter iRODS Zone [tempZone]:                                              # iRODS Zone Name. By default, it is tempZone
    Enter Authentication Schema (STANDARD, GSI, PAM, KERBEROS) [STANDARD]:    # iRODS authentication mechanism. By default, it is STANDARD
    Enter iRODS Admin User [rods]:                                            # iRODS Admin Username. By default, it is rods.
    Enter iRODS Admin Password (it will not be displayed):                    # iRODS Admin Password. We do not provide any default value for that.
    
The iRODS credentials are tested to make sure Metalnx is able to connect to the grid. If the connection is successful, the following message is shown:

    * Testing iRODS connection...
    * iRODS connection successful.

After the iRODS configuration step, the script checks if it is necessary to restore any previous configuration files.

    Executing config_restore_conf (9/13)
 
The next step is related to HTTPS configuration. 
 
    Executing config_set_https (10/13)
    Do you want to enable HTTPS on Tomcat and Metalnx? [Y/n]:  

If you respond *n* Tomcat and Metalnx will be setup to respond to `http` protocol on port 8080. If you press *Y* or *y* it will configure Tomcat to use `https` for its connections on port 8443.

The 11th step asks you to confirm if all params you set for both iRODS and the Metalnx database are correct.

    Executing config_confirm_props (11/13)
        - Confirm configuration parameters

    Metalnx Database Parameters:
        * db.type = mysql
        * db.db_name = metalnx
        * db.port = 3306
        * db.host = localhost
        * db.username = metalnx

    iRODS Paramaters:
        * irods.port = 1247
        * irods.auth.scheme = STANDARD
        * irods.zoneName = tempZone
        * jobs.irods.username = rods
        * irods.host = icat.localdomain
    
    Do you accept these configuration parameters? (yes, no) [yes]:

By answering *no*, the execution of the script is aborted. By saying *yes*, the script creates the configuration files necessary for the Metalnx Web application to run:

    * Creating Database properties file...
    * Database properties file created.
    * Creating iRODS properties file...
    * iRODS properties file created.

The following steps are the two last steps for configuring Metalnx.

    [*] Executing config_tomcat_startup (12/13)
       - Starting tomcat back again
    Redirecting to /bin/systemctl start  tomcat.service

    [*] Executing config_displays_summary (13/13)
       - Metalnx configuration finished

If everything is successful, the script shows where to access Metalnx:

    Metalnx configuration finished successfully!
    You can access your Metalnx instance at:
        http://<hostname>:8080/emc-metalnx-web/login/
    
    For further information and help, refer to:
        https://github.com/Metalnx/metalnx-web

If you encounter a message in the script that reads:

    Starting Tomcat ... Failed. Tomcat could not be started. Please do it manually.

This message means that the installation script could not restart Tomcat on its own.  You will need to take this action manually using the systemctl command (see below).   This is a known issue.

     # systemctl start tomcat

**By now, Metalnx should be up and running.**

# Other Tools

After installing Metalnx, we strongly recommend the installation of two other tools: *Remote Monitor Daemon (RMD)* and *Metalnx Microservices*. These tools provide information on storage usage and server status on the Dashboard and the Server Details page.

- **[RMD](https://github.com/Metalnx/metalnx-rmd/):**  Small, lightweight daemon that provides basic availability information of each server in the iRODS grid which allows Metalnx to report on the overall health of the grid.  
- **[Metalnx Microservices](https://github.com/Metalnx/metalnx-msi/):**  Metalnx provides microservices that automatically extract metadata when uploading files to iRODS via Metalnx.