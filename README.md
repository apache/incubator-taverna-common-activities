<!--
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
-->


## Taverna Project Retired

> tl;dr: The Taverna code base is **no longer maintained** 
> and is provided here for archival purposes.

From 2014 till 2020 this code base was maintained by the 
[Apache Incubator](https://incubator.apache.org/) project _Apache Taverna (incubating)_
(see [web archive](https://web.archive.org/web/20200312133332/https://taverna.incubator.apache.org/)
and [podling status](https://incubator.apache.org/projects/taverna.html)).

In 2020 the Taverna community 
[voted](https://lists.apache.org/thread.html/r559e0dd047103414fbf48a6ce1bac2e17e67504c546300f2751c067c%40%3Cdev.taverna.apache.org%3E)
to **retire** Taverna as a project and withdraw the code base from the Apache Software Foundation. 

This code base remains available under the Apache License 2.0 
(see _License_ below), but is now simply called 
_Taverna_ rather than ~~Apache Taverna (incubating)~~.

While the code base is no longer actively maintained, 
Pull Requests are welcome to the 
[GitHub organization taverna](http://github.com/taverna/), 
which may infrequently be considered by remaining 
volunteer caretakers.


### Previous releases

Releases 2015-2018 during incubation at Apache Software Foundation
are available from the ASF Download Archive <http://archive.apache.org/dist/incubator/taverna/>

Releases 2014 from the University of Manchester are on BitBucket <https://bitbucket.org/taverna/>

Releases 2009-2013 from myGrid are on LaunchPad <https://launchpad.net/taverna/>

Releases 2003-2009 are on SourceForge <https://sourceforge.net/projects/taverna/files/taverna/>

Binary JARs for Taverna are available from 
Maven Central <https://repo.maven.apache.org/maven2/org/apache/taverna/>
or the myGrid Maven repository <https://repository.mygrid.org.uk/>




# Taverna Common Activities

Common Activities to be invoked as part of 
[Taverna](https://web.archive.org/web/*/https://taverna.incubator.apache.org/) workflows.

Note that this module relies on other
[Taverna modules](https://web.archive.org/web/*/https://taverna.incubator.apache.org/code) for
the actual workflow execution.


## License

* (c) 2007-2014 University of Manchester
* (c) 2014-2020 Apache Software Foundation

This product includes software developed at The
[Apache Software Foundation](http://www.apache.org/).

Licensed under the
[Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0), see the file
[LICENSE](LICENSE) for details.

The file [NOTICE](NOTICE) contains any additional attributions and
details about embedded third-party libraries and source code.


# Contribute

Any contributions received are assumed to be covered by the [Apache License
2.0](https://www.apache.org/licenses/LICENSE-2.0). We might ask you
to sign a [Contributor License Agreement](https://www.apache.org/licenses/#clas)
before accepting a larger contribution.



## Prerequisites

* Java 1.8 or newer (tested with OpenJDK 1.8)
* [Apache Maven](https://maven.apache.org/download.html) 3.2.5 or newer (older
  versions probably also work)

This code relies on other
[Taverna modules](https://web.archive.org/web/*/https://taverna.incubator.apache.org/download/code/),
which Maven shuold automatically
download from
[Apache's Maven repository](https://web.archive.org/web/*/https://taverna.incubator.apache.org/download/maven/);
however you might want to compile these yourself in the below order:

* [taverna-language](https://web.archive.org/web/*/https://taverna.incubator.apache.org/download/language/)
* [taverna-osgi](https://web.archive.org/web/*/https://taverna.incubator.apache.org/download/osgi/)
* [taverna-engine](https://web.archive.org/web/*/https://taverna.incubator.apache.org/download/engine/)

Please see the `<properties>` of this [pom.xml](pom.xml) to find the
correct versions to build.


# Building

To build, use

    mvn clean install

This will build each module and run their tests.


## Building on Windows

If you are building on Windows, ensure you unpack this source code
to a folder with a [short path name](http://stackoverflow.com/questions/1880321/why-does-the-260-character-path-length-limit-exist-in-windows) 
lenght, e.g. `C:\src` - as 
Windows has a [limitation on the total path length](https://msdn.microsoft.com/en-us/library/aa365247%28VS.85%29.aspx#maxpath) 
which might otherwise
prevent this code from building successfully.


## Skipping tests

To skip the tests (these can be time-consuming), use:

    mvn clean install -DskipTests


If you are modifying this source code independent of the
Taverna project, you may not want to run the
[Rat Maven plugin](https://creadur.apache.org/rat/apache-rat-plugin/)
that enforces Apache headers in every source file - to disable it, try:

    mvn clean install -Drat.skip=true

# Modules

Each module implement a particular type of 
Taverna [Activity](https://web.archive.org/web/*/https://taverna.incubator.apache.org/javadoc/taverna-engine/org/apache/taverna/workflowmodel/processor/activity/Activity.html).

* [taverna-beanshell-activity](taverna-beanshell-activity/): Taverna Beanshell Activity, 
  runs [Beanshell](https://github.com/beanshell/beanshell/) scripts.
* [taverna-external-tool-activity](taverna-external-tool-activity/): Taverna External Tool Activity, 
  run command line locally or over SSH
* [taverna-interaction-activity](taverna-interaction-activity/): Taverna Interaction Activity, 
  ask questions to the user through a browser
* [taverna-rest-activity](taverna-rest-activity/): Taverna REST Activity, 
  invoke [RESTful](https://en.wikipedia.org/wiki/Representational_state_transfer) HTTP(S) web services based on a URI template.
* [taverna-spreadsheet-import-activity](taverna-spreadsheet-import-activity/): Taverna Spreadsheet Import Activity, 
  imports from CSV, Excel and OpenOffice.
* [taverna-wsdl-activity](taverna-wsdl-activity/): Taverna WSDL Activity, 
  invoke [WSDL](https://en.wikipedia.org/wiki/Web_Services_Description_Language)-described SOAP services.
* [taverna-wsdl-generic](taverna-wsdl-generic/): Taverna WSDL-generic Library, 
  library for parsing WSDL and calling SOAP services
* [taverna-xpath-activity](taverna-xpath-activity/): Taverna XPath Activity, 
  select XML fragments using [XPath](https://www.w3.org/TR/xpath/) expressions


# Export restrictions

This distribution includes cryptographic software.
The country in which you currently reside may have restrictions
on the import, possession, use, and/or re-export to another country,
of encryption software. BEFORE using any encryption software,
please check your country's laws, regulations and policies
concerning the import, possession, or use, and re-export of
encryption software, to see if this is permitted.
See <http://www.wassenaar.org/> for more information.

The U.S. Government Department of Commerce, Bureau of Industry and Security (BIS),
has classified this software as Export Commodity Control Number (ECCN) 5D002.C.1,
which includes information security software using or performing
cryptographic functions with asymmetric algorithms.
The form and manner of this Apache Software Foundation distribution makes
it eligible for export under the License Exception
ENC Technology Software Unrestricted (TSU) exception
(see the BIS Export Administration Regulations, Section 740.13)
for both object code and source code.

The following provides more details on the included cryptographic software:

* [taverna-rest-activity](taverna-rest-activity)
  depends on
  [Apache HttpComponents](https://hc.apache.org/) Client,
  and can be configured to initiate
  `https://` connections.
* [taverna-wsdl-generic](taverna-wsdl-generic)
  and [taverna-wsdl-activity](taverna-wsdl-activity) use
  [Java Secure Socket Extension](https://docs.oracle.com/javase/8/docs/technotes/guides/security/jsse/JSSERefGuide.html)
  (JSSE) and depend on
  [Apache WSS4J](https://ws.apache.org/wss4j/),
  [Apache XML Security for Java](https://santuario.apache.org/javaindex.html)
  for accessing secure SOAP Web Services.
* Taverna Common Activities depends on the
  [Taverna Engine](http://taverna.incubator.apache.org/download/engine/)
  Credential Manager API for
  management of username/password and client/server SSL certificates.
* [taverna-interaction-activity](taverna-interaction-activity) depends on
  [Jetty](http://www.eclipse.org/jetty/),
  which includes UnixCrypt.java for one way cryptography, and can be
  configured for SSL encryption using
  [Java Secure Socket Extension](https://docs.oracle.com/javase/8/docs/technotes/guides/security/jsse/JSSERefGuide.html).

