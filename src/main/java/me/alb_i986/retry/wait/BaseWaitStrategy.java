package me.alb_i986.retry.wait;

public abstract class BaseWaitStrategy implements WaitStrategy {

    @Override
    public String toString() {
        return getDescription();
    }
}
