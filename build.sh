#!/bin/bash
#!/bin/sh
echo -----------------Building dependencies------------------------
mvn -f ./itms-dependencies/pom.xml clean install 
if [ $? != 0 ]
then	echo "Build failure while running the itms-dependencies"
	read -p "Press any key"
	exit 25
fi
echo -----------------Building parent------------------------
mvn -f ./itms-parent/pom.xml clean install 
if [ $? != 0 ]
then	echo "Build failure while running the itms-parent"
	read -p "Press any key"
	exit 25
fi

echo -----------------Building services------------------------
mvn clean install
if [ $? != 0 ]
then	echo "Build failure while running the itms-services"
	read -p "Press any key"
	exit 25
fi

echo -----------------Completed building all--------------------------
