java-dirq
=========

[![Build Status](https://secure.travis-ci.org/mpaladin/java-dirq.png)](http://travis-ci.org/mpaladin/java-dirq)

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
[Directory::Queue](http://search.cpan.org/~lcons/Directory-Queue/)
and a Python [dirq](http://pypi.python.org/pypi/dirq/) implementation of
the same algorithm are available so readers and writers
can be written in different programming languages.

Install
-------

To install this module, run the following commands:

    mvn install
    # or
    mvn package
    # get the jar from target folder

Documentation
-------------

After installing, you can find documentation at this page:

[https://mpaladin.web.cern.ch/mpaladin/java/dirq/](https://mpaladin.web.cern.ch/mpaladin/java/dirq/)

License and Copyright
---------------------

Apache License, Version 2.0

Copyright (C) 2010-2012 CERN

