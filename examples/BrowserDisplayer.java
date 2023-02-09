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
import com.meterware.httpunit.*;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;

/**
 * This example is influenced from
 * http://m0smith.freeshell.org/blog-portletunit/2008/05/bare
 * -bones-browser-launch-for-java-use
 * 
 * @author Matthew O. Smith
 * 
 */
public class BrowserDisplayer {
	/**
	 * Show the response in a browser.
	 * 
	 * @param response
	 *          the response
	 * @throws Exception
	 *           on error
	 */
	public static void showResponseInBrowser(WebResponse response)
			throws Exception {
		String text = response.getText();
		File f = File.createTempFile("httpUnit", ".html");
		f.deleteOnExit();
		PrintWriter fod = new PrintWriter(new FileOutputStream(f));
		if (!text.startsWith("<?xml")) {
			fod.print("<head><base href=\"'http://localhost'/\"> </head>");
		}
		fod.print(text);
		fod.close();
		URL url = f.toURL();
		openURL(url);
	}

	static final String[] browsers = { "google-chrome", "firefox", "opera",
			"epiphany", "konqueror", "conkeror", "midori", "kazehakase", "mozilla" };

	static final String errMsg = "Error attempting to launch web browser";

	/**
	 * Bare Bones Browser Launch Version 1.5 (December 10, 2005) By Dem Pilafian.
	 * Supports: Mac OS X, GNU/Linux, Unix, Windows XP
	 * 
	 * Example Usage: String url = "http://www.centerkey.com/";
	 * BareBonesBrowserLaunch.openURL(url); Public Domain Software -- Free to Use
	 * as You Like
	 * 
	 * @see http://www.centerkey.com/java/browser/
	 * 
	 * @param url
	 *          the url to open
	 * @throws ClassNotFoundException
	 *           getting class
	 * @throws NoSuchMethodException
	 *           yes
	 * @throws SecurityException
	 *           well
	 * @throws InvocationTargetException
	 *           trying to invloke
	 * @throws IllegalAccessException
	 *           trying to access
	 * @throws IllegalArgumentException
	 *           bad arguement
	 * @throws IOException
	 *           opening window
	 * @throws InterruptedException
	 *           waiting
	 */
	public static void openURL(URL url) throws ClassNotFoundException,
			NoSuchMethodException, IllegalAccessException, InvocationTargetException,
			IOException, InterruptedException {
		String osName = System.getProperty("os.name");
		String urltext=url.toString();
	  // Try java desktop API first (new in Java 1.6)
   	// basically: java.awt.Desktop.getDesktop().browse(new URI(url));
   	try {
   	    Class desktop = Class.forName("java.awt.Desktop");
   	    Method getDesktop = desktop.getDeclaredMethod("getDesktop", new Class[] {});
   	    Object desktopInstance = getDesktop.invoke(null, new Object[] {});
   	    Method browse = desktop.getDeclaredMethod("browse", new Class[] {URI.class});
   	    URI uri = new URI(urltext);
   	    //logger.fine("Using Java Desktop API to open URL '"+url+"'");
   	    browse.invoke(desktopInstance, new Object[] {uri});
   	    return;
   	} catch(Exception e) { }
   	
   	// Failed, resort to executing the browser manually

		
		if (osName.startsWith("Mac OS")) {
			Class fileMgr = Class.forName("com.apple.eio.FileManager");
			Method openURL = fileMgr.getDeclaredMethod("openURL",
					new Class[] { String.class });
			openURL.invoke(null, new Object[] { urltext });
		} else if (osName.startsWith("Windows")) {
			String cmdLine = "rundll32 url.dll,FileProtocolHandler " + urltext;
			Process exec = Runtime.getRuntime().exec(cmdLine);
			exec.waitFor();
		} else { // assume Unix or Linux
			String browser = null;
			for (int count = 0; count < browsers.length && browser == null; count++) {
				if (Runtime.getRuntime()
						.exec(new String[] { "which", browsers[count] }).waitFor() == 0) {
					browser = browsers[count];
				}
			}
			if (browser == null) {
				throw new IllegalStateException(errMsg);
			} else {
				Runtime.getRuntime().exec(new String[] { browser, urltext });
			}
		}
	}

	/**
	 * open a given url indirectly
	 * 
	 * @param params
	 */
	public static void main(String[] params) {
		try {
			if (params.length < 1) {
				System.out.println("Usage: java BrowserDisplay [url]");
				System.out.println("");
				System.out
						.println("will demonstrate usage with the url 'http://www.meterware.com' now ...");
				String[] defaultParams = { "http://www.meterware.com" };
				params = defaultParams;
			}
			// direct call first
			String url = params[0];
			openURL(new URL(url));
			// and now indirectly
			// create the conversation object which will maintain state for us
			WebConversation wc = new WebConversation();

			// Obtain the main page on the meterware web site
			WebRequest request = new GetMethodWebRequest(url);
			WebResponse response = wc.getResponse(request);
			showResponseInBrowser(response);

			// find the link which contains the string "HttpUnit" and click it
			WebLink httpunitLink = response.getFirstMatchingLink(
					WebLink.MATCH_CONTAINED_TEXT, "HttpUnit");
			response = httpunitLink.click();
			showResponseInBrowser(response);
			System.out.println("Your browser should show three pages now:");
			System.out.println("1. a direct invocation of " + url);
			System.out.println("2. an indirect invocation of " + url
					+ " via httpunit");
			System.out
					.println("3. the result httpunit clicking the httpunit link on 2.");

		} catch (Exception e) {
			System.err.println("Exception: " + e);
		}
	}
}
