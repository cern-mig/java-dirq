

Generate documentation and upload it to GitHub Pages
====================================================

    # generate apidocs in target/site/apidocs/
    mvn javadoc:javadoc

The documentation for this package is served by GitHub Pages.

GitHub Maven plugin is used in order to automate the process of uploading
the documentation to the gh-pages branch of the repository.

For this reason, in order to proceed with the upload of the documentation
you should make sure that you have the proper configuration for the
GitHub Maven plugin (in the settings.xml).

You find the recipe in the plugin page:
https://github.com/github/maven-plugins

    # upload it to gh-pages branch in github
    mvn site


Build a snapshot
================

Sonatype Nexus is used for the package release, if you want to build
a snapshot first have a read at their guide:

http://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide

Building a snapshot is as easy as running a single command when you have
proper configuration.

    mvn clean deploy


Perform a release
=================

In order to release the package and get it synchronized with
central Maven repo follow this guide:

http://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide

If you have the proper configuration for Sonatype and it is not your first
release then the release process can be summarized with the following steps:

    # make sure to clear any pending commit/push
    # eventually you want to build a snapshot and test it
    mvn clean deploy
    # then proceed with the release steps
    mvn release:clean
    mvn release:prepare
    mvn release:perform

At this point follow point 8 of the Sonatype guide in order to confirm
the release and get it synchronized with central Maven repository.

