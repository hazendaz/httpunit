package com.meterware.httpunit;

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.reflect.*;
import java.security.Provider;
import java.security.Security;

/**
 * A request sent to a web server.
 **/
abstract
public class WebRequest {


    /**
     * Sets the value of a parameter in a web request.
     **/
    public void setParameter( String name, String value ) {
        if (HttpUnitOptions.getParameterValuesValidated()) validateParameterValue( name, value );
        _parameters.put( name, value );
    }


    /**
     * Sets the multiple values of a parameter in a web request.
     **/
    public void setParameter( String name, String[] values ) {
        if (HttpUnitOptions.getParameterValuesValidated()) validateParameterValues( name, values );
        _parameters.put( name, values );
    }


    /**
     * Returns the value of a parameter in this web request.
     * @return the value of the named parameter, or null if it is not set.
     **/
    public String getParameter( String name ) {
        Object value = _parameters.get( name );
        if (value instanceof String[]) {
            return ((String[]) value)[0];
        } else {
            return (String) value;
        }
    }


    /**
     * Returns the multiple default values of the named parameter.
     **/
    public String[] getParameterValues( String name ) {
        Object result = _parameters.get( name );
        if (result instanceof String) return new String[] { (String) result };
        if (result instanceof String[]) return (String[]) result;
        return new String[0];
    }


    /**
     * Removes a parameter from this web request.
     **/
    public void removeParameter( String name ) {
        _parameters.remove( name );
    }


    
    /**
     * Returns the final URL associated with this web request.
     **/
    public URL getURL() throws MalformedURLException {
        if (getURLBase() == null || getURLString().indexOf( ':' ) > 0) validateProtocol( getURLString() );
        return new URL( getURLBase(), getURLString() );
    }


    /**
     * Returns the target for this web request.
     **/
    public String getTarget() {
        return _target;
    }



//------------------------------------- protected members ------------------------------------


    /**
     * Constructs a web request using an absolute URL string.
     **/
    protected WebRequest( String urlString ) {
        this( (URL) null, urlString );
    }
    
    
    /**
     * Constructs a web request using a base URL and a relative URL string.
     **/
    protected WebRequest( URL urlBase, String urlString ) {
        this( urlBase, urlString, (WebForm) null );
    }


    /**
     * Constructs a web request using a base URL, a relative URL string, and a target.
     **/
    protected WebRequest( URL urlBase, String urlString, String target ) {
        this( urlBase, urlString, target, null );
    }


    /**
     * Constructs a web request using a base URL and a relative URL string.
     **/
    protected WebRequest( URL urlBase, String urlString, WebForm sourceForm ) {
        this( urlBase, urlString, TOP_FRAME, sourceForm );
    }
    

    /**
     * Constructs a web request using a base URL and a relative URL string.
     **/
    protected WebRequest( URL urlBase, String urlString, String target, WebForm sourceForm ) {
        _urlBase   = urlBase;
        _urlString = urlString;
        _sourceForm = sourceForm;
        _target     = target;
    }
    

    /**
     * Constructs a web request using a base request and a relative URL string.
     **/
    protected WebRequest( WebRequest baseRequest, String urlString ) throws MalformedURLException {
        this( baseRequest.getURL(), urlString, (WebForm) null );
    }
    

    /**
     * Performs any additional processing necessary to complete the request.
     **/
    protected void completeRequest( URLConnection connection ) throws IOException {
    }


    final
    protected URL getURLBase() {
        return _urlBase;
    }


    protected String getURLString() {
        return _urlString;
    }


    final
    protected boolean hasNoParameters() {
        return _parameters.size() == 0;
    }


    final
    protected String getParameterString() {
        StringBuffer sb = new StringBuffer();
        for (Enumeration e = _parameters.keys(); e.hasMoreElements();) {
            String name = (String) e.nextElement();
            Object value = _parameters.get( name );
            if (value instanceof String) {
                appendParameter( sb, name, (String) value, e.hasMoreElements() );
            } else {
                appendParameters( sb, name, (String[]) value, e.hasMoreElements() );
            }
        }
        return sb.toString();
    }

    
//---------------------------------- package members --------------------------------

    /** The name of the topmost frame. **/
    final static String TOP_FRAME = "_top";


//--------------------------------------- private members ------------------------------------

    /** The name of the system parameter used by java.net to locate protocol handlers. **/
    private final static String PROTOCOL_HANDLER_PKGS  = "java.protocol.handler.pkgs";

    /** The name of the JSSE class which provides support for SSL. **/
    private final static String SunJSSE_PROVIDER_CLASS = "com.sun.net.ssl.internal.ssl.Provider";

    /** The name of the JSSE class which supports the https protocol. **/
    private final static String SSL_PROTOCOL_HANDLER   = "com.sun.net.ssl.internal.www.protocol";


    private URL       _urlBase;
    private String    _urlString;
    private Hashtable _parameters = new Hashtable();
    private WebForm   _sourceForm;
    private String    _target = TOP_FRAME;

    private boolean   _httpsProtocolSupportEnabled;


    private void appendParameters( StringBuffer sb, String name, String[] values, boolean moreToCome ) {
        for (int i = 0; i < values.length; i++) {
            appendParameter( sb, name, values[i], (i < values.length-1 || moreToCome ) );
        }
    }


