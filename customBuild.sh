echo "       ------------------------------------------------------------------------"
echo "       Building ehr-dependencies"
echo "       ------------------------------------------------------------------------"
mvn -f ./itms-dependencies/pom.xml clean install -Dmaven.skip.test=true
if [ $? != 0 ]
then echo "Build failure while running the itms-dependencies"
	exit 25
fi

echo "       ------------------------------------------------------------------------"
echo "       Building ehr-parent"
echo "       ------------------------------------------------------------------------"
mvn -f ./itms-parent/pom.xml clean install -Dmaven.skip.test=true
if [ $? != 0 ]
then echo "Build failure while running the itms-parent"
	exit 25
fi

echo "       ------------------------------------------------------------------------"
echo "       Building ehr-common"
echo "       ------------------------------------------------------------------------"
mvn -f ./ehr-common/pom.xml clean install -Dmaven.skip.test=true
if [ $? != 0 ]
then echo "Build failure while running the ehr-common"
	exit 25
fi

echo "       ------------------------------------------------------------------------"
echo "       Building ehr-services-parent"
echo "       ------------------------------------------------------------------------"
mvn -f ./itms-services-parent/pom.xml clean install -Dmaven.skip.test=true
if [ $? != 0 ]
then echo "Build failure while running the ehr-services-parent"
	exit 25sssssss
fi

echo "       ------------------------------------------------------------------------"
echo "       Building ehr-starters"
echo "       ------------------------------------------------------------------------"
mvn -f ./ehr-starters/pom.xml clean install -Dmaven.skip.test=true
if [ $? != 0 ]
then echo "Build failure while running the ehr-starters"
	exit 25
fi

echo "       ------------------------------------------------------------------------"
echo "       Building itms-product"
echo "       ------------------------------------------------------------------------"
mvn -f ./itms-product/pom.xml clean install -Dmaven.skip.test=true
if [ $? != 0 ]
then echo "Build failure while running the itms-product"
	exit 25
fi

echo "       ------------------------------------------------------------------------"
echo "       Building ehr-common-services"
echo "       ------------------------------------------------------------------------"
mvn -f ./itms-common-services/pom.xml clean install -Dmaven.skip.test=true
if [ $? != 0 ]
then echo "Build failure while running the itms-common-services"
	exit 25
fi

echo "       ------------------------------------------------------------------------"
echo "       Building config-server"
echo "       ------------------------------------------------------------------------"
mvn -f ./itms-common-services/config-server/pom.xml clean install -Dmaven.skip.test=true
if [ $? != 0 ]
then echo "Build failure while running the itms-common-services"
	exit 25
fi

echo "       ------------------------------------------------------------------------"
echo "       Building discovery-service"
echo "       ------------------------------------------------------------------------"
mvn -f ./itms-common-services/discovery-service/pom.xml clean install -Dmaven.skip.test=true
if [ $? != 0 ]
then echo "Build failure while running the itms-common-services"
	exit 25
fi