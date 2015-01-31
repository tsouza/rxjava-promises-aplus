package com.github.tsouza.promises.functions;

public interface Spread {

	@FunctionalInterface
	public static interface Args2<I1, I2, O> {		
		public O call(I1 i1, I2 i2) throws Throwable;		
	}
	
	@FunctionalInterface
	public static interface Args3<I1, I2, I3, O> {		
		public O call(I1 i1, I2 i2, I3 i3) throws Throwable;		
	}
	
	@FunctionalInterface
	public static interface Args4<I1, I2, I3, I4, O> {		
		public O call(I1 i1, I2 i2, I3 i3, I4 i4) throws Throwable;		
	}
	
	@FunctionalInterface
	public static interface Args5<I1, I2, I3, I4, I5, O> {		
		public O call(I1 i1, I2 i2, I3 i3, I4 i4, I5 i5) throws Throwable;
	}

}