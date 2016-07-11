# Carbon UUF Maven Plugins

[![Build Status](https://wso2.org/jenkins/buildStatus/icon?job=carbon-uuf-maven-plugin)](https://wso2.org/jenkins/me/my-views/view/All/job/carbon-uuf-maven-plugin/)

Carbon UUF Maven Plugins project provides a maven plugin and archetypes for creating UUF Applications, UUF Components and UUF Themes of the [Unified UI Framework(UUF)](https://github.com/wso2/carbon-uuf).

## 1. Carbon UUF Maven Archetypes

Carbon UUF Maven Archetypes provides project templates for each UUF projects. This project provides three maven archetypes;

* UUF Application Archetype : This archtype is used for creating UUF Application template project.
* UUF Compoenent Archetype : This archtype is used for creating UUF Component template project.
* UUF Theme Archetype : This archtype is used for creating UUF Theme template project.

### Getting Started

#### Installing UUF Archetypes

First you need to download correct version of maven archetype. Replace [version] with required version you need to download. 

    git clone -b release-[version] --single-branch https://github.com/wso2/carbon-uuf-maven-plugin.git

To install this maven archetype into your local system, issue following command inside the carbon-uuf-maven-plugin.

    mvn clean install

#### Create sample UUF-Application using uuf-application-archetype

Navigate into a preferred location where you need to create your sample project.
  
    mvn archetype:generate -DarchetypeCatalog=local
  
Then select `the org.wso2.carbon.uuf.maven.archtype:uuf-application-archetype` as new archetype.   

For more information on Carbon UUF Archetypes please [click here](https://github.com/rasika/carbon-uuf-maven-plugin/tree/master/arche-types).

## 2. Carbon UUF Maven Plugin

Carbon UUF Maven Plugin tries to reusing the existing maven plugins where as possible(i.e.Maven-Assembly-Plugin, Maven-Dependency-Plugin). This plugin provides two maven goals;

* create-component : This goal is used for creating UUF Component.
* create-application : This goal is used for creating UUF Application.
* create-theme : This goal is used for creating UUF Theme.

### Getting Started

A client maven module which needs to create a UUF application and/or component should add the plugin dependency into project pom.xml file.

#### Creating a UUF Application

```xml
<plugin>
    <groupId>org.wso2.carbon.uuf.maven</groupId>
    <artifactId>carbon-uuf-maven-plugin</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>create-application</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```
For more information on Carbon UUF Maven Plugin please [click here](https://github.com/rasika/carbon-uuf-maven-plugin/tree/master/maven-plugin).

## Download 

Use Maven snippet:
````xml
<dependency>
    <groupId>org.wso2.carbon.uuf.maven</groupId>
    <artifactId>carbon-uuf-maven-plugin</artifactId>
    <version>${carbon-uuf-maven-plugin.version}</version>
</dependency>
````

### Snapshot Releases

Use following Maven repository for snapshot versions of Carbon Maven UUF Plugin.

````xml
<repository>
    <id>wso2.snapshots</id>
    <name>WSO2 Snapshot Repository</name>
    <url>http://maven.wso2.org/nexus/content/repositories/snapshots/</url>
    <snapshots>
        <enabled>true</enabled>
        <updatePolicy>daily</updatePolicy>
    </snapshots>
    <releases>
        <enabled>false</enabled>
    </releases>
</repository>
````

### Released Versions

Use following Maven repository for released stable versions of Carbon Maven UUF Plugin.

````xml
<repository>
    <id>wso2.releases</id>
    <name>WSO2 Releases Repository</name>
    <url>http://maven.wso2.org/nexus/content/repositories/releases/</url>
    <releases>
        <enabled>true</enabled>
        <updatePolicy>daily</updatePolicy>
        <checksumPolicy>ignore</checksumPolicy>
    </releases>
</repository>
````

## Building From Source

Clone this repository first (`git clone https://github.com/wso2/carbon-uuf-maven-plugin.git`) and use Maven install to build `mvn clean install`.

## Contributing to Carbon Maven UUF Plugin Project

Pull requests are highly encouraged and we recommend you to create a GitHub issue to discuss the issue or feature that you are contributing to.  

## License

Carbon Maven UUF Plugin is available under the Apache 2 License.

## Copyright

Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