    private void appendParameter( StringBuffer sb, String name, String value, boolean moreToCome ) {
        sb.append( name ).append( '=' );
        sb.append( URLEncoder.encode( value ) );
        if (moreToCome) sb.append( '&' );
    }


    private void validateParameterValue( String name, String value ) {
        if (_sourceForm == null) return;
        if (_sourceForm.isTextParameter( name )) return;
        if (!inArray( name, _sourceForm.getParameterNames() )) throw new NoSuchParameterException( name );
        if (!inArray( value, _sourceForm.getOptionValues( name ) )) throw new IllegalParameterValueException( name, value );
    }


    private void validateParameterValues( String name, String[] values ) {
        if (_sourceForm == null) return;
        if (values.length != 1 && !_sourceForm.isMultiValuedParameter( name )) {
            throw new SingleValuedParameterException( name );
        }

        for (int i = 0; i < values.length; i++) validateParameterValue( name, values[i] );
    }


    private boolean inArray( String candidate, String[] values ) {
        for (int i = 0; i < values.length; i++) {
            if (candidate.equals( values[i] )) return true;
        }
        return false;
    }


    private void validateProtocol( String urlString ) {
        if (urlString.indexOf(':') <= 0) {
            throw new RuntimeException( "No protocol specified in URL '" + urlString + "'" );
        }

        String protocol = urlString.substring( 0, urlString.indexOf( ':' ) );
        if (protocol.equalsIgnoreCase( "http" )) {
            return;
        } else if (protocol.equalsIgnoreCase( "https" )) {
            validateHttpsProtocolSupport();
        }
    }


    void validateHttpsProtocolSupport() {
        if (!_httpsProtocolSupportEnabled) {
            verifyHttpsSupport();
            _httpsProtocolSupportEnabled = true;
        }
    }


    private static void listProviders() {
        Provider[] list = Security.getProviders();
        for (int i = 0; i < list.length; i++) {
            System.out.println( "provider" + i + "=" + list[i] );
        }
    }


    private static boolean hasProvider( Class providerClass ) {
        Provider[] list = Security.getProviders();
        for (int i = 0; i < list.length; i++) {
            if (list[i].getClass().equals( providerClass )) return true;
        }
        return false;
    }

    private static void verifyHttpsSupport() {
        if (System.getProperty( "java.version" ).startsWith( "1.1" )) {
            throw new RuntimeException( "https support requires Java 2" );
        } else {
            try {
                Class providerClass = Class.forName( SunJSSE_PROVIDER_CLASS );
                if (!hasProvider( providerClass )) Security.addProvider( (Provider) providerClass.newInstance() );
                registerSSLProtocolHandler();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException( "https support requires the Java Secure Sockets Extension. See http://java.sun.com/products/jsse" );
            } catch (Throwable e) {
                throw new RuntimeException( "Unable to enable https support. Make sure that you have installed JSSE " +
                                            "as described in http://java.sun.com/products/jsse/install.html: " + e );
            }
        }
    }


    private static void registerSSLProtocolHandler() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException { 
        String list = System.getProperty( PROTOCOL_HANDLER_PKGS );
        Method setMethod = System.class.getMethod( "setProperty", new Class[] { String.class, String.class } );
        if (list == null || list.length() == 0) {
            setMethod.invoke( null, new String[] { PROTOCOL_HANDLER_PKGS, SSL_PROTOCOL_HANDLER } );
        } else if (list.indexOf( SSL_PROTOCOL_HANDLER ) < 0) {
            setMethod.invoke( null, new String[] { PROTOCOL_HANDLER_PKGS, SSL_PROTOCOL_HANDLER + " | " + list } );
        }    
    }


}


//================================ exception class NoSuchParameterException =========================================


/**
 * This exception is thrown on an attempt to set a parameter to a value not permitted to it by the form.
 **/
class NoSuchParameterException extends IllegalRequestParameterException {


    NoSuchParameterException( String parameterName ) {
        _parameterName = parameterName;
    }


    public String getMessage() {
        return "No parameter named '" + _parameterName + "' is defined in the form";
    }


    private String _parameterName;

}


//============================= exception class IllegalParameterValueException ======================================


/**
 * This exception is thrown on an attempt to set a parameter to a value not permitted to it by the form.
 **/
class IllegalParameterValueException extends IllegalRequestParameterException {


    IllegalParameterValueException( String parameterName, String badValue ) {
        _parameterName = parameterName;
        _badValue      = badValue;
    }

    public String getMessage() {
        return "May not set parameter '" + _parameterName + "' to '" + _badValue + "'";
    }

    private String _parameterName;
    private String _badValue;
}


//============================= exception class SingleValuedParameterException ======================================


/**
 * This exception is thrown on an attempt to set a single-valued parameter to multiple values.
 **/
class SingleValuedParameterException extends IllegalRequestParameterException {


    SingleValuedParameterException( String parameterName ) {
        _parameterName = parameterName;
    }


    public String getMessage() {
        return "Parameter '" + _parameterName + "' may only have one value.";
    }


    private String _parameterName;

}

