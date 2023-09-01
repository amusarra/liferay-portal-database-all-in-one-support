# Liferay Portal CE 7, 7.1, 7.2, 7.3 and 7.4 Database All In One Support
[![Antonio Musarra's Blog](https://img.shields.io/badge/maintainer-Antonio_Musarra's_Blog-purple.svg?colorB=6e60cc)](https://www.dontesta.it)
[![Twitter Follow](https://img.shields.io/twitter/follow/antonio_musarra.svg?style=social&label=%40antonio_musarra%20on%20Twitter&style=plastic)](https://twitter.com/antonio_musarra)

Those who follow Liferay is aware of the fact that the Community Edition
version 7 of Liferay, were eliminated quite a bit of components App Server,
Database & Clustering Support. For more detail information you can read the
blog post by [Bryan Cheung]( https://www.liferay.com/it/web/bryan.cheung/blog/-/blogs/liferay-portal-7-ce-app-server-database-clustering-support) published on April 7, 2016.

The Liferay 7 CE no more support OOTB (Out Of The Box):

* Application Server: Oracle WebLogic, IBM WebSphere
* Clustering
* MultiVM Cache
* Oracle Database, Microsoft SQL Server, IBM DB2, Sybase DB

This project add support to the [Oracle Database](https://www.oracle.com/database/), [SQL Server](https://www.microsoft.com/sql-server/sql-server-2019).
Liferay has performed refactorting the code so that it is possible and easy
to add support for databases no longer supported OOTB (out-of-the-box)

You can refer to the [change log](CHANGELOG.markdown) for more information about the changes made to.

[Fai una donazione su PayPal a Antonio Musarra (author)](https://paypal.me/AntonioMusarra)

I invite you to read the article [How to build a Docker Liferay 7.2 image with the Oracle Database support](https://www.dontesta.it/en/2019/08/21/how-to-build-a-docker-liferay-7-2-image-with-the-oracle-database-support/) and the [How to setup Docker container Oracle Database 19c for Liferay Development Environment](https://www.dontesta.it/en/2020/03/15/how-to-setup-docker-container-oracle-database-19c-for-liferay-development-environment/) which
may be interesting for you.

In the following video, I will guide you step-by-step instructions on how to
add support for Oracle Database to Liferay 7 Community Edition in the bundled
version of **Wildfly**.

[![Liferay 7 Wildfly: How to add support for Oracle DB ](https://img.youtube.com/vi/7fojCjko7Ac/0.jpg)](https://www.youtube.com/watch?v=7fojCjko7Ac)

## 1. Introduction
To extend support to other databases, Liferay has decided to refactory code to
use Java. Invite you to read [*SPI (Service Provider Interface)*](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html) and [*Creating Extensible Applications (The Java™ Tutorials > The Extension Mechanism > Creating and Using Extensions)*](https://docs.oracle.com/javase/tutorial/ext/basics/spi.html)
**SPI** is the mechanism that allows you to extend/change the behavior within a
system without changing the source. It includes interfaces, classes or methods
that the user extends or implements in order to obtain a certain functionality.

In short we must:
* Implement the SPI interface [com.liferay.portal.kernel.dao.db.DBFactory](https://github.com/liferay/liferay-portal/blob/7.3.1-ga2/portal-kernel/src/com/liferay/portal/kernel/dao/db/DBFactory.java). Implementation class inside this project is **OracleDBFactory.java**
* Implement the abstract class [com.liferay.portal.dao.db.BaseDB](https://github.com/liferay/liferay-portal/blob/7.3.1-ga2/portal-impl/src/com/liferay/portal/dao/db/BaseDB.java) for Oracle DB. Implementation class inside this project is **OracleDB.java**

The following code shows how service providers are loaded via SPI.

```java
public DBManagerImpl() {
  ServiceLoader<DBFactory> serviceLoader = ServiceLoader.load(
    DBFactory.class, DBManagerImpl.class.getClassLoader());

  for (DBFactory dbFactory : serviceLoader) {
    _dbFactories.put(dbFactory.getDBType(), dbFactory);
  }
}
```
Source Code 1 - Shows how service providers are loaded via SPI

To register your service provider, you create a provider configuration file,
which is stored in the **META-INF/services** directory of the service provider's
JAR file. The name of the configuration file is the fully qualified class name
of the service provider, in which each component of the name is separated by a
period (.), and nested classes are separated by a dollar sign ($).

The provider configuration file contains the fully qualified class names (FQCN)
of your service providers, one name per line. The file must be UTF-8 encoded.
Additionally, you can include comments in the file by beginning the comment line
with the number sign (#).

Our file (inside `META-INF/services` directory) is called `com.liferay.portal.kernel.dao.db.DBFactory` and contain the
FQCN of this class:

1. [it.dontesta.labs.liferay.portal.dao.db.OracleDBFactory](https://github.com/amusarra/liferay-portal-database-all-in-one-support/blob/master/src/main/java/it/dontesta/labs/liferay/portal/dao/db/OracleDBFactory.java)
2. [it.dontesta.labs.liferay.portal.dao.db.SQLServerDBFactory](https://github.com/amusarra/liferay-portal-database-all-in-one-support/blob/master/src/main/java/it/dontesta/labs/liferay/portal/dao/db/SQLServerDBFactory.java)

The class diagrams of each database driver are shown below.

![Class Diagram SQLServer](./docs/images/ClassDiagram_SQLServer_1.png)

Figure 1 - Class diagram of the SQLServerDB driver

![Class Diagram OracleDB](./docs/images/ClassDiagram_OracleDB_1.png)

Figure 2 - Class diagram of the OracleDB driver

## 2. How-To Build the project from sources
Requirements for build the project
1. Sun/Oracle JDK 1.8/JDK 11
2. Maven 3.x (for build project)

The driver that adds support for *Oracle*, SQLServer and *DB2* database
is a jar artifact (**liferay-portal-database-all-in-one-support-${version}.jar**) which
then will be installed in `ROOT/WEB-INF/lib` (for the Apache Tomcat).

To generate the all in one driver just follow the instructions below.

**You can download the latest version binary jar from Maven Central Repository**
[liferay-portal-database-all-in-one-support](https://search.maven.org/#search%7Cga%7C1%7Cit.dontesta),
by doing so you can avoid doing the build.

```bash
$ git clone https://github.com/amusarra/liferay-portal-database-all-in-one-support.git
$ cd liferay-portal-database-all-in-one-support
$ mvn package
```

Console 1 - Clone the source project from GitHub and run package goal

The build process create the jar
`liferay-portal-database-all-in-one-support-${version}.jar` inside the (maven)
target directory.

## 3. Installation notes for Liferay 7.4 GA3
Since version 7.4 GA3 of Liferay, the so-called [Shielded Container](https://issues.liferay.com/browse/LPS-104371)
mechanism has been introduced, so the directory on which to install the JDBC
driver and the driver of this project changes.

In the case of the Tomcat bundle, the new directory is:
**tomcat-9.0.43/webapps/ROOT/WEB-INF/shielded-container-lib**

![New directory installation to db driver for Liferay 7.4 GA3](./docs/images/new_location_to_install_driver.png)

Figure 3 - New directory installation to db driver for Liferay 7.4 GA3

## 4. How-To Configure Liferay Portal

Below you can see the `portal-ext.properties`. In the sample file are shown JDBC
configurations sample for Oracle, SQL Server, and DB2.

```properties
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

Source Code 2 - Sample JDBC connection strings for Oracle, SQL Server and DB2 database

You could also configure database access as a **[JNDI](https://en.wikipedia.org/wiki/Java_Naming_and_Directory_Interface)** resource and specify the
resource name in configuration.

```properties
##
## JDBC
##
    #
    # Set the JNDI name to lookup the JDBC data source. If none is set,
    # then the portal will attempt to create the JDBC data source based on the
    # properties prefixed with "jdbc.default."
    #
    jdbc.default.jndi.name=java:jdbc/LiferayPool
```

Source Code 3 - Configure the JDBC connection via JNDI

In order for Liferay to be able to connect to the database, it is necessary to
install the JDBC driver compatible with the version of the specific database and
JVM. Here are the links to the resources to download the JDBC driver.

1. [Oracle JDBC Drivers](https://www.oracle.com/database/technologies/appdev/jdbc-downloads.html)
2. [Microsoft SQL Server JDBC Drivers and support matrix](https://docs.microsoft.com/en-us/sql/connect/jdbc/microsoft-jdbc-driver-for-sql-server-support-matrix?view=sql-server-ver15)

The following documents (see database section) provide details of the
configurations that are certified by Liferay. You can see the complete
documents on [Liferay Portal](https://web.liferay.com/it/services/support/compatibility-matrix).

![Liferay_72_Compatibility_Matrix_Database](docs/images/Liferay_72_Compatibility_Matrix_Database.png)

Figure 4 - Liferay_72_Compatibility_Matrix_Database

The figure below shows an example of connection to the Liferay 7.4 GA3 Oracle database.

![New directory installation to db driver for Liferay 7.4 GA3](./docs/images/view_database_oracle_19c_liferay_74_ga3.png)

Figure 5 - Connection to Liferay 7.4 GA3

## 4. Other useful resources

1. [How to build a Docker Liferay 7.2 image with the Oracle Database support](https://www.dontesta.it/en/2019/08/21/how-to-build-a-docker-liferay-7-2-image-with-the-oracle-database-support/)
2. [How to build a Docker Liferay 7.2 image with the SQL Server 2017 Database support](https://www.dontesta.it/en/2019/10/06/how-to-build-a-docker-liferay-7-2-image-with-the-sql-server-2017-database-support/)
3. [Liferay 7.1: How to add support for Oracle DB](https://www.dontesta.it/en/2018/10/07/liferay-7-1-how-to-add-support-for-oracle-db/)
4. [Come installare Liferay 7 su JBOSS EAP con il supporto Oracle Database](https://www.slideshare.net/amusarra/come-installare-liferay-7-su-jboss-eap-con-il-support-oracle-database)
5. [Liferay 7 Wildfly: How to add support for Oracle DB](https://www.youtube.com/watch?v=7fojCjko7Ac) (video on Antonio Musarra's Blog YouTube Channel)
6. [Liferay 7 Community Edition GA5 & Oracle 12c via Docker Composer](https://www.youtube.com/watch?v=yLVCEl8L8cU) (video on Antonio Musarra's Blog YouTube Channel)
7. [Come installare Liferay 7 su JBoss EAP con il supporto per Oracle Database](https://www.youtube.com/watch?v=QaVaP89yWiM&t=848s) (video on Antonio Musarra's Blog YouTube Channel)
8. [How to setup Docker container Oracle Database 19c for Liferay Development Environment](https://www.dontesta.it/en/2020/03/15/how-to-setup-docker-container-oracle-database-19c-for-liferay-development-environment/)

## Liferay Portal Database All-In-One License
SPDX-License-Identifier: LGPL-2.1-or-later

Liferay Portal Database All-In-One is free software: you can redistribute it
and/or modify it under the terms of the GNU Lesser General Public License as
published by the Free Software Foundation, either version 2.1 of the License,
or (at your option) any later version.

Liferay Portal Database All-In-One is distributed in the hope that it will
be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Liferay Portal Community Edition. If not,
see https://www.gnu.org/licenses/