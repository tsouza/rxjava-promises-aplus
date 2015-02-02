/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 - Thiago Souza <tcostasouza@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.tsouza.promises.internal;

import com.github.tsouza.promises.Promise;
import com.github.tsouza.promises.PromiseOrValue;
import com.github.tsouza.promises.Value;
import com.github.tsouza.promises.functions.Callable;
import com.github.tsouza.promises.functions.Mapper;
import com.github.tsouza.promises.functions.Receiver;
import com.github.tsouza.promises.functions.Spread;

import java.util.Collection;
import java.util.concurrent.*;

public class RxPromiseAdapter<R> implements Promise<R> {

    private final RxObserverAdapter<R> rxPromise;

    public RxPromiseAdapter(R value) {
        rxPromise = new RxObserverAdapter<>();
        rxPromise.fulfill(value);
    }

    public RxPromiseAdapter(Throwable exception) {
        rxPromise = new RxObserverAdapter<>();
        rxPromise.reject(exception);
    }

    public RxPromiseAdapter(RxObserverAdapter<R> rxPromise) {
        this.rxPromise = rxPromise;
    }

    @Override @SuppressWarnings("unchecked")
    public <NR> Promise<NR> then(Mapper<R, PromiseOrValue<NR>> mapper) {
        return new RxPromiseAdapter<>(rxPromise.then((R r) -> chain(r, mapper)));
    }

    @Override @SuppressWarnings("unchecked")
    public <NR> Promise<NR> then(Mapper<R, PromiseOrValue<NR>> success, Mapper<Throwable, PromiseOrValue<R>> failure) {
        return new RxPromiseAdapter<>(rxPromise.then(
                (R r) -> chain(r, success),
                (Throwable e) -> chain(e, failure)
        ));
    }

    @Override @SuppressWarnings("unchecked")
    public <T extends Throwable> Promise<R> fail(Class<T> exceptionType, Mapper<T, PromiseOrValue<R>> mapper) {
        return new RxPromiseAdapter<>(rxPromise.fail((Throwable exception) -> {
            if (!exceptionType.isAssignableFrom(exception.getClass()))
                return rxPromise;
            return chain((T) exception, mapper);
        }));
    }

    @Override @SuppressWarnings({"unchecked","rawtypes"})
    public Promise<R> tap(Mapper<R, Promise<?>> mapper) {
        return new RxPromiseAdapter<>(rxPromise.then((R r) -> {
            Promise<?> tapPromise = null;
            try {
                tapPromise = mapper.map(r);
            } catch (Throwable e) {
                rxPromise.reject(e);
                return rxPromise;
            }
            if (tapPromise == null)
                return rxPromise;

            RxObserverAdapter newPromise = new RxObserverAdapter();
            tapPromise.done(newPromise::fulfill, newPromise::reject);
            return newPromise;
        }));
    }
    @Override
    public Promise<R> always(Callable<Promise<R>> callable) {
        return new RxPromiseAdapter<>(rxPromise.always(() -> {
            Promise<?> finallyPromise = null;
            try {
                finallyPromise = callable.call();
            } catch (Throwable e) {
                rxPromise.reject(e);
            }
            if (finallyPromise == null)
                return rxPromise;
            RxObserverAdapter<R> newPromise = new RxObserverAdapter<>();
            finallyPromise.done(success -> newPromise.fulfill(rxPromise.getResult()),
                    newPromise::reject);
            return newPromise;
        }));
    }

    @Override @SuppressWarnings("unchecked")
    public <T1, T2, NR> Promise<NR> spread(Spread.Args2<T1, T2, PromiseOrValue<NR>> spread) {
        return new RxPromiseAdapter<>(rxPromise.then((R r) ->
                        chain(coerceToArray(r), input ->
                                        spread.call(
                                                (T1) safeGet(0, input),
                                                (T2) safeGet(1, input))
                        )
        ));
    }

