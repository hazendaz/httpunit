httpunit
========

A library for testing websites

### Project
* Open Source hosted at https://github.com/russgold/httpunit
* Maven based Java project including Unit tests

### Stack Overflow Questions & Answers
http://stackoverflow.com/questions/tagged/http-unit

### Fork needs to run at jdk 8, to run at jdk 11, profile must be skipped ###

in bash

- mvn clean install -P \!jdk11on

in windows

- mvn clean install -P !jdk11on

The reason behind the skip has to do with dom that comes in jdk up through 8 not being picked up for jdk 9 on.  The one with xerces is different.  While we could move over, 2 tests fail and its not clear how to address test longer term.  Therefore, just used java 8 to release and called it a day.  Clearly library works with jdk 11 or higher already anyways per usage on psi probe.
