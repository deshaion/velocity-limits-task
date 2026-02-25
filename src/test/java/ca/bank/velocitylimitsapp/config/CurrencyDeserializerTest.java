package ca.bank.velocitylimitsapp.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrencyDeserializerTest {

    @Mock
    private JsonParser jsonParser;

    @Mock
    private DeserializationContext deserializationContext;

    private final CurrencyDeserializer deserializer = new CurrencyDeserializer();

    @Test
    void testDeserializeWithSymbol() throws IOException {
        when(jsonParser.getText()).thenReturn("$1000.00");
        BigDecimal result = deserializer.deserialize(jsonParser, deserializationContext);
        assertEquals(new BigDecimal("1000.00"), result);
    }

    @Test
    void testDeserializeWithoutSymbol() throws IOException {
        when(jsonParser.getText()).thenReturn("500.50");
        BigDecimal result = deserializer.deserialize(jsonParser, deserializationContext);
        assertEquals(new BigDecimal("500.50"), result);
    }

    @Test
    void testDeserializeNull() throws IOException {
        when(jsonParser.getText()).thenReturn(null);
        BigDecimal result = deserializer.deserialize(jsonParser, deserializationContext);
        assertNull(result);
    }

    @Test
    void testDeserializeEmpty() throws IOException {
        when(jsonParser.getText()).thenReturn("");
        BigDecimal result = deserializer.deserialize(jsonParser, deserializationContext);
        assertNull(result);
    }

    @Test
    void testDeserializeEmptyAfterTrim() throws IOException {
        when(jsonParser.getText()).thenReturn("   ");
        BigDecimal result = deserializer.deserialize(jsonParser, deserializationContext);
        assertNull(result);
    }
}
