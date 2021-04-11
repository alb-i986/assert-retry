package me.alb_i986.retry;

public class RetryConfigException extends IllegalArgumentException {

    public RetryConfigException(String message) {
        super(message);
    }

    public RetryConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
