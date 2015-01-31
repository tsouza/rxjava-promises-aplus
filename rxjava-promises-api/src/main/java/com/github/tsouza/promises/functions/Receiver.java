package com.github.tsouza.promises.functions;

@FunctionalInterface
public interface Receiver<I> {
	public void receive(I input) throws Throwable;
}
