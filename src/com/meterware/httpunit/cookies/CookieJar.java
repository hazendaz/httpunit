package com.meterware.httpunit.cookies;
/********************************************************************************************************************
 * $Id$
 *
 * Copyright (c) 2002, Russell Gold
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
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.net.URL;
import java.util.*;


/**
 * A collection of HTTP cookies, which can interact with cookie and set-cookie header values.
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 * @author <a href="mailto:drew.varner@oracle.com">Drew Varner</a>
 **/
public class CookieJar {

    private static final int DEFAULT_HEADER_SIZE = 80;

    private ArrayList _cookies = new ArrayList();
    private CookiePress _press;


    /**
     * Creates an empty cookie jar.
     */
    public CookieJar() {
        _press = new CookiePress( null );
    }


    /**
     * Creates a cookie jar which is initially populated with cookies parsed from the <code>Set-Cookie</code> and
     * <code>Set-Cookie2</code> header fields.
     * <p>
     * Note that the parsing does not strictly follow the specifications, but
     * attempts to imitate the behavior of popular browsers. Specifically,
     * it allows cookie values to contain commas, which the
     * Netscape standard does not allow for, but which is required by some servers.
     * </p>
     */
    public CookieJar( CookieSource source ) {
        _press = new CookiePress( source.getURL() );
        findCookies( source.getHeaderFields( "Set-Cookie" ), new RFC2109CookieRecipe() );
        findCookies( source.getHeaderFields( "Set-Cookie2" ), new RFC2965CookieRecipe() );
    }


    private void findCookies( String cookieHeader[], CookieRecipe recipe ) {
        for (int i = 0; i < cookieHeader.length; i++) {
            recipe.findCookies( cookieHeader[i] );
        }
    }


    /**
     * Empties this cookie jar of all contents.
     */
    public void clear() {
        _cookies.clear();
    }


    /**
     * Defines a cookie to be sent to the server on every request. This bypasses the normal mechanism by which only
     * certain cookies are sent based on their host and path.
     **/
    public void addCookie( String name, String value ) {
        addUniqueCookie( new Cookie( name, value ) );
    }


    /**
     * Returns the name of all the active cookies in this cookie jar.
     **/
    public String[] getCookieNames() {
        String[] names = new String[ _cookies.size() ];
        for (int i = 0; i < names.length; i++) {
            names[i] = ((Cookie) _cookies.get(i)).getName();
        }
        return names;
    }


    /**
     * Returns a collection containing all of the cookies in this jar.
     */
    public Collection getCookies() {
        return (Collection) _cookies.clone();
    }


    /**
     * Returns the value of the specified cookie.
     **/
    public String getCookieValue( String name ) {
        Cookie cookie = getCookie( name );
        return cookie == null ? null : cookie.getValue();
    }


    /**
     * Returns the value of the specified cookie.
     **/
    public Cookie getCookie( String name ) {
        if (name == null) throw new IllegalArgumentException( "getCookieValue: no name specified" );
        for (Iterator iterator = _cookies.iterator(); iterator.hasNext();) {
            Cookie cookie = (Cookie) iterator.next();
            if (name.equals( cookie.getName() )) return cookie;
        }
        return null;
    }


    /**
     * Returns the value of the cookie header to be sent to the specified URL.
     * Will return null if no compatible cookie is defined.
     **/
    public String getCookieHeaderField( URL targetURL ) {
        if (_cookies.isEmpty()) return null;
        StringBuffer sb = new StringBuffer( DEFAULT_HEADER_SIZE );
        for (Iterator i = _cookies.iterator(); i.hasNext();) {
            Cookie cookie = (Cookie) i.next();
            if (!cookie.mayBeSentTo( targetURL )) continue;
            if (sb.length() != 0) sb.append( ';' );
            sb.append( cookie.getName() ).append( '=' ).append( cookie.getValue() );
        }
        return sb.length() == 0 ? null : sb.toString();
    }


