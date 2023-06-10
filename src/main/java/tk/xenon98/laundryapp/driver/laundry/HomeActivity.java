
package tk.xenon98.laundryapp.driver.laundry;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import tk.xenon98.laundryapp.driver.UiDriver;
import tk.xenon98.laundryapp.driver.xml.Hierarchy;
import tk.xenon98.laundryapp.driver.xml.HierarchyUtil;

public class HomeActivity implements IActivity {

    private final LaundryAppDriver laundryAppDriver;
    private final UiDriver uiDriver;

    public HomeActivity(final LaundryAppDriver laundryAppDriver) {
        this.laundryAppDriver = laundryAppDriver;
        this.uiDriver = laundryAppDriver.getUiDriver();
    }

    @Override
    public void init() throws IOException, ExecutionException, InterruptedException {
    }

    public void clickSelectWasher() throws IOException, ExecutionException, InterruptedException {
        final Hierarchy hierarchy = uiDriver.getUiHierarchy();
        final var buyWasherButton = HierarchyUtil.findByResourceId(hierarchy,
                "com.innovationscript.lalaunderette:id/buy_washer").getBounds();
        uiDriver.tap(buyWasherButton);
    }

    public void clickSelectDryer() throws IOException, ExecutionException, InterruptedException {
        final Hierarchy hierarchy = uiDriver.getUiHierarchy();
        final var buyDryerButton = HierarchyUtil.findByResourceId(hierarchy,
                "com.innovationscript.lalaunderette:id/buy_dryer").getBounds();
        uiDriver.tap(buyDryerButton);
    }

    public void clickReserveWasher() throws IOException, ExecutionException, InterruptedException {
        final Hierarchy hierarchy = uiDriver.getUiHierarchy();
        final var bookWasherButton = HierarchyUtil.findByResourceId(hierarchy,
                "com.innovationscript.lalaunderette:id/book_washer").getBounds();
        uiDriver.tap(bookWasherButton);
    }

    public boolean isReady() {
        try {
            final Hierarchy hierarchy = uiDriver.getUiHierarchy();
            return HierarchyUtil.findByResourceId(hierarchy, "com.innovationscript.lalaunderette:id/buy_washer")
                    != null;
        } catch (Exception e) {
            return false;
        }
    }
}
