# Carbon UUF Maven Archetypes

Carbon UUF Maven Archetypes project provides a set of maven archetypes for creating UUF Apps, UUF Components and UUF Themes of the [Unified UI Framework(UUF)](https://github.com/wso2/carbon-uuf).

Carbon UUF Maven Archetypes provides project templates for each UUF projects. This project provides three maven archetypes;

* UUF Application Archetype : This archtype is used for creating UUF Application template project.
* UUF Compoenent Archetype : This archtype is used for creating UUF Component template project.
* UUF Theme Archetype : This archtype is used for creating UUF Theme template project.

## Getting Started

### Installing UUF Archetypes

First you need to download correct version of maven archetype. Replace [version] with required version you need to download. 

    git clone -b release-[version] --single-branch https://github.com/wso2/carbon-uuf-maven-plugin.git

To install this maven archetype into your local system, issue following command inside the carbon-uuf-maven-plugin.

    mvn clean install

### Create sample UUF-Application using uuf-application-archetype

Navigate into a preferred location where you need to create your sample project.
  
    mvn archetype:generate -DarchetypeCatalog=local
  
Then select `the org.wso2.carbon.maven.uuf.archtype:uuf-application-archetype` as new archetype.   

```sh
mvn archetype:generate -DarchetypeCatalog=local
[INFO] Scanning for projects...
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] Building Maven Stub Project (No POM) 1
[INFO] ------------------------------------------------------------------------
[INFO]
[INFO] >>> maven-archetype-plugin:2.4:generate (default-cli) @ standalone-pom >>>
[INFO]
[INFO] <<< maven-archetype-plugin:2.4:generate (default-cli) @ standalone-pom <<<
[INFO]
[INFO] --- maven-archetype-plugin:2.4:generate (default-cli) @ standalone-pom ---
[INFO] Generating project in Interactive mode
[INFO] No archetype defined. Using maven-archetype-quickstart (org.apache.maven.archetypes:maven-archetype-quickstart:1.0)
Choose archetype:
1: local -> org.wso2.iot:mdm-android-agent-archetype (Creates a MDM-Android agent project)
2: local -> org.wso2.cdmf.devicetype:cdmf-devicetype-archetype (WSO2 CDMF Device Type Archetype)
3: local -> org.wso2.iot:cdmf-devicetype-archetype (WSO2 CDMF Device Type Archetype)
4: local -> org.wso2.msf4j:msf4j-microservice (This an archetype for WSO2 MSF4J microservice)
5: local -> org.wso2.carbon.maven.uuf.archtype:uuf-application-archetype (UUF - Application Archetype)
6: local -> org.wso2.carbon.maven.uuf.archtype:uuf-component-archetype (UUF - Component Archetype)
7: local -> org.wso2.carbon.maven.uuf.archtype:uuf-theme-archetype (UUF - Theme Archetype)
Choose a number or apply filter (format: [groupId:]artifactId, case sensitive contains): : 5
```

Then the archtype will provide some default values for groupId, artifactId, version, packaging and name of your application.
Or you can just type 'n' to start interactive mode for entering your own values.

### Building UUF App and deploy.

To build newly created UUF App, issue following command inside the UUF-App project.

    mvn clean install

Then copy-paste the built `zip` artifact inside the target folder(eg. org.wso2.carbon.uuf.sample.hello-world.zip) into UUF_HOME/deployement/uufapps folder.

### Creating sample UUF Component and UUF Theme

This is same as creating UUF App. You can use `org.wso2.carbon.maven.uuf.archtype:uuf-component-archetype` and `org.wso2.carbon.maven.uuf.archtype:uuf-theme-archetype` accordingly.

## Integrating UUF Archetypes into IDE

All the local built artifcats are saved under `~/.m2/archetype-catalog.xml` file.

### Adding UUF Archetypes into Eclipse IDE
Goto "File->New->Maven Project->Next->Next". Select "All Catalogs" on the Catalog dropdown and tick "Include snapshot archetypes", then all local catalog archetypes may appear.

### Adding UUF Archetypes into Intellij IDE

Goto "File-->New Project-->Maven-->Next". Then click "Add archetype" and enter the following values for groupId, artifactId and version. Replace [version] with required version you need. 

    GroupId : ArtifactId : Version
    org.wso2.carbon.maven.uuf.archtype:uuf-application-archetype:[version]
    org.wso2.carbon.maven.uuf.archtype:uuf-component-archetype:[version]
    org.wso2.carbon.maven.uuf.archtype:uuf-theme-archetype:[version]
