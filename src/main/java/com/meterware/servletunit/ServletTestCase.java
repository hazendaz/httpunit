/*
 * MIT License
 *
 * Copyright 2011-2023 Russell Gold
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package com.meterware.servletunit;

import junit.framework.TestCase;

/**
 * A base class for test cases to be run via {@link JUnitServlet JUnitServlet}.
 *
 * @author <a href="mailto:russgold@acm.org">Russell Gold</a>
 **/
public abstract class ServletTestCase extends TestCase {

	/**
	 * construct a ServletTestCase with the given name
	 * @param name
	 */
    protected ServletTestCase( String name ) {
        super( name );
    }

    /**
     * Returns a client object which can access the servlet context in which this test is running.
     */
    final protected ServletUnitClient newClient() {
    	if (_invocationContextFactory==null)
    		throw new RuntimeException("ServletTestCase.newClient called before setInvocationContextFactory was called");
        return ServletUnitClient.newClient( _invocationContextFactory );
    }


    /**
     * set the invocation context factory to be used
     * @param invocationContextFactory
     */
    static void setInvocationContextFactory( InvocationContextFactory invocationContextFactory ) {
    	if (invocationContextFactory==null)
    		throw new RuntimeException("setInvocationContextFactory called with null invocationContextFactory parameter");
        _invocationContextFactory = invocationContextFactory;
    }


    private static InvocationContextFactory _invocationContextFactory;
}
