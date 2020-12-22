package me.alb_i986.testing.assertions.retry;

public interface WaitStrategy {

    void runWait() throws Exception;

    String getDescription();
}
