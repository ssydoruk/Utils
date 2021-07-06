#/bin/bash

# script to install Genesys lirbraries into maven repository.
# works on Windows on mingw bash 

# set -x

D="/c/Program Files/GCTI"
ls "$D"

PDIR=/c/Program\ Files/GCTI/Platform\ SDK\ for\ Java\ 9.0
ls "${PDIR}"
APBLOCKDIR=$PDIR/applicationblocks
ls "$APBLOCKDIR"

mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile="${PDIR}/lib/netty-all-4.1.42.Final.jar"

mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile="${PDIR}/lib/pom/appblocks-bom.pom" -DpomFile="${PDIR}/lib/pom/appblocks-bom.pom"

mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile="${PDIR}/lib/pom/protocols-bom.pom" -DpomFile="${PDIR}/lib/pom/protocols-bom.pom"



for f in apptemplate-log4j2 jackson2-module protocol \
 apptemplate                 kvlistbinding             protocolmanagerappblock \
 clusterprotocolappblock     kvlists                     reportingprotocol \
 comappblock                 logging                     routingprotocol \
 commons                     managementprotocol          system \
 commonsappblock             messagebrokerappblock       voiceprotocol \
 configurationprotocol       warmstandbyappblock connection                  openmediaprotocol           webmediaprotocol \
 contactsprotocol            outboundprotocol 
do
	f=`echo $f | sed 's/ *$//g'`
	echo $f
	mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile="${PDIR}/lib/${f}.jar" -DpomFile="${PDIR}/lib/pom/${f}.pom"
done

for f in apptemplate clusterprotocol com commons \
messagebroker protocolmanager warmstandby
do
	echo $f
	cd "${APBLOCKDIR}/$f"
	mvn clean install
done
