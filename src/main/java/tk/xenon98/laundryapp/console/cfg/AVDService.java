
package tk.xenon98.laundryapp.console.cfg;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import tk.xenon98.laundryapp.driver.AdbDriver;
import tk.xenon98.laundryapp.driver.AvdManagerDriver;
import tk.xenon98.laundryapp.driver.EmulatorDriver;

@Service
public class AVDService {

    private static final int ADB_PORT = 5570;
    private final AvdManagerDriver avdManagerDriver;
    private final EmulatorDriver emulatorDriver;
    private final String avdName;
    private final ApplicationContext applicationContext;
    private Process emulatorProcess;

    @Value("${tk.xenon98.laundryapp.avd.showWindow:false}")
    private boolean showWindow;

    public AVDService(final AvdManagerDriver avdManagerDriver, final EmulatorDriver emulatorDriver,
            final String avdName, final ApplicationContext applicationContext) {
        this.avdManagerDriver = avdManagerDriver;
        this.emulatorDriver = emulatorDriver;
        this.avdName = avdName;
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init() throws IOException {
        emulatorProcess =
                emulatorDriver.runEmulatorInstance(avdName, ADB_PORT, !showWindow)
                        .redirectOutput(Redirect.INHERIT)
                        .start();
    }

    @Bean
    public int adbPort() {
        return ADB_PORT;
    }

    @PreDestroy
    public void destroy() throws InterruptedException {
        if (emulatorProcess != null) {
            emulatorProcess.waitFor();
        }
    }
}
