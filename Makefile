.PHONY : clean default dockerimage

SHELL = /bin/bash

default: dockerimage

# this docker command builds a local docker image
# includes maven and nodejs
# which will be used to build the .war file
warbuilderimage:
	docker build -f Dockerfile.warbuilder -t myimages/metalnx-warbuilder .

# this docker command uses the ./local_maven_repo directory
# to cache the maven artifacts for repeated builds
#
# the local_maven_repo directory keeps your personal maven
# repository (i.e. $HOME/.m2) clean and safe from the "root" user.
#
# it creates the .war file in packaging/docker/
packaging/docker/metalnx.war: warbuilderimage
	docker run -it --rm \
		-v "$$PWD":/usr/src \
		-v "$$PWD"/src:/usr/src/mymaven \
		-v "$$PWD/local_maven_repo":/root/.m2 \
		-w /usr/src/mymaven \
		myimages/metalnx-warbuilder \
		mvn clean package -Dmaven.test.skip=true

# this docker command builds a local docker image
dockerimage: packaging/docker/metalnx.war
	docker build -t myimages/metalnx:latest .

# this removes the .war file
# it does not clean maven's .m2 cache
# it does not remove the docker image
clean:
	rm -f packaging/docker/metalnx.war
	docker run -it --rm \
		-v "$$PWD":/usr/src \
		-v "$$PWD"/src:/usr/src/mymaven \
		-v "$$PWD/local_maven_repo":/root/.m2 \
		-w /usr/src/mymaven \
		myimages/metalnx-warbuilder \
		mvn clean
