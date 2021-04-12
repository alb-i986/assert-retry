package me.alb_i986.retry.hamcrest;

import me.alb_i986.retry.AssertRetry;
import me.alb_i986.retry.AssertRetryError;
import me.alb_i986.retry.RetryConfig;
import me.alb_i986.retry.RetryResult;
import me.alb_i986.retry.TimeFormatter;
import me.alb_i986.retry.utils.Throwables;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.SelfDescribing;
import org.hamcrest.TypeSafeMatcher;

import java.util.function.Supplier;

/**
 * A Hamcrest {@link Matcher} allowing to write assertions with tolerance,
 * i.e. assertions which may fail a number of times,
 * but eventually are expected to pass.
 * <p>
 * A typical use case is for testing async systems like message queues.
 * </p>
 *
 * @param <T> the type of the actual values we are going to test
 * @see #eventually(Matcher)
 * @since 1.0
 */
public class RetryMatcher<T> extends TypeSafeMatcher<Supplier<? extends T>> {

    /**
     * <p>A Hamcrest {@link Matcher} allowing to write assertions with tolerance,
     * i.e. assertions which may fail a number of times but <i>eventually</i>
     * are expected to pass.
     * A typical use case is for testing async systems like message queues.
     * </p>
     *
     * <p>
     * It allows to verify that the actual value returned by the {@code Supplier}
     * <i>eventually</i> satisfies the condition specified by the given {@code Matcher},
     * within the configured timeout.
     * In case the condition is never true, <i>all of</i> the actual values which did not match
     * will be reported in the AssertionError thrown.
     * </p>
     *
     * <p>
     * Let's look at an example.
     * Say that we have a JMS queue, and we need to verify that a message
     * with body "expected content" is published on the queue.
     * Given the async nature of the system, we need to employ a bit of tolerance in our assertion.
     * </p>
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
     * assertThat(messageText, eventually(containsString("expected content"))
     *         .within(60, ChronoUnit.SECONDS)
     *         .sleepForSeconds(5)
     *         .ignoring(JMSException.class));
     * </pre>
     * <p>
     * The first few lines set up the supplier of the actual values,
     * which will be used to poll the message queue for messages.
     * Then we have our assertion.
     * In this example it is asserting that the expected text message will be received within 60 seconds.
     * After each failing attempt, it will wait for 5s, and then it will try again.
     * If {@code consumer.receiveNoWait()} throws a JMSException, the assertion will be re-tried,
     * as if it returned a non-matching value.
     * Finally, if the timeout expired and the actual value never matched,
     * an AssertionError similar to the following will be thrown:
     * <pre>
     * java.lang.AssertRetryError:
     * Expected: supplied value to *eventually* match a string containing "expected content" within 60s
     *      but: The timeout expired and none of the actual values matched
     *           Actual results (in order of appearance):
     *            1. thrown javax.jms.MessageFormatException: Blah blah
     *            2. "some content"
     *            3. "some other content"
     * </pre>
     * <p>
     * For more details on the configuration options, please see {@link RetryMatcherBuilder}.
     *
     * @param <T>     the type of the actual values
     * @param matcher a Hamcrest matcher, encapsulating the condition under which the actual value is as expected
     * @return a {@link RetryMatcherBuilder}, allowing to configure the retry mechanism
     * @see RetryMatcherBuilder
     * @since 1.0
     */
    public static <T> RetryMatcherBuilder<T> eventually(Matcher<? super T> matcher) {
        return new RetryMatcherBuilder<>(matcher);
    }

    private final Matcher<? super T> matcher;
    private final RetryConfig config;
    private final TimeFormatter timeFormatter;

    private AssertRetryError retryError;

    protected RetryMatcher(Matcher<? super T> matcher, RetryConfig config) {
        this.matcher = matcher;
        this.config = config;
        this.timeFormatter = TimeFormatter.SINGLETON;
    }

    @Override
    protected boolean matchesSafely(Supplier<? extends T> actualValuesSupplier) {
        try {
            new AssertRetry<>(config)
                    .doAssert(actualValuesSupplier, matcher::matches);
            return true;
        } catch (AssertRetryError e) {
            retryError = e;
            return false;
        }
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("supplied value to *eventually* match ")
                .appendDescriptionOf(matcher)
                .appendText(" within ")
                .appendText(timeFormatter.prettyPrint(config.timeout().getDuration()));
    }

    @Override
    protected void describeMismatchSafely(Supplier<? extends T> item, Description mismatchDescription) {
        mismatchDescription.appendText(retryError.getReason())
                .appendText(System.lineSeparator())
                .appendText("          Actual results (in order of appearance):");

        for (int i = 0; i < retryError.getResults().size(); i++) {
            mismatchDescription.appendText(System.lineSeparator())
                    .appendText("           ")
                    .appendText(String.valueOf(i + 1))
                    .appendText(". ")
                    .appendDescriptionOf(new SelfDescribingResult(retryError.getResults().get(i)));
        }
    }

    private static class SelfDescribingResult implements SelfDescribing {

        private final RetryResult result;
    
        public SelfDescribingResult(RetryResult result) {
            this.result = result;
        }

        @Override
        public void describeTo(Description description) {
            if (result.getSupplierException() != null) {
                description.appendText("thrown ")
                        .appendText(Throwables.getTrimmedStackTrace(result.getSupplierException())); //TODO improve stacktrace formatting (well tabbed!)
            } else {
                description.appendValue(result.getActualValue());
            }
        }
    }
}
