/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit;

/**
 * A listener for DNS Requests. Users may implement this interface to bypass the normal DNS lookup.
 */
public interface DNSListener {

    /**
     * Returns the IP address as a string for the specified host name. Note: no validation is done to verify that the
     * returned value is an actual IP address or that the passed host name was not an IP address.
     *
     * @param hostName
     *            the host name
     *
     * @return the ip address
     */
    String getIpAddress(String hostName);

}