    /**
     * Updates the cookies maintained in this cookie jar with those in another cookie jar. Any duplicate cookies in
     * the new jar will replace those in this jar.
     **/
    public void updateCookies( CookieJar newJar ) {
        for (Iterator i = newJar._cookies.iterator(); i.hasNext();) {
            addUniqueCookie( (Cookie) i.next() );
        }
    }


    /**
     * Add the cookie to this jar, replacing any previous matching cookie.
     */
    void addUniqueCookie( Cookie cookie ) {
        _cookies.remove( cookie );
        _cookies.add( cookie );
    }


    abstract class CookieRecipe {

        /**
         * Extracts cookies from a cookie header. Works in conjunction with a cookie press class, which actually creates
         * the cookies and adds them to the jar as appropriate.
         *
         * 1. Parse the header into tokens, separated by ',' and ';' (respecting single and double quotes)
         * 2. Process tokens from the end:
         *    a. if the token contains an '=' we have a name/value pair. Add them to the cookie press, which
         *       will decide if it is a cookie name or an attribute name.
         *    b. if the token is a reserved word, flush the cookie press and continue.
         *    c. otherwise, add the token to the cookie press, passing along the last character of the previous token.
         */
        void findCookies( String cookieHeader ) {
            Vector tokens = getCookieTokens( cookieHeader );

            for (int i = tokens.size() - 1; i >= 0; i--) {
                String token = (String) tokens.elementAt( i );

                int equalsIndex = getEqualsIndex( token );
                if (equalsIndex != -1) {
                    _press.addTokenWithEqualsSign( this, token, equalsIndex );
                } else if (isCookieReservedWord( token )) {
                    _press.clear();
                } else {
                    _press.addToken( token, lastCharOf( (i == 0) ? "" : (String) tokens.elementAt( i - 1 ) ) );
                }
            }
        }


        private char lastCharOf( String string ) {
            return (string.length() == 0) ? ' ' : string.charAt( string.length()-1 );
        }


        /**
         * Returns the index (if any) of the equals sign separating a cookie name from the its value.
         * Equals signs at the end of the token are ignored in this calculation, since they may be
         * part of a Base64-encoded value.
         */
        private int getEqualsIndex( String token ) {
            if (!token.endsWith( "==" )) {
                return token.indexOf( '=' );
            } else {
                return getEqualsIndex( token.substring( 0, token.length()-2 ) );
            }
        }


        /**
         * Tokenizes a cookie header and returns the tokens in a
         * <code>Vector</code>.
         **/
        private Vector getCookieTokens(String cookieHeader) {
            StringReader sr = new StringReader(cookieHeader);
            StreamTokenizer st = new StreamTokenizer(sr);
            Vector tokens = new Vector();

            // clear syntax tables of the StreamTokenizer
            st.resetSyntax();

            // set all characters as word characters
            st.wordChars(0,Character.MAX_VALUE);

            // set up characters for quoting
            st.quoteChar( '"' ); //double quotes
            st.quoteChar( '\'' ); //single quotes

            // set up characters to separate tokens
            st.whitespaceChars(59,59); //semicolon
            st.whitespaceChars(44,44); //comma

            try {
                while (st.nextToken() != StreamTokenizer.TT_EOF) {
                    tokens.addElement( st.sval.trim() );
                }
            }
            catch (IOException ioe) {
                // this will never happen with a StringReader
            }
            sr.close();
            return tokens;
        }


        abstract protected boolean isCookieAttribute( String stringLowercase );


        abstract protected boolean isCookieReservedWord( String token );

    }


    class CookiePress {

        private StringBuffer _value = new StringBuffer();
        private HashMap _attributes = new HashMap();
        private URL     _sourceURL;


        public CookiePress( URL sourceURL ) {
            _sourceURL = sourceURL;
        }


        void clear() {
            _value.setLength(0);
            _attributes.clear();
        }


