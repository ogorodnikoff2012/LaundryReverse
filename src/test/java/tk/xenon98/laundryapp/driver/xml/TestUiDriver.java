
package tk.xenon98.laundryapp.driver.xml;

import org.junit.jupiter.api.Test;
import tk.xenon98.laundryapp.driver.AdbDriver;
import tk.xenon98.laundryapp.driver.UiDriver;

public class TestUiDriver {

    @Test
    public void testGetUiInfo() throws Exception {
        final AdbDriver adbDriver = new AdbDriver();
        final UiDriver uiDriver = new UiDriver(adbDriver);

        final Hierarchy uiInfo = uiDriver.getUiHierarchy();
        System.out.println(uiInfo);
    }
}
