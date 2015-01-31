package com.github.tsouza.promises;

public final class Value<R> implements PromiseOrValue<R> {
	
	private final R value;

	Value(R value) {
		this.value = value;
	}

	public R get() {
		return value;
	}
	
}
