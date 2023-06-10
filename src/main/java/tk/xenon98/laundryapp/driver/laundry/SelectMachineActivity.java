
package tk.xenon98.laundryapp.driver.laundry;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import tk.xenon98.laundryapp.common.utils.Utils;
import tk.xenon98.laundryapp.driver.UiDriver;
import tk.xenon98.laundryapp.driver.xml.Hierarchy;
import tk.xenon98.laundryapp.driver.xml.HierarchyUtil;
import tk.xenon98.laundryapp.driver.xml.Node;

public abstract class SelectMachineActivity implements IActivity {

    protected final LaundryAppDriver laundryAppDriver;
    protected final UiDriver uiDriver;

    protected SelectMachineActivity(final LaundryAppDriver laundryAppDriver) {
        this.laundryAppDriver = laundryAppDriver;
        this.uiDriver = laundryAppDriver.getUiDriver();
    }

    @Override
    public void init() throws IOException, ExecutionException, InterruptedException {

    }

    public void scrollTop() throws IOException, ExecutionException, InterruptedException {
        while (true) {
            final Hierarchy hierarchy = uiDriver.getUiHierarchy();
            if (HierarchyUtil.findByResourceId(hierarchy, "com.innovationscript.lalaunderette:id/location_name")
                    != null) {
                break;
            }
            uiDriver.scrollUp();
        }
    }

    protected List<Node> scanVisibleItems(final Hierarchy hierarchy)
            throws IOException, ExecutionException, InterruptedException {
        final Node itemsGrid =
                HierarchyUtil.findByResourceId(hierarchy, "com.innovationscript.lalaunderette:id/items");

        return itemsGrid.getChildren().stream().map(itemNode -> {
            final Node nameNode =
                    HierarchyUtil.findByResourceId(itemNode, "com.innovationscript.lalaunderette:id/number");
            final Node sizeNode =
                    HierarchyUtil.findByResourceId(itemNode, "com.innovationscript.lalaunderette:id/size");
            final Node statusNode = HierarchyUtil.findByResourceId(itemNode,
                    "com.innovationscript.lalaunderette:id/action_select");
            final Node timeLeftNode = HierarchyUtil.findByResourceId(itemNode,
                    "com.innovationscript.lalaunderette:id/time_left_value");

            if (nameNode == null || sizeNode == null || statusNode == null) {
                return null;
            }

            return itemNode;
        }).filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    protected MachineItem parseItem(final Node item) {
        final Node nameNode =
                HierarchyUtil.findByResourceId(item, "com.innovationscript.lalaunderette:id/number");
        final Node sizeNode =
                HierarchyUtil.findByResourceId(item, "com.innovationscript.lalaunderette:id/size");
        final Node statusNode = HierarchyUtil.findByResourceId(item,
                "com.innovationscript.lalaunderette:id/action_select");
        final Node timeLeftNode = HierarchyUtil.findByResourceId(item,
                "com.innovationscript.lalaunderette:id/time_left_value");

        return new MachineItem(nameNode.getText(), sizeNode.getText(), MachineStatus.parseValue(statusNode
                .getText()),
                Optional.ofNullable(timeLeftNode).map(Node::getText).map(
                        Utils::parseDuration));
    }

    protected Collection<MachineItem> parseItems(final Collection<Node> items) {
        return items.stream().map(this::parseItem).collect(Collectors.toList());
    }

    protected Collection<MachineItem> scanAllItems() throws IOException, ExecutionException, InterruptedException {
        scrollTop();
        final Set<MachineItem> result = new HashSet<>();

        Hierarchy lastHierarchy = uiDriver.getUiHierarchy();
        while (true) {
            result.addAll(parseItems(scanVisibleItems(lastHierarchy)));
            uiDriver.scrollDown();
            final Hierarchy hierarchy = uiDriver.getUiHierarchy();
            if (hierarchy.equals(lastHierarchy)) {
                break;
            }
            lastHierarchy = hierarchy;
        }

        return result;
    }

    protected Node scrollToItem(final String name) throws IOException, ExecutionException, InterruptedException {
        scrollTop();

        Hierarchy lastHierarchy = uiDriver.getUiHierarchy();
        while (true) {
            final var visibleItems = scanVisibleItems(lastHierarchy);
            for (final var item : visibleItems) {
                if (parseItem(item).name().equals(name)) {
                    return item;
                }
            }

            uiDriver.scrollDown();
            final Hierarchy hierarchy = uiDriver.getUiHierarchy();
            if (hierarchy.equals(lastHierarchy)) {
                break;
            }
            lastHierarchy = hierarchy;
        }

        return null;
    }
}
