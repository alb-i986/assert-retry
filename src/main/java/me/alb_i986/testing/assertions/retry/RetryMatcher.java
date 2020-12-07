package me.alb_i986.testing.assertions.retry;

import me.alb_i986.testing.assertions.retry.internal.RetryConfig;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.hamcrest.TypeSafeMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class RetryMatcher<T> extends TypeSafeMatcher<Supplier<T>> {

    private final Matcher<T> matcher;
    private final RetryConfig config;

    private List<T> suppliedValues = new ArrayList<>();
    private List<StringDescription> mismatchDescriptions = new ArrayList<>();
    private List<Exception> exceptions = new ArrayList<>();
    private FailureReason failureReason;
    private int i;

    @SuppressWarnings("deprecation")
    public RetryMatcher(Matcher<T> matcher) {
        this(matcher, new RetryConfigBuilder().build());
    }

    public RetryMatcher(Matcher<T> matcher, RetryConfig config) {
        this.matcher = matcher;
        this.config = config;
    }

    @Override
    protected boolean matchesSafely(Supplier<T> actualValuesSupplier) {
        config.getTimeout().restart();

        for (i = 1; i <= config.getMaxAttempts(); i++) { // i starts from 1
            try {
                T actual = actualValuesSupplier.get();
                suppliedValues.add(actual);

                if (matcher.matches(actual)) { // assertion PASSED!
                    return true;
                }

                StringDescription mismatchDescription = new StringDescription();
                matcher.describeMismatch(actual, mismatchDescription);
                mismatchDescriptions.add(mismatchDescription);

                if (config.getTimeout().isExpired()) {
                    failureReason = FailureReason.TIMEOUT_EXPIRED;
                    return false;
                }
                if (!isLastIteration()) {
                    config.getWaitStrategy().run();
                }
            } catch (Exception e) {
                exceptions.add(e);
                if (!config.isRetryOnException()) {
                    failureReason = FailureReason.SUPPLIER_THREW;
                    return false;
                }

                // TODO configurable type of exception to retry on
//                if (!config.getRetryException().isAssignableFrom(e.getClass())) {
//                    throw e;
//                }

                if (config.getTimeout().isExpired()) {
                    failureReason = FailureReason.TIMEOUT_EXPIRED;
                    return false;
                }
                config.getWaitStrategy().run();
            }
        }
        failureReason = FailureReason.NO_ACTUAL_VALUE_MATCHED;
        return false;
    }

    private boolean isLastIteration() {
        return i < config.getMaxAttempts();
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("supplied values to *eventually* match ")
                .appendDescriptionOf(matcher);
    }

    @Override
    protected void describeMismatchSafely(Supplier<T> item, Description mismatchDescription) {
        mismatchDescription.appendText(failureReason.getDescription())
                .appendText(String.format(" after %d/%d attempts", i - 1, config.getMaxAttempts()))
                .appendText(System.lineSeparator())
                .appendText("    Actual values (in order of appearance):");

        for (T supplied : suppliedValues) {
            mismatchDescription.appendText(System.lineSeparator());
            mismatchDescription.appendText("         - ");
            mismatchDescription.appendValue(supplied);
        }

    }

    private enum FailureReason {
        TIMEOUT_EXPIRED("Timeout expired"),
        SUPPLIER_THREW("Supplier threw"),
        NO_ACTUAL_VALUE_MATCHED("None of the actual values supplied matched"),
        ;

        private final String description;

        FailureReason(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
