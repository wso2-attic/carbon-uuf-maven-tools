# Carbon UUF Maven Plugin

Carbon UUF Maven Plugin project provides a maven plugin for creating UUF Applications and UUF Components of the [Unified UI Framework(UUF)](https://github.com/wso2/carbon-uuf).

Carbon UUF Maven Plugin tries to reusing the existing maven plugins where as possible(i.e.Maven-Assembly-Plugin, Maven-Dependency-Plugin). This plugin provides two maven goals;

* create-component : This goal is used for creating UUF Component.
* create-application : This goal is used for creating UUF Application.

## Getting Started

A client maven module which needs to create a UUF application and/or component should add the plugin dependency into project pom.xml file.

#### 1) Creating a UUF Component

```xml
<plugin>
    <groupId>org.wso2.carbon.uuf.maven</groupId>
    <artifactId>carbon-uuf-maven-plugin</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>create-component</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

#### 2) Creating a UUF Theme

```xml
<plugin>
    <groupId>org.wso2.carbon.uuf.maven</groupId>
    <artifactId>carbon-uuf-maven-plugin</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>create-theme</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

#### 3) Creating a UUF Application

This is the way of creating the UUF application. All the `Pages` and `Fragments` of the current Application will be moved into a component called "root" inside the "/components" folder.
  
```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.wso2.carbon.uuf.maven</groupId>
            <artifactId>carbon-uuf-maven-plugin</artifactId>
            <version>1.0.0-SNAPSHOT</version>
            <executions>
                <execution>
                    <id>create</id>
                    <phase>package</phase>
                    <goals>
                        <goal>create-application</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

#### 4) Adding UUF Components and Themes dependencies to the UUF Application.

Following UUF Application reuses the UUF components "base"(org.wso2.carbon.uuf.base) and "basicauth"(org.wso2.is.uuf.basicauth) and utilize the theme "dark"(org.wso2.uuf.theme.dark).

```xml
<dependencies>
   <!-- themes -->
   <dependency>
      <groupId>org.wso2.uuf</groupId>
      <artifactId>org.wso2.uuf.theme.dark</artifactId>
      <version>1.0.0-SNAPSHOT</version>
      <type>zip</type>
  </dependency>
  <!-- components -->
  <dependency>
      <groupId>org.wso2.uuf</groupId>
      <artifactId>org.wso2.uuf.base</artifactId>
      <version>1.0.0-SNAPSHOT</version>
      <type>zip</type>
  </dependency>
  <dependency>
      <groupId>org.wso2.is</groupId>
      <artifactId>org.wso2.is.uuf.basicauth</artifactId>
      <version>1.0.0-SNAPSHOT</version>
      <type>zip</type>
  </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.wso2.carbon.uuf.maven</groupId>
            <artifactId>carbon-uuf-maven-plugin</artifactId>
            <version>1.0.0-SNAPSHOT</version>
            <executions>
                <execution>
                    <id>create</id>
                    <phase>package</phase>
                    <goals>
                        <goal>create-application</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

#### 5) When a UUF Application depends on another UUF Application.

Following UUF Application reuses the UUF Application "pets-store"(org.wso2.carbon.uuf.sample.pets-store). In this case, the "root" components of the both applications are merged. When a duplicate occurs target application receives the priority.

```xml
<dependencies>
    <dependency>
        <groupId>org.wso2.uuf.sample</groupId>
        <artifactId>org.wso2.uuf.sample.pets-store</artifactId>
        <version>1.0.0-SNAPSHOT</version>
        <type>zip</type>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.wso2.carbon.uuf.maven</groupId>
            <artifactId>carbon-uuf-maven-plugin</artifactId>
            <version>1.0.0-SNAPSHOT</version>
            <executions>
                <execution>
                    <id>create</id>
                    <phase>package</phase>
                    <goals>
                        <goal>create-application</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```
#### OSGi Imports for UUF Artifacts
If you are using Java classes exported by other OSGi bundles inside your JavaScript files, you need to explicitly mention the package imports inorder to minimize classloading complexisities. For instance;

```xml
<properties>
    <import.package>
        org.wso2.carbon.uuf.*;version=[1.0.0,2.0.0],
        org.wso2.msf4j
    </import.package>
</properties>
```
