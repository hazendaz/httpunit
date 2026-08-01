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
 * The Class Faq.
 */
public class Faq extends FragmentTemplate {

    /** The sections. */
    private ArrayList _sections = new ArrayList<>();


    /**
     * New fragment.
     *
     * @return the fragment template
     */
    public FragmentTemplate newFragment() {
        return new Faq();
    }


    /**
     * As text.
     *
     * @return the string
     */
    public String asText() {
        StringBuffer sb = new StringBuffer();
        formatFaqSections( sb, new IndexFormat() );
        sb.append( "<hr/>" ).append( LINE_BREAK );
        formatFaqSections( sb, new BodyFormat() );
        return sb.toString();
    }


    /**
     * Gets the root node name.
     *
     * @return the root node name
     */
    protected String getRootNodeName() {
        return "faqs";
    }


    /**
     * Create section.
     *
     * @return the section
     */
    public Section createSection() {
        Section section = new Section();
        _sections.add( section );
        return section;
    }


    /**
     * Format faq sections.
     *
     * @param sb the sb
     * @param format the format
     */
    private void formatFaqSections( StringBuffer sb, final FaqSectionFormat format ) {
        int i = 0;
        for (int j = 0; j < _sections.size(); j++) {
            Section section = (Section) _sections.get( j );
            section.append( sb, i, format );
            i += section.getNumItems();
        }
    }


    /**
     * The Interface FaqSectionFormat.
     */
    interface FaqSectionFormat {
        /**
         * Append section start.
         *
         * @param sb the sb
         * @param startIndex the start index
         * @param title the title
         */
        void appendSectionStart( StringBuffer sb, int startIndex, String title );
        /**
         * Append section end.
         *
         * @param sb the sb
         */
        void appendSectionEnd( StringBuffer sb );
        /**
         * Append entry.
         *
         * @param sb the sb
         * @param id the id
         * @param question the question
         * @param answer the answer
         */
        void appendEntry( StringBuffer sb, String id, String question, String answer );
        /**
         * Append external entry.
         *
         * @param sb the sb
         * @param url the url
         * @param question the question
         */
        void appendExternalEntry( StringBuffer sb, String url, String question );
    }


    /**
     * The Class IndexFormat.
     */
    class IndexFormat implements FaqSectionFormat {
        /**
         * Append section start.
         *
         * @param sb the sb
         * @param startIndex the start index
         * @param title the title
         */
        public void appendSectionStart( StringBuffer sb, int startIndex, String title ) {
            sb.append( LINE_BREAK ).append( "<p class='listSubtitle'>" ).append( title ).append( "</p>" );
            sb.append( LINE_BREAK ).append( "<ol start='" ).append( startIndex+1 ).append( "'>" );
        }

        /**
         * Append section end.
         *
         * @param sb the sb
         */
        public void appendSectionEnd( StringBuffer sb ) {
            sb.append( LINE_BREAK ).append( "</ol>" );
        }

        /**
         * Append entry.
         *
         * @param sb the sb
         * @param id the id
         * @param question the question
         * @param answer the answer
         */
        public void appendEntry( StringBuffer sb, String id, String question, String answer ) {
            sb.append( LINE_BREAK ).append( "  <li><a href='#" ).append( id ).append( "'>" ).append( question ).append( "</a></li>" );
        }


        /**
         * Append external entry.
         *
         * @param sb the sb
         * @param url the url
         * @param question the question
         */
        public void appendExternalEntry( StringBuffer sb, String url, String question ) {
            sb.append( LINE_BREAK ).append( "  <li><a href='" ).append( url ).append( "'>" ).append( question ).append( "</a></li>" );
        }

    }


    /**
     * The Class BodyFormat.
     */
    class BodyFormat implements FaqSectionFormat {
        /**
         * Append section start.
         *
         * @param sb the sb
         * @param startIndex the start index
         * @param title the title
         */
        public void appendSectionStart( StringBuffer sb, int startIndex, String title ) {
            sb.append( LINE_BREAK ).append( "<h2>" ).append( title ).append( "</h2>" );
        }


        /**
         * Append section end.
         *
         * @param sb the sb
         */
        public void appendSectionEnd( StringBuffer sb ) {
        }


        /**
         * Append entry.
         *
         * @param sb the sb
         * @param id the id
         * @param question the question
         * @param answer the answer
         */
        public void appendEntry( StringBuffer sb, String id, String question, String answer ) {
            sb.append( LINE_BREAK ).append( "<h3><a name='" ).append( id ).append( "'></a>" ).append( question ).append( "</h3>" );
            sb.append( answer );
        }


        /**
         * Append external entry.
         *
         * @param sb the sb
         * @param url the url
         * @param question the question
         */
        public void appendExternalEntry( StringBuffer sb, String url, String question ) {
        }

    }



    /**
     * The Class Section.
     */
    public class Section {

        /** The title. */
        private String _title;
        /** The entries. */
        private ArrayList _entries = new ArrayList<>();

        /**
         * Sets the title.
         *
         * @param title the title
         */
        public void setTitle( String title ) {
            _title = title;
        }


        /**
         * Create faq.
         *
         * @return the faq entry
         */
        public FaqEntry createFaq() {
            FaqEntry entry = new FaqEntry();
            _entries.add( entry );
            return entry;
        }


        /**
         * Gets the num items.
         *
         * @return the num items
         */
        public int getNumItems() {
            return _entries.size();
        }


        /**
         * Append.
         *
         * @param sb the sb
         * @param startingIndex the starting index
         * @param format the format
         */
        public void append( StringBuffer sb, int startingIndex, FaqSectionFormat format ) {
            format.appendSectionStart( sb, startingIndex, _title );
            for (int i = 0; i < _entries.size(); i++) {
                FaqEntry faqEntry = (FaqEntry) _entries.get( i );
                if (faqEntry.getUrl() == null) {
                    format.appendEntry( sb, faqEntry.getId(), faqEntry.getQuestion(), faqEntry.getAnswer() );
                } else {
                    format.appendExternalEntry( sb, faqEntry.getUrl(), faqEntry.getQuestion() );
                }
            }
            format.appendSectionEnd( sb );
        }

    }


    /**
     * The Class FaqEntry.
     */
    public class FaqEntry {

        /** The id. */
        private String _id;
        /** The url. */
        private String _url;
        /** The question. */
        private String _question;
        /** The answer. */
        private String _answer;


        /**
         * Gets the id.
         *
         * @return the id
         */
        public String getId() {
            return _id;
        }


        /**
         * Sets the id.
         *
         * @param id the id
         */
        public void setId( String id ) {
            _id = id;
        }


        /**
         * Gets the url.
         *
         * @return the url
         */
        public String getUrl() {
            return _url;
        }


        /**
         * Sets the url.
         *
         * @param url the url
         */
        public void setUrl( String url ) {
            _url = url;
        }


        /**
         * Gets the question.
         *
         * @return the question
         */
        public String getQuestion() {
            return _question;
        }


        /**
         * Sets the question.
         *
         * @param question the question
         */
        public void setQuestion( String question ) {
            _question = question;
        }


        /**
         * Gets the answer.
         *
         * @return the answer
         */
        public String getAnswer() {
            return _answer;
        }


        /**
         * Sets the answer.
         *
         * @param answer the answer
         */
        public void setAnswer( String answer ) {
            _answer = answer;
        }
    }
}

