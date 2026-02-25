package ca.bank.velocitylimitsapp;

import ca.bank.velocitylimitsapp.io.ResponseWriter;
import ca.bank.velocitylimitsapp.io.TransactionReader;
import ca.bank.velocitylimitsapp.model.Response;
import ca.bank.velocitylimitsapp.service.LoadFundsManagerImpl;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
public class IntegrationTests {
    @Autowired
    private LoadFundsManagerImpl loadFundsManager;

    @MockitoBean
    private TransactionReader transactionReader;

    @MockitoBean
    private ResponseWriter responseWriter;

    @Test
    void testLoadFundsBasic() throws IOException {
        String customerId = "123";

        Stream<String> input = Stream.of(
                in("1", customerId, "$2000.00", "2026-02-01T00:00:00Z"),
                in("2", customerId, "$2000.00", "2026-02-01T00:00:00Z"),
                in("3", customerId, "$1000.00", "2026-02-01T00:00:00Z")
        );

        List<Response> expectedList = List.of(
                resp("1", customerId, true),
                resp("2", customerId, true),
                resp("3", customerId, true)
        );

        runTest(input, expectedList);
    }

    @Test
    void testLoadFundsWithCents() throws IOException {
        String customerId = "124";

        Stream<String> input = Stream.of(
                in("1", customerId, "$2000.00", "2026-02-01T02:00:00Z"),
                in("2", customerId, "$2999.99", "2026-02-01T03:00:00Z"),
                in("3", customerId, "$0.01", "2026-02-01T23:59:59Z")
        );

        List<Response> expectedList = List.of(
                resp("1", customerId, true),
                resp("2", customerId, true),
                resp("3", customerId, true)
        );

        runTest(input, expectedList);
    }

    @Test
    void testLoadFundsWithCentsNotAccepted() throws IOException {
        String customerId = "125";

        Stream<String> input = Stream.of(
                in("1", customerId, "$2000.00", "2026-02-01T02:00:00Z"),
                in("2", customerId, "$2999.99", "2026-02-01T03:00:00Z"),
                in("3", customerId, "$0.02", "2026-02-01T23:59:59Z")
        );

        List<Response> expectedList = List.of(
                resp("1", customerId, true),
                resp("2", customerId, true),
                resp("3", customerId, false)
        );

        runTest(input, expectedList);
    }

    @Test
    void testLoadFundsAcceptedWith2CentsNextDay() throws IOException {
        String customerId = "126";

        Stream<String> input = Stream.of(
                in("1", customerId, "$2000.00", "2026-02-01T23:59:59Z"),
                in("2", customerId, "$2999.99", "2026-02-01T23:59:59Z"),
                in("3", customerId, "$0.02", "2026-02-01T23:59:59Z"),
                in("4", customerId, "$0.02", "2026-02-02T00:00:00Z")
        );

        List<Response> expectedList = List.of(
                resp("1", customerId, true),
                resp("2", customerId, true),
                resp("3", customerId, false),
                resp("4", customerId, true)
        );

        runTest(input, expectedList);
    }

    @Test
    void testLoadFakeDataInTheMiddleOfFile() throws IOException {
        String customerId = "127";

        Stream<String> input = Stream.of(
                in("1", customerId, "$2000.00", "2026-02-01T23:59:59Z"),
                in("2", customerId, "$2999.99", "2026-02-01T23:59:59Z"),
                "fake-json",
                in("3", customerId, "$0.02", "2026-02-02T00:00:00Z")
        );

        List<Response> expectedList = List.of(
                resp("1", customerId, true),
                resp("2", customerId, true)
        );

        // 1. Control input
        when(transactionReader.readAsStream()).thenReturn(input);

        // 2. Execute
        Exception exception = assertThrows(Exception.class, () -> loadFundsManager.load());

        // 3. Verify
        int expectedResponseCount = expectedList.size();

        ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);

        verify(responseWriter, times(expectedResponseCount)).writeLine(responseCaptor.capture());

        List<Response> actualWrites = responseCaptor.getAllValues();

