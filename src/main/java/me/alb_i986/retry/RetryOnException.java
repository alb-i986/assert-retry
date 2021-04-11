package me.alb_i986.retry;

public class RetryOnException {

    private final Class<? extends Exception> exceptionType;

    public RetryOnException(Class<? extends Exception> exceptionType) {
        this.exceptionType = exceptionType;
    }

    public boolean matches(Exception e) {
        return isOn() && exceptionType.isInstance(e);
    }

    public boolean isOn() {
        return exceptionType != null;
    }
}
