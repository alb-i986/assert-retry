package me.alb_i986.testing.assertions.retry;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class Suppliers {

    /**
     * Generate an ascending sequence of integers, starting from the given integer.
     *
     * @param initialInt the first element of the sequence
     */
    public static Supplier<Integer> ascendingIntegersStartingFrom(int initialInt) {
        return new AscendingIntegersSupplier(initialInt);
    }

    /**
     * Generate a descending sequence of integers, starting from the given integer.
     *
     * @param initialInt the first element of the sequence
     */
    public static Supplier<Integer> descendingIntegers(int initialInt) {
        return new Supplier<Integer>() {
            private int i = initialInt;

            @Override
            public Integer get() {
                return i--;
            }
        };
    }

    public static <T> Supplier<T> fromList(T... values) {
        return fromList(Arrays.asList(values));
    }
    public static <T> Supplier<T> fromList(List<T> values) {
        return new Supplier<T>() {
            private int i = 0;

            @Override
            public T get() {
                return values.get(i++);
            }
        };
    }

    /**
     * A Supplier returning always the same value, the one given as argument.
     */
    public static <T> Supplier<T> id(T value) {
        return () -> value;
    }

    public static Supplier<String> throwing() {
        return new ThrowingSupplier();
    }

    private static class AscendingIntegersSupplier implements Supplier<Integer> {
        private int i;

        public AscendingIntegersSupplier(int initialInt) {
            i = initialInt;
        }

        @Override
        public Integer get() {
            return i++;
        }
    }

    private static class ThrowingSupplier implements Supplier<String> {
        private int i;

        @Override
        public String get() {
            throw new RuntimeException("Supplier failed");
        }
    }
}
