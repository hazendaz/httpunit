package com.meterware.httpunit;
/********************************************************************************************************************
* $Id$
*
* Copyright (c) 2000-2004, Russell Gold
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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.*;

import java.util.*;

/**
 * A request sent to a web server.
 **/
abstract
public class WebRequest {

    static final String REFERER_HEADER_NAME = "Referer";

    private static URLStreamHandler JAVASCRIPT_STREAM_HANDLER = new JavascriptURLStreamHandler();
    private static URLStreamHandler HTTPS_STREAM_HANDLER = new HttpsURLStreamHandler();

    private final ParameterHolder _parameterHolder;

    private URL          _urlBase;
    private FrameSelector  _sourceFrame;
    private String       _requestTarget;
    private String       _urlString;
    private Hashtable    _headers;
    private WebRequestSource _webRequestSource;

    private SubmitButton _button;


    /**
     * Sets the value of a header to be sent with this request. A header set here will override any matching header set
     * in the WebClient when the request is actually sent.
     */
    public void setHeaderField( String headerName, String headerValue ) {
        getHeaderDictionary().put( headerName, headerValue );
    }

    /**
     * Returns a copy of the headers to be sent with this request.
     **/
    public Dictionary getHeaders() {
        return (Dictionary) getHeaderDictionary().clone();
    }


    /**
     * Returns the final URL associated with this web request.
     **/
    public URL getURL() throws MalformedURLException {
        if (getURLBase() == null || getURLBase().toString().indexOf( "?" ) < 0) {
            return newURL( getURLBase(), getURLString() );
        } else {
            final String urlBaseString = getURLBase().toString();
            URL newurlbase = new URL( urlBaseString.substring( 0, urlBaseString.indexOf( "?" ) ) );
            return newURL( newurlbase, getURLString() );
        }
    }


    /**
     * Creates a new URL, handling the case where the relative URL begins with a '?'
     */
    private URL newURL( final URL base, final String spec ) throws MalformedURLException {
        if (spec.toLowerCase().startsWith( "javascript:" )) {
            return new URL( "javascript", null, -1, spec.substring( "javascript:".length() ), JAVASCRIPT_STREAM_HANDLER );
        } else if (spec.toLowerCase().startsWith( "https:" ) && !HttpsProtocolSupport.hasHttpsSupport()) {
            return new URL( "https", null, -1, spec.substring( "https:".length() ), HTTPS_STREAM_HANDLER );
        } else {
            if (getURLBase() == null || getURLString().indexOf( ':' ) > 0) {
                if (getURLString().indexOf(':') <= 0) {
                    throw new RuntimeException( "No protocol specified in URL '" + getURLString() + "'" );
                }
                HttpsProtocolSupport.verifyProtocolSupport( getURLString().substring( 0, getURLString().indexOf( ':' ) ) );
            }
            return spec.startsWith( "?" ) ? new URL( base + spec ) : newCombinedURL( base, spec );
        }
    }


    private URL newCombinedURL( final URL base, final String spec ) throws MalformedURLException {
        if (base == null) return new URL( getNormalizedURL( spec ) );
        else if (spec.startsWith( ".." )) return new URL( getNormalizedURL( getURLDirectory( base ) + spec ) );
        else return new URL( base, getNormalizedURL( spec ) );
    }


    private String getURLDirectory( final URL base ) {
        String url = base.toExternalForm();
        int i = url.lastIndexOf( '/' );
        return url.substring( 0, i+1 );
    }


    private String getNormalizedURL( String url ) {
        if (url.lastIndexOf( "//" ) > url.indexOf( "://" ) + 1) return getNormalizedURL( stripDoubleSlashes( url ) );
        if (url.indexOf( "/.." ) > 0) return getNormalizedURL( stripUpNavigation( url ) );
        if (url.indexOf( "/." ) > 0) return getNormalizedURL( stripInPlaceNavigation( url ) );
        return url;
    }


    private String stripInPlaceNavigation( String url ) {
        int i = url.lastIndexOf( "/." );
        return url.substring( 0, i+1 ) + url.substring( i+2 );
     }


    private String stripUpNavigation( String url ) {
        int i = url.indexOf( "/.." );
        int j = url.lastIndexOf( "/", i-1 );
        return url.substring( 0, j+1 ) + url.substring( i+ 3 );
    }


    private String stripDoubleSlashes( String url ) {
        int i = url.lastIndexOf( "//" );
        return url.substring( 0, i ) + url.substring( i+1 );
    }


    /**
     * Returns the target for this web request.
     */
    public String getTarget() {
        return _requestTarget;
    }


