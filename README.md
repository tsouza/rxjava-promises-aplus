# RxJava Promise Library for the JVM

A *promise* represents the eventual result of an asynchronous operation. This library provides a Promise/A+ like API for building callback-based applications. It's organized in 3 modules:
* *[rxjava-promises-api](rxjava-promises-api)*: defines the Promises API.
* *[rxjava-promises-core](rxjava-promises-core)*: provides a @ReactiveX RxJava based implementation.
* *[rxjava-promises-example](rxjava-promises-example)*: provides a few samples on how to use the library

## Getting Started

There is no official release (yet) of the library. You'll need to build it first.

First, add the core module to your project.

Example for Maven:
```xml
<dependency>
    <groupId>com.github.tsouza.promises</groupId>
    <artifactId>rxjava-promises-core</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

and for Gradle:
```groovy
dependency {
    compile "com.github.tsouza.promises:rxjava-promises-core:0.0.1-SNAPSHOT"
}
```

Then import *Promises* class and start using it. See the [examples project](rxjava-promises-examples).

## Building

You'll need a Java 8 compatible JDK installed.

To build:
```
$ git clone http://github.com/tsouza/rxjava-promises-aplus
$ cd rxjava-promises-aplus
$ ./gradlew install
```

## Bugs and Feedback
For bugs, questions and discussions please use the [Github Issues](issues).

## Acknowledgements

This library is based on @darylteo rxjava-promises. Thanks for the great work!

## LICENSE

Code and documentation released under [The MIT License (MIT)](blob/master/LICENSE).