# MetaLnx Web assets

This collection is an optional set of resources accessed by the metalnx pipeline, providing
a way to plug in site-specific themes. This is a first iteration and if there are better/cleaner
ways to do this we will adjust...

By default metalnx will look internally in the classpath as indicated by the metalnxConfig.xml file
that is part of the SpringMVC configuration. The emc-metalnx-web application.xml looks
for metalnxConfig.xml to be in the /etc/irods-ext/metalnx directory, and by default
it looks in the classpath. Setting this xml in this fashion will use the recommended /opt/irods-ext/metalnx
assets

```xml

<bean id="messageSource" class="org.springframework.context.support.ReloadableResourceBundleMessageSource">
  <property name="defaultEncoding" value="UTF-8" />
  <property name="fileEncodings" value="UTF-8" />
  <property name="basenames">
    <list>
        <value>file:/opt/irods-ext/metalnx/i18n/messages</value>
        <value>file:/opt/irods-ext/metalnx/i18n-users/messages</value>
      </list>
  </property>
</bean>

<!-- **************************************************************** -->
<!-- SPRING ANNOTATION PROCESSING -->
<!-- **************************************************************** -->
<mvc:annotation-driven />
<mvc:resources mapping="/images/**" location="file:/opt/irods-ext/metalnx/images,/images/,classpath:static/images/" />
<mvc:resources mapping="/fonts/**" location="file:/opt/irods-ext/metalnx/fonts,/fonts/,classpath:static/fonts/" />
<mvc:resources mapping="/css/**" location="file:/opt/irods-ext/metalnx/css/,/css/,classpath:static/css/" />
<mvc:resources mapping="/js/**" location="file:/opt/irods-ext/metalnx/js/,/js/,classpath:static/js/" />


```
