package ca.bank.velocitylimitsapp;

import ca.bank.velocitylimitsapp.service.LoadFundsManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Component
@RequiredArgsConstructor
@Slf4j
public class VelocityLimitsRunner implements CommandLineRunner {

    private final LoadFundsManager loadFundsManager;

    @Override
    public void run(String... args) throws Exception {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        loadFundsManager.load();

        stopWatch.stop();

        log.info("Processing complete in {} ms. Check output.txt", stopWatch.getTotalTimeMillis());
    }
}
