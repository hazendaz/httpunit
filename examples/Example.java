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

/** everything you need to start is in the com.meterware.httpunit package **/
import com.meterware.httpunit.*;

/** This is a simple example of using HttpUnit to read and understand web pages. **/
public class Example {


  /**
   * starting point of this Example
   * @param params
   */
  public static void main( String[] params ) {
    try {
      // create the conversation object which will maintain state for us
      WebConversation wc = new WebConversation();

      // Obtain the main page on the meterware web site
      String url="http://www.meterware.com";
      WebRequest request = new GetMethodWebRequest( url );
      WebResponse response = wc.getResponse( request );

      // find the link which contains the string "HttpUnit" and click it
      WebLink httpunitLink = response.getFirstMatchingLink( WebLink.MATCH_CONTAINED_TEXT, "HttpUnit" );
      response = httpunitLink.click();

      // print out the number of links on the HttpUnit main page
      System.out.println( "The HttpUnit main page '"+url+"' contains " + response.getLinks().length + " links" );

    } catch (Exception e) {
       System.err.println( "Exception: " + e );
       e.printStackTrace();
    }
  }
}

