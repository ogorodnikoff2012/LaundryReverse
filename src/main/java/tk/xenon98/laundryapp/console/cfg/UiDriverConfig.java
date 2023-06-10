
package tk.xenon98.laundryapp.console.cfg;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tk.xenon98.laundryapp.console.data.api.DriverApi;
import tk.xenon98.laundryapp.driver.AdbDriver;
import tk.xenon98.laundryapp.driver.UiDriver;

@Configuration
public class UiDriverConfig {

    @Bean
    public UiDriver uiDriver(@Autowired AdbDriver adbDriver) {
        return new UiDriver(adbDriver);
    }

}
