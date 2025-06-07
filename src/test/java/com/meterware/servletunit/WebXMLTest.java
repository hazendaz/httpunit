/*
 * MIT License
 *
 * Copyright 2011-2025 Russell Gold
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.meterware.httpunit.AuthorizationRequiredException;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HttpException;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.HttpUnitUtils;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Tests for Web xml access
 */
public class WebXMLTest {
    private static final String TEST_TARGET_PATH = "target/build";

    /**
     * if the dtd file is not on the CLASSPATH there will be nasty java.net.MalformedURLException problems for the
     * Eclipse environment we'll give advice what to do
     *
     * @throws Exception
     */
    // TODO This test is only applicable when using a concrete servlet implementation such as tomcat-servlet-api and
    // thus unnecessary to test in this code base.
    @Disabled
    @Test
    void dtdClassPath() throws Exception {
        boolean isDtdOnClasspath = WebXMLString.isDtdOnClasspath();
        String msg = WebXMLString.dtd
                + " should be on CLASSPATH - you might want to check that META-INF is on the CLASSPATH";
        if (!isDtdOnClasspath) {
            System.err.println(msg);
            if (HttpUnitUtils.isEclipse()) {
                System.err.println(
                        "You seem to be running in the Eclipse environment you might want to check the project settings build path to include the META-INF directory");
                System.err.println(
                        "To do this select properties/Java Build Path from your project, click 'Add Class Folder' and select the META-INF directory of the httpunit project");
            }
            System.err.println(
                    "the other tests will work around the problem by changing the DOCTYPE to avoid lots of java.net.MalformedURLExceptions");
        }
        assertTrue(isDtdOnClasspath, msg);
    }

    @Test
    void basicAccess() throws Exception {

        WebXMLString wxs = new WebXMLString();
        wxs.addServlet("/SimpleServlet", SimpleGetServlet.class);
        File webXml = createWebXml(wxs);

        ServletRunner sr = new ServletRunner(webXml);
        WebRequest request = new GetMethodWebRequest("http://localhost/SimpleServlet");
        WebResponse response = sr.getResponse(request);
        assertNotNull(response, "No response received");
        assertEquals("text/html", response.getContentType(), "content type");
        assertEquals(SimpleGetServlet.RESPONSE_TEXT, response.getText(), "requested resource");
    }

    @Test
    void realPath() throws Exception {

        WebXMLString wxs = new WebXMLString();
        wxs.addServlet("/SimpleServlet", SimpleGetServlet.class);
        File webXml = createWebXml(new File(TEST_TARGET_PATH + "/base"), wxs);

        assertRealPath("path with no context", new ServletRunner(webXml), new File("something.txt"), "/something.txt");
        assertRealPath("path with context", new ServletRunner(webXml, "/testing"),
                new File(TEST_TARGET_PATH + "/base/something.txt"), "/something.txt");
        // attempt for an assertion a long the line of bug report [ 1113728 ] getRealPath throws
        // IndexOutOfBoundsException on empty string
        // TODO check what was meant by Adrian Baker
        // assertRealPath( "empty path with context", new ServletRunner( webXml, "/testing" ), new File( "" ),
        // "/testing" );
        assertRealPath("path with no context, no slash", new ServletRunner(webXml), new File("something.txt"),
                "something.txt");
        assertRealPath("path with context, no slash", new ServletRunner(webXml, "/testing"),
                new File(TEST_TARGET_PATH + "/base/something.txt"), "something.txt");
    }

    private void assertRealPath(String comment, ServletRunner sr, File expectedFile, String relativePath) {
        String realPath = sr.getSession(true).getServletContext().getRealPath(relativePath);
        assertEquals(expectedFile.getAbsolutePath(), realPath, comment);
    }

    private File createWebXml(WebXMLString wxs) throws IOException {
        return createWebXml(new File(TEST_TARGET_PATH), wxs);
    }

    private File createWebXml(File parent, WebXMLString wxs) throws IOException {
        File dir = new File(parent, "META-INF");
        dir.mkdirs();
        File webXml = new File(dir, "web.xml");
        FileOutputStream fos = new FileOutputStream(webXml);
        fos.write(wxs.asText().getBytes(StandardCharsets.UTF_8));
        fos.close();
        return webXml;
    }

