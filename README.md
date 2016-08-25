Run in the following order:

Run Server1
Run Server2

Execute tests in the following order:

TestClient1_1_PopulateRegions
TestClient2_2_PopulateRegions

TestClient1_3_UpdateVersions
TestClient2_4_UpdateVersions


Spring Data Gemfire - Standalone Template
==============================================================================

This template demonstrates using Spring Data Gemfire in a standalone Java application with an embedded cache and a single region with a simple CacheListener.

You can run the application by either

* running the "Main" class from within STS (Right-click on Main class --> Run As --> Java Application)
* or from the command line:
    - mvn package
    - mvn exec:java

--------------------------------------------------------------------------------

For help please take a look at the Spring Data Gemfire documentation:

http://www.springsource.org/spring-gemfire

