package com.meterware.httpunit;
/********************************************************************************************************************
* $Id$
*
* Copyright (c) 2000-2002, Russell Gold
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
*
*******************************************************************************************************************/
import com.meterware.pseudoserver.HttpUserAgentTest;
import com.meterware.httpunit.parsing.HTMLParserFactory;

/**
 * a base class for HttpUnit regression tests.
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
abstract
public class HttpUnitTest extends HttpUserAgentTest {

    private boolean _showTestName;
    private long _startTime;


    /**
     * construct a test with the given name
     * @param name
     */
    public HttpUnitTest( String name ) {
        super( name );
    }

    /**
     * construct a test with the given name and show it depending on the flag showTestName
     * @param name
     * @param showTestName
     */
    public HttpUnitTest( String name, boolean showTestName ) {
        super( name );
        _showTestName = showTestName;
    }


    /**
     * setup the test by resetting the environment for Http Unit tests
     */
    public void setUp() throws Exception {
        super.setUp();
        HttpUnitOptions.reset();
        HTMLParserFactory.reset();
        if (_showTestName) {
            System.out.println( "----------------------- " + getName() + " ------------------------");
            _startTime = System.currentTimeMillis();
        }
    }


    /**
     * tear down the test and if the name should be shown do so with the duration of the test
     * in millisecs
     */
    public void tearDown() throws Exception {
        super.tearDown();
        if (_showTestName) {
            long duration = System.currentTimeMillis() - _startTime;
            System.out.println( "... took " + duration + " msec");
        }
    }

    /**
     * handling of tests that are temporarily disabled
     */
    public static boolean WARN_DISABLED=true;
    public static int disabledIndex=0;
    
    /**
     * show a warning for disabled Tests
     * @param testName
     * @param comment
     */
    public void warnDisabled(String testName,String comment) {
    	if (WARN_DISABLED) {
    		disabledIndex++;
    		System.err.println("*** "+disabledIndex+". Test "+testName+" disabled: "+comment);
    	}	
    }

    static {
        new WebConversation();
    }


}
