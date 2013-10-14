#!/bin/bash

#reattach repo to master
git checkout master
git pull

#extract new version to file for config build trigger
version=`cat project.clj | grep 'defproject' | cut -f2 -d '"' | cut -f1 -d '-'`
echo "VERSION=$version" > release_version.properties

export http_no_proxy=*.brislabs.com
export no_proxy=*.brislabs.com
export JVM_OPTS="-Dservice.port=$JETTY_PORT"
export LEIN_SNAPSHOTS_IN_RELEASE=y
lein release || exit 1

#upload rpm
scp -i ~/.ssh/dmt-key exploud/RPMS/noarch/*.rpm root@yumrepo.brislabs.com:/var/tmp/rpms/
name=`find . -name '*.noarch.rpm'`
name=`basename $name`
scp -i ~/.ssh/dmt-key project.clj root@yumrepo.brislabs.com:/var/tmp/rpms/$name.RECEIPT

#push version increment
git push --tags origin master