        void addToken( String token, char lastChar ) {
            _value.insert( 0, token );
            if (lastChar != '=') _value.insert( 0, ',' );
        }


        void addTokenWithEqualsSign( CookieRecipe recipe, String token, int equalsIndex ) {
            String name = token.substring( 0, equalsIndex ).trim();
            _value.insert( 0, token.substring( equalsIndex + 1 ).trim() );
            if (recipe.isCookieAttribute( name.toLowerCase() )) {
                _attributes.put( name.toLowerCase(), _value.toString() );
            } else {
                addCookieIfValid( new Cookie( name, _value.toString(), _attributes ) );
                _attributes.clear();
            }
            _value.setLength(0);
        }


        private void addCookieIfValid( Cookie cookie ) {
            if (acceptCookie( cookie )) addUniqueCookie( cookie );
        }


        private boolean acceptCookie( Cookie cookie ) {
            if (cookie.getPath() == null) {
                cookie.setPath( getParentPath( _sourceURL.getPath() ) );
            } else {
                if (!pathAttributeIsValid( _sourceURL.getPath(), cookie.getPath() )) return false;
            }

            if (cookie.getDomain() == null) {
                cookie.setDomain( _sourceURL.getHost() );
            } else {
                if (!domainAttributeIsValid( cookie.getDomain(), _sourceURL.getHost() )) return false;
            }

            return true;
        }


        private String getParentPath( String path ) {
            int rightmostSlashIndex = path.lastIndexOf( '/' );
            return rightmostSlashIndex < 0 ? "/" : path.substring( 0, rightmostSlashIndex );
        }


        private boolean pathAttributeIsValid( String sourcePath, String pathAttribute ) {
            return sourcePath.length() == 0 || sourcePath.startsWith( pathAttribute );
        }


        private boolean domainAttributeIsValid( String domainAttribute, String sourceHost ) {
            if (!domainAttribute.startsWith(".")) return false;
            if (domainAttribute.lastIndexOf('.') == 0) return false;
            if (!sourceHost.endsWith( domainAttribute )) return false;
            if (sourceHost.lastIndexOf( domainAttribute ) > sourceHost.indexOf( '.' )) return false;
            return true;
        }


    }


    /**
     * Parses cookies according to
     * <a href="http://www.ietf.org/rfc/rfc2109.txt">RFC 2109</a>
     *
     * <br />
     * These cookies come from the <code>Set-Cookie:</code> header
     **/
    class RFC2109CookieRecipe extends CookieRecipe {

        protected boolean isCookieAttribute( String stringLowercase ) {
            return stringLowercase.equals("path") ||
                   stringLowercase.equals("domain") ||
                   stringLowercase.equals("expires") ||
                   stringLowercase.equals("comment") ||
                   stringLowercase.equals("max-age") ||
                   stringLowercase.equals("version");
        }


        protected boolean isCookieReservedWord( String token ) {
            return token.equalsIgnoreCase( "secure" );
        }
    }


    /**
     * Parses cookies according to
     * <a href="http://www.ietf.org/rfc/rfc2965.txt">RFC 2965</a>
     *
     * <br />
     * These cookies come from the <code>Set-Cookie2:</code> header
     **/
    class RFC2965CookieRecipe extends CookieRecipe {

        protected boolean isCookieAttribute( String stringLowercase ) {
            return stringLowercase.equals("path") ||
                   stringLowercase.equals("domain") ||
                   stringLowercase.equals("comment") ||
                   stringLowercase.equals("commenturl") ||
                   stringLowercase.equals("max-age") ||
                   stringLowercase.equals("version") ||
                   stringLowercase.equals("$version") ||
                   stringLowercase.equals("port");
        }


        protected boolean isCookieReservedWord( String token ) {
            return token.equalsIgnoreCase( "discard" ) || token.equalsIgnoreCase( "secure" );
        }
    }


}