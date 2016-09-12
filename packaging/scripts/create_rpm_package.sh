#!/bin/sh
#	Copyright (c) 2015-2016, EMC Corporation
#
#	Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.

PROJECT_NAME=emc-metalnx-webapp
PROJECT_VERSION="$1"
PROJECT_RELEASE="$2"
RPMBUILD_DIR=~/rpmbuild

if [ -d $RPMBUILD_DIR ]; then
    rm -rf $RPMBUILD_DIR
fi

rpmdev-setuptree > /dev/null
if [ $? -ne 0 ]; then
	echo "rpmdev-setuptree is not installed on your system. Creating the RPM BUILD tree structure manually."
	mkdir -p $RPMBUILD_DIR/SOURCES/
	mkdir -p $RPMBUILD_DIR/RPMS/
	mkdir -p $RPMBUILD_DIR/SPECS/
fi

cp $WORKSPACE/packaging/rpm/emc-metalnx-webapp.spec $RPMBUILD_DIR/SPECS/emc-metalnx-webapp.spec

echo "Creating RPMBUILD directory tree structure..."
cd $RPMBUILD_DIR/SOURCES
mkdir -p $PROJECT_NAME-$PROJECT_VERSION/tmp/emc-tmp
mkdir -p $PROJECT_NAME-$PROJECT_VERSION/opt/emc/ldap
cp $WORKSPACE/src/emc-metalnx-web/target/emc-metalnx-web.war $PROJECT_NAME-$PROJECT_VERSION/tmp/emc-tmp/
cp $WORKSPACE/packaging/scripts/setup_metalnx.py $RPMBUILD_DIR/SOURCES/$PROJECT_NAME-$PROJECT_VERSION/opt/emc/setup_metalnx.py
cp -r $WORKSPACE/packaging/scripts/lib $RPMBUILD_DIR/SOURCES/$PROJECT_NAME-$PROJECT_VERSION/opt/emc/
cp $WORKSPACE/packaging/scripts/usage_information.sh $RPMBUILD_DIR/SOURCES/$PROJECT_NAME-$PROJECT_VERSION/opt/emc/usage_information.sh
cp $WORKSPACE/contrib/ldap/* $RPMBUILD_DIR/SOURCES/$PROJECT_NAME-$PROJECT_VERSION/opt/emc/ldap/

cd $RPMBUILD_DIR/SOURCES/$PROJECT_NAME-$PROJECT_VERSION/opt/emc/
wget https://bintray.com/metalnx/generic/download_file?file_path=metalnx-connection-test-1.0-RELEASE-jar-with-dependencies.jar -O test-connection.jar --no-check-certificate
cd -

echo "Creating tarball of the sources..."
cd $RPMBUILD_DIR/SOURCES
tar -cvzf $PROJECT_NAME-$PROJECT_VERSION-$PROJECT_RELEASE.tar.gz $PROJECT_NAME-$PROJECT_VERSION

echo "Getting spec file..."
cd $RPMBUILD_DIR/SPECS

sed -i "s/VERSION/$PROJECT_VERSION/" $RPMBUILD_DIR/SPECS/$PROJECT_NAME.spec
sed -i "s/DEV/$PROJECT_RELEASE/" $RPMBUILD_DIR/SPECS/$PROJECT_NAME.spec

echo "Building package..."
cd ~
rpmbuild -v -bb $RPMBUILD_DIR/SPECS/$PROJECT_NAME.spec

echo "Done."
