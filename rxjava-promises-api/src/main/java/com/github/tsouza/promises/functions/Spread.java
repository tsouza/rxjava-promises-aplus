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