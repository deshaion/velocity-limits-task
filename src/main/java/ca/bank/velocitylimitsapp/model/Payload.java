package ca.bank.velocitylimitsapp.model;

import ca.bank.velocitylimitsapp.config.CurrencyDeserializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@NoArgsConstructor
@Getter
public class Payload {
    private String id;

    @JsonProperty("customer_id")
    private String customerId;

    @JsonProperty("load_amount")
    @JsonDeserialize(using = CurrencyDeserializer.class)
    private BigDecimal loadAmount;

    private OffsetDateTime time;
}
