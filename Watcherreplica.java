import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.Watch;
import com.google.gson.reflect.TypeToken;

public class DeploymentReadyWatcher {
    public static void main(String[] args) {
        // Initialize the API client
        ApiClient client = Config.defaultClient();
        Configuration.setDefaultApiClient(client);
        AppsV1Api api = new AppsV1Api();

        // Specify the namespace and the deployment name
        String namespace = "default";  // Change this to the desired namespace
        String deploymentName = "example-deployment";  // Change this to the target deployment name

        try {
            Watch<V1Deployment> watch = Watch.createWatch(
                client,
                api.listNamespacedDeploymentCall(namespace, null, null, null, null, "metadata.name=" + deploymentName, null, null, null, true, null),
                new TypeToken<Watch.Response<V1Deployment>>(){}.getType());

            // Iterate over the watch responses
            for (Watch.Response<V1Deployment> item : watch) {
                V1Deployment deployment = item.object;
                if (deployment.getStatus() != null &&
                    deployment.getStatus().getReplicas() != null &&
                    deployment.getStatus().getReadyReplicas() != null &&
                    deployment.getStatus().getReplicas().equals(deployment.getStatus().getReadyReplicas())) {
                    
                    System.out.println("Deployment " + deploymentName + " is ready with " + deployment.getStatus().getReadyReplicas() + " replicas.");
                    // Execute any specific code here when the deployment is ready
                    break; // Exit the watch after the condition is met
                } else {
                    System.out.println("Waiting for deployment " + deploymentName + " to be ready...");
                }
            }
        } catch (ApiException e) {
            System.err.println("Exception occurred while watching the deployment");
            e.printStackTrace();
        }
    }
}
