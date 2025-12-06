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
package com.meterware.httpunit;

import com.meterware.httpunit.protocol.MessageBody;
import com.meterware.httpunit.protocol.ParameterCollection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * A web request which contains a non-empty message body. Note that such requests <em>must</em> use the
 * <code>http</code> or <code>https</code> protocols.
 **/
public abstract class MessageBodyWebRequest extends WebRequest {

    /** The body. */
    protected MessageBody _body;

    /** The mime encoded. */
    private boolean _mimeEncoded;

    /**
     * Constructs a web request using a specific absolute url string.
     *
     * @param urlString
     *            the url string
     * @param mimeEncoded
     *            the mime encoded
     */
    protected MessageBodyWebRequest(String urlString, boolean mimeEncoded) {
        super(urlString);
        _mimeEncoded = mimeEncoded;
    }

    /**
     * Constructs a web request using a specific absolute url string.
     *
     * @param urlString
     *            the url string
     * @param messageBody
     *            the message body
     */
    protected MessageBodyWebRequest(String urlString, MessageBody messageBody) {
        super(urlString);
        _body = messageBody;
    }

    /**
     * Constructs a web request with a specific target.
     *
     * @param urlBase
     *            the url base
     * @param urlString
     *            the url string
     * @param target
     *            the target
     * @param mimeEncoded
     *            the mime encoded
     */
    protected MessageBodyWebRequest(URL urlBase, String urlString, String target, boolean mimeEncoded) {
        super(urlBase, urlString, target);
        _mimeEncoded = mimeEncoded;
    }

    /**
     * Constructs a web request for a form submitted via a button.
     *
     * @param sourceForm
     *            the source form
     * @param parameterHolder
     *            the parameter holder
     * @param button
     *            the button
     * @param x
     *            the x
     * @param y
     *            the y
     */
    protected MessageBodyWebRequest(WebForm sourceForm, ParameterHolder parameterHolder, SubmitButton button, int x,
            int y) {
        super(sourceForm, parameterHolder, button, x, y);
        _mimeEncoded = parameterHolder.isSubmitAsMime();
        setHeaderField(REFERER_HEADER_NAME, sourceForm.getBaseURL().toExternalForm());
    }

    /**
     * Constructs a web request for a form submitted via script.
     *
     * @param sourceForm
     *            the source form
     */
    protected MessageBodyWebRequest(WebForm sourceForm) {
        super(sourceForm, WebRequest.newParameterHolder(sourceForm));
        _mimeEncoded = sourceForm.isSubmitAsMime();
        setHeaderField(REFERER_HEADER_NAME, sourceForm.getBaseURL().toExternalForm());
    }

    /**
     * Subclasses may override this method to provide a message body for the request.
     *
     * @return the message body
     */
    protected MessageBody getMessageBody() {
        return _body;
    }

    // ---------------------------------- WebRequest methods --------------------------------

    @Override
    protected void writeMessageBody(OutputStream stream) throws IOException {
        getMessageBody().writeTo(stream, getParameterHolder());
    }

    /**
     * Performs any additional processing necessary to complete the request.
     **/
    @Override
    protected void completeRequest(URLConnection connection) throws IOException {
        super.completeRequest(connection);
        connection.setDoInput(true);
        connection.setDoOutput(true);

        OutputStream stream = connection.getOutputStream();
        writeMessageBody(stream);
        stream.flush();
        stream.close();
    }

    @Override
    protected String getContentType() {
        return getMessageBody().getContentType();
    }

    @Override
    public boolean isMimeEncoded() {
        return _mimeEncoded;
    }

    // ============================= class InputStreamMessageBody ======================================

    /**
     * A method request message body read directly from an input stream.
     **/
    public static class InputStreamMessageBody extends MessageBody {

        /**
         * Instantiates a new input stream message body.
         *
         * @param source
         *            the source
         * @param contentType
         *            the content type
         */
        public InputStreamMessageBody(InputStream source, String contentType) {
            super(null);
            _source = source;
            _contentType = contentType;
        }

        /**
         * Returns the content type of this message body.
         *
         * @return the content type
         */
        @Override
        public String getContentType() {
            return _contentType;
        }

        /**
         * Transmits the body of this request as a sequence of bytes.
         *
         * @param outputStream
         *            the output stream
         * @param parameters
         *            the parameters
         *
         * @throws IOException
         *             if the tranmission fails
         */
        @Override
        public void writeTo(OutputStream outputStream, ParameterCollection parameters) throws IOException {
            if (_source.markSupported()) {
                mark();
            }
            byte[] buffer = new byte[8 * 1024];
            int count = 0;
            do {
                outputStream.write(buffer, 0, count);
                count = _source.read(buffer, 0, buffer.length);
            } while (count != -1);

            written = true;
        }

        /**
         * Mark.
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        public void mark() throws IOException {
            if (written) {
                _source.reset();
            } else {
                // amount of bytes to be read after mark gets invalid
                int readlimit = 1024 * 1024; // ! MByte
                // Marks the current position in this input stream.
                // A subsequent call to the reset method repositions
                // this stream at the last marked position so that subsequent reads re-read the same bytes.
                _source.mark(readlimit);
            }
        }

        /** The written. */
        private boolean written = false;

        /** The source. */
        private InputStream _source;

        /** The content type. */
        private String _contentType;
    }
}
