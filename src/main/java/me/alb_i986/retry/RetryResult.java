package me.alb_i986.retry;

import me.alb_i986.retry.utils.Throwables;
import org.hamcrest.Description;
import org.hamcrest.SelfDescribing;

import static java.util.Objects.requireNonNull;

public class RetryResult implements SelfDescribing {

    private Object actualValue;
    private Exception supplierException;

    public RetryResult(Object actualValue) {
        this.actualValue = actualValue;
    }

    public RetryResult(Exception supplierException) {
        this.supplierException = requireNonNull(supplierException);
    }

    @Override
    public void describeTo(Description description) {
        if (supplierException != null) {
            description.appendText("thrown ")
                    .appendText(Throwables.getTrimmedStackTrace(supplierException)); //TODO improve stacktrace formatting (well tabbed!)
        } else {
            description.appendValue(actualValue);
        }
    }

    @Override
    public String toString() {
        if (supplierException != null) {
            return supplierException.toString();
        } else {
            return String.valueOf(actualValue);
        }
    }
}
