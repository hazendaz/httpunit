/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.site;

import com.meterware.website.FragmentTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;


public class News extends FragmentTemplate  {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat( "d MMM yyyy");

    private ArrayList _items = new ArrayList<>();

    public FragmentTemplate newFragment() {
        return new News();
    }


    public String asText() {
        StringBuilder sb = new StringBuilder( "<h2>News</h2>" );
        sb.append( LINE_BREAK ).append( "<table>" );
        for (int i = 0; i < _items.size(); i++) {
            Item item = (Item) _items.get( i );
            sb.append( item.asText( i % 2 ) );
        }
        sb.append( LINE_BREAK ).append( "</table>" );
        return sb.toString();
    }


    protected String getRootNodeName() {
        return "news";
    }


    public Item createItem() {
        Item item = new Item();
        _items.add( item );
        return item;
    }


    public class Item {

        private Date _date;
        private String _text;
        private String _url;

        public String asText( int styleIndex ) {
            StringBuilder sb = new StringBuilder( LINE_BREAK );
            sb.append( "<tr><td class='news' align='right'>" );
            sb.append( DATE_FORMAT.format( _date ) ).append( "</td><td class='news'>" );
            if (_url != null) sb.append( "<a href='" ).append( _url ).append( "'>" );
            sb.append( _text );
            if (_url != null) sb.append( "</a>" );
            sb.append( "</td></tr>" );
            return sb.toString();
        }


        public void setDate( Date date ) {
            _date = date;
        }


        public void setText( String text ) {
            _text = text;
        }


        public void setUrl( String url ) {
            _url = url;
        }
    }
}
