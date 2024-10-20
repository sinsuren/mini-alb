package in.sinsuren.mini.alb;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

class LeastConnectionStrategy implements LoadBalancingStrategy {

  @Override
  public String selectServer(List<String> serverPool, Map<String, Integer> activeConnections) {
    return serverPool.stream()
        .min(Comparator.comparingInt(activeConnections::get))
        .orElseThrow(() -> new IllegalStateException("No available servers"));
  }
}
