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
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.net.URL;
import java.util.ArrayList;

/**
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 * @author <a href="mailto:bx@bigfoot.com">Benoit Xhenseval</a>
 **/
class ParsedHTML {

    private Node _rootNode;

    private URL _baseURL;

    private String _baseTarget;

    private String _characterSet;

    private WebForm[]    _forms;
    private WebImage[]   _images;
    private WebLink[]    _links;
    private WebResponse  _response;


    ParsedHTML( WebResponse response, URL baseURL, String baseTarget, Node rootNode, String characterSet ) {
        _response     = response;
        _baseURL      = baseURL;
        _baseTarget   = baseTarget;
        _rootNode     = rootNode;
        _characterSet = characterSet;
    }


    /**
     * Returns the forms found in the page in the order in which they appear.
     **/
    public WebForm[] getForms() {
        if (_forms == null) {
            NodeList forms = NodeUtils.getElementsByTagName( getRootNode(), "form" );

            _forms = new WebForm[ forms.getLength() ];
            for (int i = 0; i < _forms.length; i++) {
                _forms[i] = new WebForm( _response, _baseURL, _baseTarget, forms.item( i ), _characterSet );
            }
        }
        return _forms;
    }


    /**
     * Returns the form found in the page with the specified ID.
     **/
    public WebForm getFormWithID( String ID ) {
        WebForm[] forms = getForms();
        for (int i = 0; i < forms.length; i++) {
            if (forms[i].getID().equals( ID )) return forms[i];
            else if (HttpUnitOptions.getMatchesIgnoreCase() && forms[i].getID().equalsIgnoreCase( ID )) return forms[i];
        }
        return null;
    }


    /**
     * Returns the form found in the page with the specified name.
      **/
    public WebForm getFormWithName( String name ) {
        WebForm[] forms = getForms();
        for (int i = 0; i < forms.length; i++) {
            if (forms[i].getName().equals( name )) return forms[i];
            else if (HttpUnitOptions.getMatchesIgnoreCase() && forms[i].getName().equalsIgnoreCase( name )) return forms[i];
        }
        return null;
    }


    /**
     * Returns the links found in the page in the order in which they appear.
     **/
    public WebLink[] getLinks() {
        if (_links == null) {
            final ArrayList list = new ArrayList();
            NodeUtils.processNodes( getRootNode().getChildNodes(), new NodeUtils.NodeAction() {
                public boolean processElement( Element element ) {
                    if (element.getNodeName().equalsIgnoreCase( "a" )) addLinkAnchor( list, element );
                    else if (element.getNodeName().equalsIgnoreCase( "area" )) addLinkAnchor( list, element );
                    return true;
                }
                public void processTextNodeValue( String value ) {
                }
            } );
            _links = (WebLink[]) list.toArray( new WebLink[ list.size() ] );
        }

        return _links;
    }


    private void addLinkAnchor( ArrayList list, Node child ) {
        if (isLinkAnchor( child )) {
            list.add( new WebLink( _response, _baseURL, _baseTarget, child ) );
        }
    }


    /**
     * Returns the first link which contains the specified text.
     **/
    public WebLink getLinkWith( String text ) {
        return getFirstMatchingLink( WebLink.MATCH_CONTAINED_TEXT, text );
    }


    /**
     * Returns the link which contains the first image with the specified text as its 'alt' attribute.
     **/
    public WebLink getLinkWithImageText( String text ) {
        WebImage image = getImageWithAltText( text );
        return image == null ? null : image.getLink();
    }


    /**
     * Returns the link found in the page with the specified ID.
     **/
    public WebLink getLinkWithID( String ID ) {
        return getFirstMatchingLink( WebLink.MATCH_ID, ID );
    }


    /**
     * Returns the link found in the page with the specified name.
     **/
    public WebLink getLinkWithName( String name ) {
        return getFirstMatchingLink( WebLink.MATCH_NAME, name );
    }


    /**
     * Returns the first link found in the page matching the specified criteria.
     **/
    public WebLink getFirstMatchingLink( HTMLElementPredicate predicate, Object criteria ) {
        WebLink[] links = getLinks();
        for (int i = 0; i < links.length; i++) {
            if (predicate.matchesCriteria( links[i], criteria )) return links[i];
        }
        return null;
    }


    /**
     * Returns all links found in the page matching the specified criteria.
     **/
    public WebLink[] getMatchingLinks( HTMLElementPredicate predicate, Object criteria ) {
        ArrayList matches = new ArrayList();
        WebLink[] links = getLinks();
        for (int i = 0; i < links.length; i++) {
            if (predicate.matchesCriteria( links[i], criteria )) matches.add( links[i] );
        }
        return (WebLink[]) matches.toArray( new WebLink[ matches.size() ] );
    }


    /**
     * Returns the images found in the page in the order in which they appear.
     **/
    public WebImage[] getImages() {
        if (_images == null) {
            NodeList images = NodeUtils.getElementsByTagName( getRootNode(), "img" );

            _images = new WebImage[ images.getLength() ];
            for (int i = 0; i < _images.length; i++) {
                _images[i] = new WebImage( _response, this, _baseURL, images.item( i ), _baseTarget );
            }
        }
        return _images;
    }


    /**
     * Returns the image found in the page with the specified name.
     **/
    public WebImage getImageWithName( String name ) {
        WebImage[] images = getImages();
        for (int i = 0; i < images.length; i++) {
            if (images[i].getName().equals( name )) return images[i];
            else if (HttpUnitOptions.getMatchesIgnoreCase() && images[i].getName().equalsIgnoreCase( name )) return images[i];
        }
        return null;
    }


