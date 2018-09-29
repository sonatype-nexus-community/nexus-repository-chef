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
## Chef Repositories

### Introduction

[Chef](https://www.chef.io/) is a application management format used to help automate and run an organizations 
infrastructure, using Cookbooks, which are effectively recipes for running an application. 

### Proxying The Chef Supermarket

You can set up an Chef proxy repository to access a remote repository location, for example to proxy the stable Cookbooks 
at [the Chef Supermarket](https://supermarket.chef.io/)

To proxy a Chef repository, you simply create a new 'chef (proxy)' as documented in 
[Repository Management](https://help.sonatype.com/display/NXRM3/Configuration#Configuration-RepositoryManagement) in
details. Minimal configuration steps are:

- Define 'Name'
- Define URL for 'Remote storage' e.g. [https://supermarket.chef.io/](https://supermarket.chef.io/)
- Select a 'Blob store' for 'Storage'

### Configuring Chef and Knife 

Configuring Chef to use Nexus Repository is fairly easy! 

You'll need to have Chef installed, and as well install Knife, for interaction with the Supermarket

TODO: Fill in how to install Chef and Knife

Once you have Chef up and running you'll want to run commands similar to the following:

TODO: Fill in how to configure Knife and Chef for use against Nexus Repository

### Browsing Chef Repository Packages

You can browse Chef repositories in the user interface inspecting the components and assets and their details, as
described in [Browsing Repositories and Repository Groups](https://help.sonatype.com/display/NXRM3/Browsing+Repositories+and+Repository+Groups).
