/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.site;

import com.meterware.website.WebPage;

public class SourceForgeWebPage extends WebPage {

    private boolean _license;


    public boolean isLicense() {
        return _license;
    }


    public void setLicense( boolean license ) {
        _license = license;
    }
}
