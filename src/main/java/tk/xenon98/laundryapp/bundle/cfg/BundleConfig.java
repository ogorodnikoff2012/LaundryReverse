
package tk.xenon98.laundryapp.bundle.cfg;

import java.io.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import tk.xenon98.laundryapp.console.cfg.EmulatorDriverConfig;
import tk.xenon98.laundryapp.console.cfg.SdkManagerDriverConfig;

@Configuration
@Import({
        ResourceConfig.class,
        AndroidSDKConfig.class,
        AVDConfig.class,
        SdkManagerDriverConfig.class,
        EmulatorDriverConfig.class,
        LaundryAppConfig.class,
})
public class BundleConfig {

    @Bean
    public Void setupBundle(@Autowired File androidSDKPlatformTools) {
        System.out.println(androidSDKPlatformTools.getAbsolutePath());
        return null;
    }
}
