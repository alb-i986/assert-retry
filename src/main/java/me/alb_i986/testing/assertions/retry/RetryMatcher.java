package me.alb_i986.testing.assertions.retry;

import me.alb_i986.testing.assertions.retry.internal.AssertRetryResult;
import me.alb_i986.testing.assertions.retry.internal.TimeFormatter;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * A {@link Matcher} allowing for making assertions <i>with tolerance</i>,
 * i.e. assertions which may fail a number of times but <i>eventually</i> pass.
 * <p>
 * Typically useful when testing async systems like message queues.
 * <p>
 * Can be configured to:
 * <ul>
 *     <li>retry for maximum a fixed number of times</li>
 *     <li>retry for a maximum number of seconds</li>
 *     <li>set the strategy for waiting between attempts, e.g. sleep, or, better, to check some value</li>
 *     <li>whether to retry in case the Supplier throws</li>
 * </ul>
 *
 * @param <T> the type of the actual values we are gonna test
 */
public class RetryMatcher<T> extends TypeSafeMatcher<Supplier<? extends T>> {

    private static final Logger logger = LoggerFactory.getLogger(RetryMatcher.class);

    private final Matcher<? super T> matcher;
    private final RetryConfig config;
    private final TimeFormatter timeFormatter;


    private List<AssertRetryResult> retryResults = new ArrayList<>();

    private FailureReason failureReason;

    RetryMatcher(Matcher<? super T> matcher, RetryConfig config) {
        this.matcher = matcher;
        this.config = config;
        this.timeFormatter = TimeFormatter.SINGLETON;
    }

    @Override
    protected boolean matchesSafely(Supplier<? extends T> actualValuesSupplier) {
        config.getTimeout().restart();

        while (true) {
            AssertRetryResult result = new AssertRetryResult();
            retryResults.add(result);
            try {
                T actual = actualValuesSupplier.get();
                result.suppliedValue(actual);

                if (matcher.matches(actual)) { // assertion PASSED!
                    logger.debug("The actual value supplied MATCHED: {}", actual);
                    result.actualMatches();
                    return true;
                }
                logger.debug("The actual value supplied did not match: {}", actual);
            } catch (Exception e) {
                logger.debug("The Supplier threw", e);
                result.supplierThrew(e);
                if (!config.isRetryOnException()) {
                    failureReason = FailureReason.SUPPLIER_THREW;
                    return false;
                }

                // TODO configurable type of exception to retry on
//                if (!config.getRetryException().isAssignableFrom(e.getClass())) {
//                    throw e;
//                }
            }

            if (config.getTimeout().isExpired()) {
                failureReason = FailureReason.TIMEOUT_EXPIRED;
                return false;
            }
            logger.debug("The timeout has not expired yet: we're gonna wait before trying again. {}", config.getWaitStrategy());

            try {
                config.getWaitStrategy().waitt();
            } catch (Exception e) {
                // continue with the next attempt
                logger.debug("The WaitStrategy threw: we'll try again NOW", e);
            }
        }
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("supplied values to *eventually* match ")
                .appendDescriptionOf(matcher)
                .appendText(" within " + timeFormatter.prettyPrint(
                        config.getTimeout().getDuration()));
    }

    @Override
    protected void describeMismatchSafely(Supplier<? extends T> item, Description mismatchDescription) {
        mismatchDescription.appendText(failureReason.getDescription())
                .appendText(System.lineSeparator())
                .appendText("          Actual values (in order of appearance):");

        for (AssertRetryResult retryResult : retryResults) {
            mismatchDescription.appendText(System.lineSeparator());
            mismatchDescription.appendText("           - ");
            mismatchDescription.appendDescriptionOf(retryResult);
        }
    }

    private enum FailureReason {
        TIMEOUT_EXPIRED("The timeout was reached and none of the actual values matched"),
        SUPPLIER_THREW("The Supplier threw"),
        ;

        private final String description;

        FailureReason(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public static <T> Matcher<Supplier<? extends T>> eventually(Matcher<? super T> matcher, RetryConfigBuilder retryConfigBuilder) {
        return new RetryMatcher<>(matcher, retryConfigBuilder.build());
    }

    /**
     * Allows to verify that the actual value returned by the {@code Supplier} <i>eventually</i>
     * satisfies the condition specified by the given {@code Matcher},
     * within the configured timeout.
     * In case the condition is never true, <i>all of</i> the actual values which did not match
     * will be reported in the AssertionError thrown.
     * <p>
     * Let's look at an example.
     * Say that we have a JMS queue, and we need to verify that a message with body "expected content"
     * is published on the queue.
     * Given the async nature of the system, we need to employ a bit of tolerance in our assertions.
     *
     * <pre>
     * MessageConsumer consumer = session.createConsumer(queue);
     * connection.start();
     * Supplier&lt;String&gt; messageText = new Supplier&lt;&gt;() {
     *   {@literal @}Override
     *    public String get() throws JMSException {
     *       TextMessage m = (TextMessage) consumer.receiveNoWait();  // polling for messages, non blocking
     *       return m == null ? null : m.getText();
     *    }
     * };
     *
     * assertThat(messageText, eventually(containsString("expected content"),
     *         RetryConfig.builder()
     *             .timeoutAfter(Duration.ofSeconds(60))
     *             .sleepFor(Duration.ofSeconds(5))
     *             .retryOnException(true)));
     * </pre>
     *
     * The first few lines set up the supplier of the actual values,
     * which will be used to poll the message queue for messages.
     * Then we have our assertion.
     * In this example it is asserting that the expected text message will be received within 60 seconds.
     * After each failing attempt, it will wait for 5s, and then it will try again.
     * If {@code consumer.receiveNoWait()} throws a JMSException, the assertion will be re-tried,
     * as if it returned a non-matching value.
     * Finally, if the timeout is reached and the actual value never matched,
     * an AssertionError similar to the following will be thrown:
     * <pre>
     * java.lang.AssertionError:
     * Expected: supplied values to *eventually* match a string containing "expected content" within 60s
     *      but: The timeout was reached and none of the actual values matched
     *           Actual values (in order of appearance):
     *            - thrown javax.jms.MessageFormatException: Blah blah
     *            - "some content"
     *            - "some other content"
     * </pre>
     *
     * <h3>Configuration</h3>
     * The retry mechanism can be configured in terms of:
     * <ul>
     *     <li>the timeout: {@link RetryConfigBuilder#timeoutAfter(Duration)}</li>
     *     <li>how long to sleep for before retrying: {@link RetryConfigBuilder#sleepFor(Duration)}</li>
     *     <li>or, as an alternative, a custom wait strategy: {@link RetryConfigBuilder#waitStrategy(WaitStrategy)}</li>
     *     <li>whether to retry in case the {@code Supplier} throws: {@link RetryConfigBuilder#retryOnException(boolean)}</li>
     * </ul>
     *
     * As shown in the example above, {@link RetryConfig#builder()}
     * provides access to the Builder API for configuring the retry mechanism.
     *
     * @param <T> the type of the actual values
     *
     * @param matcher a Hamcrest matcher, encapsulating the condition under which the actual value is as expected
     * @param retryConfig athe configuration of the retry mechanism
     *
     * @see RetryConfigBuilder
     */
    public static <T> Matcher<Supplier<? extends T>> eventually(Matcher<? super T> matcher, RetryConfig retryConfig) {
        return new RetryMatcher<>(matcher, retryConfig);
    }
}
