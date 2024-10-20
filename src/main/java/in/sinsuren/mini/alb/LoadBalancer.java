package in.sinsuren.mini.alb;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;

public class LoadBalancer {

  private final List<String> serverPool;
  private final Map<String, Integer> activeConnections;
  private LoadBalancingStrategy strategy;
  private final int healthCheckFrequency = 5000;

  private final ExecutorService requestExecutor = Executors.newFixedThreadPool(10);

  public LoadBalancer(List<String> serverPool, LoadBalancingStrategy strategy) {
    this.serverPool = new CopyOnWriteArrayList<>(serverPool);
    this.activeConnections = new ConcurrentHashMap<>();
    this.strategy = strategy;

    // Initialize active connections for all servers
    for (String server : serverPool) {
      activeConnections.put(server, 0);
    }

    // Start health check
    startHealthCheck();
  }

  public void setStrategy(LoadBalancingStrategy strategy) {
    this.strategy = strategy;
  }

  public String getServer() {
    return strategy.selectServer(serverPool, activeConnections);
  }

  private void startHealthCheck() {
    Executors.newSingleThreadScheduledExecutor()
        .scheduleAtFixedRate(
            () -> {
              for (String server : new ArrayList<>(serverPool)) {
                if (!isServerHealthy(server)) {
                  System.out.println("Removing unhealthy server: " + server);
                  serverPool.remove(server);
                } else if (!serverPool.contains(server)) {
                  System.out.println("Adding healthy server back: " + server);
                  serverPool.add(server);
                }
              }
            },
            0,
            healthCheckFrequency,
            TimeUnit.MILLISECONDS);
  }

  private boolean isServerHealthy(String server) {
    try {
      URL url = new URL(server);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");
      connection.setConnectTimeout(1000);
      connection.setReadTimeout(1000);
      int responseCode = connection.getResponseCode();
      return responseCode == 200;
    } catch (IOException e) {
      return false;
    }
  }

  public void handleRequest(String clientRequest) {
    requestExecutor.submit(
        () -> {
          String server = getServer();
          sendRequestToServer(server, clientRequest);
        });
  }

  private void sendRequestToServer(String server, String request) {
    try {
      System.out.println("Forwarding request [" + request + "] to server: " + server);
      activeConnections.put(server, activeConnections.get(server) + 1);

      // Simulate server processing delay
      Thread.sleep(100);

      System.out.println("Request [" + request + "] processed by server: " + server);

      activeConnections.put(server, activeConnections.get(server) - 1);
    } catch (InterruptedException e) {
      System.out.println("Request handling interrupted");
    }
  }

  public void shutdown() {
    requestExecutor.shutdown();
    try {
      if (!requestExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
        requestExecutor.shutdownNow();
      }
    } catch (InterruptedException e) {
      requestExecutor.shutdownNow();
    }
  }

  public static void main(String[] args) throws InterruptedException {
    // Simulated list of backend servers
    List<String> servers =
        Arrays.asList("http://localhost:8080", "http://localhost:8081", "http://localhost:8082");

    // Select a load balancing strategy
    LoadBalancingStrategy roundRobinStrategy = new RoundRobinStrategy();
    LoadBalancingStrategy leastConnectionsStrategy = new LeastConnectionStrategy();
    LoadBalancingStrategy randomStrategy = new RandomStrategy();

    // Optional: Weighted round robin example with server weights
    List<Integer> serverWeights = Arrays.asList(3, 2, 5); // Higher weight, more requests
    LoadBalancingStrategy weightedRoundRobinStrategy =
        new WeightedRoundRobinStrategy(serverWeights);

    // Create Load Balancer with Round Robin strategy
    LoadBalancer loadBalancer = new LoadBalancer(servers, roundRobinStrategy);

    // Switch strategies dynamically
    loadBalancer.setStrategy(leastConnectionsStrategy);

    // Simulate incoming client requests
    for (int i = 1; i <= 10; i++) {
      String clientRequest = "Request " + i;
      loadBalancer.handleRequest(clientRequest);
      Thread.sleep(300); // Simulate client request interval
    }

    // Switch strategy to Random
    loadBalancer.setStrategy(randomStrategy);
    for (int i = 11; i <= 20; i++) {
      String clientRequest = "Request " + i;
      loadBalancer.handleRequest(clientRequest);
      Thread.sleep(300); // Simulate client request interval
    }

    // Shutdown the load balancer after handling all requests
    loadBalancer.shutdown();
  }
}
