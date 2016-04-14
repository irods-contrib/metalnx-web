#!/bin/sh

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
mkdir -p $PROJECT_NAME-$PROJECT_VERSION/opt/emc
cp $WORKSPACE/src/emc-metalnx-web/target/emc-metalnx-web.war $PROJECT_NAME-$PROJECT_VERSION/tmp/emc-tmp/
cp $WORKSPACE/packaging/scripts/config_metalnx.sh $RPMBUILD_DIR/SOURCES/$PROJECT_NAME-$PROJECT_VERSION/opt/emc/config_metalnx.sh
cp $WORKSPACE/packaging/scripts/usage_information.sh $RPMBUILD_DIR/SOURCES/$PROJECT_NAME-$PROJECT_VERSION/opt/emc/usage_information.sh

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
