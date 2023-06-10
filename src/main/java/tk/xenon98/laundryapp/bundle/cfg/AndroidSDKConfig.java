
package tk.xenon98.laundryapp.bundle.cfg;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import tk.xenon98.laundryapp.bundle.resources.Resource;
import tk.xenon98.laundryapp.bundle.resources.ResourceRegistry;
import tk.xenon98.laundryapp.bundle.resources.integrity.IIntegrityVerificator;
import tk.xenon98.laundryapp.bundle.resources.resolver.ZipResourceResolver;
import tk.xenon98.laundryapp.bundle.resources.util.URIUtils;
import tk.xenon98.laundryapp.driver.SdkManagerDriver;

@Configuration
public class AndroidSDKConfig {

    private static final Logger LOG = LoggerFactory.getLogger(AndroidSDKConfig.class);

    private static final String ANDROID_SDK = "android_sdk";
    private static final String ANDROID_SDK_ZIP = ANDROID_SDK + ".zip";
    private static final String ANDROID_SDK_SDKMANAGER_EXECUTABLE = "cmdline-tools/bin/sdkmanager";
    private static final String ANDROID_SDK_AVDMANAGER_EXECUTABLE = "cmdline-tools/bin/avdmanager";

    private static final Resource SDK_ZIP_LINUX;
    private static final Resource SDK_ZIP_MAC;
    private static final Resource SDK_ZIP_WINDOWS;

    private static final Resource SDK_DIR = new Resource(ANDROID_SDK,
            URIUtils.uriBuilder().scheme(ZipResourceResolver.ZIP_SCHEME).path(ANDROID_SDK_ZIP)
                    .query(Map.of(ZipResourceResolver.ZIP_SET_EXECUTABLE, "cmdline-tools/bin/*")).build(),
            IIntegrityVerificator.neverValid());

    private static final Resource PLATFORM_TOOLS = new Resource(ANDROID_SDK,
            URIUtils.uriBuilder().scheme(SdkManagerDriver.URI_SCHEME).path("/platform-tools").build(),
            IIntegrityVerificator.neverValid());

    static {
        try {
            SDK_ZIP_LINUX = new Resource(ANDROID_SDK_ZIP,
                    URI.create(
                            "https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip"),
                    IIntegrityVerificator.sha256(
                            "bd1aa17c7ef10066949c88dc6c9c8d536be27f992a1f3b5a584f9bd2ba5646a0"));
            SDK_ZIP_MAC = new Resource(ANDROID_SDK_ZIP,
                    URI.create("https://dl.google.com/android/repository/commandlinetools-mac-9477386_latest.zip"),
                    IIntegrityVerificator.sha256(
                            "2072ffce4f54cdc0e6d2074d2f381e7e579b7d63e915c220b96a7db95b2900ee"));
            SDK_ZIP_WINDOWS = new Resource(ANDROID_SDK_ZIP,
                    URI.create(
                            "https://dl.google.com/android/repository/commandlinetools-windows-9477386_latest.zip"),
                    IIntegrityVerificator.sha256(
                            "696431978daadd33a28841320659835ba8db8080a535b8f35e9e60701ab8b491"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Bean
    public File androidSDKZipFile(@Autowired ResourceRegistry resourceRegistry) throws IOException {
        LOG.info("Downloading Android SDK Tools");
        final Resource resource = selectResource();
        return resourceRegistry.getResource(resource);
    }

    @Bean
    public File androidSDK(@Autowired ResourceRegistry resourceRegistry) throws IOException {
        LOG.info("Extracting Android SDK");
        return resourceRegistry.getResource(SDK_DIR);
    }

    @Bean
    public File sdkManagerExecutable(@Autowired File androidSDK) {
        return androidSDK.toPath().resolve(ANDROID_SDK_SDKMANAGER_EXECUTABLE).toFile();
    }

    @Bean
    @DependsOn("sdkManagerDriver")
    public File androidSDKPlatformTools(@Autowired ResourceRegistry resourceRegistry) throws IOException {
        return resourceRegistry.getResource(PLATFORM_TOOLS).toPath().resolve("platform-tools").toFile();
    }

    @Bean
    public File adbExecutable(@Autowired File androidSDKPlatformTools) throws IOException {
        return androidSDKPlatformTools.toPath().resolve("adb").toFile();
    }

    @Bean
    public File avdManagerExecutable(@Autowired File androidSDK) throws IOException {
        return androidSDK.toPath().resolve(ANDROID_SDK_AVDMANAGER_EXECUTABLE).toFile();
    }

    private static Resource selectResource() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return SDK_ZIP_WINDOWS;
        } else if (SystemUtils.IS_OS_LINUX) {
            return SDK_ZIP_LINUX;
        } else if (SystemUtils.IS_OS_MAC) {
            return SDK_ZIP_MAC;
        } else {
            throw new UnsupportedOperationException(
                    "OS %s is not supported".formatted(System.getProperty("os.name")));
        }
    }
}
