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
package com.github.tsouza.promises;


import com.github.tsouza.promises.functions.Callable;
import com.github.tsouza.promises.functions.Mapper;
import com.github.tsouza.promises.functions.Receiver;
import com.github.tsouza.promises.functions.Reducer;
import com.github.tsouza.promises.spi.DeferredManager;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Promises {

	@SuppressWarnings("unchecked")
	public static <R> Promise<R> resolve(Object promiseOrValue) {
		if (promiseOrValue instanceof PromiseOrValue)
			return resolve((PromiseOrValue<R>) promiseOrValue);
		return manager().resolved(promiseOrValue);
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
		return manager().rejected(exception);
	}
	
	public static <R> Promise<R> defer(Receiver<Resolver<R>> receiver) {
		return defer(receiver, ThreadProfile.CPU);
	}

	public static <R> Promise<R> defer(Receiver<Resolver<R>> receiver, ThreadProfile profile) {
		Deferred<R> deferred = deferred();
		manager().schedule(profile, receiver, deferred.resolver());
		return deferred.promise();
	}
	
	public static <R> Deferred<R> deferred() {
		return manager().deferred();
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

	public static <R> Promise<R> reduce(Object[] promisesOrValues, Reducer<R, PromiseOrValue<R>> reducer, R initialValue) {
		return reduce(Arrays.asList(promisesOrValues), reducer, initialValue);
	}

	@SuppressWarnings("unchecked")
	public static <R> Promise<R> reduce(Iterable<Object> promisesOrValues, Reducer<R, PromiseOrValue<R>> reducer, R initialValue) {
		if (promisesOrValues == null)
			return resolve(initialValue);
		return reduceNext((Iterator<R>) promisesOrValues.iterator(), reducer, initialValue);
	}

    @SuppressWarnings("unchecked")
	private static <R> Promise<R> reduceNext(Iterator<R> promisesOrValues, Reducer<R, PromiseOrValue<R>> reducer, R previousValue) {
		if (!promisesOrValues.hasNext())
			return resolve(previousValue);
		return resolve(promisesOrValues.next()).
				then(currentValue -> reducer.reduce(previousValue, (R) currentValue)).
				then(nextValue -> reduceNext(promisesOrValues, reducer, nextValue));
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

	private static DeferredManager manager() {
		if (MANAGER == null)
			MANAGER = ServiceLoader.load(DeferredManager.class).
					iterator().next();
		return MANAGER;
	}

	private static DeferredManager MANAGER;

}
