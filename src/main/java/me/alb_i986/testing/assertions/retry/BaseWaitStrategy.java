package me.alb_i986.testing.assertions.retry;

public abstract class BaseWaitStrategy implements WaitStrategy {

    @Override
    public String toString() {
        return getDescription();
    }
}
