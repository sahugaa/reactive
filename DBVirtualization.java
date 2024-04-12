package com.kube.poc;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.ClientBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

public class DBVirtualization {



    public  void createDB() throws IOException {

        try {
            System.out.println("Started  createDB-----");

            ApiClient client = ClientBuilder.standard().build();
            AppsV1Api appsV1Api = new AppsV1Api(client);
            CoreV1Api coreV1Api = new CoreV1Api(client);

            String namespace = "default";
            createDeployment(appsV1Api, namespace);
            System.out.println("Done -----");

        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void createDeployment(AppsV1Api api, String namespace) {

        V1Deployment body = new V1Deployment()
                .apiVersion("apps/v1")
                .kind("Deployment")
                .metadata(new V1ObjectMeta().name("postgres-deployment"))
                .spec(new V1DeploymentSpec()
                        .selector(new V1LabelSelector().matchLabels(Collections.singletonMap("app", "postgres")))
                        .replicas(1)
                        .template(new V1PodTemplateSpec()
                                .metadata(new V1ObjectMeta().labels(Collections.singletonMap("app", "postgres")))
                                .spec(new V1PodSpec()
                                        .containers(Collections.singletonList(
                                                new V1Container()
                                                        .name("postgres")
                                                        .image("postgres:13")
                                                        .env(Arrays.asList(
                                                                new V1EnvVar().name("POSTGRES_DB").value("mydatabase"),
                                                                new V1EnvVar().name("POSTGRES_USER").value("user"),
                                                                new V1EnvVar().name("POSTGRES_PASSWORD").value("password")
                                                        ))
                                                        .ports(Collections.singletonList(new V1ContainerPort().containerPort(5432)))
                                                        .volumeMounts(Collections.singletonList(
                                                                new V1VolumeMount()
                                                                        .name("postgres-storage")
                                                                        .mountPath("/var/lib/postgresql/data")
                                                        ))
                                        ))
                                        .volumes(Collections.singletonList(
                                                new V1Volume()
                                                        .name("postgres-storage")
                                                        .persistentVolumeClaim(new V1PersistentVolumeClaimVolumeSource().claimName("postgres-pvc"))
                                        ))
                                )
                        )
                );


        try {
            V1Deployment deployment = api.createNamespacedDeployment(namespace, body, null, null, null);
            System.out.println("Created pod: " + deployment.getMetadata().getName());
        } catch (ApiException e) {
            System.err.println("Exception when calling Kubernetes API");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }


    }
}

