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
package com.meterware.httpunit.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A POST-method message body which is MIME-encoded. This is used when uploading files, and is selected when the enctype
 * parameter of a form is set to "multi-part/form-data".
 **/
class MimeEncodedMessageBody extends MessageBody {

    MimeEncodedMessageBody(String characterSet) {
        super(characterSet);
    }

    /**
     * Returns the content type of this message body.
     **/
    @Override
    public String getContentType() {
        return "multipart/form-data; boundary=" + BOUNDARY;
    }

    /**
     * Transmits the body of this request as a sequence of bytes.
     **/
    @Override
    public void writeTo(OutputStream outputStream, ParameterCollection parameters) throws IOException {
        MimeEncoding encoding = new MimeEncoding(outputStream);
        parameters.recordParameters(encoding);
        encoding.sendClose();
    }

    private static final String BOUNDARY = "--HttpUnit-part0-aSgQ2M";
    private static final byte[] CRLF = { 0x0d, 0x0A };

    private String encode(String string) {
        char[] chars = string.toCharArray();
        StringBuilder sb = new StringBuilder(chars.length + 20);
        for (char element : chars) {
            if (element == '\\') {
                sb.append("\\\\"); // accomodate MS-DOS file paths ??? is this safe??
            } else {
                sb.append(element);
            }
        }
        return sb.toString();
    }

    private void writeLn(OutputStream os, String value, String encoding) throws IOException {
        os.write(value.getBytes(encoding));
        os.write(CRLF);
    }

    private void writeLn(OutputStream os, String value) throws IOException {
        writeLn(os, value, getCharacterSet());
    }

    class MimeEncoding implements ParameterProcessor {

        public MimeEncoding(OutputStream outputStream) {
            _outputStream = outputStream;
        }

        public void sendClose() throws IOException {
            writeLn(_outputStream, "--" + BOUNDARY + "--");
        }

        /**
         * add the parameter with the given name value and characterSet
         *
         * @param name
         *            - the name
         * @param value
         *            - the value to set
         * @param characterSet
         *            - the name of the characterSet to use
         */
        @Override
        public void addParameter(String name, String value, String characterSet) throws IOException {
            if (name == null || name.isEmpty() || value == null) {
                return;
            }
            writeLn(_outputStream, "--" + BOUNDARY);
            writeLn(_outputStream, "Content-Disposition: form-data; name=\"" + name + '"'); // XXX need to handle
            // non-ascii names here
            writeLn(_outputStream, "Content-Type: text/plain; charset=" + getCharacterSet());
            writeLn(_outputStream, "");
            writeLn(_outputStream, fixLineEndings(value), getCharacterSet());
        }

        private static final char CR = 0x0D;
        private static final char LF = 0x0A;

        private String fixLineEndings(String value) {
            StringBuilder sb = new StringBuilder();
            char[] chars = value.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                if (chars[i] == CR || chars[i] == LF && (i == 0 || chars[i - 1] != CR)) {
                    sb.append(CR).append(LF);
                } else {
                    sb.append(chars[i]);
                }
            }
            return sb.toString();
        }

        @Override
        public void addFile(String name, UploadFileSpec spec) throws IOException {
            byte[] buffer = new byte[8 * 1024];

            writeLn(_outputStream, "--" + BOUNDARY);
            writeLn(_outputStream, "Content-Disposition: form-data; name=\"" + encode(name) + "\"; filename=\""
                    + encode(spec.getFileName()) + '"'); // XXX need to handle non-ascii names here
            writeLn(_outputStream, "Content-Type: " + spec.getContentType());
            writeLn(_outputStream, "");

            InputStream in = spec.getInputStream();
            int count = 0;
            do {
                _outputStream.write(buffer, 0, count);
                count = in.read(buffer, 0, buffer.length);
            } while (count != -1);

            in.close();
            writeLn(_outputStream, "");
        }

        private OutputStream _outputStream;
    }

}
