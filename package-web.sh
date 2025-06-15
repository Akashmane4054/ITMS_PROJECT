#!/bin/bash
#!/bin/sh


echo "Deployment Directory: $1"


echo -----------------Creating Deployables for web node deployable -------------------

mvn -f ./itms-common-services/edge-service/pom.xml clean package -P deployment -Dpackage.location=$1

echo -----------------Completed creating  web node deployable-------------------------

