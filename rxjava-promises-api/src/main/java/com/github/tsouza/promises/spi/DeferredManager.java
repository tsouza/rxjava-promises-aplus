package com.github.tsouza.promises.spi;

import com.github.tsouza.promises.Deferred;
import com.github.tsouza.promises.Promise;
import com.github.tsouza.promises.Resolver;
import com.github.tsouza.promises.ThreadProfile;
import com.github.tsouza.promises.functions.Receiver;

public interface DeferredManager {
    public <R> Deferred<R> deferred();
    public <R> Promise<R> resolved(R value);
    public <R> Promise<R> rejected(Throwable exception);
    public <R> void schedule(ThreadProfile profile, Receiver<Resolver<R>> receiver, Resolver<R> resolver);
}
