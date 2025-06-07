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

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A challenge for authentication from the server to a client.
 **/
class AuthenticationChallenge extends HttpHeader {

    private WebClient _client;
    private WebRequest _request;

    private static final AuthenticationStrategy BASIC_AUTHENTICATION = new BasicAuthenticationStrategy();
    private static final AuthenticationStrategy DIGEST_AUTHENTICATION = new DigestAuthenticationStrategy();

    static AuthorizationRequiredException createException(String wwwAuthenticateHeader) {
        AuthenticationChallenge challenge = new AuthenticationChallenge(null, null, wwwAuthenticateHeader);
        return challenge.createAuthorizationRequiredException();
    }

    AuthenticationChallenge(WebClient client, WebRequest request, String headerString) {
        super(headerString, "Basic");
        _client = client;
        _request = request;
    }

    /**
     * check whether authentication is needed
     *
     * @return
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

    private String getAuthenticationType() {
        String result = getLabel();
        if (_headerString != null && _headerString.equals("Negotiate")) {
            result = null;
        }
        return result;
    }

    String createAuthenticationHeader() {
        PasswordAuthentication credentials = getCredentialsForRealm();
        return getAuthenticationStrategy().createAuthenticationHeader(this, credentials.getUserName(),
                new String(credentials.getPassword()));
    }

    private AuthenticationStrategy getAuthenticationStrategy() {
        if (getAuthenticationType().equalsIgnoreCase("basic")) {
            return BASIC_AUTHENTICATION;
        }
        if (getAuthenticationType().equalsIgnoreCase("digest")) {
            return DIGEST_AUTHENTICATION;
        }
        throw new RuntimeException("Unsupported authentication type '" + getAuthenticationType() + "'");
    }

    private AuthorizationRequiredException createAuthorizationRequiredException() {
        return AuthorizationRequiredException.createException(getAuthenticationType(), getProperties());
    }

    /**
     * get the credentials for the realm property
     *
     * @return
     */
    private PasswordAuthentication getCredentialsForRealm() {
        String realm = getProperty("realm");
        PasswordAuthentication result = null;
        if (realm != null) {
            result = _client.getCredentialsForRealm(realm);
        }
        return result;
    }

    private String getMethod() {
        return null == _request ? null : _request.getMethod();
    }

    private String getRequestUri() {
        try {
            return null == _request ? null : _request.getURL().getFile();
        } catch (MalformedURLException e) {
            return null;
        }
    }

    private interface AuthenticationStrategy {
        String createAuthenticationHeader(AuthenticationChallenge challenge, String username, String password);
    }

    private static class BasicAuthenticationStrategy implements AuthenticationStrategy {

        @Override
        public String createAuthenticationHeader(AuthenticationChallenge challenge, String userName, String password) {
            return "Basic " + Base64.encode(userName + ':' + password);
        }

    }

    private static class DigestAuthenticationStrategy implements AuthenticationStrategy {

        private static class Algorithm {

            public void appendParams(StringBuilder sb, AuthenticationChallenge challenge, String userName,
                    String password) {
                appendDigestParams(sb, challenge.getProperty("realm"), challenge.getProperty("nonce"),
                        challenge.getRequestUri(), userName, password, challenge.getMethod(),
                        challenge.getProperty("opaque"));
            }

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

            protected String getResponse(String userName, String realm, String password, String nonce, String uri,
                    String method) {
                try {
                    String a1 = A1(userName, password, realm, nonce);
                    String a2 = A2(uri, method);
                    String ha1 = H(a1);
                    String ha2 = H(a2);
                    return KD(ha1, nonce + ':' + ha2);
                } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
                    return "";
                }
            }

            protected String A1(String userName, String password, String realm, String nonce)
                    throws NoSuchAlgorithmException, UnsupportedEncodingException {
                return userName + ':' + realm + ':' + password;
            }

            protected String A2(String uri, String method) {
                return method + ':' + uri;
            }

            final protected String KD(String secret, String data)
                    throws NoSuchAlgorithmException, UnsupportedEncodingException {
                return H(secret + ":" + data);
            }

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

            private void append(StringBuilder sb, String name, String value) {
                sb.append(",").append(name).append("=").append(quote(value));
            }

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
