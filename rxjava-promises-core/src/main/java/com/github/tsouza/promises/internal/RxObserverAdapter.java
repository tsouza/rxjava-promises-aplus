package com.github.tsouza.promises.internal;

import com.github.tsouza.promises.internal.functions.OnFinally;
import com.github.tsouza.promises.internal.functions.OnFulfilled;
import com.github.tsouza.promises.internal.functions.OnRejected;
import rx.Observable;
import rx.Observer;
import rx.functions.*;
import rx.subjects.ReplaySubject;

import java.util.concurrent.Future;

import static com.github.tsouza.promises.internal.RxObserverAdapter.State.*;

public class RxObserverAdapter<R> implements Observer<R> {

    private final ReplaySubject<R> subject;
    private final Observable<R> observable;

    private State state = PENDING;
    private R result = null;
    private Throwable reason;

    public RxObserverAdapter() {
        this(null);
    }

    private RxObserverAdapter(Observable<R> source) {
        subject = ReplaySubject.create();
        observable = subject.last();

        if (source != null)
            source.subscribe(subject);

        observable.subscribe(new StateObserver());
    }

    public boolean isFulfilled() {
        return state == State.FULFILLED;
    }

    public State getState() {
        return state;
    }

    public R getResult() {
        return result;
    }

    public Throwable getReason() {
        return reason;
    }

    public Future<R> toFuture() {
        return observable.toBlocking().toFuture();
    }

    public void fulfill(R result) {
        subject.onNext(result);
        subject.onCompleted();
    }

    public void reject(Throwable reason) {
        subject.onError(reason);
    }

    public void become(RxObserverAdapter<R> other) {
        other.subject.subscribe(this);
    }

    public <NR> RxObserverAdapter<NR> then(OnFulfilled<R, NR> onFulfilled) {
        return then(onFulfilled, null, null);
    }

    public <NR> RxObserverAdapter<NR> then(OnFulfilled<R, NR> onFulfilled, OnRejected<NR> onRejected) {
        return then(onFulfilled, onRejected, null);
    }

    public <NR> RxObserverAdapter<NR> fail(OnRejected<NR> onRejected) {
        return then(null, onRejected, null);
    }

    public <NR> RxObserverAdapter<NR> always(OnFinally<NR> onFinally) {
        return then(null, null, onFinally);
    }

    public void done(Action0 onDone) {
        then(null, null, onDone);
    }

    public <NR> RxObserverAdapter<NR> then(
            Function onFulfilled,
            Function onRejected,
            Function onFinally) {

        ChainedObserver<NR> chain = new ChainedObserver<>(
                onFulfilled, onRejected, onFinally);

        observable.subscribe(chain);

        return chain.next;
    }

    @Override
    public void onCompleted() {
        fulfill(result);
    }

    @Override
    public void onError(Throwable e) {
        reject(e);
    }

    @Override
    public void onNext(R r) {
        result = r;
    }

    class StateObserver implements Observer<R> {
        @Override
        public void onCompleted() {
            state = FULFILLED;
        }

        @Override
        public void onError(Throwable e) {
            state = REJECTED;
            reason = e;
        }

        @Override
        public void onNext(R r) {
            result = r;
        }
    }

    class ChainedObserver<NR> implements Observer<R> {

        private final Function onFulfilled;
        private final Function onRejected;
        private final Function onFinally;

        private final RxObserverAdapter<NR> next =
                new RxObserverAdapter<>();

        ChainedObserver(Function onFulfilled, Function onRejected, Function onFinally) {
            this.onFulfilled = onFulfilled;
            this.onRejected = onRejected;
            this.onFinally = onFinally;
        }

        @Override
        public void onCompleted() { evaluate(); }

        @Override
        public void onError(Throwable e) { evaluate(); }

        @Override
        public void onNext(R r) {}

        private void evaluate() {
            try {
                if (onFinally != null)
                    evaluateFinally();
                else if (FULFILLED == state)
                    evaluateFulfilled();
                else if (REJECTED == state)
                    evaluateRejected();
            } catch (Throwable e) {
                next.reject(e);
            }
        }

        @SuppressWarnings("unchecked")
        private void evaluateFinally() {
            RxObserverAdapter<?> result = callFinally();
            if (result != null)
                result.then(
                    (Action1<NR>) next::fulfill,
                    (Action1<Throwable>) next::reject,
                    null
                );
            else {
                if (FULFILLED == state)
                    next.fulfill((NR) result);
                else
                    next.reject(reason);
            }

        }

        @SuppressWarnings("unchecked")
        private void evaluateFulfilled() {
            if (onFulfilled != null)
                evalResult(callFunction(onFulfilled, result));
            else
                next.fulfill((NR) result);
        }

        private void evaluateRejected() {
            if (onRejected != null)
                evalResult(callFunction(onRejected, reason));
            else
                next.reject(reason);
        }

        private RxObserverAdapter<?> callFinally() {
            if (onFinally instanceof Func0)
                return (RxObserverAdapter<?>) ((Func0) onFinally).call();

            ((Action0) onFinally).call();
            return null;
        }

        @SuppressWarnings("unchecked")
        private <O> Object callFunction(Function function, O value) {
            if (function instanceof Action0) {
                ((Action0) function).call();
                return null;
            }

            if (function instanceof Action1<?>) {
                ((Action1<O>) function).call(value);
                return null;
            }

            if (function instanceof Func0<?>)
                return ((Func0<?>) function).call();

            if (function instanceof Func1<?, ?>)
                return ((Func1<O, ? super Object>) function).call(value);

           throw new IllegalArgumentException("Unknown type of function " + function.getClass().getName());
        }

        @SuppressWarnings("unchecked")
        private void evalResult(Object result) {
            if (result instanceof RxObserverAdapter)
                next.become((RxObserverAdapter<NR>) result);
            else
                next.fulfill((NR) result);
        }
    }

    public static enum State {
        PENDING,
        FULFILLED,
        REJECTED;
    }

}
