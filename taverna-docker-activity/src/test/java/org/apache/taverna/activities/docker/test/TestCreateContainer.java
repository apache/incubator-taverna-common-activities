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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.taverna.activities.docker.DockerContainerConfigurationImpl;
import org.apache.taverna.activities.docker.DockerHttpResponse;
import org.apache.taverna.activities.docker.RESTUtil;
import org.junit.Test;

import java.io.IOException;

public class TestCreateContainer{


//    @Test
//    public void testCreateContainer(){
//        try {
//             String payload = "{\"Image\":\"6fae60ef3446\", \"ExposedPorts\":{\"8080/tcp\":{}}}";
//             DockerContainerConfigurationImpl config = new DockerContainerConfigurationImpl("192.168.99.100",2376,"https",new ObjectMapper().readTree(payload));
//            DockerHttpResponse res = RESTUtil.createContainer(config);
//            System.out.println(">>>" + res.toString());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
