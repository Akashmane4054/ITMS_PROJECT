#!/bin/bash
#!/bin/sh


echo "Deployment Directory: $1"


echo -----------------Creating Deployables for Common Services-------------------

mvn -f ./itms-common-services/config-service/pom.xml clean package -P deployment -Dpackage.location=$1
mvn -f ./itms-common-services-service/pom.xml clean package -P deployment -Dpackage.location=$1
mvn -f ./itms-common-services/discovery-service/pom.xml clean package -P deployment -Dpackage.location=$1
mvn -f ./itms-common-services/edge-service/pom.xml clean package -P deployment -Dpackage.location=$1
mvn -f ./itms-common-services/user-service/pom.xml clean package -P deployment -Dpackage.location=$1
mvn -f ./itms-common-services/spring-security-oauth-server/pom.xml clean package -P deployment -Dpackage.location=$1


echo -----------------Creating Deployable for Product Services------------------------
mvn -f ./itms-product/asset-management/pom.xml clean package -P deployment -Dpackage.location=$1

echo -----------------Creating Deployable for Gateway Services------------------------


echo -----------------Completed creating all deployables-------------------------
