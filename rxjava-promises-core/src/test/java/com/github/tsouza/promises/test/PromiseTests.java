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
package com.github.tsouza.promises.test;

import com.github.tsouza.promises.Deferred;
import com.github.tsouza.promises.Promise;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.tsouza.promises.Promises.*;
import static com.google.common.util.concurrent.Futures.immediateFailedFuture;
import static com.google.common.util.concurrent.Futures.immediateFuture;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PromiseTests {

    @Test
    public void testAlreadyFulfilled() throws ExecutionException, InterruptedException {
        assertFulfillment(resolve(true), false);
    }

    @Test
    public void testEventuallyFulfilled() throws ExecutionException, InterruptedException {
        assertFulfillment(defer(receiver -> receiver.resolve(true)), false);
    }

    @Test
    public void testFutureFulfilled() throws ExecutionException, InterruptedException {
        assertFulfillment(resolve(immediateFuture(true)), false);
    }

    @Test(expected = ExecutionException.class)
    public void testAlreadyRejected() throws ExecutionException, InterruptedException {
        assertFulfillment(reject(new Exception()), true);
    }

    @Test(expected = ExecutionException.class)
    public void testEventuallyRejected() throws ExecutionException, InterruptedException {
        assertFulfillment(defer(receiver -> receiver.reject(new Exception())), true);
    }

    @Test(expected = ExecutionException.class)
    public void testFutureRejected() throws ExecutionException, InterruptedException {
        assertFulfillment(resolve(immediateFailedFuture(new Exception())), true);
    }

    @Test
    public void testDeferredFulfilled() throws ExecutionException, InterruptedException {
        Deferred<Boolean> deferred = deferred();
        deferred.resolver().resolve(true);
        assertFulfillment(deferred.promise(), false);
    }

    @Test(expected = ExecutionException.class)
    public void testDeferredRejected() throws ExecutionException, InterruptedException {
        Deferred<Boolean> deferred = deferred();
        deferred.resolver().reject(new Exception());
        assertFulfillment(deferred.promise(), true);
    }

    @Test
    public void testDeferredFulFilledThenRejected() throws ExecutionException, InterruptedException {
        Deferred<Boolean> deferred = deferred();
        deferred.resolver().resolve(true);
        deferred.resolver().reject(new Exception());
        assertFulfillment(deferred.promise(), false);
    }

    @Test(expected = ExecutionException.class)
    public void testDeferredRejectedThenFulfilled() throws ExecutionException, InterruptedException {
        Deferred<Boolean> deferred = deferred();
        deferred.resolver().reject(new Exception());
        deferred.resolver().resolve(true);
        assertFulfillment(deferred.promise(), true);
    }

    @Test
    public void testAlreadyFulfilledChain() throws ExecutionException, InterruptedException {
        assertEquals("1 2", resolve("1").
                then(r -> value(r + " 2")).
                future().get());
    }

    @Test
    public void testEventuallyFulfilledChain() throws ExecutionException, InterruptedException {
        assertEquals("1 2", defer(receiver -> receiver.resolve("1")).
                then(r -> value(r + " 2")).
                future().get());
    }

    @Test
    public void testFutureFulfilledChain() throws ExecutionException, InterruptedException {
        assertEquals("1 2", resolve(immediateFuture("1")).
                then(r -> value(r + " 2")).
                future().get());
    }

    @Test
    public void testMapOfValueList() throws ExecutionException, InterruptedException {
        assertEquals(asList(2, 3, 4),
                map(asList(1, 2, 3), (Integer n) -> value(n + 1)).
                        future().get());
    }

    @Test
    public void testMapOfPromiseList() throws ExecutionException, InterruptedException {
        assertEquals(asList(2, 3, 4),
                map(asList(resolve(1), resolve(2), resolve(3)),
                        (Integer n) -> value(n + 1)).
                        future().get());
    }

    @Test
    public void testJoinThenMapOfValueList() throws ExecutionException, InterruptedException {
        assertEquals(asList(2, 3, 4),
                join(1, 2, 3).map((Integer n) -> value(n + 1)).
                        future().get());
    }

    @Test
    public void testJoinThenMapOfPromiseList() throws ExecutionException, InterruptedException {
        assertEquals(asList(2, 3, 4),
                join(resolve(1), resolve(2), resolve(3)).
                        map((Integer n) -> value(n + 1)).
                        future().get());
    }

    @Test
    public void testJoinThenSpreadOfValueList() throws ExecutionException, InterruptedException {
        assertEquals((Object) 3, join(1, 2).
                        spread((Integer n1, Integer n2) -> value(n1 + n2)).
                        future().get());
        assertEquals((Object) 6, join(1, 2, 3).
                spread((Integer n1, Integer n2, Integer n3) -> value(n1 + n2 + n3)).
                future().get());
        assertEquals((Object) 10, join(1, 2, 3, 4).
                spread((Integer n1, Integer n2, Integer n3, Integer n4) -> value(n1 + n2 + n3 + n4)).
                future().get());
        assertEquals((Object) 15, join(1, 2, 3, 4, 5).
                spread((Integer n1, Integer n2, Integer n3, Integer n4, Integer n5) -> value(n1 + n2 + n3 + n4 + n5)).
                future().get());
    }

    @Test
    public void testJoinThenSpreadOfPromiseList() throws ExecutionException, InterruptedException {
        assertEquals((Object) 3,
                join(resolve(1), resolve(2)).
                spread((Integer n1, Integer n2) -> value(n1 + n2)).
                future().get());
        assertEquals((Object) 6,
                join(resolve(1), resolve(2), resolve(3)).
                spread((Integer n1, Integer n2, Integer n3) -> value(n1 + n2 + n3)).
                future().get());
        assertEquals((Object) 10,
                join(resolve(1), resolve(2), resolve(3), resolve(4)).
                spread((Integer n1, Integer n2, Integer n3, Integer n4) -> value(n1 + n2 + n3 + n4)).
                future().get());
        assertEquals((Object) 15,
                join(resolve(1), resolve(2), resolve(3), resolve(4), resolve(5)).
                spread((Integer n1, Integer n2, Integer n3, Integer n4, Integer n5) -> value(n1 + n2 + n3 + n4 + n5)).
                future().get());
    }

    @Test(expected = ExecutionException.class)
    public void testMapUmappableValue() throws ExecutionException, InterruptedException {
        resolve(true).map((Boolean b) -> value(asList(b))).future().get();
    }

    @Test
    public void testReduceValueList() throws ExecutionException, InterruptedException {
        assertEquals((Object) 6,
                reduce(asList(1, 2, 3), (Integer p, Integer c) -> value(p + c), 0).
                        future().get());
    }

    @Test
    public void testAlwaysReturningPromise() throws ExecutionException, InterruptedException {
        AtomicBoolean wasRejected = new AtomicBoolean(false);
        Promise<Object> rejectedPromise = reject(new RuntimeException());

        defer(resolver -> resolver.chain(rejectedPromise)).
                always(() -> resolve((Object) null)).
                done(s -> wasRejected.set(false), f -> wasRejected.set(true));

        Thread.sleep(100);

        assertTrue(wasRejected.get());
    }

    private void assertFulfillment(Promise<Boolean> promise, boolean expected) throws ExecutionException, InterruptedException {
        AtomicBoolean fulfilled = new AtomicBoolean(expected);

        promise.done(fulfilled::set,
                f -> fulfilled.set(false));

        Thread.sleep(100);
        assertEquals(true, promise.future().get());
        assertEquals(true, fulfilled.get());
    }
}