    /**
     * Returns the frame from which this request originates.
     */
    FrameSelector getSourceFrame() {
        return _sourceFrame;
    }


    /**
     * Returns the HTTP method defined for this request.
     **/
    abstract
    public String getMethod();


    /**
     * Returns the query string defined for this request. The query string is sent to the HTTP server as part of
     * the request header. This default implementation returns an empty string.
     **/
    public String getQueryString() {
        return "";
    }


//------------------------------------- ParameterCollection methods ------------------------------------


    /**
     * Sets the value of a parameter in a web request.
     **/
    public void setParameter( String name, String value ) {
        _parameterHolder.setParameter( name, value );
    }


    /**
     * Sets the multiple values of a parameter in a web request.
     **/
    public void setParameter( String name, String[] values ) {
        _parameterHolder.setParameter( name, values );
    }


    /**
     * Sets the multiple values of a file upload parameter in a web request.
     **/
    public void setParameter( String parameterName, UploadFileSpec[] files ) {
        if (!maySelectFile( parameterName )) throw new IllegalNonFileParameterException( parameterName );
        if (!isMimeEncoded()) throw new MultipartFormRequiredException();
        _parameterHolder.setParameter( parameterName, files );
    }


    /**
     * Specifies the click position for the submit button. When a user clioks on an image button, not only the name
     * and value of the button, but also the position of the mouse at the time of the click is submitted with the form.
     * This method allows the caller to override the position selected when this request was created.
     *
     * @exception IllegalRequestParameterException thrown if the request was not created from a form with an image button.
     **/
    public void setImageButtonClickPosition( int x, int y ) throws IllegalRequestParameterException {
        if (_button == null) throw new IllegalButtonPositionException();
        _parameterHolder.selectImageButtonPosition( _button, x, y );
    }


    /**
     * Returns true if the specified parameter is a file field.
     **/
    public boolean isFileParameter( String name ) {
        return _parameterHolder.isFileParameter( name );
    }


    /**
     * Sets the file for a parameter upload in a web request.
     **/
    public void selectFile( String parameterName, File file ) {
        setParameter( parameterName, new UploadFileSpec[] { new UploadFileSpec( file ) } );
    }


    /**
     * Sets the file for a parameter upload in a web request.
     **/
    public void selectFile( String parameterName, File file, String contentType ) {
        setParameter( parameterName, new UploadFileSpec[] { new UploadFileSpec( file, contentType ) } );
    }


    /**
     * Sets the file for a parameter upload in a web request.
     **/
    public void selectFile( String parameterName, String fileName, InputStream inputStream, String contentType ) {
        setParameter( parameterName, new UploadFileSpec[] { new UploadFileSpec( fileName, inputStream, contentType ) } );
    }


    /**
     * Returns an array of all parameter names defined as part of this web request.
     * @since 1.3.1
     **/
    public String[] getRequestParameterNames() {
        final HashSet names = new HashSet();
        ParameterProcessor pp = new ParameterProcessor() {
            public void addParameter( String name, String value, String characterSet ) throws IOException {
                names.add( name );
            }
            public void addFile( String parameterName, UploadFileSpec fileSpec ) throws IOException {
                names.add( parameterName );
            }
        };

        try {
            _parameterHolder.recordPredefinedParameters( pp );
            _parameterHolder.recordParameters( pp );
        } catch (IOException e) {}

        return (String[]) names.toArray( new String[ names.size() ] );
    }


    /**
     * Returns the value of a parameter in this web request.
     * @return the value of the named parameter, or empty string
     *         if it is not set.
     **/
    public String getParameter( String name ) {
        String[] values = getParameterValues( name );
        return values.length == 0 ? "" : values[0];
    }


    /**
     * Returns the multiple default values of the named parameter.
     **/
    public String[] getParameterValues( String name ) {
        return _parameterHolder.getParameterValues( name );
    }


    /**
     * Removes a parameter from this web request.
     **/
    public void removeParameter( String name ) {
        _parameterHolder.removeParameter( name );
    }


//------------------------------------- Object methods ------------------------------------


    public String toString() {
        return getMethod() + " request for (" + getURLBase() + ") " + getURLString();
    }



//------------------------------------- protected members ------------------------------------


    /**
     * Constructs a web request using an absolute URL string.
     **/
    protected WebRequest( String urlString ) {
        this( null, urlString );
    }


    /**
     * Constructs a web request using a base URL and a relative URL string.
     **/
    protected WebRequest( URL urlBase, String urlString ) {
        this( urlBase, urlString, TOP_FRAME );
    }


