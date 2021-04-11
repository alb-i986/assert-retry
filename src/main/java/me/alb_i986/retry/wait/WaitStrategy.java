package me.alb_i986.retry.wait;

public interface WaitStrategy {

    void runWait();

    /**
     * @return a description of the strategy, to make logs meaningful.
     * e.g. "waiting for a message to be published on the queue myQueue".
     */
    String getDescription();
}
