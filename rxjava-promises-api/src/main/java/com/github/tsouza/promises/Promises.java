package com.github.tsouza.promises;


import com.github.tsouza.promises.functions.Callable;
import com.github.tsouza.promises.functions.Mapper;
import com.github.tsouza.promises.functions.Receiver;
import com.github.tsouza.promises.spi.DeferredManager;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Promises {

	public static <R> Promise<R> resolve() {
		return resolve((Object) null);
	}

	@SuppressWarnings("unchecked")
	public static <R> Promise<R> resolve(Object promiseOrValue) {
		if (promiseOrValue instanceof PromiseOrValue)
			return resolve((PromiseOrValue<R>) promiseOrValue);

		return manager.resolved((R) promiseOrValue);
	}

	public static <R> Promise<R> resolve(PromiseOrValue<R> promiseOrValue) {
		if (promiseOrValue instanceof Promise)
			return (Promise<R>) promiseOrValue;

		if (promiseOrValue instanceof Value)
			return resolve(((Value<R>) promiseOrValue).get());

		throw new InternalError();
	}

	public static <R> Promise<R> resolve(Future<R> future) {
		return resolve(future, ThreadProfile.IO);
	}

	public static <R> Promise<R> resolve(Future<R> future, ThreadProfile profile) {
		return defer(resolver -> {
			try {
				resolver.resolve(future.get());
			} catch (ExecutionException e) {
				resolver.reject(e.getCause());
			}
		}, profile);
	}

	public static <R> Promise<R> resolve(Future<R> future, ThreadProfile profile, long timeout, TimeUnit unit) {
		return defer(resolver -> {
			try {
				resolver.resolve(future.get(timeout, unit));
			} catch (ExecutionException e) {
				resolver.reject(e.getCause());
			}
		}, profile);
	}

	public static <R> Promise<R> resolve(Future<R> future, long timeout, TimeUnit unit) {
		return resolve(future, ThreadProfile.IO, timeout, unit);
	}

	public static <R> PromiseOrValue<R> value(R value) {
		return new Value<>(value);
	}

	public static <R> Promise<R> reject(Throwable exception) {
		return manager.rejected(exception);
	}
	
	public static <R> Promise<R> defer(Receiver<Resolver<R>> receiver) {
		return defer(receiver, ThreadProfile.CPU);
	}

	public static <R> Promise<R> defer(Receiver<Resolver<R>> receiver, ThreadProfile profile) {
		Deferred<R> deferred = deferred();
		manager.schedule(profile, receiver, deferred.resolver());
		return deferred.promise();
	}
	
	public static <R> Deferred<R> deferred() {
		return manager.deferred();
	}

	public static <R> Promise<R> resolve(Callable<PromiseOrValue<R>> callback) {
		try {
			return resolve(callback.call());
		} catch (Throwable e) {
			return reject(e);
		}
	}
	
	public static <I, O> Promise<List<O>> map(Object[] promisesOrValues, Mapper<I, PromiseOrValue<O>> mapper) {
		return map(Arrays.asList(promisesOrValues), mapper);
	}
	
	@SuppressWarnings("unchecked")
	public static <I, O> Promise<List<O>> map(Collection<Object> promisesOrValues, Mapper<I, PromiseOrValue<O>> mapper) {
		if (promisesOrValues == null || promisesOrValues.size() == 0)
			return resolve(Collections.emptyList());
		return defer((Resolver<List<O>> resolver) -> {
			Object[] result = new Object[promisesOrValues.size()];
			AtomicInteger replyCount = new AtomicInteger(promisesOrValues.size());
			int i = 0;
			for (Object promiseOrValue : promisesOrValues) {
				final int idx = i;
				((Promise<I>) resolve(promiseOrValue)).
					then(mapper::map).done((success) -> {
					result[idx] = success;
					if (replyCount.decrementAndGet() == 0)
						resolver.resolve((List<O>) Arrays.asList(result));
				}, resolver::reject);
				i++;
			}
		});
		
	}
	
	public static <R> Promise<List<R>> join(Object... promisesOrValues) {
		return all(promisesOrValues);
	}
	
	public static <R> Promise<List<R>> all(Object[] promisesOrValues) {
		return all(Arrays.asList(promisesOrValues));
	}
	
	public static <R> Promise<List<R>> all(Collection<Object> promisesOrValues) {
		return map(promisesOrValues, Mapper.noop());
	}

	private static final DeferredManager manager = ServiceLoader.load(DeferredManager.class).
			iterator().next();
}
