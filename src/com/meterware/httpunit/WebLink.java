package com.meterware.httpunit;
/********************************************************************************************************************
* $Id$
*
* Copyright (c) 2000-2002, Russell Gold
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
import com.meterware.httpunit.scripting.ScriptableDelegate;
import com.meterware.httpunit.scripting.NamedDelegate;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * This class represents a link in an HTML page. Users of this class may examine the
 * structure of the link (as a DOM), or create a {@link WebRequest} to simulate clicking
 * on the link.
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 * @author <a href="mailto:benoit.xhenseval@avondi.com>Benoit Xhenseval</a>
 **/
public class WebLink extends FixedURLWebRequestSource {

    /** Predicate to match part or all of a link's URL string. **/
    public final static HTMLElementPredicate MATCH_URL_STRING;

    /** Predicate to match part or all of a link's contained text. **/
    public final static HTMLElementPredicate MATCH_CONTAINED_TEXT;

    /** Predicate to match a link's ID. **/
    public final static HTMLElementPredicate MATCH_ID;

    /** Predicate to match a link's name. **/
    public final static HTMLElementPredicate MATCH_NAME;

    private Scriptable _scriptable;


    /**
     * Returns the URL referenced by this link. This may be a relative URL. It will not include any fragment identifier.
     **/
    public String getURLString() {
        return getRelativeURL();
    }


    /**
     * Returns the text value of this link.
     **/
    public String asText() {
        if (getNode().getNodeName().equalsIgnoreCase( "area" )) {
            return getAttribute( "alt" );
        } else if (!getNode().hasChildNodes()) {
            return "";
        } else {
            return NodeUtils.asText( getNode().getChildNodes() );
        }
    }


    /**
     * Submits a request as though the user had clicked on this link. Will also fire the 'onClick' event if defined.
     **/
    public WebResponse click() throws IOException, SAXException {
        String event = getAttribute( "onclick" );
        if (event.length() == 0 || getScriptableObject().doEvent( event )) return submitRequest();
        return getBaseResponse();
    }


    /**
     * Simulates moving the mouse over the link. Will fire the 'onMouseOver' event if defined.
     **/
    public void mouseOver() {
        String event = getAttribute( "onmouseover" );
        if (event.length() > 0) getScriptableObject().doEvent( event );
    }


    public class Scriptable extends ScriptableDelegate implements NamedDelegate {

        public String getName() {
            return WebLink.this.getName();
        }


        public Object get( String propertyName ) {
            if (propertyName.equalsIgnoreCase( "href" )) {
                return getReference().toExternalForm();
            } else {
               return super.get( propertyName );
            }
        }


        public void set( String propertyName, Object value ) {
            if (propertyName.equals( "href" )) {
                setDestination( (String) value );
            } else {
                super.set( propertyName, value );
            }
        }


        private URL getReference() {
            try {
                return getRequest().getURL();
            } catch (MalformedURLException e) {
                return WebLink.this.getBaseURL();
            }
        }
    }


//----------------------------------------- WebRequestSource methods ---------------------------------------------------


    /**
     * Returns the scriptable delegate.
     */
    ScriptableDelegate getScriptableDelegate() {
        return getScriptableObject();
    }


//--------------------------------------------------- package members --------------------------------------------------


    /**
     * Contructs a web link given the URL of its source page and the DOM extracted
     * from that page.
     **/
    WebLink( WebResponse response, URL baseURL, String parentTarget, Node node ) {
        super( response, node, baseURL, NodeUtils.getNodeAttribute( node, "href" ), parentTarget );
    }


    /**
     * Returns an object which provides scripting access to this link.
     **/
    Scriptable getScriptableObject() {
        if (_scriptable == null) {
            _scriptable = new Scriptable();
            _scriptable.setScriptEngine( getBaseResponse().getScriptableObject().getDocument().getScriptEngine( _scriptable ) );
        }
        return _scriptable;
    }


    static {
        MATCH_URL_STRING = new HTMLElementPredicate() {
            public boolean matchesCriteria( Object htmlElement, Object criteria ) {
                return HttpUnitUtils.contains( ((WebLink) htmlElement).getURLString(), (String) criteria );
            };
        };

        MATCH_CONTAINED_TEXT = new HTMLElementPredicate() {
            public boolean matchesCriteria( Object htmlElement, Object criteria ) {
                return HttpUnitUtils.contains( ((WebLink) htmlElement).asText(), (String) criteria );
            };
        };


        MATCH_ID = new HTMLElementPredicate() {
            public boolean matchesCriteria( Object htmlElement, Object criteria ) {
                return HttpUnitUtils.matches( ((WebLink) htmlElement).getID(), (String) criteria );
            };
        };


        MATCH_NAME = new HTMLElementPredicate() {
            public boolean matchesCriteria( Object htmlElement, Object criteria ) {
                return HttpUnitUtils.matches( ((WebLink) htmlElement).getName(), (String) criteria );
            };
        };

    }


}
