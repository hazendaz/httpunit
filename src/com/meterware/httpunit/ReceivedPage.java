package com.meterware.httpunit;
/********************************************************************************************************************
* $Id$
*
* Copyright (c) 2000, Russell Gold
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
import java.io.ByteArrayInputStream;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.Vector;

import org.w3c.dom.*;

import org.w3c.tidy.Tidy;

import org.xml.sax.SAXException;


/**
 * This class represents an HTML page returned from a request.
 **/
class ReceivedPage extends ParsedHTML {


    public ReceivedPage( URL url, String parentTarget, String pageText ) throws SAXException {
        super( url, parentTarget, getParser().parseDOM( new ByteArrayInputStream( pageText.getBytes() ), null ) );
        setBaseAttributes();
    }


    /**
     * Returns the title of the page.
     **/
    public String getTitle() throws SAXException {
        NodeList nl = ((Document) getDOM()).getElementsByTagName( "title" );
        if (nl.getLength() == 0) return "";
        if (!nl.item(0).hasChildNodes()) return "";
        return nl.item(0).getFirstChild().getNodeValue();
    }


//---------------------------------- private members --------------------------------


    private void setBaseAttributes() throws SAXException {
        NodeList nl = ((Document) getDOM()).getElementsByTagName( "base" );
        if (nl.getLength() == 0) return;
        try {
            applyBaseAttributes( NodeUtils.getNodeAttribute( nl.item(0), "href" ), 
                                 NodeUtils.getNodeAttribute( nl.item(0), "target" ) );
        } catch (MalformedURLException e) {
            throw new RuntimeException( "Unable to set document base: " + e );
        }
    }


    private void applyBaseAttributes( String baseURLString, String baseTarget ) throws MalformedURLException {
        if (baseURLString.length() > 0) {
            this.setBaseURL( new URL( baseURLString ) );
        }
        if (baseTarget.length() > 0) {
            this.setBaseTarget( baseTarget );
        }
    }


    private static Tidy getParser() {
        Tidy tidy = new Tidy();
        tidy.setQuiet( true );
        tidy.setShowWarnings( HttpUnitOptions.getParserWarningsEnabled() );
        return tidy;
    }

}
