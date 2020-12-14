package me.alb_i986.testing.assertions.retry.internal;

import org.junit.Test;

import java.time.Duration;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class TimeFormatterTest {
    
    @Test
    public void print_0ms() {
        String print = TimeFormatter.SINGLETON.prettyPrint(Duration.ofMillis(0));
        assertThat(print, is("0ms"));
    }

    @Test
    public void print_999ms() {
        String print = TimeFormatter.SINGLETON.prettyPrint(Duration.ofMillis(999));
        assertThat(print, is("999ms"));
    }

    @Test
    public void print_1s_1ms() {
        String print = TimeFormatter.SINGLETON.prettyPrint(
                Duration.ofSeconds(1).plusMillis(1));
        assertThat(print, is("1s 1ms"));
    }

    @Test
    public void print_1m() {
        String print = TimeFormatter.SINGLETON.prettyPrint(Duration.ofMinutes(1));
        assertThat(print, is("1m"));
    }

    @Test
    public void print_1m_1s() {
        String print = TimeFormatter.SINGLETON.prettyPrint(
                Duration.ofMinutes(1).plusSeconds(1));
        assertThat(print, is("1m 1s"));
    }

    @Test
    public void print_1m_1ms() {
        String print = TimeFormatter.SINGLETON.prettyPrint(
                Duration.ofMinutes(1).plusMillis(1));
        assertThat(print, is("1m 1ms"));
    }

    @Test
    public void print_1m_1s_1ms() {
        String print = TimeFormatter.SINGLETON.prettyPrint(
                Duration.ofMinutes(1).plusSeconds(1).plusMillis(1));
        assertThat(print, is("1m 1s 1ms"));
    }

    @Test
    public void print_1h() {
        String print = TimeFormatter.SINGLETON.prettyPrint(Duration.ofHours(1));
        assertThat(print, is("1h"));
    }

    @Test
    public void print_1h_1s() {
        String print = TimeFormatter.SINGLETON.prettyPrint(
                Duration.ofHours(1).plusSeconds(1));
        assertThat(print, is("1h 1s"));
    }

    @Test
    public void print_1h_1m_1s() {
        String print = TimeFormatter.SINGLETON.prettyPrint(
                Duration.ofHours(1).plusMinutes(1).plusSeconds(1));
        assertThat(print, is("1h 1m 1s"));
    }

    @Test
    public void print_1day() {
        String print = TimeFormatter.SINGLETON.prettyPrint(Duration.ofDays(1));
        assertThat(print, is("24h"));
    }
}