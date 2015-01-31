package com.github.tsouza.promises;

public interface Deferred<R> {
	public Promise<R> promise();
	public Resolver<R> resolver();
}
