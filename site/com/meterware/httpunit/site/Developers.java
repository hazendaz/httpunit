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

/**
 * The Class Developers.
 */
public class Developers extends FragmentTemplate {

    /** The groups. */
    private ArrayList _groups = new ArrayList<>();

    /**
     * New fragment.
     *
     * @return the fragment template
     */
    public FragmentTemplate newFragment() {
        return new Developers();
    }


    /**
     * As text.
     *
     * @return the string
     */
    public String asText() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < _groups.size(); i++) {
            Group group = (Group) _groups.get( i );
            group.appendTo( sb );
        }
        return sb.toString();
    }


    /**
     * Gets the root node name.
     *
     * @return the root node name
     */
    protected String getRootNodeName() {
        return "developers";
    }


    /**
     * Create group.
     *
     * @return the group
     */
    public Group createGroup() {
        Group group = new Group();
        _groups.add( group );
        return group;
    }


    /**
     * The Class Group.
     */
    public class Group {

        /** The developers. */
        private ArrayList _developers = new ArrayList<>();
        /** The summaries. */
        private ArrayList _summaries = new ArrayList<>();
        /** The type. */
        private String _type;
        /** The max summary columns. */
        private static final int MAX_SUMMARY_COLUMNS = 6;


        /**
         * Sets the type.
         *
         * @param type the type
         */
        public void setType( String type ) {
            _type = type;
        }


        /**
         * Create developer.
         *
         * @return the developer
         */
        public Developer createDeveloper() {
            Developer developer = new Developer();
            _developers.add( developer );
            return developer;
        }


        /**
         * Append to.
         *
         * @param sb the sb
         */
        void appendTo( StringBuffer sb ) {
            sb.append( "<h2>" ).append( _type ).append( "</h2>" ).append( LINE_BREAK );
            sb.append( "<dl class='developers'>" ).append( LINE_BREAK );
            for (int i = 0; i < _developers.size(); i++) {
                Developer developer = (Developer) _developers.get( i );
                if (developer.isNameOnly()) {
                    _summaries.add( developer );
                } else {
                    developer.appendTo( sb );
                }
            }
            sb.append( "</dl>" ).append( LINE_BREAK );
            if (_summaries.size() != 0) appendSummary( sb );
        }


        /**
         * Append summary.
         *
         * @param sb the sb
         */
        private void appendSummary( StringBuffer sb ) {
            sb.append( "<table>" ).append( LINE_BREAK );
            int numColumns = Math.min( _summaries.size(), MAX_SUMMARY_COLUMNS );
            int numRows = (_summaries.size() + numColumns - 1) / numColumns;

            for (int j = 0; j < numRows; j++) {
                sb.append( "<tr>" );
                for (int k = 0; k < numColumns; k++) {
                    int i = j + k*numRows;
                    if (i >= _summaries.size()) continue;
                    sb.append( "<td class='summaries'>");
                    Developer developer = (Developer) _summaries.get(i);
                    developer.appendNameTo( sb );
                    sb.append( "</td>" );
                }
                sb.append( "</tr>" ).append( LINE_BREAK );
            }
            sb.append( "</table>" ).append( LINE_BREAK );
        }
    }


    /**
     * The Class Developer.
     */
    public class Developer {

        /** The name. */
        private String _name;
        /** The username. */
        private String _username;
        /** The email. */
        private String _email;
        /** The text. */
        private String _text;

        /**
         * Sets the name.
         *
         * @param name the name
         */
        public void setName( String name ) {
            _name = name;
        }


        /**
         * Sets the username.
         *
         * @param username the username
         */
        public void setUsername( String username ) {
            _username = username;
        }


        /**
         * Sets the email.
         *
         * @param email the email
         */
        public void setEmail( String email ) {
            _email = email;
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
         * Checks if is name only.
         *
         * @return true, if successful
         */
        boolean isNameOnly() {
            return _text == null;
        }


        /**
         * Append to.
         *
         * @param sb the sb
         */
        void appendTo( StringBuffer sb ) {
            sb.append( "<dt>" );
            appendNameTo( sb );
            sb.append( "</dt>" ).append( LINE_BREAK );
            if (_text != null) sb.append( "<dd>" ).append( _text ).append( "</dd>" ).append( LINE_BREAK );
        }


        /**
         * Append name to.
         *
         * @param sb the sb
         */
        void appendNameTo( StringBuffer sb ) {
            if (_username == null /* && _email == null */) {
                sb.append( _name );
            } else {
                sb.append( "<a" );
                if (_username != null) sb.append( " name='" ).append( _username ).append( "'" );
                if (_email != null) sb.append( " href='mailto:" ).append( _email ).append( "'" );
                sb.append( ">" ).append( _name ).append( "</a>" );
            }
        }
    }
}
