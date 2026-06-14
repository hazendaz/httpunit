/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.site;

import com.meterware.website.*;


/**
 * The Class SourceForgeSiteTemplate.
 */
public class SourceForgeSiteTemplate extends BasicSiteTemplate {

    /**
     * Append page header.
     *
     * @param sb the sb
     * @param site the site
     * @param currentPage the current page
     */
    public void appendPageHeader( StringBuffer sb, Site site, SiteLocation currentPage ) {
        super.appendPageHeader( sb, site, currentPage );
        if (isLicensePage( currentPage )) sb.append( "<p>" ).append( site.getCopyRight().getNotice() ).append( "</p>" ). append( FragmentTemplate.LINE_BREAK );
    }


    /**
     * Append page footer.
     *
     * @param sb the sb
     * @param site the site
     * @param currentPage the current page
     */
    public void appendPageFooter( StringBuffer sb, Site site, SiteLocation currentPage ) {
        if (!isLicensePage( currentPage )) {
            sb.append( "<hr/><div style='position:relative'>" ).append( FragmentTemplate.LINE_BREAK );
            sb.append( "  <div>" ).append( site.getCopyRight().getNotice() ).append( "</div>" ).append( FragmentTemplate.LINE_BREAK );
            ((WebSite) site).appendSourceForgeHostingNotice( sb );
        }
        super.appendPageFooter( sb, site, currentPage );
    }


    /**
     * Is license page.
     *
     * @param currentPage the current page
     * @return true, if successful
     */
    private boolean isLicensePage( SiteLocation currentPage ) {
        return ((SourceForgeWebPage) currentPage).isLicense();
    }


}
