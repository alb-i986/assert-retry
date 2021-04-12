package me.alb_i986.retry;

import static java.util.Objects.requireNonNull;

public class RetryResult {

    private Object actualValue;
    private Exception supplierException;

    public RetryResult(Object actualValue) {
        this.actualValue = actualValue;
    }

    public RetryResult(Exception supplierException) {
        this.supplierException = requireNonNull(supplierException);
    }

    public Object getActualValue() {
        return actualValue;
    }

    public Exception getSupplierException() {
        return supplierException;
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
