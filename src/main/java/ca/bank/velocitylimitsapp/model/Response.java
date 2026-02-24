package ca.bank.velocitylimitsapp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Response {
    private String id;

    @JsonProperty("customer_id")
    private String customerId;

    private Boolean accepted;
}
