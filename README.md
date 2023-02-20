httpunit
========

A library for testing websites

### Project
* Open Source hosted at https://github.com/russgold/httpunit
* Maven based Java project including Unit tests

### Stack Overflow Questions & Answers
http://stackoverflow.com/questions/tagged/http-unit

### Servlet 3.1.0 and jsp initializer

After getting ServletRunner to establish the Instance Manager, add the following to your code to use tomcat 8 and better

```
    // Initializer Jasper
    final JasperInitializer jsp = new JasperInitializer();
    jsp.onStartup(null, this.runner.getSession(true).getServletContext());
```

### Jakarta Support

See 'jakarta' branch with support at release 2.0.0
