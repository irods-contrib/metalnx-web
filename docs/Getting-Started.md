# Before Installation
Make sure your system has all dependencies necessary for the Metalnx Web application to work. For more information about it, check out the [Dependencies](https://github.com/Metalnx/metalnx-web/wiki/Dependencies) section on our Wiki.

# Releases
If you want to know what is new in each release, check out all releases of this project [here](https://github.com/irods-contrib/metalnx-web/releases).

# Install Metalnx

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

# Other Tools

After installing Metalnx, we strongly recommend the installation of two other tools: *Remote Monitor Daemon (RMD)* and *Metalnx Microservices*. These tools provide information on storage usage and server status on the Dashboard and the Server Details page.

- **[RMD](https://github.com/Metalnx/metalnx-rmd/):**  Small, lightweight daemon that provides basic availability information of each server in the iRODS grid which allows Metalnx to report on the overall health of the grid.  
- **[Metalnx Microservices](https://github.com/Metalnx/metalnx-msi/):**  Metalnx provides microservices that automatically extract metadata when uploading files to iRODS via Metalnx.
