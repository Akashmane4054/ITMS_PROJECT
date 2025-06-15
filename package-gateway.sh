#!/bin/bash
#!/bin/sh


echo "Deployment Directory: $1"


echo -----------------Creating Deployables for Common Services-------------------

mvn -f ./itms-common-services/pom.xml clean package -P deployment -Dpackage.location=$1
if [ $? != 0 ]
then	echo "Created Deoloyables for Common Services"
	read -p "Press any key"
	exit 25
fi

echo -----------------Creating Deployable for Product Services------------------------
mvn -f ./itms-product/asset-management/pom.xml clean package -P deployment -Dpackage.location=$1

echo -----------------Creating Deployable for Gateway Services------------------------


echo -----------------Completed creating all deployables-------------------------

