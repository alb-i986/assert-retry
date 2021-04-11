package me.alb_i986.retry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * The core of the retry assertion.
 * <p>
 * It does not depend on Hamcrest by design, therefore it might also be included
 * in projects which do not use Hamcrest.
 * </p>
 *
 * @param <T> the type of the actual values we are going to test
 */
public class AssertRetry<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssertRetry.class);

    private final Logger log;
    private final RetryConfig config;

    public AssertRetry(RetryConfig config) {
        this(config, LOGGER);
    }

    /**
     * For testing purposes only.
     */
    protected AssertRetry(RetryConfig config, Logger log) {
        this.config = config;
        this.log = log;
    }

    public void doAssert(Supplier<? extends T> supplier, Predicate<? super T> predicate) {
        int i = 0;
        List<RetryResult> retryResults = new ArrayList<>();

        config.timeout().start();
        while (true) {
            i++;
            T actual = null;
            try {
                actual = supplier.get();
            } catch (Exception e) {
                log.info("Attempt #{} FAILED. An exception was thrown by the Supplier", i, e);
                retryResults.add(new RetryResult(e));
                if (!config.getRetryOnException().isOn()) {
                    throw new SupplierThrewError(e, retryResults);
                }
                if (!config.getRetryOnException().matches(e)) {
                    throw new SupplierThrewError(e, retryResults);
                }
            }

            if (actual != null) { // supplier did not throw
                if (predicate.test(actual)) { // assertion PASSED!
                    retryResults.add(new RetryResult(actual));
                    log.info("Assertion eventually PASSED after {} attempts. Actual values: {}", i, retryResults);
                    return;
                } else {
                    retryResults.add(new RetryResult(actual));
                    log.info("Attempt #{} FAILED: the value did not match. Actual: {}", i, actual); //TODO new PrettyPrinter().printValue(actual));
                }
            }

            if (config.timeout().isExpired()) {
                throw new AssertRetryTimeoutError(config.timeout().getDuration(), retryResults);
            }

            log.debug("About to wait: {}", safeToString(config.getWaitStrategy()));
            try {
                config.getWaitStrategy().runWait();
            } catch (Exception e) {
                log.warn("WaitStrategy failed. About to run the next attempt.", e);
            }
        }
    }

    // patch protecting ourselves from custom WaitStrategies not well implemented
    private static String safeToString(Object o) {
        try {
            return String.valueOf(o);
        } catch (Exception e) {
            return o.getClass().getName() + "@" + Integer.toHexString(o.hashCode());
        }
    }
}
