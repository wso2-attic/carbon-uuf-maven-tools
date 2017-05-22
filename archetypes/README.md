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

### Create sample UUF-Application using uuf-app-archetype

Navigate into a preferred location where you need to create your sample project.
  
    mvn archetype:generate -DarchetypeCatalog=local
  
Then select `the org.wso2.carbon.uuf.maven.archtype:uuf-app-archetype` as new archetype.   

```sh
$ mvn archetype:generate -DarchetypeCatalog=local
[INFO] Scanning for projects...
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building Maven Stub Project (No POM) 1
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] >>> maven-archetype-plugin:2.4:generate (default-cli) > generate-sources @ standalone-pom >>>
[INFO] 
[INFO] <<< maven-archetype-plugin:2.4:generate (default-cli) < generate-sources @ standalone-pom <<<
[INFO] 
[INFO] --- maven-archetype-plugin:2.4:generate (default-cli) @ standalone-pom ---
[INFO] Generating project in Interactive mode
[INFO] No archetype defined. Using maven-archetype-quickstart (org.apache.maven.archetypes:maven-archetype-quickstart:1.0)
Choose archetype:
1: local -> org.apache.synapse:synapse-package-archetype (This archetype can be used to create Maven projects that bundle a mediation
        into a standalone distribution ready to be executed)
2: local -> org.wso2.carbon.uuf.maven:uuf-theme-archetype (Creates an UUF theme project)
3: local -> org.wso2.carbon.uuf.maven:uuf-component-archetype (Creates an UUF component project)
4: local -> org.wso2.carbon.uuf.maven:uuf-app-archetype (Maven archetype for UUF apps)
Choose a number or apply filter (format: [groupId:]artifactId, case sensitive contains): : 4

```

Then the archtype will provide some default values for groupId, artifactId, version, packaging and name of your app.
Or you can just type 'n' to start interactive mode for entering your own values.

### Building UUF App and deploy.

To build newly created UUF App, issue following command inside the UUF-App project.

    mvn clean install

The above build will generate a carbon-p2-feature compatible feature for the app as a zip archive.
To use the the built app (i.e carbon-feature) in your project, you can use carbon-feature-plugin to integrate with the build like below.

Add the app as a dependency like below for your project so that carbon-feature-plugin resolves it during build time.

    <dependency>
           <groupId>org.wso2.carbon.uuf.sample</groupId>
           <artifactId>org.wso2.carbon.uuf.sample.hello-world.feature</artifactId>
           <version>1.0.0-SNAPSHOT</version>
           <type>zip</type>
    </dependency>

Then configure your project p2 profile generation config sections to reflect the feature configuration like below under
"generate-repo" and "install" goals of "package" phase of carbon-feature-plugin.

Add the following feature config under "generate-repo" goal

    <feature>
      <id>org.wso2.carbon.uuf.sample.hello-world.feature</id>
       <version>1.0.0-SNAPSHOT</version>
    </feature>

Add the following feature config under "install" goal

    <feature>
       <id>org.wso2.carbon.uuf.sample.hello-world.feature.group</id>
       <version>1.0.0-SNAPSHOT</version>
    </feature>


Once the above app feature is successfully added to your project build, then it will be installed in CARBON_HOME/deployment/uufapps folder.

You can refer the UUF product build on how to include carbon-feature-plugin and apps as carbon-features from - https://github.com/wso2/carbon-uuf/blob/master/product/pom.xml

### Creating sample UUF Component and UUF Theme

This is same as creating UUF App. You can use `org.wso2.carbon.uuf.maven.archtype:uuf-component-archetype` and `org.wso2.carbon.uuf.maven.archtype:uuf-theme-archetype` accordingly.

## Integrating UUF Archetypes into IDE

All the local built artifcats are saved under `~/.m2/archetype-catalog.xml` file.

### Adding UUF Archetypes into Eclipse IDE
Goto "File->New->Maven Project->Next->Next". Select "All Catalogs" on the Catalog dropdown and tick "Include snapshot archetypes", then all local catalog archetypes may appear.

### Adding UUF Archetypes into Intellij IDE

Goto "File-->New Project-->Maven-->Next". Then click "Add archetype" and enter the following values for groupId, artifactId and version. Replace [version] with required version you need. 

    GroupId : ArtifactId : Version
    org.wso2.carbon.uuf.maven:uuf-app-archetype:[version]
    org.wso2.carbon.uuf.maven:uuf-component-archetype:[version]
    org.wso2.carbon.uuf.maven:uuf-theme-archetype:[version]
