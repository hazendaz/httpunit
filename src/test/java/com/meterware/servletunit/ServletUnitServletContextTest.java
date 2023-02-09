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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.InputStream;
import java.net.URL;

import javax.servlet.ServletContext;

import org.junit.Test;

public class ServletUnitServletContextTest {
	private static final String EXISTENT_RESOURCE_PATH = "src/test/resources/existent.xml";
	private static final String NONEXISTENT_RESOURCE_PATH = "src/test/resources/nonexistent.xml";
	
	@Test
	public void testGetResource() throws Exception {
		WebApplication webapp = new WebApplication();
		ServletContext sc = new ServletUnitServletContext(webapp);

		// for existent resources
		InputStream is = sc.getResourceAsStream(EXISTENT_RESOURCE_PATH);
		assertNotNull("must not return a null", is);
		is.close();
		
		URL r = sc.getResource(EXISTENT_RESOURCE_PATH);
		assertNotNull("must not return a null", r);

		// for non-existent resources
		is = sc.getResourceAsStream(NONEXISTENT_RESOURCE_PATH);
		assertNull("must return a null", is);
		
		r = sc.getResource(NONEXISTENT_RESOURCE_PATH);
		assertNull("must return a null", r);

	}

}
