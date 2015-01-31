package com.github.tsouza.promises.internal;

import com.github.tsouza.promises.Deferred;
import com.github.tsouza.promises.Promise;
import com.github.tsouza.promises.Resolver;

public class RxDeferredAdapter<R> implements Deferred<R>, Resolver<R> {

    private final RxObserverAdapter<R> rxPromise = new RxObserverAdapter<>();

    @Override
    public Promise<R> promise() {
        return new RxPromiseAdapter<>(rxPromise);
    }

    @Override
    public Resolver<R> resolver() {
        return this;
    }

    @Override
    public void resolve(R result) {
        rxPromise.fulfill(result);
    }

    @Override
    public void reject(Throwable exception) {
        rxPromise.reject(exception);
    }

    @Override
    public void chain(Promise<R> promise) {
        promise.done(this::resolve, this::reject);
    }
}
