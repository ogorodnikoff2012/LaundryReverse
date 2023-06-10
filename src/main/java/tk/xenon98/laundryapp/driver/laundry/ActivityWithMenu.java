
package tk.xenon98.laundryapp.driver.laundry;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import tk.xenon98.laundryapp.driver.UiDriver;
import tk.xenon98.laundryapp.driver.xml.Hierarchy;
import tk.xenon98.laundryapp.driver.xml.HierarchyUtil;
import tk.xenon98.laundryapp.driver.xml.Node;

public class ActivityWithMenu implements IActivity {

    private final LaundryAppDriver laundryAppDriver;
    private final UiDriver uiDriver;

    private Rectangle openMenuButton;
    private Rectangle closeMenuButton;

    private final Map<MenuItem, Rectangle> menuItems = new EnumMap<>(MenuItem.class);

    public ActivityWithMenu(final LaundryAppDriver laundryAppDriver) {
        this.laundryAppDriver = laundryAppDriver;
        this.uiDriver = laundryAppDriver.getUiDriver();
    }

    @Override
    public void init() throws IOException, ExecutionException, InterruptedException {
        this.openMenuButton = findOpenMenuButton();
        uiDriver.tap(this.openMenuButton);
        this.closeMenuButton = findCloseMenuButton();

        discoverMenuItems();

        uiDriver.tap(this.closeMenuButton);
    }

    public void openMenu() throws IOException, ExecutionException, InterruptedException {
        if (!menuIsOpened()) {
            doOpenMenu();
        }
    }

    public void closeMenu() throws IOException, ExecutionException, InterruptedException {
        if (menuIsOpened()) {
            doCloseMenu();
        }
    }

    public boolean menuIsOpened() throws IOException, ExecutionException, InterruptedException {
        final Hierarchy hierarchy = uiDriver.getUiHierarchy();
        return HierarchyUtil.findByResourceId(hierarchy, "com.innovationscript.lalaunderette:id/drawer") != null;
    }

    private void doOpenMenu() throws IOException, ExecutionException, InterruptedException {
        uiDriver.tap(this.openMenuButton);
    }

    private void doCloseMenu() throws IOException, ExecutionException, InterruptedException {
        uiDriver.tap(this.closeMenuButton);
    }

    private void discoverMenuItems() throws IOException, ExecutionException, InterruptedException {
        final Hierarchy hierarchy = uiDriver.getUiHierarchy();
        for (final MenuItem menuItem : MenuItem.values()) {
            this.menuItems.put(menuItem, discoverMenuItem(menuItem, hierarchy));
        }
    }

    private Rectangle discoverMenuItem(final MenuItem menuItem, final Hierarchy hierarchy) {
        final String resourceId = "com.innovationscript.lalaunderette:id/menu_" + menuItem.name().toLowerCase();
        return HierarchyUtil.findByResourceId(hierarchy, resourceId).getBounds();
    }

    private Rectangle findCloseMenuButton() throws IOException, ExecutionException, InterruptedException {
        final Hierarchy hierarchy = uiDriver.getUiHierarchy();
        return HierarchyUtil.findByResourceId(hierarchy,
                "com.innovationscript.lalaunderette:id/button_close_drawer").getBounds();
    }

    private Rectangle findOpenMenuButton() throws IOException, ExecutionException, InterruptedException {
        final Hierarchy hierarchy = uiDriver.getUiHierarchy();
        final Node toolbarNode =
                HierarchyUtil.findByResourceId(hierarchy, "com.innovationscript.lalaunderette:id/toolbar");
        final Node openMenuButtonNode = HierarchyUtil.findByClassName(toolbarNode, "android.widget.ImageButton");
        return openMenuButtonNode.getBounds();
    }

    public void selectItem(final MenuItem item) throws IOException, ExecutionException, InterruptedException {
        uiDriver.tap(menuItems.get(item));
    }

    public enum MenuItem {

        HOME(LaundryAppState.HOME_ACTIVITY),
        ORDER_HISTORY(LaundryAppState.ORDER_HISTORY_ACTIVITY),
        PROFILE(LaundryAppState.PROFILE_ACTIVITY),
        SETTINGS(LaundryAppState.SETTINGS_ACTIVITY),
        FAQ(LaundryAppState.FAQ_ACTIVITY),
        TC_PRIVACY(LaundryAppState.TC_PRIVACY_ACTIVITY),
        NUMBER(LaundryAppState.SHUTDOWN),
        LOGOUT(LaundryAppState.LOGIN_ACTIVITY),
        ;

        private static final Map<LaundryAppState, MenuItem> menuItemByActivity;

        static {
            menuItemByActivity = Arrays.stream(values())
                    .collect(Collectors.toMap(MenuItem::getCorrespondingActivity, Function.identity()));
        }

        public static MenuItem findByActivity(final LaundryAppState activity) {
            return menuItemByActivity.get(activity);
        }

        @Getter
        private final LaundryAppState correspondingActivity;

        MenuItem(final LaundryAppState correspondingActivity) {
            this.correspondingActivity = correspondingActivity;
        }
    }
}
