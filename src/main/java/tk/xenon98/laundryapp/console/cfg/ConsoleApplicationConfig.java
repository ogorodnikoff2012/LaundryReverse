
package tk.xenon98.laundryapp.console.cfg;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import tk.xenon98.laundryapp.bundle.cfg.BundleConfig;
import tk.xenon98.laundryapp.console.controller.ConsoleController;
import tk.xenon98.laundryapp.console.controller.ConsoleRestController;

@Configuration
@Import({BundleConfig.class,

        ConsoleController.class, ConsoleRestController.class,

        SdkManagerDriverConfig.class, AdbDriverConfig.class, AppDriverConfig.class, UiDriverConfig.class,

		TelegramBotConfig.class,
})
public class ConsoleApplicationConfig {

}
