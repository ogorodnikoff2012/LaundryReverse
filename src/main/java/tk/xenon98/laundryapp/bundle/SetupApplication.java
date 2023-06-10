package tk.xenon98.laundryapp.bundle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import tk.xenon98.laundryapp.bundle.cfg.BundleConfig;

@SpringBootApplication
@Import(BundleConfig.class)
public class SetupApplication {

    public static void main(String[] args) {
        SpringApplication.run(SetupApplication.class, args);
        System.exit(0);
    }

}
