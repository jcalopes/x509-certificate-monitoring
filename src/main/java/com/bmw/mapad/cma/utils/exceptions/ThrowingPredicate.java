package com.bmw.mapad.cma.utils.exceptions;

/**
 * Functional generic interface to deal with checked exceptions.
 */
@FunctionalInterface
public interface ThrowingPredicate<T, E extends Exception> {
    boolean apply(T t) throws E;
}