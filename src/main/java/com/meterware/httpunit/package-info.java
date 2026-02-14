/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
/**
 * Classes for testing HTTP server systems. Each test session should begin by creating a
 * {@link com.meterware.httpunit.WebConversation WebConversation} to which it should submit an initial
 * {@link com.meterware.httpunit.GetMethodWebRequest http request} using the
 * {@link com.meterware.httpunit.WebConversation#getResponse getResponse} method. With each subsequent step, it will
 * typically examine the response either textually or as a DOM, and create new requests based on either submitting a
 * form or clicking on a link.
 * <h2>Installation</h2> The package depends on a number of external jar files:
 * <dl>
 * <dt>nekohtml.jar</dt>
 * <dd>The <a href="http://www.apache.org/~andyc/neko/doc/html/index.html">NekoHTML parser</a>, used to convert raw HTML
 * into an XML DOM. This is required for handling HTML.</dd>
 * <dt>js.jar</dt>
 * <dd>The <a href="http://www.mozilla.org/rhino">Rhino JavaScript interpreter</a>, required for any JavaScript
 * processing.</dd>
 * <dt>xmlParserAPIs.jar</dt>
 * <dd>The interfaces for a W3-compliant XML parser. Required for interpreting either HTML or XML pages.</dd>
 * <dt>xercesImpl.jar</dt>
 * <dd>The <a href="http://xml.apache.org/xerces2-j/index.html">Xerces 2 implementation</a> of an XML parser. NekoHTML
 * requires this implementation.</dd>
 * <dt>servlet.jar</dt>
 * <dd>The APIs and common classes for the Java Servlet 1.3 standard. Required for use with
 * <a href="../../../../servletunit-intro.html">ServletUnit</a>.</dd>
 * <dt>junit.jar</dt>
 * <dd><a href="http://www.junit.org">JUnit</a>, the unit test framework. Used to test HttpUnit and recommended for
 * writing tests that use HttpUnit.</dd>
 * <dt>tidy.jar</dt>
 * <dd><a href="http://lempinen.net/sami/jtidy">JTidy</a>, an alternate HTML parser/validator. JTidy is a lot pickier
 * about HTML structure than NekoHTML, and uses its own implementation of the DOM classes, rather than using those found
 * in the xerces jar. Some JavaScript features, such as <code>document.write()</code> will only work with NekoHTML.</dd>
 * </dl>
 * <h2>Example</h2>
 *
 * <pre>
 * import com.meterware.httpunit.*;
 *
 * import java.io.IOException;
 * import java.net.MalformedURLException;
 *
 * import org.xml.sax.*;
 *
 * public class Example {
 *     public static void main(String[] params) {
 *         try {
 *             WebConversation conversation = new WebConversation();
 *             WebResponse response = conversation.getResponse("http://www.meterware.com/servlet/TopSecret");
 *             System.out.println(response);
 *             WebForm loginForm = response.getForms()[0];
 *             loginForm.setParameter("name", "master");
 *             response = loginForm.submit();
 *             System.out.println(response);
 *         } catch (Exception e) {
 *             System.err.println("Exception: " + e);
 *         }
 *     }
 * }
 * </pre>
 *
 * Please direct any questions to <a href="mailto:russgold@httpunit.org">Russell Gold</a>.
 */
package com.meterware.httpunit;
