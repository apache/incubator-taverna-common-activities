
<snippet>
  <content>
# Taverna-CWL-Activity-UI Plugin
This module contains the UI plugin implementaion of the [Common Workflow Language(CWL)](http://www.commonwl.org/). The basic funcionalty of this plugin is to find the CWL tool in the given directory and extract configuration information from the tools. 

##Implementaion Approach

The class [CwlServiceProvider](https://github.com/ThilinaManamgoda/incubator-taverna-common-activities/blob/master/taverna-cwl-activity-ui/src/main/java/org/apache/taverna/cwl/ui/serviceprovider/CwlServiceProvider.java) is responsible for finding the cwl tool descriptions and extract configuration info. Some of new Java 8 features are used in this class(Stream, Lambda expressions). This Service provider is configurable, which means the user can give the location of the cwl tools or it can use default paths if not. Cwl tools are parsed using [SnakeYaml](https://bitbucket.org/asomov/snakeyaml) lib.  

The class [CwlContextualView](https://github.com/ThilinaManamgoda/incubator-taverna-common-activities/blob/master/taverna-cwl-activity-ui/src/main/java/org/apache/taverna/cwl/ui/view/CwlContextualView.java) is responsible for displaying the information of the tool in the service detail panel in the Taverna workbench.

##Abstract view of the plugin

 ![](https://github.com/ThilinaManamgoda/incubator-taverna-common-activities/blob/master/taverna-cwl-activity-ui/images/Cwl-activity-Ui.png)
  </content>


</snippet>