    @Test
    void basicAuthenticationConfig() throws Exception {
        WebXMLString wxs = new WebXMLString();
        wxs.requireBasicAuthentication("SampleRealm");

        WebApplication app = new WebApplication(newDocument(wxs.asText()));
        assertTrue(app.usesBasicAuthentication(), "Did not detect basic authentication");
        assertEquals("SampleRealm", app.getAuthenticationRealm(), "Realm name");
    }

    @Test
    void formAuthenticationConfig() throws Exception {
        WebXMLString wxs = new WebXMLString();
        wxs.requireFormAuthentication("SampleRealm", "/Login", "/Error");

        WebApplication app = new WebApplication(newDocument(wxs.asText()));
        assertTrue(app.usesFormAuthentication(), "Did not detect form-based authentication");
        assertEquals("SampleRealm", app.getAuthenticationRealm(), "Realm name");
        assertEquals("/Login", app.getLoginURL().getFile(), "Login path");
        assertEquals("/Error", app.getErrorURL().getFile(), "Error path");
    }

    @Test
    void securityConstraint() throws Exception {
        WebXMLString wxs = new WebXMLString();
        wxs.addSecureURL("SecureArea1", "/SimpleServlet");
        wxs.addAuthorizedRole("SecureArea1", "supervisor");

        WebApplication app = new WebApplication(newDocument(wxs.asText()));
        assertTrue(app.requiresAuthorization(new URL("http://localhost/SimpleServlet")),
                "Did not require authorization");
        assertFalse(app.requiresAuthorization(new URL("http://localhost/FreeServlet")),
                "Should not require authorization");

        List roles = Arrays.asList(app.getPermittedRoles(new URL("http://localhost/SimpleServlet")));
        assertTrue(roles.contains("supervisor"), "Should have access");
        assertFalse(roles.contains("peon"), "Should not have access");
    }

    /**
     * Verifies that the default display name is null.
     */
    @Test
    void defaultContextNameConfiguration() throws Exception {
        WebXMLString wxs = new WebXMLString();
        WebApplication app = new WebApplication(newDocument(wxs.asText()));
        assertNull(app.getDisplayName(), "Context name should default to null");
    }

    /**
     * Verifies that a web application can read its display name from the configuration.
     *
     * @throws Exception
     */
    @Test
    void contextNameConfiguration() throws Exception {
        WebXMLString wxs = new WebXMLString();
        wxs.setDisplayName("samples");
        wxs.addServlet("simple", "/SimpleServlet", SimpleGetServlet.class);
        WebApplication app = new WebApplication(newDocument(wxs.asText()));
        assertEquals("samples", app.getDisplayName(), "Display name");

        ServletRunner sr = new ServletRunner(wxs.asInputStream());
        ServletUnitClient client = sr.newClient();
        InvocationContext ic = client.newInvocation("http://localhost/SimpleServlet");
        ServletContext servletContext = ic.getServlet().getServletConfig().getServletContext();
        assertEquals("samples", servletContext.getServletContextName(), "Context name");
    }

    @Test
    void servletParameters() throws Exception {
        WebXMLString wxs = new WebXMLString();
        Properties params = new Properties();
        params.setProperty("color", "red");
        params.setProperty("age", "12");
        wxs.addServlet("simple", "/SimpleServlet", SimpleGetServlet.class, params);

        ServletRunner sr = new ServletRunner(wxs.asInputStream());
        ServletUnitClient client = sr.newClient();
        InvocationContext ic = client.newInvocation("http://localhost/SimpleServlet");
        ServletConfig servletConfig = ic.getServlet().getServletConfig();
        assertEquals("simple", servletConfig.getServletName(), "Servlet name");
        assertNull(servletConfig.getInitParameter("gender"), "init parameter 'gender' should be null");
        assertEquals("red", ic.getServlet().getServletConfig().getInitParameter("color"), "init parameter via config");
        assertEquals("12", ((HttpServlet) ic.getServlet()).getInitParameter("age"), "init parameter directly");
    }

    @Test
    void contextParameters() throws Exception {
        WebXMLString wxs = new WebXMLString();
        wxs.addServlet("/SimpleServlet", SimpleGetServlet.class);
        wxs.addContextParam("icecream", "vanilla");
        wxs.addContextParam("cone", "waffle");
        wxs.addContextParam("topping", "");

        ServletRunner sr = new ServletRunner(wxs.asInputStream());
        ServletUnitClient client = sr.newClient();
        assertEquals("vanilla", sr.getContextParameter("icecream"), "Context parameter 'icecream'");
        InvocationContext ic = client.newInvocation("http://localhost/SimpleServlet");

        jakarta.servlet.ServletContext sc = ((HttpServlet) ic.getServlet()).getServletContext();
        assertNotNull(sc, "ServletContext should not be null");
        assertEquals("vanilla", sc.getInitParameter("icecream"), "ServletContext.getInitParameter()");
        assertEquals("waffle", sc.getInitParameter("cone"), "init parameter: cone");
        assertEquals("", sc.getInitParameter("topping"), "init parameter: topping");
        assertNull(sc.getInitParameter("shoesize"), "ServletContext.getInitParameter() should be null");
    }

