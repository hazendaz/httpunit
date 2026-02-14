/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.meterware.pseudoserver.PseudoServlet;
import com.meterware.pseudoserver.WebResource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

/**
 * A unit test to verify miscellaneous requests with message bodies.
 **/
class MessageBodyRequestTest extends HttpUnitTest {

    /**
     * make a Request from the given parameters.
     *
     * @param resourceName
     *            the resource name
     * @param sourceData
     *            the source data
     * @param contentType
     *            the content type
     *
     * @return the new WebRequest
     */
    public WebRequest makeRequest(String resourceName, String sourceData, String contentType) {
        defineResource(resourceName, new BodyEcho());
        InputStream source = new ByteArrayInputStream(sourceData.getBytes(StandardCharsets.UTF_8));
        return new PostMethodWebRequest(getHostPath() + "/" + resourceName, source, contentType);
    }

    /**
     * test a generic Post request.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void genericPostRequest() throws Exception {
        WebConversation wc = new WebConversation();
        String sourceData = "This is an interesting test\nWith two lines";
        WebRequest wr = makeRequest("ReportData", sourceData, "text/sample");
        WebResponse response = wc.getResponse(wr);
        assertEquals("\nPOST\n" + sourceData, response.getText(), "Body response");
        assertEquals("text/sample", response.getContentType(), "Content-type");
    }

    /**
     * test for Patch by Serge Maslyukov for empty content Types.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void emptyContentType() throws Exception {
        WebConversation wc = new WebConversation();
        String emptyContentType = ""; // this is an emptyContentType
        WebRequest wr = makeRequest("something", "some data", emptyContentType);
        wc.getResponse(wr);
        assertEquals("", wr.getContentType(), "Content-type");
    }

    /**
     * Put request.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void putRequest() throws Exception {
        defineResource("ReportData", new BodyEcho());
        String sourceData = "This is an interesting test\nWith two lines";
        InputStream source = new ByteArrayInputStream(sourceData.getBytes(StandardCharsets.UTF_8));

        WebConversation wc = new WebConversation();
        WebRequest wr = new PutMethodWebRequest(getHostPath() + "/ReportData", source, "text/plain");
        WebResponse response = wc.getResponse(wr);
        assertEquals("\nPUT\n" + sourceData, response.getText(), "Body response");
    }

    /**
     * test for download problem described by Oliver Wahlen.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void downloadRequestUsingGetText() throws Exception {
        defineResource("ReportData", new BodyEcho());
        byte[] binaryData = new byte[256];
        for (int i = 0; i <= 255; i++) {
            binaryData[i] = (byte) i;
        }

        InputStream source = new ByteArrayInputStream(binaryData);

        WebConversation wc = new WebConversation();
        WebRequest wr = new PutMethodWebRequest(getHostPath() + "/ReportData", source, "application/random");
        WebResponse response = wc.getResponse(wr);
        // currently the following line does not work:
        byte[] download = response.getText().getBytes(StandardCharsets.UTF_8);
        // currently the following line works:
        // byte[] download = getDownload( response );
        // and this one is now available ...
        download = response.getBytes();
        assertArrayEquals(binaryData, download, "Body response");
    }

    /**
     * Download request.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void downloadRequest() throws Exception {
        defineResource("ReportData", new BodyEcho());
        byte[] binaryData = { 0x01, 0x05, 0x0d, 0x0a, 0x02 };

        InputStream source = new ByteArrayInputStream(binaryData);

        WebConversation wc = new WebConversation();
        WebRequest wr = new PutMethodWebRequest(getHostPath() + "/ReportData", source, "application/random");
        WebResponse response = wc.getResponse(wr);
        byte[] download = response.getBytes();
        assertArrayEquals(binaryData, download, "Body response");

        download = getDownload(response);
        assertArrayEquals(binaryData, download, "Body response");

        download = response.getBytes();
        assertArrayEquals(binaryData, download, "Body response");
    }

    /**
     * test for BR [ 1964665 ] HeaderOnlyRequest cannot be constructed.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void headerOnlyWebRequest() throws Exception {
        assertDoesNotThrow(() -> {
            new HeaderOnlyWebRequest("http://www.google.com");
        });
    }

    /**
     * please do not copy this function any more - use getBytes instead ...
     *
     * @param response
     *            the response
     *
     * @return the download
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private byte[] getDownload(WebResponse response) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        InputStream inputStream = response.getInputStream();

        byte[] buffer = new byte[8 * 1024];
        int count = 0;
        do {
            outputStream.write(buffer, 0, count);
            count = inputStream.read(buffer, 0, buffer.length);
        } while (count != -1);

        inputStream.close();
        return outputStream.toByteArray();
    }

}

class BodyEcho extends PseudoServlet {
    /**
     * Returns a resource object as a result of a get request.
     **/
    @Override
    public WebResource getResponse(String method) {
        String contentType = getHeader("Content-type");
        if (contentType.startsWith("text")) {
            return new WebResource("\n" + method + "\n" + new String(getBody(), StandardCharsets.UTF_8), contentType);
        }
        return new WebResource(getBody(), contentType);
    }
}
