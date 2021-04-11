package me.alb_i986.retry.wait;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.verify;

public class SleepWaitStrategyTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private Sleeper sleeperMock;

    @Test
    public void happyPath() {
        SleepWaitStrategy sut = new SleepWaitStrategy(100, sleeperMock);

        sut.runWait();

        verify(sleeperMock).sleep(100);
        assertThat(sut.getDescription(), equalTo("sleep for 100ms"));
    }
}