
package tk.xenon98.laundryapp.driver.xml;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class HierarchyUtil {

    private HierarchyUtil() {
    }

    public static Stream<Node> walk(final Node root) {
        return Stream.concat(Stream.of(root), root.getChildren().stream().flatMap(HierarchyUtil::walk));
    }

    public static Stream<Node> walk(final Hierarchy hierarchy) {
        return walk(hierarchy.getNode());
    }

    public static Stream<Node> findAll(final Hierarchy hierarchy, final Predicate<Node> predicate) {
        return walk(hierarchy).filter(predicate);
    }

    public static Stream<Node> findAll(final Node root, final Predicate<Node> predicate) {
        return walk(root).filter(predicate);
    }

    public static Node findByResourceId(final Hierarchy hierarchy, final String resourceId) {
        return HierarchyUtil.findByResourceId(hierarchy.getNode(), resourceId);
    }

    public static Node findByResourceId(final Node root, final String resourceId) {
        return HierarchyUtil.findBy(root, node -> Objects.equals(resourceId, node.getResourceId()));
    }

    public static Node findByClassName(final Hierarchy hierarchy, final String className) {
        return HierarchyUtil.findByClassName(hierarchy.getNode(), className);
    }

    public static Node findByClassName(final Node root, final String className) {
        return HierarchyUtil.findBy(root, node -> Objects.equals(className, node.getClazz()));
    }

    public static Node findBy(final Hierarchy hierarchy, final Predicate<Node> predicate) {
        return HierarchyUtil.findBy(hierarchy.getNode(), predicate);
    }

    public static Node findBy(final Node node, final Predicate<Node> predicate) {
        if (predicate.test(node)) {
            return node;
        }
        return node.getChildren().stream().map(child -> findBy(child, predicate)).filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }
}