    /**
     * test for Patch [ 1838699 ] setContextParameter in ServletRunner
     *
     * @throws Exception
     */
    public void xtestSetContextParameter() throws Exception {
        WebXMLString wxs = new WebXMLString();
        wxs.addServlet("/SimpleServlet", SimpleGetServlet.class);
        wxs.addContextParam("icecream", "vanilla");

        ServletRunner sr = new ServletRunner(wxs.asInputStream());
        ServletUnitClient client = sr.newClient();
        sr.setContextParameter("icecream", "strawberry");
        assertEquals("strawberry", sr.getContextParameter("icecream"), "Context parameter 'icecream'");

        InvocationContext ic = client.newInvocation("http://localhost/SimpleServlet");

        jakarta.servlet.ServletContext sc = ((HttpServlet) ic.getServlet()).getServletContext();
        assertNotNull(sc, "ServletContext should not be null");
        assertEquals("strawberry", sc.getInitParameter("icecream"), "ServletContext.getInitParameter()");
        assertNull(sc.getInitParameter("shoesize"), "ServletContext.getInitParameter() should be null");
    }

    /**
     * create a new document based on the given contents
     *
     * @param contents
     *
     * @return the new document
     *
     * @throws SAXException
     * @throws IOException
     */
    private Document newDocument(String contents) throws SAXException, IOException {
        return HttpUnitUtils.parse(toInputStream(contents));
    }

