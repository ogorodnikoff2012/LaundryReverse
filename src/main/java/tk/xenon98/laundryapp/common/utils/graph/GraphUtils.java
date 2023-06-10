
package tk.xenon98.laundryapp.common.utils.graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class GraphUtils {

    private GraphUtils() {
    }

    public static <V> List<V> findPath(final IGraph<V> graph, final V source, final V target) {
        final var distances = new HashMap<V, Integer>();
        final var ancestors = new HashMap<V, V>();
        final var queue = new ArrayDeque<V>();

        ancestors.put(source, null);
        distances.put(source, 0);
        queue.add(source);

        while (!queue.isEmpty()) {
            final V current = queue.remove();
            if (Objects.equals(current, target)) {
                return restorePath(ancestors, target);
            }

            final int currentDistance = distances.get(current);

            for (final var edge : graph.outgoingEdges(current)) {
                if (!distances.containsKey(edge.to())) {
                    distances.put(edge.to(), currentDistance + 1);
                    ancestors.put(edge.to(), current);
                    queue.add(edge.to());
                }
            }
        }

        throw new IllegalStateException("Path " + source + " -> " + target + " does not exist");
    }

    private static <V> List<V> restorePath(final HashMap<V, V> ancestors, final V target) {
        final var result = new ArrayList<V>();

        V current = target;
        while (current != null) {
            result.add(current);
            current = ancestors.get(current);
        }

        Collections.reverse(result);
        return result;
    }
}
