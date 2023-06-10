
package tk.xenon98.laundryapp.bundle.cfg;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import tk.xenon98.laundryapp.driver.AvdManagerDriver;
import tk.xenon98.laundryapp.driver.AvdManagerDriver.AvdDescription;
import tk.xenon98.laundryapp.driver.SdkManagerDriver;

@Configuration
public class AVDConfig {

    private static final String AVD_NAME = "LAUNDRY_AVD";
    public static final String SYSTEM_IMAGE = "system-images;android-30;google_apis_playstore;x86";
    public static final String PLATFORM = "platforms;android-30";
    public static final String DEVICE_NAME = "pixel_2";
    private static final AvdDescription AVD_DESCRIPTION = AvdDescription.builder()
            .name(AVD_NAME)
            .systemImage(SYSTEM_IMAGE)
            .deviceName(DEVICE_NAME)
            .build();

    @Bean
    public AvdManagerDriver avdManagerDriver(@Autowired File avdManagerExecutable, @Autowired File avdDirectory) {
        return new AvdManagerDriver(avdManagerExecutable, avdDirectory);
    }

    @Bean
    @DependsOn("androidVirtualDevice")
    public String avdName() {
        return AVD_NAME;
    }

    @Bean
    public Void avdSystemImage(@Autowired SdkManagerDriver sdkManagerDriver, @Autowired File androidSDK)
            throws IOException, InterruptedException {
        sdkManagerDriver.installPackage(SYSTEM_IMAGE, androidSDK);
        sdkManagerDriver.installPackage(PLATFORM, androidSDK);
        return null;
    }

    @Bean
    @DependsOn({"avdSystemImage"})
    public AvdDescription androidVirtualDevice(@Autowired AvdManagerDriver avdManagerDriver)
            throws IOException, InterruptedException {
        if (!avdManagerDriver.getInstalledAVDs().contains(AVD_DESCRIPTION.getName())) {
            avdManagerDriver.installAVD(AVD_DESCRIPTION);
        }
        return AVD_DESCRIPTION;
    }

    @Bean
    public UUID avdUuid() {
        return UUID.randomUUID();
    }

    @Bean
    public File avdDirectory(@Autowired File androidSDK) throws IOException {
        final var dir = androidSDK.toPath().resolve("avd");
        Files.createDirectories(dir);
        return dir.toFile();
    }
}
