
package tk.xenon98.laundryapp.driver;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import lombok.Getter;
import lombok.NonNull;
import tk.xenon98.laundryapp.driver.xml.Hierarchy;
import tk.xenon98.laundryapp.driver.xml.HierarchyUtil;

public class NexusLauncherAppDriver implements IDriver, IAppDriver {

    public static final String PACKAGE_NAME = "com.google.android.apps.nexuslauncher";
    public static final String ATTRIBUTE_STATE = "state";

    @Getter
    private final UiDriver uiDriver;

    public NexusLauncherAppDriver(final UiDriver uiDriver) {
        this.uiDriver = uiDriver;
    }

    @Override
    public String getPackageName() {
        return PACKAGE_NAME;
    }

    @Override
    public @NonNull Map<String, String> getUiStateAttributes(final String activityName,
            final Hierarchy hierarchy) {
        final LauncherState launcherState = getLauncherState(hierarchy);
        return Map.of(ATTRIBUTE_STATE, launcherState.name());
    }

    @Override
    public void launchApp() throws IOException, ExecutionException, InterruptedException {
        uiDriver.homeButton();
    }

    public void ensureState(final LauncherState state) throws IOException, ExecutionException,
            InterruptedException {
        launchApp();
        switch (state) {
            case HOME_PAGE, UNKNOWN -> {
                // Do nothing
            }
            case APPS_PAGE -> uiDriver.swipeUp();
            case TASKS_PAGE -> uiDriver.appSwitchButton();
        }
    }

    public void killAllTasks() throws IOException, ExecutionException, InterruptedException {
        ensureState(LauncherState.TASKS_PAGE);
        while (HierarchyUtil.findByResourceId(uiDriver.getUiHierarchy(),
                "com.google.android.apps.nexuslauncher:id/snapshot") != null) {
            uiDriver.swipeUp();
        }
        ensureState(LauncherState.HOME_PAGE);
    }

    public enum LauncherState {
        HOME_PAGE,
        APPS_PAGE,
        TASKS_PAGE,
        UNKNOWN,
    }

    private LauncherState getLauncherState(final Hierarchy uiHierarchy) {
        if (HierarchyUtil.findByResourceId(uiHierarchy, "com.google.android.apps.nexuslauncher:id/workspace")
                != null) {
            return LauncherState.HOME_PAGE;
        } else if (HierarchyUtil.findByResourceId(uiHierarchy,
                "com.google.android.apps.nexuslauncher:id/apps_list_view") != null) {
            return LauncherState.APPS_PAGE;
        } else if (HierarchyUtil.findByResourceId(uiHierarchy,
                "com.google.android.apps.nexuslauncher:id/overview_panel") != null) {
            return LauncherState.TASKS_PAGE;
        } else {
            return LauncherState.UNKNOWN;
        }
    }
}
