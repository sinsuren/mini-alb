package in.sinsuren.mini.alb;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

class RoundRobinStrategy implements LoadBalancingStrategy {
  private final AtomicInteger roundRobinCounter = new AtomicInteger(0);

  @Override
  public String selectServer(List<String> serverPool, Map<String, Integer> activeConnections) {
    int serverIndex = roundRobinCounter.getAndIncrement() % serverPool.size();
    return serverPool.get(serverIndex);
  }
}
