package com.github.tsouza.promises.functions;

public interface Reducer<I, O> {
    public O reduce(I previousValue, I currentValue) throws Throwable;
}
