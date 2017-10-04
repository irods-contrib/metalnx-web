#### Authentication using LDAP (Lightweight Directory Access Protocol) 

The diagram below illustrates how Metalnx syncs user information with LDAP (Lightweight Directory Access Protocol): 

![Figure 8 - LDAP Syncing With Metalnx](https://github.com/Metalnx/metalnx-web/blob/master/docs/IMAGES/ldap_sync_diagram.png)

1.	In the LDAP server, Metalnx users must be members of a unique group that will be imported into iRODS.
2.	In iRODS, authentication must be set to PAM (Pluggable Authentication Modules). Refer to iRODS documentation for more information. 
3.	Metalnx must be set to work with PAM authentication.  
4.	New users will be created in iRODS server based on information retrieved from group. 
5.	New users will be created in Metalnx based on information retrieved from iRODS.