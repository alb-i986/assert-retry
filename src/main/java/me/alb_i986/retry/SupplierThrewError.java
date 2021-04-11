package me.alb_i986.retry;

import java.util.List;

public class SupplierThrewError extends AssertRetryError {

    protected SupplierThrewError(Exception e, List<RetryResult> results) {
        super("an exception was thrown while retrieving the actual value", e, results);
    }
}
