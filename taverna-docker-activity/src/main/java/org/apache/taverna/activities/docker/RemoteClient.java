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


import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectImageResponse;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import org.apache.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class RemoteClient {

    /**
     * Docker client
     */
    private DockerClient dockerClient;

    private DockerContainerConfiguration containerConfig;

    private DockerRemoteConfig remoteConfig;

    private static Logger LOG = Logger.getLogger(RemoteClient.class);

    public RemoteClient(DockerContainerConfiguration containerConfig) {
        this.containerConfig = containerConfig;
        init(containerConfig.getDockerRemoteConfig());
    }

    private void init(DockerRemoteConfig remoteConfig) {
        this.remoteConfig = remoteConfig;
        DockerClientConfig config = config();
        dockerClient = DockerClientBuilder.getInstance(config).build();
    }

    /**
     * Login to the Docker
     * @return Status of the login response
     */
    public String login(){
        return dockerClient.authCmd().exec().getStatus();
    }

    /**
     *
     * @param name Image Name/Id
     * @return Complete docker response
     */
    public InspectImageResponse inspect(String name){
        return dockerClient.inspectImageCmd(name).exec();
    }

    /**
     * This creates a container based on the inout params. Docker command "docker ps -a" will show you all created containers.
     * @return complete docker response
     */
    public CreateContainerResponse createContainer(){
        CreateContainerResponse response = buildCreateContainerCmd().exec();
        return response;
    }

    /**
     * @return List all containers
     */
    public List<Container> listContainers(){
        return dockerClient.listContainersCmd().withShowAll(true).exec();
    }

    /**
     * @return Docker Info response from docker
     */
    public Info info(){
        return dockerClient.infoCmd().exec();
    }

    /**
     * @param containerId To be start
     */
    public Void startContainer( String containerId){
       return dockerClient.startContainerCmd(containerId).exec();
    }

    /**
     * @param containerId to be stopped
     */
    public void stopContainer(String containerId){
        dockerClient.stopContainerCmd(containerId).exec();
    }

    /**
     * @param term Search term for images (ex: image name)
     * @return List of Images
     */
    public  List<SearchItem>  searchImages(String term){
      return dockerClient.searchImagesCmd(term).exec();
    }


    public void deleteContainer(String id){
        dockerClient.removeContainerCmd(id).exec();
    }

    private CreateContainerCmd buildCreateContainerCmd(){
        CreateContainerCmd createCmd = dockerClient.createContainerCmd(containerConfig.getImage());

        if(containerConfig.getCmd() != null) {
            createCmd.withCmd(containerConfig.getCmd());
        }

        if(containerConfig.getName() != null) {
            createCmd.withName(containerConfig.getName());
        }

        if(containerConfig.getBindings() != null && containerConfig.getExposedPorts() != null) {
            Ports portBindings = new Ports();
            for (int i = 0; i < containerConfig.getBindings().length; i++) {
                portBindings.bind(containerConfig.getExposedPorts()[i], containerConfig.getBindings()[i]);
            }
            createCmd.withExposedPorts(containerConfig.getExposedPorts());
            createCmd.withPortBindings(portBindings);
        }

        if(containerConfig.getHostName() != null) {
            createCmd.withHostName(containerConfig.getHostName());
        }

        if(containerConfig.getDomainName() != null) {
            createCmd.withDomainName(containerConfig.getDomainName());
        }

        if(containerConfig.getUser() != null) {
            createCmd.withUser(containerConfig.getUser());
        }

        if(containerConfig.getWorkingDir() != null) {
            createCmd.withWorkingDir(containerConfig.getWorkingDir());
        }

        if(containerConfig.getMacAddress() != null) {
            createCmd.withMacAddress(containerConfig.getMacAddress());
        }

        if(containerConfig.getStopSignal() != null) {
            createCmd.withStopSignal(containerConfig.getStopSignal());
        }

        if(containerConfig.getIpv4Address() != null) {
            createCmd.withIpv4Address(containerConfig.getIpv4Address());
        }

        if(containerConfig.getIpv6Address() != null) {
            createCmd.withIpv6Address(containerConfig.getIpv6Address());
        }

        createCmd.withAttachStdin(containerConfig.getAttachStdin());
        createCmd.withAttachStdout(containerConfig.getAttachStdout());
        createCmd.withAttachStderr(containerConfig.getAttachStderr());
        createCmd.withTty(containerConfig.getTty());
        createCmd.withStdInOnce(containerConfig.getStdinOnce());
        createCmd.withStdinOpen(containerConfig.getStdinOpen());
        createCmd.withNetworkDisabled(containerConfig.getNetworkDisabled());


        if(containerConfig.getPortSpecs() != null) {
            createCmd.withPortSpecs(containerConfig.getPortSpecs());
        }

        if(containerConfig.getEnv() != null) {
            createCmd.withEnv(containerConfig.getEnv());
        }

        if(containerConfig.getEntryPoint() != null) {
            createCmd.withEntrypoint(containerConfig.getEntryPoint());
        }

        if(containerConfig.getVolumes() != null) {
            createCmd.withVolumes(containerConfig.getVolumes());
        }

        if(containerConfig.getAliases() != null) {
            createCmd.withAliases(containerConfig.getAliases());
        }

        if(containerConfig.getLabels() != null) {
            createCmd.withLabels(containerConfig.getLabels());
        }
        
        return createCmd;
    }

    private DockerClientConfig config() {
        DockerClientConfig.DockerClientConfigBuilder builder = DockerClientConfig.createDefaultConfigBuilder();
        builder.withDockerHost(remoteConfig.getDockerHost());
        builder.withDockerTlsVerify(remoteConfig.isDockerTlsVerify());
        builder.withApiVersion(remoteConfig.getApiVersion());

        if(remoteConfig.getDockerCertPath() != null){
            builder.withDockerCertPath(remoteConfig.getDockerCertPath());
        }

        if(remoteConfig.getRegistryUrl() != null){
            builder.withRegistryUrl(remoteConfig.getRegistryUrl());
        }

        if(remoteConfig.getRegistryUsername() != null){
            builder.withRegistryUsername(remoteConfig.getRegistryUsername());
        }

        if(remoteConfig.getRegistryPassword() != null){
            builder.withRegistryPassword(remoteConfig.getRegistryPassword());
        }

        if(remoteConfig.getRegistryEmail() != null){
            builder.withRegistryEmail(remoteConfig.getRegistryEmail());
        }

        return builder.build();
    }

    }
