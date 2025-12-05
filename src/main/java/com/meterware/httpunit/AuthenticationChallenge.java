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

import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * A challenge for authentication from the server to a client.
 **/
class AuthenticationChallenge extends HttpHeader {

    /** The client. */
    private WebClient _client;

    /** The request. */
    private WebRequest _request;

    /** The Constant BASIC_AUTHENTICATION. */
    private static final AuthenticationStrategy BASIC_AUTHENTICATION = new BasicAuthenticationStrategy();

    /** The Constant DIGEST_AUTHENTICATION. */
    private static final AuthenticationStrategy DIGEST_AUTHENTICATION = new DigestAuthenticationStrategy();

    /**
     * Creates the exception.
     *
     * @param wwwAuthenticateHeader
     *            the www authenticate header
     *
     * @return the authorization required exception
     */
    static AuthorizationRequiredException createException(String wwwAuthenticateHeader) {
        AuthenticationChallenge challenge = new AuthenticationChallenge(null, null, wwwAuthenticateHeader);
        return challenge.createAuthorizationRequiredException();
    }

    /**
     * Instantiates a new authentication challenge.
     *
     * @param client
     *            the client
     * @param request
     *            the request
     * @param headerString
     *            the header string
     */
    AuthenticationChallenge(WebClient client, WebRequest request, String headerString) {
        super(headerString, "Basic");
        _client = client;
        _request = request;
    }

    /**
     * check whether authentication is needed.
     *
     * @return true, if successful
     */
    boolean needToAuthenticate() {
        if (getAuthenticationType() == null) {
            return false;
        }
        if (getCredentialsForRealm() != null) {
            return true;
        }
        if (!_client.getExceptionsThrownOnErrorStatus()) {
            return false;
        }

        throw createAuthorizationRequiredException();
    }

    /**
     * Gets the authentication type.
     *
     * @return the authentication type
     */
    private String getAuthenticationType() {
        String result = getLabel();
        if (_headerString != null && _headerString.equals("Negotiate")) {
            result = null;
        }
        return result;
    }

    /**
     * Creates the authentication header.
     *
     * @return the string
     */
    String createAuthenticationHeader() {
        PasswordAuthentication credentials = getCredentialsForRealm();
        return getAuthenticationStrategy().createAuthenticationHeader(this, credentials.getUserName(),
                new String(credentials.getPassword()));
    }

    /**
     * Gets the authentication strategy.
     *
     * @return the authentication strategy
     */
    private AuthenticationStrategy getAuthenticationStrategy() {
        if (getAuthenticationType().equalsIgnoreCase("basic")) {
            return BASIC_AUTHENTICATION;
        }
        if (getAuthenticationType().equalsIgnoreCase("digest")) {
            return DIGEST_AUTHENTICATION;
        }
        throw new RuntimeException("Unsupported authentication type '" + getAuthenticationType() + "'");
    }

    /**
     * Creates the authorization required exception.
     *
     * @return the authorization required exception
     */
    private AuthorizationRequiredException createAuthorizationRequiredException() {
        return AuthorizationRequiredException.createException(getAuthenticationType(), getProperties());
    }

    /**
     * get the credentials for the realm property.
     *
     * @return the credentials for realm
     */
    private PasswordAuthentication getCredentialsForRealm() {
        String realm = getProperty("realm");
        PasswordAuthentication result = null;
        if (realm != null) {
            result = _client.getCredentialsForRealm(realm);
        }
        return result;
    }

    /**
     * Gets the method.
     *
     * @return the method
     */
    private String getMethod() {
        return null == _request ? null : _request.getMethod();
    }

    /**
     * Gets the request uri.
     *
     * @return the request uri
     */
    private String getRequestUri() {
        try {
            return null == _request ? null : _request.getURL().getFile();
        } catch (MalformedURLException e) {
            return null;
        }
    }

    /**
     * The Interface AuthenticationStrategy.
     */
    private interface AuthenticationStrategy {

        /**
         * Creates the authentication header.
         *
         * @param challenge
         *            the challenge
         * @param username
         *            the username
         * @param password
         *            the password
         *
         * @return the string
         */
        String createAuthenticationHeader(AuthenticationChallenge challenge, String username, String password);
    }

    /**
     * The Class BasicAuthenticationStrategy.
     */
    private static class BasicAuthenticationStrategy implements AuthenticationStrategy {

        @Override
        public String createAuthenticationHeader(AuthenticationChallenge challenge, String userName, String password) {
            return "Basic "
                    + Base64.getEncoder().encodeToString((userName + ':' + password).getBytes(StandardCharsets.UTF_8));
        }

    }

