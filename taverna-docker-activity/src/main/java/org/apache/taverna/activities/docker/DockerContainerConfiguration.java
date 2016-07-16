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
package org.apache.taverna.activities.docker;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;

import java.util.Map;

public interface DockerContainerConfiguration {

    /**
     * String Values
     */
    public static final String NAME = "name";

    public static final String HOST_NAME = "hostName";

    public static final String DOMAIN_NAME = "domainName";

    public static final String USER = "user";

    public static final String IMAGE = "image";

    public static final String WORKING_DIR = "workingDir";

    public static final String MAC_ADDRESS = "macAddress";

    public static final String STOP_SIGNAL = "stopSignal";

    public static final String IPV4_ADDRESS = "ipv4Address";

    public static final String IPV6_ADDRESS = "ipv6Address";

    /**
     * Boolean values
     */

    public static final String ATTACH_STDIN = "attachStdin";

    public static final String ATTACH_STDOUT = "attachStdout";

    public static final String ATTACH_STDERR =  "attachStderr";

    public static final String TTY = "tty";

    public static final String STDIN_OPEN = "stdinOpen";

    public static final String STDIN_ONCE = "stdInOnce";

    public static final String NETWORK_DISABLED = "networkDisabled";

    /**
     * String Arrays
     */
    public static final String PORT_SPECS = "portSpecs";

    public static final String ENV = "env";

    public static final String CMD = "cmd";

    public static final String CMD_DELIMITER = ",";

    public static final String ENTRY_POINT = "entrypoint";

    /**
     * Type Volumes[]
     */
    public static final String VOLUMES = "volumes";

    /**
     * Type Bindings[]
     */
    public static final String BINDINGS = "bindings";

    /**
     * List<String> aliases
     */
    public static final String ALIASES = "aliases";

    /**
     * Type ExposedPorts[]
     */
    public static final String EXPOSED_PORTS = "exposedPorts";

    /**
     * Type Map<String,String>
     */
    public static final String LABELS = "labels";

    /**
     * Type HostConfig
     */
    public static final String HOST_CONFIG = "hostConfig";

    /**
     * Type CreateContainerCmdImpl.NetworkingConfig
     */
    public static final String NETWORKING_CONFIG = "networkingConfig";


    public void setDockerRemoteConfig(DockerRemoteConfig dockerRemoteConfig);

    public Map<String, String> getInternalPropertyMap();

    public String getName();

    public String getImage();

    public String[] getCmd();

    public DockerRemoteConfig getDockerRemoteConfig();

    public ExposedPort[] getExposedPorts();

    public Ports.Binding[] getBindings();

    //TODO add all remaining getters

}