    /**
     * Constructs a web request using a base request and a relative URL string.
     **/
    protected WebRequest( WebRequest baseRequest, String urlString, String target ) throws MalformedURLException {
        this( baseRequest.getURL(), urlString, target );
    }


    /**
     * Constructs a web request using a base URL, a relative URL string, and a target.
     **/
    protected WebRequest( URL urlBase, String urlString, String target ) {
        this( urlBase, urlString, FrameSelector.TOP_FRAME, target );
    }


    /**
     * Constructs a web request using a base URL, a relative URL string, and a target.
     **/
    protected WebRequest( URL urlBase, String urlString, FrameSelector frame, String target ) {
        this( urlBase, urlString, frame, target, new UncheckedParameterHolder() );
    }


    /**
     * Constructs a web request from a form.
     **/
    protected WebRequest( WebForm sourceForm, ParameterHolder parameterHolder, SubmitButton button, int x, int y ) {
        this( sourceForm, parameterHolder );
        if (button != null && button.isImageButton() && button.getName().length() > 0) {
            _button = button;
            _parameterHolder.selectImageButtonPosition( _button, x, y );
        }
    }


    protected WebRequest( WebRequestSource requestSource, ParameterHolder parameterHolder ) {
        this( requestSource.getBaseURL(), requestSource.getRelativePage(), requestSource.getFrame(), requestSource.getTarget(), parameterHolder );
        _webRequestSource = requestSource;
        setHeaderField( REFERER_HEADER_NAME, requestSource.getBaseURL().toExternalForm() );
    }


    static ParameterHolder newParameterHolder( WebRequestSource requestSource ) {
        if (HttpUnitOptions.getParameterValuesValidated()) {
            return requestSource;
        } else {
            return new UncheckedParameterHolder( requestSource );
        }
    }


    /**
     * Constructs a web request using a base URL, a relative URL string, and a target.
     **/
    private WebRequest( URL urlBase, String urlString, FrameSelector sourceFrame, String requestTarget, ParameterHolder parameterHolder ) {
        _urlBase   = urlBase;
        _sourceFrame = sourceFrame;
        _requestTarget = requestTarget;
        _urlString = urlString.toLowerCase().startsWith( "http" ) ? escape( urlString ) : urlString;
        _parameterHolder = parameterHolder;
    }


    private static String escape( String urlString ) {
        if (urlString.indexOf( ' ' ) < 0) return urlString;
        StringBuffer sb = new StringBuffer();

        int start = 0;
        do {
            int index = urlString.indexOf( ' ', start );
            if (index < 0) {
                sb.append( urlString.substring( start ) );
                break;
            } else {
                sb.append( urlString.substring( start, index ) ).append( "%20" );
                start = index+1;
            }
        } while (true);
        return sb.toString();
    }


    /**
     * Returns true if selectFile may be called with this parameter.
     */
    protected boolean maySelectFile( String parameterName )
    {
        return isFileParameter( parameterName );
    }


    /**
     * Selects whether MIME-encoding will be used for this request. MIME-encoding changes the way the request is sent
     * and is required for requests which include file parameters. This method may only be called for a POST request
     * which was not created from a form.
     **/
    protected void setMimeEncoded( boolean mimeEncoded )
    {
        _parameterHolder.setSubmitAsMime( mimeEncoded );
    }


    /**
     * Returns true if this request is to be MIME-encoded.
     **/
    protected boolean isMimeEncoded() {
        return _parameterHolder.isSubmitAsMime();
    }


    /**
     * Returns the content type of this request. If null, no content is specified.
     */
    protected String getContentType() {
        return null;
    }


    /**
     * Returns the character set required for this request.
     **/
    final
    protected String getCharacterSet() {
        return _parameterHolder.getCharacterSet();
    }


    /**
     * Performs any additional processing necessary to complete the request.
     **/
    protected void completeRequest( URLConnection connection ) throws IOException {
        if (connection instanceof HttpURLConnection) ((HttpURLConnection) connection).setRequestMethod( getMethod() );
    }


    /**
     * Writes the contents of the message body to the specified stream.
     */
    protected void writeMessageBody( OutputStream stream ) throws IOException {
    }


    final
    protected URL getURLBase() {
        return _urlBase;
    }


//------------------------------------- protected members ---------------------------------------------


    protected String getURLString() {
        final String queryString = getQueryString();
        if (queryString.length() == 0) {
            return _urlString;
        } else {
            return _urlString + "?" + queryString;
        }
    }


    final
    protected ParameterHolder getParameterHolder() {
        return _parameterHolder;
    }


//---------------------------------- package members --------------------------------

    /** The target indicating the topmost frame of a window. **/
    static final String TOP_FRAME = "_top";

