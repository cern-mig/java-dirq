java-dirq
=========

![Build Status](https://github.com/cern-mig/java-dirq/actions/workflows/test.yml/badge.svg)


Overview
--------

The goal of this module is to offer a queue system using the underlying
filesystem for storage, security and to prevent race conditions via atomic
operations. It focuses on simplicity, robustness and scalability.

This module allows multiple concurrent readers and writers to interact with
the same queue. A Perl implementation
([Directory::Queue](http://search.cpan.org/dist/Directory-Queue/))
and a Python implementation
([dirq](https://github.com/cern-mig/python-dirq))
of the same algorithm are available so readers and writers can be
written in different programming languages.


Install
-------

To install this module, run the following commands:
```bash
    mvn install
    # or
    mvn package
    # get the jar from target folder
```

This module is available in the
[Central Maven Repository](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22dirq%22)
so you can simply add to your `pom.xml` file something like:
```xml
    <dependency>
      <groupId>ch.cern.dirq</groupId>
      <artifactId>dirq</artifactId>
      <version>1.8</version>
    </dependency>
```

Documentation
-------------

See:
[http://cern-mig.github.io/java-dirq/](http://cern-mig.github.io/java-dirq/)


Acknowledgments
---------------

YourKit is kindly supporting open source projects with its full-featured Java Profiler.
YourKit, LLC is the creator of innovative and intelligent tools for profiling
Java and .NET applications. Take a look at YourKit's leading software products:
[YourKit Java Profiler](http://www.yourkit.com/java/profiler/index.jsp) and
[YourKit .NET Profiler](http://www.yourkit.com/.net/profiler/index.jsp).


License and Copyright
---------------------

Apache License, Version 2.0

Copyright (C) CERN 2012-2024
