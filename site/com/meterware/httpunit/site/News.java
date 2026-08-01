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


/**
 * The Class News.
 */
public class News extends FragmentTemplate  {

    /** The date format. */
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat( "d MMM yyyy");

    /** The items. */
    private ArrayList _items = new ArrayList<>();

    /**
     * New fragment.
     *
     * @return the fragment template
     */
    public FragmentTemplate newFragment() {
        return new News();
    }


    /**
     * As text.
     *
     * @return the string
     */
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


    /**
     * Gets the root node name.
     *
     * @return the root node name
     */
    protected String getRootNodeName() {
        return "news";
    }


    /**
     * Create item.
     *
     * @return the item
     */
    public Item createItem() {
        Item item = new Item();
        _items.add( item );
        return item;
    }


    /**
     * The Class Item.
     */
    public class Item {

        /** The date. */
        private Date _date;
        /** The text. */
        private String _text;
        /** The url. */
        private String _url;

        /**
         * As text.
         *
         * @param styleIndex the style index
         * @return the string
         */
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


        /**
         * Sets the date.
         *
         * @param date the date
         */
        public void setDate( Date date ) {
            _date = date;
        }


        /**
         * Sets the text.
         *
         * @param text the text
         */
        public void setText( String text ) {
            _text = text;
        }


        /**
         * Sets the url.
         *
         * @param url the url
         */
        public void setUrl( String url ) {
            _url = url;
        }
    }
}
