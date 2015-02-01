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
package com.github.tsouza.promises.example;

import com.github.tsouza.promises.Promises;
import com.github.tsouza.promises.Resolver;
import com.github.tsouza.promises.ThreadProfile;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.net.URL;

public class BlockingIOPromise {

    public static void main(String[] args) throws Exception {

        Promises.defer((Resolver<String> resolver) -> {
            URL url = new URL("http://api.ipify.org");

            try (InputStream stream = url.openStream()) {
                resolver.resolve(IOUtils.toString(stream));
            }

        }, ThreadProfile.IO).done(System.out::println);

        Thread.sleep(5000);
    }
}
