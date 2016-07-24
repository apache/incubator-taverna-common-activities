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

import com.github.dockerjava.api.model.*;
import org.apache.taverna.configuration.AbstractConfigurable;
import org.apache.taverna.configuration.ConfigurationManager;
import java.util.*;

public class DockerContainerConfigurationImpl extends AbstractConfigurable implements DockerContainerConfiguration{

    private DockerRemoteConfig dockerRemoteConfig;

    private static final String ARR_DELIMITER = ",";

    private static final String MAP_DELIMITER = ":";

    private ExposedPort[] exposedPorts = null;

    private Ports.Binding[] bindings = null;

    private Volume[] volumes = null;

    private Map<String, String> labels = null;


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
       return this.getInternalPropertyMap().get(NAME) != null? this.getInternalPropertyMap().get(NAME) : null;
    }

    public String getImage() {
        return this.getInternalPropertyMap().get(IMAGE) != null?this.getInternalPropertyMap().get(IMAGE):null;
    }

    public ExposedPort[] getExposedPorts() {
        if(exposedPorts == null) {
            String exposedPortStr = this.getInternalPropertyMap().get(EXPOSED_PORTS);
            if(exposedPortStr == null){
                return null;
            }
            List<ExposedPort> exposedPortList = new ArrayList<ExposedPort>();
            if (exposedPortStr != null) {
                for (String portStr : exposedPortStr.split(ARR_DELIMITER)) {
                    exposedPortList.add(ExposedPort.tcp(Integer.valueOf(portStr)));
                }
            }
           exposedPorts =  exposedPortList.toArray(new ExposedPort[0]);
        }

        return exposedPorts;
    }

    public Ports.Binding[] getBindings() {
        if(bindings == null) {
            String bindingsStr = this.getInternalPropertyMap().get(BINDINGS);
            if(bindingsStr == null){
                return null;
            }
            List<Ports.Binding> bindingList = new ArrayList<Ports.Binding>();
            if (bindingsStr != null) {
                for (String bind : bindingsStr.split(ARR_DELIMITER)) {
                    bindingList.add(Ports.Binding.bindPort(Integer.valueOf(bind)));
                }
            }
            bindings = bindingList.toArray(new Ports.Binding[0]);
        }
        return bindings;
    }

    public String getHostName() {
        return this.getInternalPropertyMap().get(HOST_NAME);
    }

    public String getDomainName() {
        return this.getInternalPropertyMap().get(DOMAIN_NAME);
    }

    public String getUser() {
        return this.getInternalPropertyMap().get(USER);
    }

    public String getWorkingDir() {
        return this.getInternalPropertyMap().get(WORKING_DIR);
    }

    public String getMacAddress() {
        return this.getInternalPropertyMap().get(MAC_ADDRESS);
    }

    public String getStopSignal() {
        return this.getInternalPropertyMap().get(STOP_SIGNAL);
    }

    public String getIpv4Address() {
        return this.getInternalPropertyMap().get(IPV4_ADDRESS);
    }

    public String getIpv6Address() {
        return this.getInternalPropertyMap().get(IPV6_ADDRESS);
    }

    public boolean getAttachStdin() {
        return Boolean.parseBoolean(this.getInternalPropertyMap().get(ATTACH_STDIN));
    }

    public boolean getAttachStdout() {
        return Boolean.parseBoolean(this.getInternalPropertyMap().get(ATTACH_STDOUT));
    }

    public boolean getAttachStderr() {
        return Boolean.parseBoolean(this.getInternalPropertyMap().get(ATTACH_STDERR));
    }

    public boolean getTty() {
        return Boolean.parseBoolean(this.getInternalPropertyMap().get(TTY));
    }

    public boolean getStdinOpen() {
        return Boolean.parseBoolean(this.getInternalPropertyMap().get(STDIN_OPEN));
    }

    public boolean getStdinOnce() {
        return Boolean.parseBoolean(this.getInternalPropertyMap().get(STDIN_ONCE));
    }

    public boolean getNetworkDisabled() {
        return Boolean.parseBoolean(this.getInternalPropertyMap().get(NETWORK_DISABLED));
    }

    public String[] getPortSpecs() {
        return this.getInternalPropertyMap().get(PORT_SPECS) != null ?
                this.getInternalPropertyMap().get(PORT_SPECS).split(ARR_DELIMITER): null;
    }

    public String[] getEnv() {
        return this.getInternalPropertyMap().get(ENV) != null ?
                this.getInternalPropertyMap().get(ENV).split(ARR_DELIMITER):null;
    }

    public String[] getEntryPoint() {
        return this.getInternalPropertyMap().get(ENTRY_POINT) != null ?
                this.getInternalPropertyMap().get(ENTRY_POINT).split(ARR_DELIMITER):null;
    }

    public String[] getCmd() {
        return this.getInternalPropertyMap().get(CMD) != null ?
                this.getInternalPropertyMap().get(CMD).split(ARR_DELIMITER): null;
    }

    public Volume[] getVolumes() {
        if(volumes == null) {
            if(this.getInternalPropertyMap().get(VOLUMES) == null){
                return null;
            }
            String[] arr = this.getInternalPropertyMap().get(VOLUMES).split(ARR_DELIMITER);
            volumes = new Volume[arr.length];
            for (int i = 0; i < arr.length; i++) {
                volumes[i] = new Volume(arr[i]);
            }
        }
        return volumes;
    }

    public String[] getAliases() {
        return this.getInternalPropertyMap().get(ALIASES) != null ?
                this.getInternalPropertyMap().get(ALIASES).split(ARR_DELIMITER): null;
    }

    public Map<String, String> getLabels() {
        if (labels == null) {
            labels = new HashMap<>();
            if(this.getInternalPropertyMap().get(LABELS) == null){
                return null;
            }
            String[] entries = this.getInternalPropertyMap().get(LABELS).split(MAP_DELIMITER);
            for (String entry : entries) {
             String[] kv = entry.split(ARR_DELIMITER);
                labels.put(kv[0], kv[1]);
            }
        }
        return labels;
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
