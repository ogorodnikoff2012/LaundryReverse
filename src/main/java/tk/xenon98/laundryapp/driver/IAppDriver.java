
package tk.xenon98.laundryapp.driver;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import lombok.NonNull;
import tk.xenon98.laundryapp.driver.xml.Hierarchy;

public interface IAppDriver {

    String getPackageName();

    @NonNull
    Map<String, String> getUiStateAttributes(final String activityName, final Hierarchy hierarchy);

    void launchApp() throws IOException, ExecutionException, InterruptedException;

    IAppDriver GENERIC_APP_DRIVER = new IAppDriver() {

        @Override
        public String getPackageName() {
            return "";
        }

        @Override
        public @NonNull Map<String, String> getUiStateAttributes(final String activityName,
                final Hierarchy hierarchy) {
            return Map.of();
        }

        @Override
        public void launchApp() throws IOException, ExecutionException, InterruptedException {
            // Do nothing
        }
    };
}
