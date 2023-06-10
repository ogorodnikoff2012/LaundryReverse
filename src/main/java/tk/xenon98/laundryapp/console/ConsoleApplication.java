
package tk.xenon98.laundryapp.console;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import tk.xenon98.laundryapp.console.cfg.ConsoleApplicationConfig;

@SpringBootApplication
@Import(ConsoleApplicationConfig.class)
public class ConsoleApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(ConsoleApplication.class)
                .initializers(new PropertySourceInitializer())
                .run(args);
    }
}
