
package tk.xenon98.laundryapp.common.utils.graph;

import java.util.Collection;
import java.util.stream.Collectors;

public interface IGraph<V> {

    record Edge<V>(V from, V to) {
    }

    default Collection<Edge<V>> outgoingEdges(final V from) {
        return outgoingNeighbours(from).stream().map(to -> new Edge<>(from, to)).collect(Collectors.toList());
    }

    Collection<V> outgoingNeighbours(final V from);
}
