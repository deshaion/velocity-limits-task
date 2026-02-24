package ca.bank.velocitylimitsapp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
@ConfigurationProperties(prefix = "load.limit")
@Getter
@Setter
public class LoadLimitProperties {
    private BigDecimal dailyAmount;
    private BigDecimal weeklyAmount;
    private int dailyCount;
}
