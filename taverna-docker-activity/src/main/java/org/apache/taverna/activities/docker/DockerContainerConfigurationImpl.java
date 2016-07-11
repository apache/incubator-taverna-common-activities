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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.command.CreateContainerCmdImpl;
import org.apache.taverna.configuration.AbstractConfigurable;
import org.apache.taverna.configuration.Configurable;
import org.apache.taverna.configuration.ConfigurationManager;

import java.io.IOException;
import java.util.*;

public class DockerContainerConfigurationImpl extends AbstractConfigurable implements DockerContainerConfiguration{

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


    private DockerRemoteConfig dockerRemoteConfig;


    public DockerContainerConfigurationImpl(ConfigurationManager configurationManager){
        super(configurationManager);
    }

    public DockerContainerConfigurationImpl(){
        super(null);
    }

    public DockerRemoteConfig getDockerRemoteConfig() {
        return dockerRemoteConfig;
    }

    public void setDockerRemoteConfig(DockerRemoteConfig dockerRemoteConfig) {
        this.dockerRemoteConfig = dockerRemoteConfig;
    }

    public String getName() {
       return this.getInternalPropertyMap().get(NAME);
    }

    public String getHostName() {
        return HOST_NAME;
    }

    public String getDomainName() {
        return DOMAIN_NAME;
    }

    public String getUser() {
        return USER;
    }

    public String getImage() {
        return this.getInternalPropertyMap().get(IMAGE);
    }

    public String getWorkingDir() {
        return WORKING_DIR;
    }

    public String getMacAddress() {
        return MAC_ADDRESS;
    }

    public String getStopSignal() {
        return STOP_SIGNAL;
    }

    public String getIpv4Address() {
        return IPV4_ADDRESS;
    }

    public String getIpv6Address() {
        return IPV6_ADDRESS;
    }

    public String getAttachStdin() {
        return ATTACH_STDIN;
    }

    public String getAttachStdout() {
        return ATTACH_STDOUT;
    }

    public String getAttachStderr() {
        return ATTACH_STDERR;
    }

    public String getTty() {
        return TTY;
    }

    public String getStdinOpen() {
        return STDIN_OPEN;
    }

    public String getStdinOnce() {
        return STDIN_ONCE;
    }

    public String getNetworkDisabled() {
        return NETWORK_DISABLED;
    }

    public String getPortSpecs() {
        return PORT_SPECS;
    }

    public String getEnv() {
        return ENV;
    }

    public String[] getCmd() {
        return this.getInternalPropertyMap().get(CMD).split(CMD_DELIMITER);
    }

    public String getEntryPoint() {
        return ENTRY_POINT;
    }

    public String getVolumes() {
        return VOLUMES;
    }

    public Ports.Binding[] getBindings() {
       String bindingsStr = this.getInternalPropertyMap().get(BINDINGS);
        List<Ports.Binding> bindingList = new ArrayList<Ports.Binding>();
        if(bindingsStr != null) {
            for(String bind : bindingsStr.split(",")){
             bindingList.add(Ports.Binding.bindPort(Integer.valueOf(bind)));
          }
        }
        return bindingList.toArray(new Ports.Binding[0]);
    }

    public String getAliases() {
        return ALIASES;
    }

    public ExposedPort[] getExposedPorts() {
       String exposedPortStr =  this.getInternalPropertyMap().get(EXPOSED_PORTS);
        List<ExposedPort> exposedPortList = new ArrayList<ExposedPort>();
        if(exposedPortStr != null){
            for(String portStr: exposedPortStr.split(",")){
                exposedPortList.add(ExposedPort.tcp(Integer.valueOf(portStr)));
            }
        }
        return exposedPortList.toArray(new ExposedPort[0]);
    }

    public String getLabels() {
        return LABELS;
    }

    public String getHostConfig() {
        return HOST_CONFIG;
    }

    public String getNetworkingConfig() {
        return NETWORKING_CONFIG;
    }

    @Override
    public Map<String, String> getDefaultPropertyMap() {
        Map<String,String> defaultMap = new HashMap<String,String>();
        return defaultMap;
    }

    @Override
    public String getUUID() {
        return "6BR3F5C1-DK8D-4893-8D9B-2F46FA1DDB87";
    }

    @Override
    public String getDisplayName() {
        return "Docker Config";
    }

    @Override
    public String getFilePrefix() {
        return "Docker";
    }

    @Override
    public String getCategory() {
        return null;
    }


}
