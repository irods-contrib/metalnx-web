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
echo "In order to deploy it on your Tomcat instance, execute /opt/emc/config_metalnx.sh"

chmod 755 /opt/emc/config_metalnx.sh
chmod 755 /opt/emc/usage_information.sh

%clean
rm -rf $RPM_BUILD_ROOT

%files
%defattr(-,root,root,-)
/tmp/emc-tmp/emc-metalnx-web.war
/opt/emc/config_metalnx.sh
/opt/emc/usage_information.sh