    private ByteArrayInputStream toInputStream(String contents) {
        return new ByteArrayInputStream(contents.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void basicAuthorization() throws Exception {
        WebXMLString wxs = new WebXMLString();
        wxs.addServlet("/SimpleServlet", SimpleGetServlet.class);
        wxs.requireBasicAuthentication("Sample Realm");
        wxs.addSecureURL("SecureArea1", "/SimpleServlet");
        wxs.addAuthorizedRole("SecureArea1", "supervisor");

        ServletRunner sr = new ServletRunner(wxs.asInputStream());
        ServletUnitClient wc = sr.newClient();
        try {
            wc.getResponse("http://localhost/SimpleServlet");
            fail("Did not insist on validation for access to servlet");
        } catch (AuthorizationRequiredException e) {
            assertEquals("Sample Realm", e.getAuthenticationParameter("realm"), "Realm");
            assertEquals("Basic", e.getAuthenticationScheme(), "Method");
        }

        try {
            wc.setAuthorization("You", "peon");
            wc.getResponse("http://localhost/SimpleServlet");
            fail("Permitted wrong user to access");
        } catch (HttpException e) {
            assertEquals(403, e.getResponseCode(), "Response code");
        }

        wc.setAuthorization("Me", "supervisor,agent");
        wc.getResponse("http://localhost/SimpleServlet");

        InvocationContext ic = wc.newInvocation("http://localhost/SimpleServlet");
        assertEquals("Me", ic.getRequest().getRemoteUser(), "Authenticated user");
        assertFalse(ic.getRequest().isUserInRole("bogus"), "User assigned to 'bogus' role");
        assertTrue(ic.getRequest().isUserInRole("supervisor"), "User not assigned to 'supervisor' role");
    }

    @Test
    void formAuthentication() throws Exception {
        HttpUnitOptions.setLoggingHttpHeaders(true);
        WebXMLString wxs = new WebXMLString();
        wxs.addServlet("/Logon", SimpleLogonServlet.class);
        wxs.addServlet("/Error", SimpleErrorServlet.class);
        wxs.addServlet("/Example/SimpleServlet", SimpleGetServlet.class);
        wxs.requireFormAuthentication("Sample Realm", "/Logon", "/Error");
        wxs.addSecureURL("SecureArea1", "/Example/SimpleServlet");
        wxs.addAuthorizedRole("SecureArea1", "supervisor");
        File webXml = createWebXml(wxs);

        ServletRunner sr = new ServletRunner(webXml, "/samples");
        ServletUnitClient wc = sr.newClient();
        WebResponse response = wc.getResponse("http://localhost/samples/Example/SimpleServlet");
        WebForm form = response.getFormWithID("login");
        assertNotNull(form, "did not find login form");

        WebRequest request = form.getRequest();
        request.setParameter("j_username", "Me");
        request.setParameter("j_password", "supervisor");
        response = wc.getResponse(request);
        assertNotNull(response, "No response received after authentication");
        assertEquals("text/html", response.getContentType(), "content type");
        assertEquals(SimpleGetServlet.RESPONSE_TEXT, response.getText(), "requested resource");

        InvocationContext ic = wc.newInvocation("http://localhost/samples/Example/SimpleServlet");
        assertEquals("Me", ic.getRequest().getRemoteUser(), "Authenticated user");
        assertFalse(ic.getRequest().isUserInRole("bogus"), "User assigned to 'bogus' role");
        assertTrue(ic.getRequest().isUserInRole("supervisor"), "User not assigned to 'supervisor' role");
    }

    @Test
    void getContextPath() throws Exception {
        WebXMLString wxs = new WebXMLString();
        wxs.addServlet("/SimpleServlet", SimpleGetServlet.class);

        ServletRunner sr = new ServletRunner(wxs.asInputStream(), "/mount");
        ServletUnitClient wc = sr.newClient();
        InvocationContext ic = wc.newInvocation("http://localhost/mount/SimpleServlet");
        assertEquals("/mount", ic.getRequest().getContextPath());

        sr = new ServletRunner(wxs.asInputStream());
        wc = sr.newClient();
        ic = wc.newInvocation("http://localhost/SimpleServlet");
        assertEquals("", ic.getRequest().getContextPath());
    }

    @Test
    void mountContextPath() throws Exception {
        WebXMLString wxs = new WebXMLString();
        wxs.addServlet("/SimpleServlet", SimpleGetServlet.class);

        ServletRunner sr = new ServletRunner(wxs.asInputStream(), "/mount");
        ServletUnitClient wc = sr.newClient();
        InvocationContext ic = wc.newInvocation("http://localhost/mount/SimpleServlet");
        assertTrue(ic.getServlet() instanceof SimpleGetServlet);
        assertEquals("/mount/SimpleServlet", ic.getRequest().getRequestURI());

        try {
            ic = wc.newInvocation("http://localhost/SimpleServlet");
            ic.getServlet();
            fail("Attempt to access url outside of the webapp context path should have thrown a 404");
        } catch (com.meterware.httpunit.HttpNotFoundException e) {
        }
    }

    @Test
    void servletMapping() throws Exception {
        WebXMLString wxs = new WebXMLString();
        wxs.addServlet("/foo/bar/*", Servlet1.class);
        wxs.addServlet("/baz/*", Servlet2.class);
        wxs.addServlet("/catalog", Servlet3.class);
        wxs.addServlet("*.bop", Servlet4.class);
        wxs.addServlet("/", Servlet5.class);
        ServletRunner sr = new ServletRunner(wxs.asInputStream());
        ServletUnitClient wc = sr.newClient();

        checkMapping(wc, "http://localhost/foo/bar/index.html", Servlet1.class, "/foo/bar", "/index.html");
        checkMapping(wc, "http://localhost/foo/bar/index.bop", Servlet1.class, "/foo/bar", "/index.bop");
        checkMapping(wc, "http://localhost/baz", Servlet2.class, "/baz", null);
        checkMapping(wc, "http://localhost/baz/index.html", Servlet2.class, "/baz", "/index.html");
        checkMapping(wc, "http://localhost/catalog", Servlet3.class, "/catalog", null);
        checkMapping(wc, "http://localhost/catalog/racecar.bop", Servlet4.class, "/catalog/racecar.bop", null);
        checkMapping(wc, "http://localhost/index.bop", Servlet4.class, "/index.bop", null);
        checkMapping(wc, "http://localhost/something/else", Servlet5.class, "/something/else", null);
    }

    private void checkMapping(ServletUnitClient wc, final String url, final Class servletClass,
            final String expectedPath, final String expectedInfo) throws IOException, ServletException {
        InvocationContext ic = wc.newInvocation(url);
        assertTrue(servletClass.isInstance(ic.getServlet()),
                "selected servlet is " + ic.getServlet() + " rather than " + servletClass);
        assertEquals(expectedPath, ic.getRequest().getServletPath(), "ServletPath for " + url);
        assertEquals(expectedInfo, ic.getRequest().getPathInfo(), "ServletInfo for " + url);
    }

    /**
     * Verifies that only those servlets designated will pre-load when the application is initialized. SimpleGetServlet
     * and each of its subclasses adds its classname to the 'initialized' context attribute.
     */
    @Test
    void loadOnStartup() throws Exception {
        WebXMLString wxs = new WebXMLString();
        wxs.addServlet("servlet1", "one", Servlet1.class);
        wxs.setLoadOnStartup("servlet1");
        wxs.addServlet("servlet2", "two", Servlet2.class);
        wxs.addServlet("servlet3", "three", Servlet3.class);

        ServletRunner sr = new ServletRunner(toInputStream(wxs.asText()));
        ServletUnitClient wc = sr.newClient();
        InvocationContext ic = wc.newInvocation("http://localhost/three");
        assertEquals("Servlet1,Servlet3",
                ic.getServlet().getServletConfig().getServletContext().getAttribute("initialized"),
                "Initialized servlets");
    }

    /**
     * Verifies that servlets pre-load in the order specified. SimpleGetServlet and each of its subclasses adds its
     * classname to the 'initialized' context attribute.
     */
    @Test
    void loadOrder() throws Exception {
        WebXMLString wxs = new WebXMLString();
        wxs.addServlet("servlet1", "one", Servlet1.class);
        wxs.setLoadOnStartup("servlet1", 2);
        wxs.addServlet("servlet2", "two", Servlet2.class);
        wxs.setLoadOnStartup("servlet2", 3);
        wxs.addServlet("servlet3", "three", Servlet3.class);
        wxs.setLoadOnStartup("servlet3", 1);

        ServletRunner sr = new ServletRunner(toInputStream(wxs.asText()));
        ServletUnitClient wc = sr.newClient();
        InvocationContext ic = wc.newInvocation("http://localhost/two");
        assertEquals("Servlet3,Servlet1,Servlet2",
                ic.getServlet().getServletConfig().getServletContext().getAttribute("initialized"),
                "Initialized servlets");
    }

    // ===============================================================================================================

    // ===============================================================================================================

    static class SimpleLogonServlet extends HttpServlet {
        private static final long serialVersionUID = 1L;
        static String RESPONSE_TEXT = "<html><body>\r\n"
                + "<form id='login' action='j_security_check' method='POST'>\r\n" + "  <input name='j_username' />\r\n"
                + "  <input type='password' name='j_password' />\r\n" + "</form></body></html>";

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType("text/html");
            PrintWriter pw = resp.getWriter();
            pw.print(RESPONSE_TEXT);
            pw.close();
        }
    }

    // ===============================================================================================================

    static class SimpleErrorServlet extends HttpServlet {
        private static final long serialVersionUID = 1L;
        static String RESPONSE_TEXT = "<html><body>Sorry could not login</body></html>";

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType("text/html");
            PrintWriter pw = resp.getWriter();
            pw.print(RESPONSE_TEXT);
            pw.close();
        }
    }

