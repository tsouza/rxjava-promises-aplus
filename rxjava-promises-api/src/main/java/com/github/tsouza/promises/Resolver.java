package com.github.tsouza.promises;

public interface Resolver<R> {
	public void resolve(R result);
	public void reject(Throwable exception);
	public void chain(Promise<R> promise);
}