    /**
     * Returns the first image found in the page with the specified src attribute.
     **/
    public WebImage getImageWithSource( String source ) {
        WebImage[] images = getImages();
        for (int i = 0; i < images.length; i++) {
            if (images[i].getSource().equals( source )) return images[i];
            else if (HttpUnitOptions.getMatchesIgnoreCase() && images[i].getSource().equalsIgnoreCase( source )) return images[i];
        }
        return null;
    }


    /**
     * Returns the first image found in the page with the specified alt attribute.
     **/
    public WebImage getImageWithAltText( String altText ) {
        WebImage[] images = getImages();
        for (int i = 0; i < images.length; i++) {
            if (images[i].getSource().equals( altText )) return images[i];
            else if (HttpUnitOptions.getMatchesIgnoreCase() && images[i].getAltText().equalsIgnoreCase( altText )) return images[i];
        }
        return null;
    }


    /**
     * Returns the top-level tables found in this page in the order in which
     * they appear.
     **/
    public WebTable[] getTables() {
        return WebTable.getTables( _response, getOriginalDOM(), _baseURL, _baseTarget, _characterSet );
    }


    /**
     * Returns the first table in the response which matches the specified predicate and value.
     * Will recurse into any nested tables, as needed.
     * @return the selected table, or null if none is found
     **/
    public WebTable getFirstMatchingTable( HTMLElementPredicate predicate, Object criteria ) {
        return getTableSatisfyingPredicate( getTables(), predicate, criteria );
    }


    /**
     * Returns the first table in the response which has the specified text as the full text of
     * its first non-blank row and non-blank column. Will recurse into any nested tables, as needed.
     * @return the selected table, or null if none is found
     **/
    public WebTable getTableStartingWith( String text ) {
        return getFirstMatchingTable( WebTable.MATCH_FIRST_NONBLANK_CELL, text );
    }


    /**
     * Returns the first table in the response which has the specified text as a prefix of the text
     * in its first non-blank row and non-blank column. Will recurse into any nested tables, as needed.
     * @return the selected table, or null if none is found
     **/
    public WebTable getTableStartingWithPrefix( String text ) {
        return getFirstMatchingTable( WebTable.MATCH_FIRST_NONBLANK_CELL_PREFIX, text );
    }


    /**
     * Returns the first table in the response which has the specified text as its summary attribute.
     * Will recurse into any nested tables, as needed.
     * @return the selected table, or null if none is found
     **/
    public WebTable getTableWithSummary( String summary ) {
        return getFirstMatchingTable( WebTable.MATCH_SUMMARY, summary );
    }


    /**
     * Returns the first table in the response which has the specified text as its ID attribute.
     * Will recurse into any nested tables, as needed.
     * @return the selected table, or null if none is found
     **/
    public WebTable getTableWithID( String ID ) {
        return getFirstMatchingTable( WebTable.MATCH_ID, ID );
    }


    /**
     * Returns a copy of the domain object model associated with this page.
     **/
    public Node getDOM() {
        return getRootNode().cloneNode( /* deep */ true );
    }

//---------------------------------- Object methods --------------------------------


    public String toString() {
        return _baseURL.toExternalForm() + System.getProperty( "line.separator" ) +
               _rootNode;
    }


//---------------------------------- protected members ------------------------------


    /**
     * Overrides the base URL for this HTML segment.
     **/
    protected void setBaseURL( URL baseURL ) {
        _baseURL = baseURL;
    }


    /**
     * Overrides the base target for this HTML segment.
     **/
    protected void setBaseTarget( String baseTarget ) {
        _baseTarget = baseTarget;
    }



    protected Node getRootNode() {
        if (_rootNode == null) throw new IllegalStateException( "The root node has not been specified" );
        return _rootNode;
    }


//---------------------------------- package members --------------------------------


    /**
     * Specifies the root node for this HTML fragment.
     */
    protected void setRootNode( Node rootNode ) {
        if (_rootNode != null && rootNode != _rootNode )
            throw new IllegalStateException( "The root node has already been defined as " + _rootNode + " and cannot be redefined as " + rootNode );
        _rootNode = rootNode;

        _links = null;
        _forms = null;
        _images = null;
    }


    /**
     * Returns the base URL for this HTML segment.
     **/
    URL getBaseURL() {
        return _baseURL;
    }


    WebResponse getResponse() {
        return _response;
    }


    /**
     * Returns the domain object model associated with this page, to be used internally.
     **/
    Node getOriginalDOM() {
        return getRootNode();
    }


//---------------------------------- private members --------------------------------

    /**
     * Returns true if the node is a link anchor node.
     **/
    private boolean isLinkAnchor( Node node ) {
        if (node.getNodeType() != Node.ELEMENT_NODE) {
            return false;
        } else if (!node.getNodeName().equalsIgnoreCase( "a" ) && !node.getNodeName().equalsIgnoreCase( "area" )) {
            return false;
        } else {
            return (node.getAttributes().getNamedItem( "href" ) != null);
        }
    }



    /**
     * Returns the table with the specified text in its summary attribute.
     **/
    private WebTable getTableSatisfyingPredicate( WebTable[] tables, HTMLElementPredicate predicate, Object value ) {
        for (int i = 0; i < tables.length; i++) {
            if (predicate.matchesCriteria( tables[i], value )) {
                return tables[i];
            } else {
                for (int j = 0; j < tables[i].getRowCount(); j++) {
                    for (int k = 0; k < tables[i].getColumnCount(); k++) {
                        TableCell cell = tables[i].getTableCell(j,k);
                        if (cell != null) {
                            WebTable[] innerTables = cell.getTables();
                            if (innerTables.length != 0) {
                                WebTable result = getTableSatisfyingPredicate( innerTables, predicate, value );
                                if (result != null) return result;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

}