    /** The target indicating the parent of a frame. **/
    static final String PARENT_FRAME = "_parent";

    /** The target indicating a new, empty window. **/
    static final String NEW_WINDOW = "_blank";

    /** The target indicating the same frame. **/
    static final String SAME_FRAME = "_self";

    WebRequestSource getWebRequestSource() {
        return _webRequestSource;
    }


    Hashtable getHeaderDictionary() {
        if (_headers == null) {
            _headers = new Hashtable();
            if (getContentType() != null) _headers.put( "Content-Type", getContentType() );
        }
        return _headers;
    }


    String getReferer() {
        return _headers == null ? null : (String) _headers.get( REFERER_HEADER_NAME );
    }

}


class URLEncodedString implements ParameterProcessor {

    private StringBuffer _buffer = new StringBuffer( HttpUnitUtils.DEFAULT_BUFFER_SIZE );

    private boolean _haveParameters = false;


    public String getString() {
        return _buffer.toString();
    }


    public void addParameter( String name, String value, String characterSet ) {
        if (_haveParameters) _buffer.append( '&' );
        _buffer.append( encode( name, characterSet ) );
        if (value != null) _buffer.append( '=' ).append( encode( value, characterSet ) );
        _haveParameters = true;
    }


    public void addFile( String parameterName, UploadFileSpec fileSpec ) {
        throw new RuntimeException( "May not URL-encode a file upload request" );
    }


    /**
     * Returns a URL-encoded version of the string, including all eight bits, unlike URLEncoder, which strips the high bit.
     **/
    private String encode( String source, String characterSet ) {
        if (characterSet.equalsIgnoreCase( HttpUnitUtils.DEFAULT_CHARACTER_SET )) {
            return URLEncoder.encode( source );
        } else {
            try {
                byte[] rawBytes = source.getBytes( characterSet );
                StringBuffer result = new StringBuffer( 3*rawBytes.length );
                for (int i = 0; i < rawBytes.length; i++) {
                    int candidate = rawBytes[i] & 0xff;
                    if (candidate == ' ') {
                        result.append( '+' );
                    } else if ((candidate >= 'A' && candidate <= 'Z') ||
                               (candidate >= 'a' && candidate <= 'z') ||
                               (candidate == '.') || (candidate == '-' ) ||
                               (candidate == '*') || (candidate == '_') ||
                               (candidate >= '0' && candidate <= '9')) {
                        result.append( (char) rawBytes[i] );
                    } else if (candidate < 16) {
                        result.append( "%0" ).append( Integer.toHexString( candidate ).toUpperCase() );
                    } else {
                        result.append( '%' ).append( Integer.toHexString( candidate ).toUpperCase() );
                    }
                }
                return result.toString();
            } catch (java.io.UnsupportedEncodingException e) {
                return "???";    // XXX should pass the exception through as IOException ultimately
            }
        }
    }

}





//======================================== class JavaScriptURLStreamHandler ============================================


class JavascriptURLStreamHandler extends URLStreamHandler {

    protected URLConnection openConnection( URL u ) throws IOException {
        return null;
    }
}


//======================================== class HttpsURLStreamHandler ============================================


class HttpsURLStreamHandler extends URLStreamHandler {

    protected URLConnection openConnection( URL u ) throws IOException {
        throw new RuntimeException( "https support requires the Java Secure Sockets Extension. See http://java.sun.com/products/jsse" );
    }
}


//============================= exception class IllegalNonFileParameterException ======================================


/**
 * This exception is thrown on an attempt to set a non-file parameter to a file value.
 **/
class IllegalNonFileParameterException extends IllegalRequestParameterException {


    IllegalNonFileParameterException( String parameterName ) {
        _parameterName = parameterName;
    }


    public String getMessage() {
        return "Parameter '" + _parameterName + "' is not a file parameter and may not be set to a file value.";
    }


    private String _parameterName;

}


//============================= exception class MultipartFormRequiredException ======================================


/**
 * This exception is thrown on an attempt to set a file parameter in a form that does not specify MIME encoding.
 **/
class MultipartFormRequiredException extends IllegalRequestParameterException {


    MultipartFormRequiredException() {
    }


    public String getMessage() {
        return "The request does not use multipart/form-data encoding, and cannot be used to upload files ";
    }

}


//============================= exception class IllegalButtonPositionException ======================================


/**
 * This exception is thrown on an attempt to set a file parameter in a form that does not specify MIME encoding.
 **/
class IllegalButtonPositionException extends IllegalRequestParameterException {


    IllegalButtonPositionException() {
    }


    public String getMessage() {
        return "The request was not created with an image button, and cannot accept an image button click position";
    }

}
