package in.sinsuren.mini.alb;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;

class RandomStrategy implements LoadBalancingStrategy {
  private final Random random = new Random();

  @Override
  public String selectServer(List<String> serverPool, Map<String, Integer> activeConnections) {
    int serverIndex = random.nextInt(serverPool.size());
    return serverPool.get(serverIndex);
  }
}
