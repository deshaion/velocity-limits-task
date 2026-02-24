package ca.bank.velocitylimitsapp;

import ca.bank.velocitylimitsapp.service.LoadFundsManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class VelocityLimitsRunnerTest {

    @Mock
    private LoadFundsManager loadFundsManager;

    @InjectMocks
    private VelocityLimitsRunner runner;

    @Test
    void testRun() throws Exception {
        runner.run();
        verify(loadFundsManager).load();
    }
}
