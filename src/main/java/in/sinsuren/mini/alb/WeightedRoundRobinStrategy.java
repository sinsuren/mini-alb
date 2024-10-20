package in.sinsuren.mini.alb;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

class WeightedRoundRobinStrategy implements LoadBalancingStrategy {
  private final AtomicInteger currentIndex = new AtomicInteger(0);
  private final List<Integer> weights;
  private int currentWeight = 0;

  public WeightedRoundRobinStrategy(List<Integer> weights) {
    this.weights = weights;
  }

  @Override
  public String selectServer(List<String> serverPool, Map<String, Integer> activeConnections) {
    int totalWeight = weights.stream().mapToInt(Integer::intValue).sum();
    while (true) {
      currentIndex.set((currentIndex.get() + 1) % serverPool.size());
      if (currentIndex.get() == 0) {
        currentWeight = (currentWeight + 1) % totalWeight;
      }
      if (weights.get(currentIndex.get()) >= currentWeight) {
        return serverPool.get(currentIndex.get());
      }
    }
  }
}
