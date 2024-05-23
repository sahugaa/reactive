package com.test.test.agent.kube.strategy;

import io.kubernetes.client.openapi.models.*;

import java.util.Arrays;
import java.util.Collections;

public class PostgresDeploymentStrategy implements DeploymentStrategy {
    @Override
    public V1Deployment createDeployment(String namespace) throws ApiException {
        return new V1Deployment()
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
    }
}
