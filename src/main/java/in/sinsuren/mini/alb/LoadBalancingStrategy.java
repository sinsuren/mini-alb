package in.sinsuren.mini.alb;

import java.util.List;
import java.util.Map;

interface LoadBalancingStrategy {
    String selectServer(List<String> serverPool, Map<String, Integer> activeConnections);
}
