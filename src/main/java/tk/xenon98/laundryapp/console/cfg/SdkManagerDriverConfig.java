
package tk.xenon98.laundryapp.console.cfg;

import java.io.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tk.xenon98.laundryapp.bundle.resources.ResourceRegistry;
import tk.xenon98.laundryapp.bundle.resources.resolver.SdkManagerDriverResourceResolver;
import tk.xenon98.laundryapp.driver.SdkManagerDriver;

@Configuration
public class SdkManagerDriverConfig {

    @Bean
    SdkManagerDriver sdkManagerDriver(@Autowired ResourceRegistry resourceRegistry,
            @Autowired File sdkManagerExecutable) {
        final SdkManagerDriver driver = new SdkManagerDriver(sdkManagerExecutable);
        resourceRegistry.registerResourceResolver(SdkManagerDriver.URI_SCHEME,
                new SdkManagerDriverResourceResolver(driver));
        return driver;
    }
}
