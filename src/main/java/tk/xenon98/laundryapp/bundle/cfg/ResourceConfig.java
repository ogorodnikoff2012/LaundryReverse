
package tk.xenon98.laundryapp.bundle.cfg;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tk.xenon98.laundryapp.bundle.resources.DownloadManager;
import tk.xenon98.laundryapp.bundle.resources.ResourceRegistry;
import tk.xenon98.laundryapp.bundle.resources.resolver.SeleniumResolver;
import tk.xenon98.laundryapp.bundle.resources.resolver.URLResourceResolver;
import tk.xenon98.laundryapp.bundle.resources.resolver.ZipResourceResolver;

@Configuration
public class ResourceConfig {

    @Bean
    public ResourceRegistry resourceRegistry(@Autowired final File resourceDirectory) {
        final var downloadManager = new DownloadManager();
        final var registry = new ResourceRegistry(resourceDirectory, new URLResourceResolver(downloadManager));
        registry.registerResourceResolver(ZipResourceResolver.ZIP_SCHEME, new ZipResourceResolver());
        registry.registerResourceResolver(SeleniumResolver.URI_SCHEME, new SeleniumResolver(downloadManager));
        return registry;
    }

    @Bean
    public File resourceDirectory() throws IOException {
        final Path path = Paths.get("bundle");
        Files.createDirectories(path);
        return path.toFile();
    }
}
