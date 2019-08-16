# Vue.js development notes

### Introduction

Metalnx will move over time to a Vue.js based application. This transition can occur over time, leveraging the Thymeleaf page rendering engine to deliver distinct Vue.js apps. These apps are routed based on the vue.config.js file, for example:

```javascript


pages: {
   collections: {
     entry: 'src/main/javascript/collections/main.js'
   },
   home: {
     entry: 'src/main/javascript/home/main.js'
   },
   notifications: {
     entry: 'src/main/javascript/notifications/main.js'
   }

```

The various sub-routes are Thymeleaf templates with an associated controller. Note that the main Thymeleaf template that represents that Vue.js app refers to an alternative template that contains updated .css and javascript files for Vue and Bootstrap. This allows the existing Metalnx pages to use the original libraries side-by-side.

For example in the 'home' template, note the layout:decorator tag refers to 'templatev' as opposed to the existing 'template' Thymeleaf master template.


```html


<html
	xmlns="http://www.w3.org/1999/xhtml"
    xmlns:th="http://www.thymeleaf.org"
    xmlns:layout="http://www.thymeleaf.org"
    lang="en"
    layout:decorator="templatev">

```

### Developer Doc Links

* VueJs Guide - https://vuejs.org/v2/guide/
* VueJs Proxies - https://vuejs-templates.github.io/webpack/proxy.html
 


### Development workflow

In a normal deployment mode, the pom.xml file is configured to properly install node and run the Vue.js build process, the deployed .war file will properly execute the Vue.js enabled services without further effort.

```xml

	<plugin>
				<groupId>com.github.eirslett</groupId>
				<artifactId>frontend-maven-plugin</artifactId>

				<version>1.7.6</version>
				<executions>
					<execution>
						<id>install node and npm</id>
						<goals>
							<goal>install-node-and-npm</goal>
						</goals>
						<configuration>

							<nodeVersion>v8.11.1</nodeVersion>
							<npmVersion>5.6.0</npmVersion>
						</configuration>
					</execution>
					<execution>
						<id>npm install</id>
						<goals>
							<goal>npm</goal>
						</goals>
						<configuration>
							<arguments>install</arguments>
						</configuration>
					</execution>
					<execution>
						<id>npm run</id>
						<goals>
							<goal>npm</goal>
						</goals>
						<configuration>
							<arguments>run build</arguments>
						</configuration>
					</execution>
				</executions>
			</plugin>


```

This approach requires packaging and deploying Metalnx for testing, and can be inefficient when doing page style, layout, and interaction testing. In order to shorten the code->test cycle, developers can utilize the Vue.js proxy facility (https://vuejs-templates.github.io/webpack/proxy.html). This can run interactively in the front end via the built-in NodeJS web server and make calls to the running Metalnx backend. This allows immediate loading of changes to javascript and css/html assets.

The proxy settings are found in the vue.config.js file:

```json
 publicPath: '/metalnx/',
  devServer: {
	  port:8888,
    proxy: {
      '/metalnx': {
        target: 'http://localhost:8080',
        ws: true,
        changeOrigin: true
      }
    }
  },

```

switching to the src/metalnx-web project, the developer can invoke npm run serve to start the NodeJS proxy server.

```
(base) ~/Documents/workspace-niehs-rel/metalnx-web/src/metalnx-web @ ALMBP-02010755(conwaymc): npm run serve

> metalnx-vue@0.1.0 serve /Users/conwaymc/Documents/workspace-niehs-rel/metalnx-web/src/metalnx-web
> vue-cli-service serve

 INFO  Starting development server...


```

This allows access to the underlying Vue.js routes defined in vue.config.js. The proxy settings will 

