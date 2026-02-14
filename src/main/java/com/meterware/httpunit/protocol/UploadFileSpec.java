/*
 * MIT License
 *
 * Copyright 2011-2026 Russell Gold
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * A description of a file to be uploaded as part of a form submission.
 **/
public class UploadFileSpec {

    /**
     * Creates a specification based on a File object. The content type will be guessed from the file extension.
     *
     * @param file
     *            the file
     */
    public UploadFileSpec(File file) {
        _file = file;
        guessContentType();
    }

    /**
     * Creates a specification based on a File object and with a specified content type.
     *
     * @param file
     *            the file
     * @param contentType
     *            the content type
     */
    public UploadFileSpec(File file, String contentType) {
        _file = file;
        _contentType = contentType;
    }

    /**
     * Creates a specification for an upload from an input stream. The file name and content type must be specified.
     *
     * @param fileName
     *            the file name
     * @param inputStream
     *            the input stream
     * @param contentType
     *            the content type
     */
    public UploadFileSpec(String fileName, InputStream inputStream, String contentType) {
        _fileName = fileName;
        _inputStream = inputStream;
        _contentType = contentType;
    }

    /**
     * get the Inputstream - even if it has been closed previously.
     *
     * @return the inputstream for the current file
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public InputStream getInputStream() throws IOException {
        if (_inputStream == null) {
            _inputStream = Files.newInputStream(_file.toPath());
        }
        try {
            _inputStream.available();
        } catch (IOException ex) {
            _inputStream = Files.newInputStream(_file.toPath());
        }
        return _inputStream;
    }

    /**
     * Gets the file name.
     *
     * @return the file name
     */
    public String getFileName() {
        if (_fileName == null) {
            _fileName = _file.getAbsolutePath();
        }
        return _fileName;
    }

    /**
     * Returns the content type associated with this file upload specification.
     *
     * @return the content type
     */
    public String getContentType() {
        return _contentType;
    }

    /** The file. */
    private File _file;

    /** The input stream. */
    private InputStream _inputStream;

    /** The file name. */
    private String _fileName;

    /** The content type. */
    private String _contentType = "text/plain";

    /** the default content extensions. */
    private static String[][] CONTENT_EXTENSIONS = { { "text/plain", "txt", "text" }, { "text/html", "htm", "html" },
            { "image/gif", "gif" }, { "image/jpeg", "jpg", "jpeg" }, { "image/png", "png" },
            { "image/tiff", "tif", "tiff" }, { "application/pdf", "pdf" }, { "application/octet-stream", "zip" } };

    /**
     * Guess content type.
     */
    private void guessContentType() {
        String extension = getExtension(_file.getName());
        for (String[] element : CONTENT_EXTENSIONS) {
            for (int j = 1; j < element.length; j++) {
                if (extension.equalsIgnoreCase(element[j])) {
                    _contentType = element[0];
                    return;
                }
            }
        }
    }

    /**
     * Gets the extension.
     *
     * @param fileName
     *            the file name
     *
     * @return the extension
     */
    private String getExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }
}
