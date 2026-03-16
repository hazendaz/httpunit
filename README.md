httpunit
========

[![Java CI](https://github.com/hazendaz/httpunit/actions/workflows/ci.yaml/badge.svg)](https://github.com/hazendaz/httpunit/actions/workflows/ci.yaml)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.hazendaz.httpunit/httpunit.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/com.github.hazendaz/httpunit)
[![MIT License](https://img.shields.io/badge/license-MIT-green.svg)](https://opensource.org/licenses/MIT)

![hazendaz](src/site/resources/images/hazendaz-banner.jpg)

A library for testing websites

### Project
* Open Source hosted at https://github.com/russgold/httpunit
* Maven based Java project including Unit tests

### Stack Overflow Questions & Answers
https://stackoverflow.com/questions/tagged/http-unit

### Servlet 3.1.0 and jsp initializer

After getting ServletRunner to establish the Instance Manager, add the following to your code to use tomcat 8 and better

```
    // Initializer Jasper
    final JasperInitializer jsp = new JasperInitializer();
    jsp.onStartup(null, this.runner.getSession(true).getServletContext());
```
