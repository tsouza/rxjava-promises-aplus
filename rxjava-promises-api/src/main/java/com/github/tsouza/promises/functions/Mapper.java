package com.github.tsouza.promises.functions;

@FunctionalInterface
public interface Mapper<I, O> {
	public O map(I input) throws Throwable;

	@SuppressWarnings("rawtypes")
	public static Mapper NOOP = (input) -> input;

	@SuppressWarnings("unchecked")
	public static <I> Mapper<I, I> noop() {
		return NOOP;
	}
}