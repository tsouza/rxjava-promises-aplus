# RxJava Promises/A+ Library for the JVM

A *promise* represents the eventual result of an asynchronous operation. This library provides a Promise/A+ like API for building callback-based applications.

## Modules

It's organized in 3 modules:

* *[rxjava-promises-api](https://github.com/tsouza/rxjava-promises-aplus/tree/master/rxjava-promises-api)*: defines the Promises API.
* *[rxjava-promises-core](https://github.com/tsouza/rxjava-promises-aplus/tree/master/rxjava-promises-core)*: provides a [RxJava](https://github.com/ReactiveX/RxJava) based implementation.
* *[rxjava-promises-example](https://github.com/tsouza/rxjava-promises-aplus/tree/master/rxjava-promises-example)*: provides a few samples on how to use the library

## Getting Started

First, add Sonatype OSS repository and core module:

Example for Maven:
```xml
<project>
    <dependencies>
        <dependency>
            <groupId>com.github.tsouza.promises</groupId>
            <artifactId>rxjava-promises-core</artifactId>
            <version>1.0.0-rc.5</version>
        </dependency>
    </dependencies>
    <repositories>
        <repository>
          <id>sonatype</id>
          <url>https://oss.sonatype.org/content/groups/public/</url>
        </repository>
    </repositories>
</project>
```

and for Gradle:
```groovy
repositories {
    maven { url 'http://repository.sonatype.org/content/groups/public/' }
}

dependency {
    compile "com.github.tsouza.promises:rxjava-promises-core:1.0.0-rc.5"
}
```

Then import *Promises* class and start using it. See the [examples project](https://github.com/tsouza/rxjava-promises-aplus/tree/master/rxjava-promises-example).

## Building

<a href="https://travis-ci.org/tsouza/rxjava-promises-aplus/builds"><img src="https://travis-ci.org/tsouza/rxjava-promises-aplus.svg?branch=master"></a>

You'll need a Java 8 compatible JDK installed.

To build:
```
$ git clone http://github.com/tsouza/rxjava-promises-aplus
$ cd rxjava-promises-aplus
$ ./gradlew build
```

## Bugs and Feedback
For bugs, questions and discussions please use the [Github Issues](issues).

## Acknowledgements

This library is based on [Daryl Teo's rxjava-promises](https://github.com/darylteo/rxjava-promises). Thanks for the great work!

## LICENSE

Code and documentation released under [The MIT License (MIT)](LICENSE).
