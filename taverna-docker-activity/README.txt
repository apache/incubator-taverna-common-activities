########################## Docker Activity Plugin ############################

This module implements the taverna activity plugin for Docker. The plugin capable of following functionality.

1. Inspect Image
2. Create Container
3. Start Container
4. List Containers
5. Stop Container  - TODO
6. Delete Container - TODO

Test module src/test/java/ covers each and every capability. org.apache.taverna.activities.docker.RemoteClient
capable of executing docker commands over TCP on the actual remote docker host using the docker-java api.

1. TestDockerCommands covers unit tests for each Docker commands that is supported by RemoteClient api.
2. DockerActivityTest written on top of RemoteClient to support different docker commands.


 Prerequisites

1. Copy your key store files to src/test/java/resources/cert directory.
  Example: If you are in Mac Copy files in /Users/Jack/.docker/machine/certs/, where you finds following files
  ca-key.pem	ca.pem		cert.pem	key.pem

2. Change DockerActivityTest.DOCKER_REMOTE to your docker remote host:port

3. Change DockerActivityTest.DOCKER_REGISTRY to your docker registry (defaults to public docker registry)

Then do mvn clean install to build the project and run the tests.
##############################################################################