package com.github.tsouza.promises.internal;

import com.github.tsouza.promises.Deferred;
import com.github.tsouza.promises.Promise;
import com.github.tsouza.promises.Resolver;
import com.github.tsouza.promises.ThreadProfile;
import com.github.tsouza.promises.functions.Receiver;
import com.github.tsouza.promises.spi.DeferredManager;
import rx.Scheduler;
import rx.schedulers.Schedulers;

public class RxDeferredManager implements DeferredManager {

    @Override
    public <R> Deferred<R> deferred() {
        return new RxDeferredAdapter<>();
    }

    @Override
    public <R> Promise<R> resolved(R value) {
        return new RxPromiseAdapter<>(value);
    }

    @Override
    public <R> Promise<R> rejected(Throwable exception) {
        return new RxPromiseAdapter<>(exception);
    }

    @Override
    public <R> void schedule(ThreadProfile profile, Receiver<Resolver<R>> receiver, Resolver<R> resolver) {
        Scheduler scheduler = getSchedulerFor(profile);
        Scheduler.Worker worker = scheduler.createWorker();
        worker.schedule(() -> {
            try {
                receiver.receive(resolver);
            } catch (Throwable e) {
                resolver.reject(e);
            } finally {
                if (!worker.isUnsubscribed()) {
                    worker.unsubscribe();
                }
            }
        });
    }

    private Scheduler getSchedulerFor(ThreadProfile profile) {
        switch (profile) {
            case CPU: return Schedulers.computation();
            case IMMEDIATE: return Schedulers.immediate();
            case IO: return Schedulers.io();
            case TRAMPOLINE: return Schedulers.trampoline();
            default: throw new InternalError();
        }
    }

}
