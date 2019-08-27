# Pluggable Search

## Intro





## Developing pluggable search








## Configuration

As described in the installation instructions, Metalnx operations are controlled by a metalnx.properties file that is mounted to the Docker image at /etc/irods-ext. Search and JWT settings are near the bottom of the configuration, with sections for JWTs (Metalnx talks to back-end microservices using JWT for auth), and for search itself. Search is not required for Metalnx operation (setting pluggablesearch.enabled=false turns off all pluggable search in metalnx). If it is enabled, the pluggablesearch.xxxx properties tune the search operations.

The search plugins operate as a well-defined REST endpoint (see https://github.com/michael-conway/grid-search-service for the swagger definition as well as a sample implementation). Metalnx is then provided with a ',' delimited list of REST endpoints. Metalnx will scan these endpoints and maintain a directory of available search plugins. 

The following are the metalnx.properties settings controlling the behavior of pluggable search:

```


#############################
# JWT configuration (necessary when using search and notification services). Otherwise can be left as-is and ignored
#############################

jwt.issuer=
jwt.secret=
jwt.algo=HS384


#############################
# Pluggable search configuration. Turn on and off pluggable search globally, and configure search endpoints.
# N.B. pluggable search also requires provisioning of the jwt.* information above 
#############################

# enable pluggable search globally and show the search GUI components
pluggablesearch.enabled=true
# JWT subject claim used to access search endpoint for data gathering. User searches will utilize the name of the individual
pluggablesearch.endpointAccessSubject=
# timeout for info/attribute gathering, set to 0 for no timeout
pluggablesearch.info.timeout=0
# timeout for actual search, set to 0 for no timeout
pluggablesearch.search.timeout=0


```





