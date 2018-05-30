# MetaLnx Tools

## Database schema migration and setup

Database schema should be configured using flywaydb. This project includes code that supports database migrations 
across versions.

For information on flywaydb see: https://flywaydb.org/documentation/

We'll use the Maven plugin in our own processes and documentation, but there are other options!

### Initial setup of a database

User and password information needs to be kept secure. There are configuration options here: https://flywaydb.org/documentation/maven/

First we need to set up the database (we will use postgres in the spirit of the iRODS install)
```
$ (sudo) su - postgres
postgres$ psql
psql> CREATE USER irodsext WITH PASSWORD 'password';
psql> CREATE DATABASE "IRODS-EXT";
psql> GRANT ALL PRIVILEGES ON DATABASE "IRODS-EXT" TO irodsext;

```
Using maven properties, set up:

```xml

<profile>
			<id>flyway-local</id>
			<properties>
				<flyway.jdbc.url>jdbc:postgresql://<address>:<port>/<metalnx-db-name></flyway.jdbc.url>
				<flyway.db.user>irodsext</flyway.db.user>
				<flyway.db.password>password</flyway.db.password>
				<flyway.db.schema>public</flyway.db.schema>
			</properties>
		
		</profile>
	


```

* mvn flyway:clean - clean database (destructive! clears your database)

* mvn flyway:benchmark - look at existing database to set to initial version. This is not for new databases.

* mvn flyway:migrate - do migration, this is the first command to run from 0 on a clean database
