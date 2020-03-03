[![Antonio Musarra's Blog](https://img.shields.io/badge/maintainer-Antonio_Musarra's_Blog-purple.svg?colorB=6e60cc)](https://www.dontesta.it)
![travis ci](https://travis-ci.org/amusarra/liferay-portal-database-all-in-one-support.svg?branch=master)
[![Twitter Follow](https://img.shields.io/twitter/follow/antonio_musarra.svg?style=social&label=%40antonio_musarra%20on%20Twitter&style=plastic)](https://twitter.com/antonio_musarra)


# Welcome to Liferay Community Edition 7, 7.1 and 7.2 Database All In One Support
Those who follow Liferay is aware of the fact that the Community Edition 
version 7 of Liferay, were eliminated quite a bit of components App Server, 
Database & Clustering Support. For more detail information you can read the 
blog post by [Bryan Cheung]( https://www.liferay.com/it/web/bryan.cheung/blog/-/blogs/liferay-portal-7-ce-app-server-database-clustering-support) published on April 7, 2016.

The Liferay 7 CE no more support OOTB (Out Of The Box):
* Application Server: Oracle WebLogic, IBM WebSphere
* Clustering
* MultiVM Cache
* Oracle Database, Microsoft SQL Server, IBM DB2, Sybase DB

This project add support to the Oracle, SQL Server and IBM DB2 database. 
Liferay has performed refactorting the code so that it is possible and easy 
to add support for databases no longer supported OOTB

**Attention update:** The last version (1.1.4) of the driver works with the 
Liferay 7.2.1 CE GA2.

[<img src="https://www.dontesta.it/wp-content/uploads/2017/04/PayPalMeAntonioMusarra.png">](https://paypal.me/AntonioMusarra)

In the following video, I will guide you step-by-step instructions on how to 
add support for Oracle Database to Liferay 7 Community Edition in the bundled 
version of **Wildfly**.

[![Liferay 7 Wildfly: How to add support for Oracle DB ](https://img.youtube.com/vi/7fojCjko7Ac/0.jpg)](https://www.youtube.com/watch?v=7fojCjko7Ac)

## 1. Introduction
To extend support to other databases, Liferay has decided to refactory code to 
use Java [*SPI (Service Provider Interface)*](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html). 
SPI is the mechanism that allows you to extend / change the behavior within a 
system without changing the source. It includes interfaces, classes or methods 
that the user extends or implements in order to obtain a certain functionality.

In short we must:
* Implement the SPI interface [com.liferay.portal.kernel.dao.db.DBFactory](https://github.com/liferay/liferay-portal/blob/2960360870ae69360861a720136e082a06c5548f/portal-kernel/src/com/liferay/portal/kernel/dao/db/DBFactory.java). Implementation class inside this project is **OracleDBFactory.java**
* Implement the abstract class [com.liferay.portal.dao.db.BaseDB](https://github.com/liferay/liferay-portal/blob/master/portal-impl/src/com/liferay/portal/dao/db/BaseDB.java) for Oracle DB. Implementation class inside this project is **OracleDB.java**

The following code shows how service providers are loaded via SPI.
```
public DBManagerImpl() {
  ServiceLoader<DBFactory> serviceLoader = ServiceLoader.load(
    DBFactory.class, DBManagerImpl.class.getClassLoader());

  for (DBFactory dbFactory : serviceLoader) {
    _dbFactories.put(dbFactory.getDBType(), dbFactory);
  }
}
```
To register your service provider, you create a provider configuration file, 
which is stored in the **META-INF/services** directory of the service provider's 
JAR file. The name of the configuration file is the fully qualified class name 
of the service provider, in which each component of the name is separated by a 
period (.), and nested classes are separated by a dollar sign ($).

The provider configuration file contains the fully qualified class names (FQDN) 
of your service providers, one name per line. The file must be UTF-8 encoded. 
Additionally, you can include comments in the file by beginning the comment line 
with the number sign (#).

Our file is called com.liferay.portal.kernel.dao.db.DBFactory and contain the 
FQDN of the class [it.dontesta.labs.liferay.portal.dao.db.OracleDBFactory](https://github.com/amusarra/liferay-portal-database-all-in-one-support/blob/master/src/main/java/it/dontesta/labs/liferay/portal/dao/db/OracleDBFactory.java)


In the figure below shows the complete class diagram for OracleDB.

![Class Diagram for OracleDB](https://www.dontesta.it/wp-content/uploads/2014/02/OracleDB.png)


## 2. Build project from source
Requirements for build project
1. Sun/Oracle JDK 1.8
2. Maven 3.x (for build project)

The driver that adds support for Oracle, SQLServer and DB2 database 
is a jar (**liferay-portal-database-all-in-one-support-${version}.jar**) which 
then will be installed in ROOT/WEB-INF/lib (for apache tomcat).

To generate the all in one driver just follow the instructions below.

**You can download the binary jar from Maven Central Repository**
[liferay-portal-database-all-in-one-support](https://search.maven.org/#search%7Cga%7C1%7Cit.dontesta), 
by doing so you can avoid doing the build.

```
$ git clone https://github.com/amusarra/liferay-portal-database-all-in-one-support.git
$ cd liferay-portal-database-all-in-one-support
$ mvn package
```

the build process create the jar inside the (maven) target directory:

```
liferay-portal-database-all-in-one-support-${version}.jar
```

## 3. Configure Liferay

Below you can see the portal-ext.properties. In the sample file are shown JDBC 
configurations sample for Oracle, SQL Server, and DB2.

```bash
##
## JDBC
##

    #
    # Oracle
    #
    # jdbc.default.driverClassName=oracle.jdbc.OracleDriver
    # jdbc.default.username=liferayce7
    # jdbc.default.password=liferay12345
    # jdbc.default.url=jdbc:oracle:thin:@oracledb.vm.local:1521:xe

    #
    # DB2
    #
    # jdbc.default.driverClassName=com.ibm.db2.jcc.DB2Driver
    # jdbc.default.url=jdbc:db2://db2.vm.local:50001/lportal:deferPrepares=false;fullyMaterializeInputStreams=true;fullyMaterializeLobData=true;progresssiveLocators=2;progressiveStreaming=2;
    # jdbc.default.username=db2inst1
    # jdbc.default.password=system

    #
    # SQL Server
    #
    # jdbc.default.driverClassName=com.microsoft.sqlserver.jdbc.SQLServerDriver
    # jdbc.default.username=liferay
    # jdbc.default.password=liferay12345
    # jdbc.default.url=jdbc:sqlserver://sqlserverdb.vm.local;databaseName=liferayce7
```

You could also configure database access as a JNDI resource and specify the 
resource name in configuration.

```bash
##
## JDBC
##

	#
    # Set the JNDI name to lookup the JDBC data source. If none is set,
    # then the portal will attempt to create the JDBC data source based on the
    # properties prefixed with "jdbc.default.".
    #
    jdbc.default.jndi.name=java:jdbc/LiferayPool
```
