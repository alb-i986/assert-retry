package me.alb_i986.testing.assertions.retry.internal;

import org.hamcrest.Description;
import org.hamcrest.SelfDescribing;

public class AssertRetryResult<T> implements SelfDescribing {
    private T actual;
    private boolean assertionPass;
    private Exception supplierException;

    public void suppliedValue(T actual) {
        this.actual = actual;
    }

    public void supplierThrew(Exception e) {
        supplierException = e;
    }

    public void actualMatches() {
        this.assertionPass = true;
    }

    @Override
    public void describeTo(Description description) {
        if (assertionPass) {
            description.appendText("MATCH!");
        } else {
            if (actual != null) {
                description.appendValue(actual);
            } else { // supplier threw
                description.appendText("thrown " + supplierException);
            }
        }
    }
}
