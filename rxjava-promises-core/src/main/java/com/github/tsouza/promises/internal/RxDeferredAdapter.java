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
package com.github.tsouza.promises.internal;

import com.github.tsouza.promises.Deferred;
import com.github.tsouza.promises.Promise;
import com.github.tsouza.promises.Resolver;

public class RxDeferredAdapter<R> implements Deferred<R>, Resolver<R> {

    private final RxObserverAdapter<R> rxPromise = new RxObserverAdapter<>();

    @Override
    public Promise<R> promise() {
        return new RxPromiseAdapter<>(rxPromise);
    }

    @Override
    public Resolver<R> resolver() {
        return this;
    }

    @Override
    public void resolve(R result) {
        rxPromise.fulfill(result);
    }

    @Override
    public void reject(Throwable exception) {
        rxPromise.reject(exception);
    }

    @Override
    public void chain(Promise<R> promise) {
        promise.done(this::resolve, this::reject);
    }
}
