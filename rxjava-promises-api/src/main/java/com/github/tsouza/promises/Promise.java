package com.github.tsouza.promises;

import com.github.tsouza.promises.functions.Callable;
import com.github.tsouza.promises.functions.Mapper;
import com.github.tsouza.promises.functions.Receiver;
import com.github.tsouza.promises.functions.Spread;

import java.util.List;


public interface Promise<R> extends PromiseOrValue<R> {
	public <NR> Promise<NR> then(Mapper<R, PromiseOrValue<NR>> mapper);
	public <T extends Throwable> Promise<R> fail(Class<T> exceptionType, Mapper<T, PromiseOrValue<R>> mapper);
	public Promise<R> fail(Mapper<Throwable, PromiseOrValue<R>> mapper);
	
	public Promise<R> tap(Mapper<R, Promise<?>> mapper);
	public <I, O> Promise<List<O>> map(Mapper<I, PromiseOrValue<O>> mapper);
	
	public <NR> Promise<NR> yield(NR result);
	public <NR> Promise<NR> yield(PromiseOrValue<NR> result);
	public Promise<R> always(Callable<Promise<R>> callable);
	
	public <T1, T2, NR> Promise<NR> spread(Spread.Args2<T1, T2, PromiseOrValue<NR>> spread);
	public <T1, T2, T3, NR> Promise<NR> spread(Spread.Args3<T1, T2, T3, PromiseOrValue<NR>> spread);
	public <T1, T2, T3, T4, NR> Promise<NR> spread(Spread.Args4<T1, T2, T3, T4, PromiseOrValue<NR>> spread);
	public <T1, T2, T3, T4, T5, NR> Promise<NR> spread(Spread.Args5<T1, T2, T3, T4, T5, PromiseOrValue<NR>> spread);
	
	public void done(Receiver<R> success, Receiver<Throwable> failure);
	public void done(Receiver<R> success);
}