    // ===============================================================================================================

    static class SimpleGetServlet extends HttpServlet {
        private static final long serialVersionUID = 1L;
        static String RESPONSE_TEXT = "the desired content\r\n";

        @Override
        public void init() throws ServletException {
            ServletConfig servletConfig = getServletConfig();
            String initialized = (String) servletConfig.getServletContext().getAttribute("initialized");
            if (initialized == null) {
                initialized = getLocalName();
            } else {
                initialized = initialized + "," + getLocalName();
            }
            servletConfig.getServletContext().setAttribute("initialized", initialized);
        }

        private String getLocalName() {
            String className = getClass().getName();
            int dollarIndex = className.indexOf('$');
            if (dollarIndex < 0) {
                return className;
            }
            return className.substring(dollarIndex + 1);
        }

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType("text/html");
            PrintWriter pw = resp.getWriter();
            pw.print(RESPONSE_TEXT);
            pw.close();
        }
    }

    static class Servlet1 extends SimpleGetServlet {

        private static final long serialVersionUID = 1L;
    }

    static class Servlet2 extends SimpleGetServlet {

        private static final long serialVersionUID = 1L;
    }

    static class Servlet3 extends SimpleGetServlet {

        private static final long serialVersionUID = 1L;
    }

    static class Servlet4 extends SimpleGetServlet {

        private static final long serialVersionUID = 1L;
    }

    static class Servlet5 extends SimpleGetServlet {

        private static final long serialVersionUID = 1L;
    }

}
