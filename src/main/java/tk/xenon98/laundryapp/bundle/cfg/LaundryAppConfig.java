
package tk.xenon98.laundryapp.bundle.cfg;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tk.xenon98.laundryapp.bundle.resources.Resource;
import tk.xenon98.laundryapp.bundle.resources.ResourceRegistry;
import tk.xenon98.laundryapp.bundle.resources.integrity.IIntegrityVerificator;
import tk.xenon98.laundryapp.driver.AdbDriver;

@Configuration
public class LaundryAppConfig {

    private static final Resource LAUNDRY_APK = new Resource("laundry.apk",
            URI.create("selenium+https://apkcombo.com/ecla-the-laundry/com.innovationscript.lalaunderette/"),
            IIntegrityVerificator.noOp());

    @Bean
    public File laundryApk(@Autowired ResourceRegistry resourceRegistry) throws IOException {
        return resourceRegistry.getResource(LAUNDRY_APK);
    }

    @Bean
    public Void laundryAppSetup(@Autowired File laundryApk, @Autowired AdbDriver adbDriver)
            throws IOException, InterruptedException {
        adbDriver.installAPK(laundryApk.getPath());
        return null;
    }

}