        assertEquals(expectedResponseCount, actualWrites.size()); // Double check we have 4

        assertThat(actualWrites)
                .usingRecursiveComparison()
                .isEqualTo(expectedList);
    }

    @Test
    void testIgnoringDuplicates() throws IOException {
        String customerId = "128";

        Stream<String> input = Stream.of(
                in("1", customerId, "$2000.00", "2000-01-01T00:00:00Z"),
                in("1", customerId, "$1", "2000-01-01T01:00:00Z"),
                in("1", customerId, "$1", "2000-01-02T01:00:00Z"),
                in("1", customerId, "$1", "2000-01-02T02:00:00Z"),
                in("20", customerId, "$1", "2000-01-02T03:00:00Z"),
                in("1", customerId, "$1", "2000-01-02T03:00:00Z")
        );

        List<Response> expectedList = List.of(
                resp("1", customerId, true),
                resp("20", customerId, true)
        );

        runTest(input, expectedList);
    }

    @Test
    void testWeeklyLimitByAddingAtTheEndOfWeek() throws IOException {
        String customerId = "129";

        Stream<String> input = Stream.of(
                in("1", customerId, "$5000.00", "2026-02-16T00:00:00Z"),
                in("2", customerId, "$5000.00", "2026-02-17T00:00:00Z"),
                in("3", customerId, "$5000.00", "2026-02-18T00:00:00Z"),
                in("4", customerId, "$5000.00", "2026-02-22T23:59:59Z")
        );

        List<Response> expectedList = List.of(
                resp("1", customerId, true),
                resp("2", customerId, true),
                resp("3", customerId, true),
                resp("4", customerId, true)
        );

        runTest(input, expectedList);
    }

    @Test
    void testWeeklyLimitNotAccepted() throws IOException {
        String customerId = "130";

        Stream<String> input = Stream.of(
                in("1", customerId, "$5000", "2026-02-16T00:00:00Z"),
                in("2", customerId, "$5000", "2026-02-17T00:00:00Z"),
                in("3", customerId, "$5000", "2026-02-18T00:00:00Z"),
                in("4", customerId, "$4000", "2026-02-19T23:59:59Z"),
                in("5", customerId, "$500", "2026-02-22T23:59:59Z"),
                in("6", customerId, "$501", "2026-02-22T23:59:59Z")
        );

        List<Response> expectedList = List.of(
                resp("1", customerId, true),
                resp("2", customerId, true),
                resp("3", customerId, true),
                resp("4", customerId, true),
                resp("5", customerId, true),
                resp("6", customerId, false)
        );

        runTest(input, expectedList);
    }

    @Test
    void testWeeklyLimitAcceptedOnNextWeek() throws IOException {
        String customerId = "131";

        Stream<String> input = Stream.of(
                in("1", customerId, "$5000", "2026-02-16T00:00:00Z"),
                in("2", customerId, "$5000", "2026-02-17T00:00:00Z"),
                in("3", customerId, "$5000", "2026-02-18T00:00:00Z"),
                in("4", customerId, "$4000", "2026-02-19T23:59:59Z"),
                in("5", customerId, "$500", "2026-02-22T23:59:59Z"),
                in("6", customerId, "$501", "2026-02-22T23:59:59Z"),
                in("7", customerId, "$501", "2026-02-23T00:00:00Z")
        );

        List<Response> expectedList = List.of(
                resp("1", customerId, true),
                resp("2", customerId, true),
                resp("3", customerId, true),
                resp("4", customerId, true),
                resp("5", customerId, true),
                resp("6", customerId, false),
                resp("7", customerId, true)
        );

        runTest(input, expectedList);
    }

    @Test
    void testComplexVelocityLimitsMultipleCustomers() throws IOException {
        String customer1 = "101"; // Tests daily load count (max 3) and daily limit ($5000)
        String customer2 = "102"; // Tests weekly limit ($20000) and rollover
        String customer3 = "103"; // Tests midnight boundary conditions

        Stream<String> input = Stream.of(
                // --- Customer 101 ---
                in("1", customer1, "$1000", "2026-02-16T10:00:00Z"),
                in("2", customer1, "$1000", "2026-02-16T11:00:00Z"),
                in("3", customer1, "$1000", "2026-02-16T12:00:00Z"),
                in("4", customer1, "$1000", "2026-02-16T13:00:00Z"), // Reject: 4th load in a day
                in("5", customer1, "$4000", "2026-02-17T10:00:00Z"), // Accept: New day
                in("6", customer1, "$1001", "2026-02-17T11:00:00Z"), // Reject: Exceeds $5000/day
                in("7", customer1, "$1000", "2026-02-17T12:00:00Z"), // Accept: Hits exactly $5000/day

                // --- Customer 102 ---
                in("8", customer2, "$5000", "2026-02-16T10:00:00Z"),
                in("9", customer2, "$5000", "2026-02-17T10:00:00Z"),
                in("10", customer2, "$5000", "2026-02-18T10:00:00Z"),
                in("11", customer2, "$4000", "2026-02-19T10:00:00Z"), // Total $19000 for the week
                in("12", customer2, "$1001", "2026-02-20T10:00:00Z"), // Reject: Exceeds $20000/week
                in("13", customer2, "$1000", "2026-02-20T11:00:00Z"), // Accept: Hits exactly $20000/week
                in("14", customer2, "$100",  "2026-02-21T10:00:00Z"), // Reject: Weekly limit maxed out
                in("15", customer2, "$5000", "2026-02-23T10:00:00Z"), // Accept: Next week starts (Monday)

                // --- Customer 103 ---
                in("16", customer3, "$4999", "2026-02-22T23:59:58Z"), // End of Sunday
                in("17", customer3, "$2",    "2026-02-22T23:59:59Z"), // Reject: Exceeds $5000/day by $1
                in("18", customer3, "$1",    "2026-02-22T23:59:59Z"), // Accept: Hits exactly $5000/day limit
                in("19", customer3, "$5000", "2026-02-23T00:00:00Z"), // Accept: New day (Monday) AND new week starts
                in("20", customer3, "$1",    "2026-02-23T00:00:01Z")  // Reject: Exceeds $5000/day
        );

        List<Response> expectedList = List.of(
                resp("1", customer1, true),
                resp("2", customer1, true),
                resp("3", customer1, true),
                resp("4", customer1, false),
                resp("5", customer1, true),
                resp("6", customer1, false),
                resp("7", customer1, true),

                resp("8", customer2, true),
                resp("9", customer2, true),
                resp("10", customer2, true),
                resp("11", customer2, true),
                resp("12", customer2, false),
                resp("13", customer2, true),
                resp("14", customer2, false),
                resp("15", customer2, true),

                resp("16", customer3, true),
                resp("17", customer3, false),
                resp("18", customer3, true),
                resp("19", customer3, true),
                resp("20", customer3, false)
        );

        runTest(input, expectedList);
    }

    private void runTest(Stream<String> input, List<Response> expectedList) throws IOException {
        int expectedNumberOfResponses = expectedList.size();

        // 1. Control input
        when(transactionReader.readAsStream()).thenReturn(input);

        // 2. Execute
        loadFundsManager.load();

        // 3. Verify
        ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);

        verify(responseWriter, times(expectedNumberOfResponses)).writeLine(responseCaptor.capture());

        List<Response> actualWrites = responseCaptor.getAllValues();

        assertEquals(expectedNumberOfResponses, actualWrites.size()); // Double check we have 4

        assertThat(actualWrites)
                .usingRecursiveComparison()
                .isEqualTo(expectedList);
    }

    private String in(String id, String customerId, String amount, String time) {
        return String.format("{\"id\":\"%s\",\"customer_id\":\"%s\",\"load_amount\":\"%s\",\"time\":\"%s\"}", id, customerId, amount, time);
    }

    private Response resp(String id, String customerId, boolean accepted) {
        return Response.builder().id(id).customerId(customerId).accepted(accepted).build();
    }
}
