package com.meterware.httpunit;

import java.net.URL;

import java.util.Vector;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class represents a link in an HTML page. Users of this class may examine the 
 * structure of the link (as a DOM), or create a {@tag WebRequest} to simulate clicking
 * on the link.
 **/
public class WebLink {


    /**
     * Creates and returns a web request which will simulate clicking on this link.
     **/
    public WebRequest getRequest() {
        NamedNodeMap nnm = _node.getAttributes();
        String action = getValue( nnm.getNamedItem( "href" ) );
        return new GetMethodWebRequest( _baseURL, action );
    }


    /**
     * Returns a copy of the domain object model subtree associated with this link.
     **/
    public Node getDOMSubtree() {
        return _node.cloneNode( /* deep */ true );
    }


//---------------------------------- package members --------------------------------


    /**
     * Contructs a web link given the URL of its source page and the DOM extracted
     * from that page.
     **/
    WebLink( URL baseURL, Node node ) {
        _node    = node;
        _baseURL = baseURL;
    }


//---------------------------------- private members --------------------------------


    /** The URL of the page containing this link. **/
    private URL            _baseURL;

    /** The DOM node representing the link. **/
    private Node           _node;


    private String getValue( Node node ) {
        return (node == null) ? "" : node.getNodeValue();
    }


}

