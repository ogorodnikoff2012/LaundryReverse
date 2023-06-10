
package tk.xenon98.laundryapp.console.cfg;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import tk.xenon98.laundryapp.common.utils.Utils;
import tk.xenon98.laundryapp.driver.AdbDriver;
import tk.xenon98.laundryapp.driver.AdbDriver.AdbState;

@Configuration
public class AdbDriverConfig {

    private static final Logger LOG = LoggerFactory.getLogger(AdbDriverConfig.class);

    @Bean(destroyMethod = "emuKill")
    @DependsOn("AVDService")
    public AdbDriver adbDriver(@Autowired File adbExecutable, @Autowired int adbPort) throws InterruptedException {
        return new AdbDriver(adbExecutable, "emulator-" + adbPort);
    }

    @Bean
    public Void adbSetup(@Autowired AdbDriver adbDriver) throws InterruptedException, TimeoutException {
        Utils.waitUntil(() -> {
            try {
                return adbDriver.getState() == AdbState.DEVICE;
            } catch (IOException e) {
                // Do nothing
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return false;
        }, "AVD is online", Duration.ofSeconds(120), Duration.ofSeconds(1));
        return null;
    }

}