    /**
     * The Class DigestAuthenticationStrategy.
     */
    private static class DigestAuthenticationStrategy implements AuthenticationStrategy {

        /**
         * The Class Algorithm.
         */
        private static class Algorithm {

            /**
             * Append params.
             *
             * @param sb
             *            the sb
             * @param challenge
             *            the challenge
             * @param userName
             *            the user name
             * @param password
             *            the password
             */
            public void appendParams(StringBuilder sb, AuthenticationChallenge challenge, String userName,
                    String password) {
                appendDigestParams(sb, challenge.getProperty("realm"), challenge.getProperty("nonce"),
                        challenge.getRequestUri(), userName, password, challenge.getMethod(),
                        challenge.getProperty("opaque"));
            }

            /**
             * Append digest params.
             *
             * @param sb
             *            the sb
             * @param realm
             *            the realm
             * @param nonce
             *            the nonce
             * @param uri
             *            the uri
             * @param userName
             *            the user name
             * @param password
             *            the password
             * @param method
             *            the method
             * @param opaque
             *            the opaque
             */
            protected void appendDigestParams(StringBuilder sb, String realm, String nonce, String uri, String userName,
                    String password, String method, String opaque) {
                sb.append("username=").append(quote(userName));
                append(sb, "realm", realm);
                append(sb, "nonce", nonce);
                append(sb, "uri", uri);
                append(sb, "response", getResponse(userName, realm, password, nonce, uri, method));
                if (opaque != null) {
                    append(sb, "opaque", opaque);
                }
            }

            /**
             * Gets the response.
             *
             * @param userName
             *            the user name
             * @param realm
             *            the realm
             * @param password
             *            the password
             * @param nonce
             *            the nonce
             * @param uri
             *            the uri
             * @param method
             *            the method
             *
             * @return the response
             */
            protected String getResponse(String userName, String realm, String password, String nonce, String uri,
                    String method) {
                try {
                    String a1 = A1(userName, password, realm, nonce);
                    String a2 = A2(uri, method);
                    String ha1 = H(a1);
                    String ha2 = H(a2);
                    return KD(ha1, nonce + ':' + ha2);
                } catch (NoSuchAlgorithmException e) {
                    return "";
                }
            }

            /**
             * A1.
             *
             * @param userName
             *            the user name
             * @param password
             *            the password
             * @param realm
             *            the realm
             * @param nonce
             *            the nonce
             *
             * @return the string
             */
            protected String A1(String userName, String password, String realm, String nonce) {
                return userName + ':' + realm + ':' + password;
            }

            /**
             * A2.
             *
             * @param uri
             *            the uri
             * @param method
             *            the method
             *
             * @return the string
             */
            protected String A2(String uri, String method) {
                return method + ':' + uri;
            }

            /**
             * Kd.
             *
             * @param secret
             *            the secret
             * @param data
             *            the data
             *
             * @return the string
             *
             * @throws NoSuchAlgorithmException
             *             the no such algorithm exception
             */
            protected final String KD(String secret, String data) throws NoSuchAlgorithmException {
                return H(secret + ":" + data);
            }

            /**
             * H.
             *
             * @param data
             *            the data
             *
             * @return the string
             *
             * @throws NoSuchAlgorithmException
             *             the no such algorithm exception
             */
            protected final String H(String data) throws NoSuchAlgorithmException {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                digest.update(data.getBytes(StandardCharsets.UTF_8));
                byte[] bytes = digest.digest();
                StringBuilder sb = new StringBuilder();
                for (byte element : bytes) {
                    int aByte = element;
                    if (aByte < 0) {
                        aByte += 256;
                    }
                    if (aByte < 16) {
                        sb.append('0');
                    }
                    sb.append(Integer.toHexString(aByte));
                }

                return sb.toString();
            }

            /**
             * Append.
             *
             * @param sb
             *            the sb
             * @param name
             *            the name
             * @param value
             *            the value
             */
            private void append(StringBuilder sb, String name, String value) {
                sb.append(",").append(name).append("=").append(quote(value));
            }

            /**
             * Quote.
             *
             * @param value
             *            the value
             *
             * @return the string
             */
            private String quote(String value) {
                if (value.startsWith("\"")) {
                    return value;
                }
                return "\"" + value + "\"";
            }

        }

        @Override
        public String createAuthenticationHeader(AuthenticationChallenge challenge, String userName, String password) {
            StringBuilder sb = new StringBuilder("Digest ");
            Algorithm algorithm = new Algorithm();
            algorithm.appendParams(sb, challenge, userName, password);
            return sb.toString();
        }

    }

}
