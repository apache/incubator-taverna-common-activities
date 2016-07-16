/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.taverna.activities.docker.test;

import com.github.dockerjava.api.command.InspectImageResponse;
import com.github.dockerjava.api.model.Container;
import org.apache.taverna.activities.docker.*;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

public class TestDockerCommands {

    private RemoteClient remoteClient;

    private static final String IMAGE_NAME = "training/webapp";

    private static final String CONTAINER_NAME = "test-container-0";

    private static final String DOCKER_LOGIN_SUCCESS = "Login Succeeded";

    @Before
    public void setup(){
        Assume.assumeTrue(new File(DockerActivityTest.CERT_PATH).list().length > 0);

        DockerContainerConfiguration containerConfiguration = new DockerContainerConfigurationImpl(new TestConfigurationManager());
        containerConfiguration.getInternalPropertyMap().put(DockerContainerConfiguration.NAME,CONTAINER_NAME);
        containerConfiguration.getInternalPropertyMap().put(DockerContainerConfiguration.IMAGE,IMAGE_NAME);
        containerConfiguration.getInternalPropertyMap().put(DockerContainerConfiguration.CMD,"env");

        DockerRemoteConfig remoteConfig = new DockerRemoteConfig();
        remoteConfig.setDockerHost(DockerActivityTest.DOCKER_REMOTE);
        remoteConfig.setApiVersion("1.21");
        remoteConfig.setDockerTlsVerify(true);

        // You need to copy your valid certificate file to resources directory in this test module as follows.
        remoteConfig.setDockerCertPath(new File(DockerActivityTest.CERT_PATH).getAbsolutePath());
        remoteConfig.setRegistryUrl(DockerActivityTest.DOCKER_REGISTRY);
        containerConfiguration.setDockerRemoteConfig(remoteConfig);
        remoteClient = new RemoteClient(containerConfiguration);
    }

    @Test
    public void testLogin(){
        String status = remoteClient.login();
        Assert.assertEquals(DOCKER_LOGIN_SUCCESS, status);
    }

    /**
     * Corresponding docker command > docker images --no-trunc | head
     */
    @Test
    public void testInspectImage(){
        InspectImageResponse response = remoteClient.inspect(IMAGE_NAME);
        System.out.println(response.getId());
        Assert.assertNotNull(response.getId());
    }


    @Test
    public void testListContainers(){
        List<Container> list =  remoteClient.listContainers();
        for(Container container: list){
            System.out.println(container.toString());
        }
    }

}
