#!/bin/bash
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

TMP_DIR=/tmp/emc-tmp
PROJECT_NAME="emc-metalnx-webapp"
PROJECT_VERSION="$1"
PROJECT_RELEASE="$2"

rm -rf $TMP_DIR

mkdir -p $TMP_DIR/$PROJECT_NAME-$PROJECT_VERSION-$PROJECT_RELEASE/tmp/emc-tmp
mkdir -p $TMP_DIR/$PROJECT_NAME-$PROJECT_VERSION-$PROJECT_RELEASE/opt/emc/ldap
mkdir -p $TMP_DIR/$PROJECT_NAME-$PROJECT_VERSION-$PROJECT_RELEASE/opt/emc/lib

cp $WORKSPACE/src/emc-metalnx-web/target/emc-metalnx-web.war $TMP_DIR/$PROJECT_NAME-$PROJECT_VERSION-$PROJECT_RELEASE/tmp/emc-tmp/
cp -r $WORKSPACE/packaging/scripts/* $TMP_DIR/$PROJECT_NAME-$PROJECT_VERSION-$PROJECT_RELEASE/opt/emc/
cp $WORKSPACE/contrib/ldap/* $TMP_DIR/$PROJECT_NAME-$PROJECT_VERSION-$PROJECT_RELEASE/opt/emc/ldap/
cp -r $WORKSPACE/packaging/deb/DEBIAN $TMP_DIR/$PROJECT_NAME-$PROJECT_VERSION-$PROJECT_RELEASE/

rm $TMP_DIR/$PROJECT_NAME-$PROJECT_VERSION-$PROJECT_RELEASE/opt/emc/create_rpm_package.sh
rm $TMP_DIR/$PROJECT_NAME-$PROJECT_VERSION-$PROJECT_RELEASE/opt/emc/create_deb_package.sh

cd $TMP_DIR/$PROJECT_NAME-$PROJECT_VERSION-$PROJECT_RELEASE/opt/emc/
wget https://bintray.com/metalnx/generic/download_file?file_path=metalnx-connection-test-1.0-RELEASE-jar-with-dependencies.jar -O test-connection.jar --no-check-certificate
cd -

chmod -R 755 $TMP_DIR/$PROJECT_NAME-$PROJECT_VERSION-$PROJECT_RELEASE/DEBIAN
sed -i "s/{{VERSION-NUMBER}}/$PROJECT_VERSION/" $TMP_DIR/$PROJECT_NAME-$PROJECT_VERSION-$PROJECT_RELEASE/DEBIAN/control
sed -i "s/{{BUILD-NUMBER}}/$PROJECT_RELEASE/" $TMP_DIR/$PROJECT_NAME-$PROJECT_VERSION-$PROJECT_RELEASE/DEBIAN/control

dpkg-deb --build $TMP_DIR/$PROJECT_NAME-$PROJECT_VERSION-$PROJECT_RELEASE
