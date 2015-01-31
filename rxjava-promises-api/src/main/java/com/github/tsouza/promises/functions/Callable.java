package com.github.tsouza.promises.functions;

@FunctionalInterface
public interface Callable<O> {
	public O call() throws Throwable;
}
