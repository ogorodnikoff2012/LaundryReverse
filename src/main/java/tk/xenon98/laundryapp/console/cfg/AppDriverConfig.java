
package tk.xenon98.laundryapp.console.cfg;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tk.xenon98.laundryapp.driver.AppManager;
import tk.xenon98.laundryapp.driver.NexusLauncherAppDriver;
import tk.xenon98.laundryapp.driver.UiDriver;
import tk.xenon98.laundryapp.driver.laundry.LaundryAppDriver;

@Configuration
public class AppDriverConfig {

    @Value("${tk.xenon98.laundryapp.driver.laundry.email}")
    private String email;
    @Value("${tk.xenon98.laundryapp.driver.laundry.password}")
    private String password;

    @Bean
    public NexusLauncherAppDriver nexusLauncherAppDriver(@Autowired UiDriver uiDriver) {
        return new NexusLauncherAppDriver(uiDriver);
    }

    @Bean
    public LaundryAppDriver laundryAppDriver(@Autowired UiDriver uiDriver,
            @Autowired NexusLauncherAppDriver nexusLauncherAppDriver) {
        return new LaundryAppDriver(uiDriver, nexusLauncherAppDriver, email, password);
    }

    @Bean
    public AppManager appManager(@Autowired UiDriver uiDriver,
            @Autowired NexusLauncherAppDriver nexusLauncherAppDriver,
            @Autowired LaundryAppDriver laundryAppDriver) {
        final var manager = new AppManager(uiDriver);
        manager.registerAppDriver(nexusLauncherAppDriver);
        manager.registerAppDriver(laundryAppDriver);
        return manager;
    }
}
