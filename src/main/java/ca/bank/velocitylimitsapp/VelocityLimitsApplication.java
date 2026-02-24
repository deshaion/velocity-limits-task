package ca.bank.velocitylimitsapp;

import ca.bank.velocitylimitsapp.service.LoadFundsManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StopWatch;

@SpringBootApplication
@Slf4j
public class VelocityLimitsApplication {

	public static void main(String[] args) {
		SpringApplication.run(VelocityLimitsApplication.class, args);
	}

	@Bean
    CommandLineRunner processFiles(LoadFundsManager loadFundsManager) {
        return args -> {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            loadFundsManager.load();

            stopWatch.stop();

            log.info("Processing complete in {} ms. Check output.txt", stopWatch.getTotalTimeMillis());
        };
    }
}
