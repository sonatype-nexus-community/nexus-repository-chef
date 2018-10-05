<!--

    Sonatype Nexus (TM) Open Source Version
    Copyright (c) 2018-present Sonatype, Inc.
    All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.

    This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
    which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.

    Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
    of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
    Eclipse Foundation. All other trademarks are the property of their respective owners.

-->
# Nexus Repository Chef Format

[![Build Status](https://travis-ci.org/sonatype-nexus-community/nexus-repository-chef.svg?branch=master)](https://travis-ci.org/sonatype-nexus-community/nexus-repository-chef) 
[![Join the chat at https://gitter.im/sonatype/nexus-developers](https://badges.gitter.im/sonatype/nexus-developers.svg)](https://gitter.im/sonatype/nexus-developers?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![DepShield Badge](https://depshield.sonatype.org/badges/sonatype-nexus-community/nexus-repository-chef/depshield.svg)](https://depshield.github.io)

# Table Of Contents

* [Developing](#developing)
   * [Requirements](#requirements)
   * [Building](#building)
* [Using Chef with Nexus Repository Manger 3](#using-chef-with-nexus-repository-manager-3)
* [Compatibility with Nexus Repository Manager 3 Versions](#compatibility-with-nexus-repository-manager-3-versions)
* [Features Implemented In This Plugin](#features-implemented-in-this-plugin)
* [Installing the plugin](#installing-the-plugin)
   * [Temporary Install](#temporary-install)
   * [(more) Permanent Install](#more-permanent-install)
   * [(most) Permament Install](#most-permanent-install)
* [The Fine Print](#the-fine-print)
* [Getting Help](#getting-help)

## Developing

### Requirements

* [Apache Maven 3.3.3+](https://maven.apache.org/install.html)
* [Java 8+](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* Network access to https://repository.sonatype.org/content/groups/sonatype-public-grid

Also, there is a good amount of information available at [Bundle Development](https://help.sonatype.com/display/NXRM3/Bundle+Development)

### Building

To build the project and generate the bundle use Maven

    mvn clean package

If everything checks out, the bundle for chef should be available in the `target` folder

#### Build with Docker

`docker build -t nexus-repository-chef:0.0.1 .`

#### Run as a Docker container

`docker run -d -p 8081:8081 --name nexus nexus-repository-chef:0.0.1` 

For further information like how to persist volumes check out [the GitHub repo for our official image](https://github.com/sonatype/docker-nexus3).

The application will now be available from your browser at http://localhost:8081

## Using Chef With Nexus Repository Manager 3

[We have detailed instructions on how to get started here!](docs/CHEF_USER_DOCUMENTATION.md)

## Compatibility with Nexus Repository Manager 3 Versions

The table below outlines what version of Nexus Repository the plugin was built against

| Plugin Version | Nexus Repository Version |
|----------------|--------------------------|
| v0.0.1         | 3.13.0-01                |

If a new version of Nexus Repository is released and the plugin needs changes, a new release will be made, and this
table will be updated to indicate which version of Nexus Repository it will function against. This is done on a time 
available basis, as this is community supported. If you see a new version of Nexus Repository, go ahead and update the
plugin and send us a PR after testing it out!

All released versions can be found [here](https://github.com/sonatype-nexus-community/nexus-repository-chef/releases).

## Features Implemented In This Plugin 

| Feature | Implemented          |
|---------|----------------------|
| Proxy   | :heavy_check_mark: * |
| Hosted  |                      |
| Group   |                      |

`* tested primarily against the Chef Supermarket, not guaranteed to work on the wide wild world of Chef repositories.`

### Knife Commands Supported

#### Proxy

| Plugin Version               | Nexus Repository Version |
|------------------------------|--------------------------|
| `knife supermarket install`  | :heavy_check_mark:       |
| `knife supermarket download` | :heavy_check_mark:       |
| `knife supermarket list`     | :heavy_check_mark:       |
| `knife supermarket search`   | :heavy_check_mark:       |
| `knife supermarket show`     | :heavy_check_mark:       |
| `knife supermarket share`    | *                        |
| `knife supermarket unshare`  | *                        |

`* these commands are not applicable to a proxy`

## Installing the plugin

There are a range of options for installing the chef plugin. You'll need to build it first, and
then install the plugin with the options shown below:

### Temporary Install

Installations done via the Karaf console will be wiped out with every restart of Nexus Repository. This is a
good installation path if you are just testing or doing development on the plugin.

* Enable Nexus console: edit `<nexus_dir>/bin/nexus.vmoptions` and change `karaf.startLocalConsole`  to `true`.

  More details here: [Bundle Development](https://help.sonatype.com/display/NXRM3/Bundle+Development+Overview)

* Run Nexus' console:
  ```
  # sudo su - nexus
  $ cd <nexus_dir>/bin
  $ ./nexus run
  > bundle:install file:///tmp/nexus-repository-chef-0.0.1.jar
  > bundle:list
  ```
  (look for org.sonatype.nexus.plugins:nexus-repository-chef ID, should be the last one)
  ```
  > bundle:start <org.sonatype.nexus.plugins:nexus-repository-chef ID>
  ```

### (more) Permanent Install

For more permanent installs of the nexus-repository-chef plugin, follow these instructions:

* Copy the bundle (nexus-repository-chef-0.0.1.jar) into <nexus_dir>/deploy

This will cause the plugin to be loaded with each restart of Nexus Repository. As well, this folder is monitored
by Nexus Repository and the plugin should load within 60 seconds of being copied there if Nexus Repository
is running. You will still need to start the bundle using the karaf commands mentioned in the temporary install.

### (most) Permanent Install

If you are trying to use the chef plugin permanently, it likely makes more sense to do the following:

* Copy the bundle into `<nexus_dir>/system/org/sonatype/nexus/plugins/nexus-repository-chef/0.0.1/nexus-repository-chef-0.0.1.jar`
* Make the following additions marked with + to `<nexus_dir>/system/org/sonatype/nexus/assemblies/nexus-core-feature/3.x.y/nexus-core-feature-3.x.y-features.xml`

   ```
         <feature prerequisite="false" dependency="false">nexus-repository-rubygems</feature>
   +     <feature prerequisite="false" dependency="false">nexus-repository-chef</feature>
         <feature prerequisite="false" dependency="false">nexus-repository-gitlfs</feature>
     </feature>
   ```
   And
   ```
   + <feature name="nexus-repository-chef" description="org.sonatype.nexus.plugins:nexus-repository-chef" version="0.0.1">
   +     <details>org.sonatype.nexus.plugins:nexus-repository-chef</details>
   +     <bundle>mvn:org.sonatype.nexus.plugins/nexus-repository-chef/0.0.1</bundle>
   + </feature>
    </features>
   ```
This will cause the plugin to be loaded and started with each startup of Nexus Repository.

## The Fine Print

It is worth noting that this is **NOT SUPPORTED** by Sonatype, and is a contribution of ours
to the open source community (read: you!)

Remember:

* Use this contribution at the risk tolerance that you have
* Do NOT file Sonatype support tickets related to Chef support in regard to this plugin
* DO file issues here on GitHub, so that the community can pitch in

Phew, that was easier than I thought. Last but not least of all:

Have fun creating and using this plugin and the Nexus platform, we are glad to have you here!

## Getting help

Looking to contribute to our code but need some help? There's a few ways to get information:

* Chat with us on [Gitter](https://gitter.im/sonatype/nexus-developers)
* Check out the [Nexus3](http://stackoverflow.com/questions/tagged/nexus3) tag on Stack Overflow
* Check out the [Nexus Repository User List](https://groups.google.com/a/glists.sonatype.com/forum/?hl=en#!forum/nexus-users)