package com.github.tsouza.promises;

import com.github.tsouza.promises.functions.*;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

import static com.github.tsouza.promises.Promises.resolve;


public interface Promise<R> extends PromiseOrValue<R> {
	public <NR> Promise<NR> then(Mapper<R, PromiseOrValue<NR>> mapper);
	public <NR> Promise<NR> then(Mapper<R, PromiseOrValue<NR>> success, Mapper<Throwable, PromiseOrValue<R>> failure);
	public <T extends Throwable> Promise<R> fail(Class<T> exceptionType, Mapper<T, PromiseOrValue<R>> mapper);

	public default Promise<R> fail(Mapper<Throwable, PromiseOrValue<R>> mapper) {
		return fail(Throwable.class, mapper);
	}

	public Promise<R> tap(Mapper<R, Promise<?>> mapper);

	@SuppressWarnings("unchecked")
	public default <I, O> Promise<List<O>> map(Mapper<I, PromiseOrValue<O>> mapper) {
		return then(success -> {
			if (success == null) return null;
			if (success instanceof Collection)
				return Promises.map((Collection<Object>) success, mapper);
			if (success instanceof Object[])
				return Promises.map((Object[]) success, mapper);
			throw new IllegalStateException(success.getClass() + " is not a collection (can not map it)");
		});
	}

	@SuppressWarnings("unchecked")
	public default Promise<R> reduce(Reducer<R, PromiseOrValue<R>> reducer, R initialValue) {
		return then(success -> {
			if (success == null) return null;
			if (success instanceof Collection)
				return Promises.reduce((Collection<Object>) success, reducer, initialValue);
			if (success instanceof Object[])
				return Promises.reduce((Object[]) success, reducer, initialValue);
			throw new IllegalStateException(success.getClass() + " is not a collection (can not reduce it)");
		});
	}

	public default Promise<R> reduce(Reducer<R, PromiseOrValue<R>> reducer) {
		return reduce(reducer, null);
	}

	public default <NR> Promise<NR> yield(NR result) {
		return then(success -> resolve(result));
	}

	public default <NR> Promise<NR> yield(PromiseOrValue<NR> result) {
		return then(success -> resolve(result));
	}

	public Promise<R> always(Callable<Promise<R>> callable);
	
	public <T1, T2, NR> Promise<NR> spread(Spread.Args2<T1, T2, PromiseOrValue<NR>> spread);
	public <T1, T2, T3, NR> Promise<NR> spread(Spread.Args3<T1, T2, T3, PromiseOrValue<NR>> spread);
	public <T1, T2, T3, T4, NR> Promise<NR> spread(Spread.Args4<T1, T2, T3, T4, PromiseOrValue<NR>> spread);
	public <T1, T2, T3, T4, T5, NR> Promise<NR> spread(Spread.Args5<T1, T2, T3, T4, T5, PromiseOrValue<NR>> spread);
	
	public void done(Receiver<R> success, Receiver<Throwable> failure);
	public void done(Receiver<R> success);

	public Future<R> future();
}
