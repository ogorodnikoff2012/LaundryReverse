
package tk.xenon98.laundryapp.console;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.PropertiesPropertySource;

public class PropertySourceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger LOG = LoggerFactory.getLogger(PropertySourceInitializer.class);

    @Override
    public void initialize(final ConfigurableApplicationContext applicationContext) {
        final var env = applicationContext.getEnvironment();

        try (final var paths = Files.walk(Path.of("."), 1)) {
            paths
                    .filter(path -> path.getFileName().toString().endsWith(".properties"))
                    .forEach(path -> {
                        LOG.info("Loading properties from " + path);
                        try (final var input = new FileInputStream(path.toFile())) {
                            final var props = new Properties();
                            props.load(input);
                            env.getPropertySources().addFirst(new PropertiesPropertySource("file:" + path, props));
                        } catch (IOException e) {
                            LOG.warn("Failed to load properties from " + path + ": " + e);
                        }
                    });
        } catch (IOException e) {
            LOG.warn("Failed to load properties from current directory");
        }
    }
}
