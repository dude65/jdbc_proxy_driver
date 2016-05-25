# jdbc_proxy_driver
This is a JDBC proxy driver that provides connections to multiple databases according to reqular expressions without a need of a content switching. This is useful for databases with replicated data when it is needed to validate data among databases according to replication definitions (which are regular expressions). It can be use for switching sql queries to 2 and more databases.

## Prerequisites:
* **Java 1.7**
* **Maven** - build application
* **Apache commons lang3** (v. 3.4) library. This library must be on java classpath when using this driver. It should work for older versions as well, probably, but this is untested.

## Build project:
Use command in your command line "mvn package" in project root directory. To generate apidocs, use command "mvn javadoc:javadoc".

## Usage:
**The class of the driver is**: org.fit.proxy.jdbc.ProxyDriver

**The driver accepts url**: jdbc:proxy:path/to/properties.properties

To see how to configure properties and to see usage examples, visit usage.html

## License:
This project was created as a school (FIT ČVUT) work in cooperetion with Profinit s.r.o company. The project is licensed under Apache License 2.0. It is possible to see the whole text of the license in included file - license.txt or you can visit http://www.apache.org/licenses/LICENSE-2.0 .

**Author: Ondřej Marek, 2016**
