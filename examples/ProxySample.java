/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
import com.meterware.httpunit.WebConversation;

import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ProxySample extends TestCase {


	// modify proxyURL and proxyPort to your personal preferences
	// e.g. a proxy server that is accessible from your network
	public static String proxyURL="www-proxy.us.oracle.com";
	public static int proxyPort=80;
	
		/**
		 * main routine to test 
		 * @param args
		 */
    public static void main(String args[]) {
    		System.out.println("Will run this sample JUnit Testcase with proxy URL set to "+proxyURL+" port "+proxyPort);
    		System.out.println("You might want to modify the proxy server URL (see proxyURL,proxyPort fields in ProxySample.java) to one that you may use at your location for quicker response time");
    		// run this example as a Unit test
        junit.textui.TestRunner.run( suite() );
    }


    /**
     * create a Testsuite containing this ProxySample class
     * @return the testsuite
     */
    public static TestSuite suite() {
        return new TestSuite( ProxySample.class );
    }


    /**
     * constructor which just calls the super constructor
     * @param name
     */
    public ProxySample( String name ) {
        super( name );
    }


    /**
     * test the proxy access
     * - set the proxy server to one that you may use at your location for quicker response time
     * @throws Exception
     */
    public void testProxyAccess() throws Exception {
        WebConversation wc = new WebConversation();
        wc.setProxyServer( proxyURL, proxyPort );
        wc.getResponse( "http://www.meterware.com" );
    }



}

