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
import org.apache.taverna.activities.docker.DockerConfig;
import org.apache.taverna.activities.docker.RESTUtil;
import org.junit.Test;

import java.io.IOException;

public class TestCreateContainer{

//    @Test
//    public void testCreateContainer(){
//        try {
//            String payload = "{\"Hostname\":\"foo.com\", \"User\":\"foo\", \"Memory\":0, \"MemorySwap\":0,\"AttachStdin\":false, \"AttachStdout\":true,\"Attachstderr\":true,\"PortSpecs\":null,\"Tty\":false,\"OpenStdin\":false,\"StdinOnce\":false,\"Env\":null, \"Cmd\":[\"date\"], \"Image\":\"ubuntu\",\"Tag\":\"latest\",\"Volumes\":{\"/tmp\":{} },\"WorkingDir\":\"\",\"DisableNetwork\":false, \"ExposedPorts\":{\"22/tcp\": {} }}";
//            DockerConfig config = new DockerConfig("192.168.99.100",2376, new ObjectMapper().readTree(payload));
//            boolean res = RESTUtil.createContainer(config);
//            System.out.println(">>>" + res);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }
}
