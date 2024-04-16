import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.Watch;
import com.google.gson.reflect.TypeToken;

public class DeploymentWatcher {
    public static void main(String[] args) {
        // Configuring the API client
        ApiClient client = Config.defaultClient();
        Configuration.setDefaultApiClient(client);

        // AppsV1Api instance to interact with Deployment resources
        AppsV1Api api = new AppsV1Api(client);

        // Namespace and Deployment name filters
        String namespace = "your-namespace"; // Specify the namespace here
        String deploymentName = "your-deployment-name"; // Specify the deployment name here

        try {
            // Set up a Watch to monitor events related to the Deployment
            Watch<V1Deployment> watch = Watch.createWatch(
                client,
                api.listNamespacedDeploymentCall(namespace, null, null, null, null, "metadata.name=" + deploymentName, null, null, null, true, null),
                new TypeToken<Watch.Response<V1Deployment>>(){}.getType());

            // Handling the received events
            for (Watch.Response<V1Deployment> item : watch) {
                System.out.printf("Event Type: %s%n", item.type);
                System.out.printf("Deployment Name: %s%n", item.object.getMetadata().getName());
                System.out.printf("Replicas: %s%n", item.object.getStatus().getReplicas());
                System.out.printf("Available Replicas: %s%n", item.object.getStatus().getAvailableReplicas());
                System.out.printf("Ready Replicas: %s%n", item.object.getStatus().getReadyReplicas());
            }
        } catch (ApiException e) {
            System.err.println("Exception occurred while watching deployments");
            e.printStackTrace();
        }
    }
}
