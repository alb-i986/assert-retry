package me.alb_i986.retry;

import org.junit.Test;

import java.time.temporal.ChronoUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThrows;
import static org.junit.internal.matchers.ThrowableMessageMatcher.hasMessage;

public class RetryConfigBuilderTest {

    private final RetryConfigBuilder sut = new RetryConfigBuilder();

    @Test
    public void cannotBuildWithoutTimeout() {
        RetryConfigException expected = assertThrows(RetryConfigException.class, () ->
                sut.sleepForMillis(5)
                        .build());
        assertThat(expected, hasMessage(containsString("timeout")));
    }

    @Test
    public void cannotBuildWithoutWaitStrategy() {
        RetryConfigException expected = assertThrows(RetryConfigException.class, () ->
                sut.within(5, ChronoUnit.SECONDS)
                        .build());
        assertThat(expected, hasMessage(containsString("wait")));
    }

    @Test
    public void shouldProvideHelpfulErrorMessageListingAllMandatoryParamsMissing() {
        RetryConfigException expected = assertThrows(RetryConfigException.class, () ->
                sut.build());
        assertThat(expected, hasMessage(equalTo("One or more mandatory parameters have not been set:\n" +
                "\t- timeout: did you call within()?\n" +
                "\t- wait strategy: please call one of sleepForMillis(), sleepForSeconds(), or withWaitStrategy()")));
    }

    @Test
    public void sleepForSecondsOverflowing() {
        RetryConfigException expected = assertThrows(RetryConfigException.class, () ->
                sut.sleepForSeconds(Long.MAX_VALUE));
        assertThat(expected, hasMessage(equalTo("Please set a lower sleep time. Max=9223372036854775s")));
    }
}