    @Override @SuppressWarnings("unchecked")
    public <T1, T2, T3, NR> Promise<NR> spread(Spread.Args3<T1, T2, T3, PromiseOrValue<NR>> spread) {
        return new RxPromiseAdapter<>(rxPromise.then((R r) ->
                        chain(coerceToArray(r), input ->
                                        spread.call(
                                                (T1) safeGet(0, input),
                                                (T2) safeGet(1, input),
                                                (T3) safeGet(2, input))
                        )
        ));
    }

    @Override @SuppressWarnings("unchecked")
    public <T1, T2, T3, T4, NR> Promise<NR> spread(Spread.Args4<T1, T2, T3, T4, PromiseOrValue<NR>> spread) {
        return new RxPromiseAdapter<>(rxPromise.then((R r) ->
                        chain(coerceToArray(r), input ->
                                        spread.call(
                                                (T1) safeGet(0, input),
                                                (T2) safeGet(1, input),
                                                (T3) safeGet(2, input),
                                                (T4) safeGet(3, input))
                        )
        ));
    }

    @Override @SuppressWarnings("unchecked")
    public <T1, T2, T3, T4, T5, NR> Promise<NR> spread(Spread.Args5<T1, T2, T3, T4, T5, PromiseOrValue<NR>> spread) {
        return new RxPromiseAdapter<>(rxPromise.then((R r) ->
                        chain(coerceToArray(r), input ->
                                        spread.call(
                                                (T1) safeGet(0, input),
                                                (T2) safeGet(1, input),
                                                (T3) safeGet(2, input),
                                                (T4) safeGet(3, input),
                                                (T5) safeGet(4, input))
                        )
        ));
    }

    @Override
    public void done(Receiver<R> success, Receiver<Throwable> failure) {
        rxPromise.done(() -> {
            try {
                if (rxPromise.isFulfilled())
                    try {
                        success.receive(rxPromise.getResult());
                    } catch (Throwable e) {
                        if (failure != null) failure.receive(e);
                    }
                else if (failure != null)
                    failure.receive(rxPromise.getReason());
            } catch (RuntimeException | Error e) {
                throw e;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void done(Receiver<R> success) {
        done(success, null);
    }

    @Override
    public Future<R> future() {
        CountDownLatch latch = new CountDownLatch(1);
        rxPromise.done(latch::countDown);
        return new FutureAdapter(latch);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private <I> RxObserverAdapter chain(I input, Mapper<I, ?> mapper) {
        RxObserverAdapter newPromise = new RxObserverAdapter<>();
        Object promiseOrValue;
        try {
            promiseOrValue = mapper.map(input);
        } catch (Throwable e) {
            newPromise.reject(e);
            return newPromise;
        }

        if (promiseOrValue instanceof Promise)
            ((Promise<?>) promiseOrValue).done(newPromise::fulfill, newPromise::reject);
        else if (promiseOrValue instanceof Value)
            newPromise.fulfill(((Value) promiseOrValue).get());
        else
            newPromise.fulfill(promiseOrValue);

        return newPromise;
    }

    @SuppressWarnings("unchecked")
    private Object[] coerceToArray(Object value) {
        if (value == null) return new Object[0];
        if (value instanceof Object[]) return (Object[]) value;
        if (value instanceof Collection) return ((Collection<Object>) value).toArray();
        return new Object[] { value };
    }

    private Object safeGet(int idx, Object[] array) {
        return (array == null || idx > array.length) ? null : array[idx];
    }

    class FutureAdapter implements Future<R> {
        private final CountDownLatch latch;

        public FutureAdapter(CountDownLatch latch) {
            this.latch = latch;
        }
        @Override
        public boolean isDone() {
            return rxPromise.isDone();
        }

        @Override
        public synchronized R get() throws InterruptedException, ExecutionException {
            if (isDone())
                return getResult();
            latch.await();
            return getResult();
        }

        @Override
        public R get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            if (isDone())
                return getResult();
            latch.await(timeout, unit);
            return getResult();
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            throw new UnsupportedOperationException("promises are not cancellable");
        }

        private R getResult() throws ExecutionException {
            if (isDone()) {
                if (rxPromise.getReason() != null)
                    throw new ExecutionException(rxPromise.getReason());
                return rxPromise.getResult();
            }
            return null;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }
    }
}
