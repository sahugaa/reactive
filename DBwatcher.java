package com.kube.poc;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.Watch;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

public class DBVirtualization {

    public void createDB() throws IOException, ApiException {
        System.out.println("Started createDB-----");

        ApiClient client = ClientBuilder.standard().build();
        AppsV1Api appsV1Api = new AppsV1Api(client);
        CoreV1Api coreV1Api = new CoreV1Api(client);

        String namespace = "default";
        V1Deployment deployment = createDeployment(appsV1Api, namespace);
        System.out.println("Deployment created, watching for readiness...");

        watchDeployment(appsV1Api, coreV1Api, namespace, deployment.getMetadata().getName());
        System.out.println("Done -----");
    }

    private V1Deployment createDeployment(AppsV1Api api, String namespace) throws ApiException {
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

        return api.createNamespacedDeployment(namespace, body, "true", null, null);
    }

    private void watchDeployment(AppsV1Api api, CoreV1Api coreApi, String namespace, String deploymentName) throws ApiException {
        try (Watch<V1Pod> watch = Watch.createWatch(
                api.getApiClient(),
                api.listNamespacedPodCall(namespace, null, null, null, null, "app=postgres", null, null, null, true, null),
                new TypeToken<Watch.Response<V1Pod>>() {}.getType())) {
            for (Watch.Response<V1Pod> item : watch) {
                V1Pod pod = item.object;
                if (item.type.equals("ADDED") && pod.getStatus().getConditions().stream().anyMatch(cond -> "Ready".equals(cond.getType()) && Boolean.TRUE.equals(cond.getStatus()))) {
                    System.out.println("Pod is ready: " + pod.getMetadata().getName());
                    V1Service service = createService(coreApi, namespace);
                    saveDatabaseCredentialsToFile(service, namespace);
                    break;
                }
            }
        }
    }

    private V1Service createService(CoreV1Api api, String namespace) throws ApiException {
        V1Service body = new V1Service()
                .apiVersion("v1")
                .kind("Service")
                .metadata(new V1ObjectMeta().name("postgres-service"))
                .spec(new V1ServiceSpec()
                        .type("ClusterIP")
                        .ports(Collections.singletonList(new V1ServicePort().port(5432).targetPort(new IntOrString(5432))))
                        .selector(Collections.singletonMap("app", "postgres"))
                );

        return api.createNamespacedService(namespace, body, "true", null, null);
    }

    private void saveDatabaseCredentialsToFile(V1Service service, String namespace) throws IOException {
        String serviceDNS = service.getMetadata().getName() + "." + namespace + ".svc.cluster.local";
        Properties prop = new Properties();
        prop.setProperty("POSTGRES_URL", "jdbc:postgresql://" + serviceDNS + ":5432/mydatabase");
        prop.setProperty("POSTGRES_USER", "user");
        prop.setProperty("POSTGRES_PASSWORD", "password");

        try (OutputStream output = new FileOutputStream("db.properties")) {
            prop.store(output, "Database Properties");
            System.out.println("Database properties file has been created.");
        }
    }

    public static void main(String[] args) throws IOException, ApiException {
        new DBVirtualization().createDB();
    }
}
