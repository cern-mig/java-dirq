TODO list
=========

- once there will be groupId ch.cern.mig this package should be moved in there
- travis matrix tests with different versions of jna
  http://about.travis-ci.org/docs/user/build-configuration/#The-Build-Matrix
- check if java 7 nio 2 has all posix call needed by dirq which would
  allow to replace jna with it
  http://docs.oracle.com/javase/tutorial/essential/io/fileio.html
  http://docs.oracle.com/javase/7/docs/api/java/nio/file/Files.html
- look at using nexus staging maven plugin to avoid manual Nexus steps:
  https://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide#SonatypeOSSMavenRepositoryUsageGuide-8b.AutomatingReleases
