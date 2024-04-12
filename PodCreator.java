package com.kube.poc;


import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.ClientBuilder;

import java.io.IOException;
import java.util.Arrays;

public class PodCreator {


    public void build()
    {

        try {
            PodCreator creator = new PodCreator();
            creator.deployNewPod();
        }
        catch (Exception ex)
        {

            ex.printStackTrace();
        }

    }


    public void deployNewPod() {
        try {
            ApiClient client = createKubernetesClient();
            createPod(client, "default", "hello-spring-boot-pod", "gsahudockerhub/hello-spring-boot:latest");
        } catch ( IOException e) {
            System.err.println("Exception when calling Kubernetes API");
            e.printStackTrace();
        }
    }

    public ApiClient createKubernetesClient() throws IOException {
        // Configuring the client to use kubeconfig from local file system
        ApiClient client = ClientBuilder.standard().build();

        // Alternatively, configure the client to use the in-cluster config like this:
        // ApiClient client = ClientBuilder.cluster().build();

        Configuration.setDefaultApiClient(client);
        return client;
    }

    public void createPod1(ApiClient client, String namespace, String podName, String imageName) throws ApiException {
        CoreV1Api api = new CoreV1Api(client);

        V1Pod pod = new V1Pod();
        pod.setApiVersion("v1");
        pod.setKind("Pod");

        V1ObjectMeta metadata = new V1ObjectMeta();
        metadata.setName(podName);
        pod.setMetadata(metadata);

        V1ContainerPort containerPort = new V1ContainerPort();
        containerPort.setContainerPort(8080);

        V1Container container = new V1Container();
        container.setName("example-container");
        container.setImage(imageName);
        container.setPorts(java.util.Arrays.asList(containerPort));

        V1PodSpec podSpec = new V1PodSpec();
        podSpec.setContainers(java.util.Arrays.asList(container));
        pod.setSpec(podSpec);

        pod = api.createNamespacedPod(namespace, pod, null, null, null);
        System.out.println("Created pod: " + pod.getMetadata().getName());
    }

    public void createPod(ApiClient client, String namespace, String podName, String imageName) {
        CoreV1Api api = new CoreV1Api(client);
        V1Pod pod = new V1Pod()
                .apiVersion("v1")
                .kind("Pod")
                .metadata(new V1ObjectMeta().name(podName))
                .spec(new V1PodSpec().containers(Arrays.asList(
                        new V1Container()
                                .name("example-container")
                                .image(imageName)
                                .ports(Arrays.asList(new V1ContainerPort().containerPort(8080)))
                )));

        try {
            pod = api.createNamespacedPod(namespace, pod, null, null, null);
            System.out.println("Created pod: " + pod.getMetadata().getName());
        } catch (ApiException e) {
            System.err.println("Exception when calling Kubernetes API");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}

