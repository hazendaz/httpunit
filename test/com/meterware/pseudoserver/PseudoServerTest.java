package com.meterware.pseudoserver;
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
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.HttpNotFoundException;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HttpException;
import com.meterware.httpunit.PostMethodWebRequest;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.net.HttpURLConnection;


public class PseudoServerTest extends HttpUserAgentTest {

    public static void main( String args[] ) {
        junit.textui.TestRunner.run( suite() );
    }


    public static TestSuite suite() {
        return new TestSuite( PseudoServerTest.class );
    }


    public PseudoServerTest( String name ) {
        super( name );
    }


    public void testNoSuchServer() throws Exception {
        WebConversation wc = new WebConversation();

        try {
            WebResponse response = wc.getResponse( "http://no.such.host" );
        } catch (HttpNotFoundException e) {
        }
    }


    public void testNotFound() throws Exception {
        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest( getHostPath() + "/nothing.htm" );
        try {
            WebResponse response = wc.getResponse( request );
            fail( "Should have rejected the request" );
        } catch (HttpNotFoundException e) {
            assertEquals( "Response code", HttpURLConnection.HTTP_NOT_FOUND, e.getResponseCode() );
        }
    }


    public void testNotModifiedResponse() throws Exception {
        defineResource( "error.htm", "Not Modified", 304 );

        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest( getHostPath() + "/error.htm" );
        WebResponse response = wc.getResponse( request );
        assertEquals( "Response code", 304, response.getResponseCode() );
        response.getText();
        response.getInputStream().read();
    }


    public void testInternalErrorException() throws Exception {
        defineResource( "error.htm", "Internal error", 501 );

        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest( getHostPath() + "/error.htm" );
        try {
            WebResponse response = wc.getResponse( request );
            fail( "Should have rejected the request" );
        } catch (HttpException e) {
            assertEquals( "Response code", 501, e.getResponseCode() );
        }
    }


    public void testInternalErrorDisplay() throws Exception {
        defineResource( "error.htm", "Internal error", 501 );

        WebConversation wc = new WebConversation();
        wc.setExceptionsThrownOnErrorStatus( false );
        WebRequest request = new GetMethodWebRequest( getHostPath() + "/error.htm" );
        WebResponse response = wc.getResponse( request );
        assertEquals( "Response code", 501, response.getResponseCode() );
        assertEquals( "Message contents", "Internal error", response.getText().trim() );
    }


    public void testSimpleGet() throws Exception {
        String resourceName = "something/interesting";
        String resourceValue = "the desired content";

        defineResource( resourceName, resourceValue );

        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest( getHostPath() + '/' + resourceName );
        WebResponse response = wc.getResponse( request );
        assertEquals( "requested resource", resourceValue, response.getText().trim() );
        assertEquals( "content type", "text/html", response.getContentType() );
    }


    private String asBytes( String s ) {
        StringBuffer sb = new StringBuffer();
        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            sb.append( Integer.toHexString( chars[i] ) ).append( " " );
        }
        return sb.toString();
    }


    public void testPseudoServlet() throws Exception {
        String resourceName = "tellMe";
        String name = "Charlie";
        final String prefix = "Hello there, ";
        String expectedResponse = prefix + name;

        defineResource( resourceName, new PseudoServlet() {

            public WebResource getPostResponse() {
                return new WebResource( prefix + getParameter( "name" )[0], "text/plain" );
            }
        } );

        WebConversation wc = new WebConversation();
        WebRequest request = new PostMethodWebRequest( getHostPath() + '/' + resourceName );
        request.setParameter( "name", name );
        WebResponse response = wc.getResponse( request );
        assertEquals( "Content type", "text/plain", response.getContentType() );
        assertEquals( "Response", expectedResponse, response.getText().trim() );
    }


}
