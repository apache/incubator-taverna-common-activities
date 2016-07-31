package org.apache.taverna.activities.docker;

import com.github.dockerjava.api.model.Container;

import java.util.List;

public class ValidationUtil {

    /**
     * Validates create container eligibility
     * @param configuration
     * @param containerName
     * @return
     */
    public static boolean validateCreateContainer(DockerContainerConfiguration configuration, String containerName){
        RemoteClient remoteClient = new RemoteClient(configuration);
        Container container =  getContainerFromName(remoteClient,containerName);
        return container == null;
    }

    /**
     * Validates start container eligibility
     * @param configuration
     * @param containerName
     * @return
     */
    public static boolean validateStartContainer(DockerContainerConfiguration configuration, String containerName){
        RemoteClient remoteClient = new RemoteClient(configuration);
        Container container =  getContainerFromName(remoteClient,containerName);
        return !isStarted(container);
    }

    /**
     * Validates stop container eligibility
     * @param configuration
     * @param containerName
     * @return
     */
    public static boolean validateStopContainer(DockerContainerConfiguration configuration, String containerName){
        RemoteClient remoteClient = new RemoteClient(configuration);
        Container container =  getContainerFromName(remoteClient,containerName);
        return isStarted(container);
    }

    /**
     * Validates create container eligibility
     * @param configuration
     * @param containerName
     * @return
     */
    public static boolean validateDeleteContainer(DockerContainerConfiguration configuration, String containerName){
        RemoteClient remoteClient = new RemoteClient(configuration);
        Container container =  getContainerFromName(remoteClient,containerName);
        return container != null;
    }

    private static Container getContainerFromName(RemoteClient remoteClient, String containerName){
        List<Container> containerList = remoteClient.listContainers();
        for(Container container : containerList){
            if(container.getNames().length > 0){
                for(String name : container.getNames()){
                    if(name.endsWith(containerName)){
                        return container;
                    }
                }
            }
        }
        return null;
    }


    private static boolean isStarted(Container container){
        return  container.getStatus() != null
                && container.getStatus().startsWith("Up");
    }

}
