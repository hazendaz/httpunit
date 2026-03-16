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


public class Faq extends FragmentTemplate {

    private ArrayList _sections = new ArrayList<>();


    public FragmentTemplate newFragment() {
        return new Faq();
    }


    public String asText() {
        StringBuffer sb = new StringBuffer();
        formatFaqSections( sb, new IndexFormat() );
        sb.append( "<hr/>" ).append( LINE_BREAK );
        formatFaqSections( sb, new BodyFormat() );
        return sb.toString();
    }


    protected String getRootNodeName() {
        return "faqs";
    }


    public Section createSection() {
        Section section = new Section();
        _sections.add( section );
        return section;
    }


    private void formatFaqSections( StringBuffer sb, final FaqSectionFormat format ) {
        int i = 0;
        for (int j = 0; j < _sections.size(); j++) {
            Section section = (Section) _sections.get( j );
            section.append( sb, i, format );
            i += section.getNumItems();
        }
    }


    interface FaqSectionFormat {
        void appendSectionStart( StringBuffer sb, int startIndex, String title );
        void appendSectionEnd( StringBuffer sb );
        void appendEntry( StringBuffer sb, String id, String question, String answer );
        void appendExternalEntry( StringBuffer sb, String url, String question );
    }


    class IndexFormat implements FaqSectionFormat {
        public void appendSectionStart( StringBuffer sb, int startIndex, String title ) {
            sb.append( LINE_BREAK ).append( "<p class='listSubtitle'>" ).append( title ).append( "</p>" );
            sb.append( LINE_BREAK ).append( "<ol start='" ).append( startIndex+1 ).append( "'>" );
        }

        public void appendSectionEnd( StringBuffer sb ) {
            sb.append( LINE_BREAK ).append( "</ol>" );
        }

        public void appendEntry( StringBuffer sb, String id, String question, String answer ) {
            sb.append( LINE_BREAK ).append( "  <li><a href='#" ).append( id ).append( "'>" ).append( question ).append( "</a></li>" );
        }


        public void appendExternalEntry( StringBuffer sb, String url, String question ) {
            sb.append( LINE_BREAK ).append( "  <li><a href='" ).append( url ).append( "'>" ).append( question ).append( "</a></li>" );
        }

    }


    class BodyFormat implements FaqSectionFormat {
        public void appendSectionStart( StringBuffer sb, int startIndex, String title ) {
            sb.append( LINE_BREAK ).append( "<h2>" ).append( title ).append( "</h2>" );
        }


        public void appendSectionEnd( StringBuffer sb ) {
        }


        public void appendEntry( StringBuffer sb, String id, String question, String answer ) {
            sb.append( LINE_BREAK ).append( "<h3><a name='" ).append( id ).append( "'></a>" ).append( question ).append( "</h3>" );
            sb.append( answer );
        }


        public void appendExternalEntry( StringBuffer sb, String url, String question ) {
        }

    }



    public class Section {

        private String _title;
        private ArrayList _entries = new ArrayList<>();

        public void setTitle( String title ) {
            _title = title;
        }


        public FaqEntry createFaq() {
            FaqEntry entry = new FaqEntry();
            _entries.add( entry );
            return entry;
        }


        public int getNumItems() {
            return _entries.size();
        }


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


    public class FaqEntry {

        private String _id;
        private String _url;
        private String _question;
        private String _answer;


        public String getId() {
            return _id;
        }


        public void setId( String id ) {
            _id = id;
        }


        public String getUrl() {
            return _url;
        }


        public void setUrl( String url ) {
            _url = url;
        }


        public String getQuestion() {
            return _question;
        }


        public void setQuestion( String question ) {
            _question = question;
        }


        public String getAnswer() {
            return _answer;
        }


        public void setAnswer( String answer ) {
            _answer = answer;
        }
    }
}

