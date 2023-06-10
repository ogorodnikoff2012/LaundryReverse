
package tk.xenon98.laundryapp.driver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import lombok.NonNull;

public class AppManager implements IDriver {

    private final UiDriver uiDriver;
    private final Map<String, IAppDriver> registeredAppDrivers = new HashMap<>();

    public AppManager(final UiDriver uiDriver) {
        this.uiDriver = uiDriver;
    }

    public void registerAppDriver(final IAppDriver appDriver) {
        this.registeredAppDrivers.put(appDriver.getPackageName(), appDriver);
    }

    public record UiState(@NonNull String packageName, @NonNull String activityName,
                          @NonNull Map<String, String> attributes) {

    }

    public IAppDriver getAppDriver(@NonNull String packageName) {
        return registeredAppDrivers.getOrDefault(packageName, IAppDriver.GENERIC_APP_DRIVER);
    }

    public UiState getUiState() throws IOException, ExecutionException, InterruptedException {
        final String[] focusedWindowParts = uiDriver.focusedWindow().split("/", 2);

        final String packageName = focusedWindowParts[0];
        final String activityName = focusedWindowParts[1];
        final Map<String, String> attributes = getAppDriver(packageName).getUiStateAttributes(activityName,
                uiDriver.getUiHierarchy());
        return new UiState(packageName, activityName, attributes);
    }
}
