java-dirq
=========

[![Build Status](https://secure.travis-ci.org/cern-mig/java-dirq.png)](http://travis-ci.org/cern-mig/java-dirq)

Overview
--------

Directory based queue.

The goal of this module is to offer a simple queue system using the
underlying filesystem for storage, security and to prevent race
conditions via atomic operations. It focuses on simplicity, robustness
and scalability.

This module allows multiple concurrent readers and writers to interact
with the same queue.

A port of Perl module
[Directory::Queue](http://search.cpan.org/dist/Directory-Queue/)
and a Python [dirq](https://github.com/cern-mig/python-dirq) implementation of
the same algorithm are available so readers and writers
can be written in different programming languages.

Install
-------

To install this module, run the following commands:

    mvn install
    # or
    mvn package
    # get the jar from target folder

Or simply add it to your `pom.xml` file:

    <dependency>
      <groupId>ch.cern.dirq</groupId>
      <artifactId>dirq</artifactId>
      <version>X.X</version>
    </dependency>


Documentation
-------------

After installing, you can find documentation at this page:

[http://cern-mig.github.com/java-dirq/](http://cern-mig.github.com/java-dirq/)


Developers
----------

Look at `dev-guide.md` file.


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

Copyright (C) 2012-2013 CERN

