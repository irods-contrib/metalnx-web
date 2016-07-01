Name:		emc-metalnx-webapp
Version:    VERSION
Release:	DEV
Summary:    EMC MetaLnx WebApp for Tomcat

Group:      System Environment/Base
License:    MIT
URL:        http://www.emc.com
Source0:    emc-metalnx-webapp-VERSION-DEV.tar.gz
Requires:	java-devel
BuildArch:	noarch
BuildRoot:	%{_tmppath}/%{name}-buildroot

%description
Install the EMC MetaLnx Web Application on Tomcat directory.

%prep
%setup -q

%install
mkdir -p "$RPM_BUILD_ROOT"
cp -R * "$RPM_BUILD_ROOT"

%post

echo
echo "The EMC MetaLnx webapp has been successfully installed!"
echo "In order to deploy it on your Tomcat instance, execute 'python /opt/emc/setup_metalnx.py'"

chmod 755 /opt/emc/*
chmod 755 /opt/emc/lib/*
chmod 755 /opt/emc/ldap/*

%clean
rm -rf $RPM_BUILD_ROOT

%files
%defattr(-,root,root,-)
/tmp/emc-tmp/emc-metalnx-web.war
/opt/emc/setup_metalnx.py
/opt/emc/test-connection.jar
/opt/emc/lib/__init__.py
/opt/emc/lib/config.py
/opt/emc/lib/utils.py
/opt/emc/usage_information.sh
/opt/emc/ldap/import_ldap.conf
/opt/emc/ldap/import_ldap.sh
/opt/emc/ldap/populate.data.csv
/opt/emc/ldap/populate.template
/opt/emc/ldap/populate_ldap.conf
/opt/emc/ldap/populate_ldap.sh