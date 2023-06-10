
package tk.xenon98.laundryapp.console.cfg;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import tk.xenon98.laundryapp.driver.EmulatorDriver;
import tk.xenon98.laundryapp.driver.SdkManagerDriver;

@Configuration
public class EmulatorDriverConfig {

    @Bean
    public File emulatorExecutable(@Autowired SdkManagerDriver sdkManagerDriver, @Autowired File androidSDK)
            throws IOException, InterruptedException {
        sdkManagerDriver.installPackage("emulator", androidSDK);
        return androidSDK.toPath().resolve("emulator/emulator").toFile();
    }

    @Bean
    public Void setupEmulator(@Autowired File androidSDK) throws IOException {
        setupRecursiveSymLink(androidSDK);
        return null;
    }

    private void setupRecursiveSymLink(final File androidSDK) throws IOException {
        final Path sdkPath = androidSDK.toPath();
        final Path symlinkPath = sdkPath.resolve(sdkPath.getFileName());
        if (Files.exists(symlinkPath, LinkOption.NOFOLLOW_LINKS)) {
            if (Files.isSymbolicLink(symlinkPath) && Files.readSymbolicLink(symlinkPath).equals(sdkPath)) {
                return;
            }
            Files.delete(symlinkPath);
        }
        Files.createSymbolicLink(symlinkPath, sdkPath);
    }

    @Bean
    @DependsOn({"setupEmulator"})
    public EmulatorDriver emulatorDriver(@Autowired File emulatorExecutable) {
        return new EmulatorDriver(emulatorExecutable);
    }
}
