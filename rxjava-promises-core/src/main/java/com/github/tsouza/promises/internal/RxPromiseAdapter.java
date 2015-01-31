package com.github.tsouza.promises.internal;

import com.github.tsouza.promises.Promise;
import com.github.tsouza.promises.PromiseOrValue;
import com.github.tsouza.promises.Promises;
import com.github.tsouza.promises.Value;
import com.github.tsouza.promises.functions.Callable;
import com.github.tsouza.promises.functions.Mapper;
import com.github.tsouza.promises.functions.Receiver;
import com.github.tsouza.promises.functions.Spread;

import java.util.Collection;
import java.util.List;

import static com.github.tsouza.promises.Promises.resolve;

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
    public <T extends Throwable> Promise<R> fail(Class<T> exceptionType, Mapper<T, PromiseOrValue<R>> mapper) {
        return new RxPromiseAdapter<>(rxPromise.fail((Throwable exception) -> {
            if (!exceptionType.isAssignableFrom(exception.getClass()))
                return rxPromise;
            return chain((T) exception, mapper);
        }));
    }

    @Override
    public Promise<R> fail(Mapper<Throwable, PromiseOrValue<R>> mapper) {
        return fail(Throwable.class, mapper);
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

    @Override @SuppressWarnings("unchecked")
    public <I, O> Promise<List<O>> map(Mapper<I, PromiseOrValue<O>> mapper) {
        return then(success -> {
            if (success == null) return null;
            if (success instanceof Collection)
                return Promises.map((Collection<Object>) success, mapper);
            if (success instanceof Object[])
                return Promises.map((Object[]) success, mapper);
            throw new IllegalStateException(success.getClass() + " is not a collection (can not map it)");
        });
    }

    @Override
    public <NR> Promise<NR> yield(NR result) {
        return then(success -> resolve(result));
    }

    @Override
    public <NR> Promise<NR> yield(PromiseOrValue<NR> result) {
        return then(success -> resolve(result));
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
                                                (T3) safeGet(3, input))
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
                                                (T3) safeGet(3, input),
                                                (T4) safeGet(4, input))
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
                                                (T3) safeGet(3, input),
                                                (T4) safeGet(4, input),
                                                (T5) safeGet(5, input))
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
            ((Promise) promiseOrValue).done(newPromise::fulfill, newPromise::reject);
        else if (promiseOrValue instanceof Value)
            newPromise.fulfill(((Value) promiseOrValue).get());
        else if (promiseOrValue == null)
            newPromise.fulfill(null);
        else
            newPromise.reject(new IllegalStateException("Illegal return type "+ promiseOrValue.getClass().getName()));

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

}
