## Overview

In Metalnx, there are some key components in the architecure: 
- **Client**:
   Consumes all services provided by Metalnx. It is a Web browser making requests to the Metalnx server to interact with the iRODS grid
- **Controller**:
   Handles all requests coming from clients
- **Service**:
   The service layer is responsible for abstracting the database and grid layer from other components. In Metalnx, all functionalities are encapsulated as services
- **Jargon**:
   Open source library used to interact with iRODS. For more information, check out its [repository](https://github.com/DICE-UNC/jargon/wiki/Jargon-overview)

![Metalnx Architecture](https://github.com/Metalnx/metalnx-web/blob/master/docs/IMAGES/metalnx_arch.png)

## HTTP Requests Examples

In order to understand a little more about the Metalnx architecure, let's take some HTTP requests and see how all Metalnx components behave when different types of request get to the server.

### List Resources
In this example, the client requested the list of all resources available in the grid. This is `HTTP GET` request with no parameters.

![List Resources HTTP request](https://github.com/Metalnx/metalnx-web/blob/master/docs/IMAGES/metalnx_list_resources_call.png)

So, what happens is: 

1. The client makes a request to `/resources` asking for all resources
2. The Metalnx server gets the request. Request handling is done by the Controller layer - in this case, `ResourceController`
3. `ResourceController` calls the service layer (more specifically, `ResourceService`) to retrieve the list of all resources
4. `ResourceService` asks Jargon to find all resources
5. Jargon goes to iRODS asking for all resources it has
6. When Jargon comes back with the existing resources, `ResourceService` creates a list of resources and return it to `ResourceController`
7. With the list of resources in hand, `ResourceController` returns a `HTML` document containing this list.
8. The client (browser) gets this `HTML` document as the response for the `/resources` call and displays it to the user

### Delete User
This second case exemplifies a request to the Metalnx with a parameter.

![Delete User HTTP request](https://github.com/Metalnx/metalnx-web/blob/master/docs/IMAGES/metalnx_del_user_call.png)

What is happening behind the scenes: 

1. The client makes a request to `/delete/bob` asking Metalnx to delete the user **bob**
2. The Metalnx server gets the request. Request handling is done by the Controller layer - in this case, `UserController`
3. `UserController` calls the service layer (more specifically, `UserService`) to delete **bob**
4. `UserService` asks Jargon to delete **bob**
5. Jargon goes to iRODS asking to delete **bob**
6. The user is removed from the iRODS catalog, Jargon comes back with with a successful deletion, then `UserService` returns to `UserController` saying that **bob** was removed successfully
7. Knowing that **bob** was removed with no issues, `UserController` returns a `HTML` document with a positive feedback message for the delete operation
8. The client (browser) gets this `HTML` document as the response for the `/delete/bob` call and displays it to the user

## Frontend Frameworks and Tools

Metalnx uses Thymeleaf, Datatables and Bootstrap as frontend technologies in addition to HTML, CSS and JavaScript.

### Thymeleaf
Thymeleaf is a modern server-side Java template engine for both web and standalone environments. It is a view technology that fits perfectly the Spring MVC framework. 

It allows developers to introduce tag attributes that only Thymeleaf can interpret and it won't get in the way of the HTML code in case the developer needs to display the page without rendering the Thymeleaf attributes.

Thymeleaf uses the standard dialects (HTML tags will contain attributes with prefix `th:` in them). It executes Spring Expression Language on the context variables to render them, which are called *model attributes* in Spring. Another great feature that Thymeleaf offers is internationalization. We can retrieve locale-specific messages from external sources (`.properties` file), referencing them by a key.

#### How does Metalnx use it?
In the Spring MVC context, by default, the methods in the controller layer must return a String that corresponds to the path of an HTML page. Methods in controller put everything that needs to be displayed in a Model object, which in turn will be rendered in the HTML page.

![Controller](https://github.com/Metalnx/metalnx-web/blob/master/docs/IMAGES/controller-thymeleaf.png)

### Datatables

Datatables is a jQuery plugin that adds multiple functionalities to the simple HTML table. Some of its features are:

- Pagination, instant search and multi-column ordering
- Supports almost any data source: DOM, Javascript, Ajax and server-side processing
- Easily theme-able: DataTables, jQuery UI, Bootstrap, Foundation
- Internationalisable

![Datatables](https://github.com/Metalnx/metalnx-web/blob/master/docs/IMAGES/datatables.png)

### Boostrap
Bootstrap is used for developing responsive Web interface for desktop and mobile browsers. It comes with components to have an easy start when developing a web interface. 

It has an extensive list of components commonly used in Web interfaces like: icons, buttons, styled forms, breadcrumb, alerts, styled lists, thumbnails, etc. 

Same layout code for different devices: desktop, tablets, and smartphones. As we can see bellow:

![Boostrap Responsive](https://github.com/Metalnx/metalnx-web/blob/master/docs/IMAGES/boostrap-responsive.png)

### Response Types 
There are two types of reponse Metalnx creates: HTML or JSON. 

#### HTML

Returning HTML pages is more common, it follows the MVC flow. The controller layer returns an HTML page from the view layer with all Thymeleaf tags translated.

![HTML Response Type](https://github.com/Metalnx/metalnx-web/blob/master/docs/IMAGES/response-types.png)

#### JSON
The other type of response existing in Metalnx is JSON. It is basically used by the datatables plugin to draw its content.

Datatables has a feature in which we can use a custom request to retrieve data, in other words, server-side processing. It delegates data processing (sorting, ordering, page size, pagination, etc) to the server instead of doing it on the browser.

![](https://github.com/Metalnx/metalnx-web/blob/master/docs/IMAGES/response-types-datatables.png)

## Source code

### emc-metalnx-core

#### entities
User, Group, Resource, Server, Metadata and many others are examples of entities in both Metalnx and iRODS ecosystem. Note that they all start with `DataGrid`. This is the prefix we use in the project.

If a new feature is implemented, let's say feature X and it requires you to create classes A, B and C, that's where you should be adding those new entities. So, they will be named `DataGridA`, `DataGridB` and `DataGridC`.

#### exceptions

This is the package where Metalnx exceptions live. If any new exception is necessary, this is the package where it should be added. For example, DataGridExpiredFeatureXException.

#### utils

Comparators & Helper classes stay in this package. There are some methods that check if a file is of a certain type, for example `BAM`, `VCF`, `JPEG`, and `Illumina`. There are more like helper methods used by other classes or services.

#### resources
Under the *resources* folder, we have `xml` files where there is some spring configuration.
		
### emc-metalnx-services

This is a Metalnx subproject containing all services that compose our service layer. The service layer is used by our controllers (it will be covered later on) to do a specific job in iRODS.

#### context
This package contains a class responsible for parsing all credentials from the `*.properties`. It will decode credentials to be able to authenticate against iRODS and DBs like MySQL and PostgreSQL.

#### utils
This package is related to the upload mechanism used in Metalnx versions 1.0.X. It will be removed any time soon.

#### auth
Packages where the classes used for iRODS authentication live.

#### Exception
Some other Metalnx exceptions. The clases in this package will be either removed or moved into the `core`.

#### Interfaces
Package where all interfaces for all services are defined.

#### irods
Implementation of all services that talk to iRODS.

#### machine
Implementation of services that talk to RMD (Remote Monitor Daemon). For more information about RMD, check out this [link](https://github.com/Metalnx/metalnx-rmd).

#### test

Contains all unit tests corresponding to the service layer.

### emc-metalnx-shared
Part of the application that contains functionalities shared by both a rods admin and a rods user. Collections, file operations (move, copy, etc), shared links, metadata, and permission are all operations that both admins and users can perform. That is the reason they stay in this subproject.

#### controller

Controllers provide access to the Metalnx service layer that can be used by users and admins.

#### utils

Helper class to retrieve the user currently logged into the application.

####  interceptors
Very useful when it's necessary to do something before or after each request. The `HttpResponseHandlerInterceptor` will intercept all HTTP responses from the server to the client and add few params: user details, metalnx version and URL mapping.

#### modelattribute
This package contains all representation to objects that will go to the frontend. Breadcrumbs, collections forms, user forms, group forms, resource forms, and URLs are things used on the UI to properly render the page.

#### utils

Again, a package to keep helper class, but this is a kind of special one. This package will contain a class named `EmcMetalnxVersion`. This class is created only when you build Metalnx. It contains information related to the build: version, build number and build time. Such information is used on the *about* page.

#### resources

The *resources* folder is where files more related to frontend live. Under the `static` folder we have static files used on the frontend such as `*.css`, `*js`, fonts and images. `views` is the folder that contains HTML files for pages that can be seen by admins and users (for example, collections, metadata, permission, templates and login). `i18n` contains labels used by the app translated in English, French and Portuguese (the default language is English).

### emc-metalnx-ui-admin
Part of the app only for rods admins (Dashboard, Group, User, and Resource management).

#### controllers
Controllers in this package provide access to the Metalnx service layer that can be used only by admins.

#### handler
Classes that handle authentication failure and success.
	
#### modelattribute
Representation of objects that will go on the UI, but only available to admins such as group, user and resource management forms.

### metalnx

This is the subproject that is the actual Web application of Metalnx. All other subprojects compose the backend side of the app while this one is the application shipped to users.

It contains all properties files: `{irods, msi, security, log4j}.properties` that can be modified by Metalnx administrators from inside a servlet container like Tomcat. 
=======

There are three locations for these files (all user `src/main/conf`): `dev`, `integration-tests` and `preprod`. Each location has a copy of all of them. The reason we have those three places is because the files under `dev` will be used in development, the ones under `integration-test` will be used for testing and finally the ones inside `preprod` will be the ones that will go to production.

`src/main/webapp/WEB-INF` keeps `xml` files used for Web application configuration. Here is what every file does:

 - applicationContext.xml
	- security: who has access to each URL (rodsadmin, rodsuser, anonymus)
	- configure login
		- URL pattern /login/
		- what to do in case of failure and in case of success
		- invalid session (URL)
	- thymeleaf configuration
		- template engine
		- HTML version -> HTML 5
	- I18N configuration
	- Interceptors
	- Static files classpath
- web.xml
	- custom error pages for 403, 404, 500
	- HTTPS configuration
	- Session expiration time
	- Character encoding

#### test package

Contains all UI tests using Selenium.

***That's it! Enjoy Metalnx!***

