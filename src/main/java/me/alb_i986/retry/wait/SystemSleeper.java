package me.alb_i986.retry.wait;

public class SystemSleeper implements Sleeper {

    @Override
    public void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
