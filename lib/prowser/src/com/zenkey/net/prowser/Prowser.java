/*#############################################################################
 * Prowser.java
 *
 * $Source: D:/Development/cvsroot/Prowser/src/com/zenkey/net/prowser/Prowser.java,v $
 * $Revision: 1.2 $
 * $Date: 2006/02/21 19:48:16 $
 * $Author: Michael $
 *
 * This file contains Java source code for the following class:
 * 
 *     com.zenkey.net.prowser.Prowser
 *
 * ============================================================================
 * 
 * Copyright (C) 2006  Michael A. Mangino
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 ############################################################################*/


package com.zenkey.net.prowser;

import java.util.ArrayList;

import org.apache.commons.httpclient.HttpState;


/**
 * The <code>Prowser</code> class serves as the creator and owner of one or
 * more {@link Tab} instances whose job it is to perform web page requests. You
 * can think of a <code>Prowser</code> object as a running instance of a web
 * browser application, while the <code>Tab</code> objects that it owns
 * represent the tabbed windows in which pages are actually retrieved.
 * <p>
 * Each <code>Tab</code> of a <code>Prowser</code> operates independently,
 * maintaining individual (i.e., <i>local</i>) state data like current page
 * information and browsing history. However, all <code>Tab</code>s owned by
 * a particular <code>Prowser</code> also <i>share</i> a common set of
 * <i>global</i> state data maintained by that <code>Prowser</code>. This
 * simulates the way mainstream tabbed browsers (like Firefox) operate: one
 * browser, with multiple tabs, all sharing various pieces of browser-wide
 * state information. For <code>Prowser</code>, this global state data
 * includes cookies, default configuration properties, and the home page (if
 * any).
 * <p>
 * As an example, <code>Prowser</code>'s cookie sharing functionality can
 * allow an applicaiton to use one <code>Tab</code> for logging into a web
 * account (and keeping the login session alive, perhaps in a concurrent thread
 * or a <code>TimerTask</code>), while other <code>Tab</code>s perform
 * actions on that account -- without having to log into separate sessions. If
 * an application ever needs <code>Tab</code>s that do not share cookies (
 * e.g., to log into multiple accounts on the same service), it can simply use
 * multiple <code>Prowser</code> instances.
 * <p>
 * A <code>Prowser</code> object is safe to use in multiple concurrent
 * threads.
 * 
 * @version $Revision: 1.2 $, $Date: 2006/02/21 19:48:16 $
 * @see Request
 * @see Response
 */

public class Prowser {

    /*#########################################################################
     *                             CONSTANTS
     *#######################################################################*/

    //
    // Preset Default Values --------------------------------------------------
    //
    
    /** Default user-agent header value. */
    /* package */ static final String  DEFAULT_USER_AGENT = "Prowser/0.1";
    
    /** Default HTTP version. */
    /* package */ static final String  DEFAULT_HTTP_VERSION = Request.HTTP_VERSION_1_1;
    
    /** Default request timeout. */
    /* package */ static final Integer DEFAULT_TIMEOUT = new Integer(Request.TIMEOUT_INFINITE);
    
    /** Default maximum "manual" redirects allowed before giving up. */
    /* package */ static final int     DEFAULT_MAX_REDIRECTS = 10;
    
    //
    // Miscellaneous ----------------------------------------------------------
    //

    
    /*#########################################################################
     *                          CLASS VARIABLES
     *#######################################################################*/


    /*#########################################################################
     *                      STATIC INITIALIZATION BLOCKS
     *#######################################################################*/

    // Turn off log messages produced by HttpClient components
    static {
        System.setProperty("org.apache.commons.logging.Log",
            "org.apache.commons.logging.impl.NoOpLog");

        // Alternative method
        //System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
        //System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "off");
    }


    /*#########################################################################
     *                        INSTANCE VARIABLES
     *#######################################################################*/

    /** Home page for this Prowser instance */
    private Request homePage = null;

    /** Default request timeout for this Prowser instance */
    private Integer defaultTimeout = null;
    
    /** Default HTTP version for this Prowser instance */
    private String defaultHttpVersion = null;
    
    /** Default user-agent string for this Prowser instance */
    private String defaultUserAgent = null;
    
    /** Maximum redirects allowed by this Prowser instance before giving up. */
    private int defaultMaxRedirects = DEFAULT_MAX_REDIRECTS;
    
    /** Array of Tabs currently open in this Prowser instance */
    private ArrayList<Tab> tabs = new ArrayList<Tab>();

    /**
     * State object used by this Prowser instance to share the same set of
     * cookies across all its Tabs
     */ 
    private HttpState httpState = new HttpState();

    
    /*#########################################################################
     *                           CONSTRUCTORS
     *#######################################################################*/

    /**************************************************************************
     * Constructs a new <code>Prowser</code> object with a preset default
     * configuration and no home page.
     */
    public Prowser() {
        initializeProwser(this, null, new Integer(DEFAULT_TIMEOUT),
            DEFAULT_HTTP_VERSION, DEFAULT_USER_AGENT);
    };

    /**************************************************************************
     * Constructs a new <code>Prowser</code> object with a preset default
     * configuration and the specified home page.
     * 
     * @param homePage
     *        The home page {@link Request} for this <code>Prowser</code>
     *        instance.
     */
    public Prowser(Request homePage) {
        initializeProwser(this, homePage, new Integer(DEFAULT_TIMEOUT),
            DEFAULT_HTTP_VERSION, DEFAULT_USER_AGENT);
    }

    /**************************************************************************
     * Constructs a new <code>Prowser</code> object with the specified home
     * page and the specified default configuration values. Any
     * <code>null</code> arguments will cause preset default values to be
     * used. (A <code>null</code> value for <code>homePage</code> leaves it
     * <code>null</code>.)
     * 
     * @param homePage
     *        The home page {@link Request} for this <code>Prowser</code>
     *        instance.
     * @param defaultHttpVersion
     *        The HTTP version to be used by this <code>Prowser</code>
     *        instance whenever a {@link Request} doesn't explicitly specify
     *        one.
     * @param defaultTimeout
     *        The request timeout (in milliseconds) to be used by this
     *        <code>Prowser</code> instance whenever a {@link Request}
     *        doesn't explicitly specify one.
     * @param defaultUserAgent
     *        The user-agent string to be used by this <code>Prowser</code>
     *        instance whenever a {@link Request} doesn't explicitly specify
     *        one.
     */
    public Prowser(Request homePage, String defaultHttpVersion,
            Integer defaultTimeout, String defaultUserAgent) {
        initializeProwser(this, homePage, defaultTimeout,
            defaultHttpVersion, defaultUserAgent);
    }

    /**************************************************************************
     * Constructs a new <code>Prowser</code> object with the specified
     * default configuration values and no home page. Any <code>null</code>
     * arguments will cause preset default values to be used.
     * 
     * @param defaultHttpVersion
     *        The HTTP version to be used by this <code>Prowser</code>
     *        instance whenever a {@link Request} doesn't explicitly specify
     *        one.
     * @param defaultTimeout
     *        The request timeout (in milliseconds) to be used by this
     *        <code>Prowser</code> instance whenever a {@link Request}
     *        doesn't explicitly specify one.
     * @param defaultUserAgent
     *        The user-agent string to be used by this <code>Prowser</code>
     *        instance whenever a {@link Request} doesn't explicitly specify
     *        one.
     */
    public Prowser(String defaultHttpVersion, Integer defaultTimeout,
            String defaultUserAgent) {
        initializeProwser(this, null, defaultTimeout,
            defaultHttpVersion, defaultUserAgent);
    }

    
    /*#########################################################################
     *                           CLASS METHODS
     *#######################################################################*/

    /**************************************************************************
     * Called by the constructors to set up the Prowser object (and HttpClient
     * object) before being put into use.
     * 
     * @param httpClient
     *        The Prowser's HttpClient object that will be used for performing
     *        page requests.
     * @param defaultHttpVersion
     *        The Prowser's default HTTP version.
     * @param defaultTimeout
     *        The Prowser's default request timeout (in milliseconds).
     * @param userAgent
     *        The Prowser's default user-agent string.
     */
    private static void initializeProwser(Prowser prowser,
            Request homePage, Integer defaultTimeout,
            String defaultHttpVersion, String defaultUserAgent) {

        // Set the home page for the Prowser
        prowser.homePage = homePage;

        // Set the default request timeout for the Prowser
        if (defaultTimeout != null)
            prowser.defaultTimeout = defaultTimeout;
        else
            prowser.defaultTimeout = DEFAULT_TIMEOUT;

        // Set the default HTTP version for the HttpClient
        if (defaultHttpVersion != null)
            prowser.defaultHttpVersion = defaultHttpVersion;
        else
            prowser.defaultHttpVersion = DEFAULT_HTTP_VERSION;
        
        // Set the default HTTP user-agent for the HttpClient 
        if (defaultUserAgent != null)
            prowser.defaultUserAgent = defaultUserAgent;
        else
            prowser.defaultUserAgent = DEFAULT_USER_AGENT;
    }

    
    /*#########################################################################
     *                          INSTANCE METHODS
     *#######################################################################*/

    /**************************************************************************
     * Closes the specified {@link Tab} so that it is no longer associated with
     * this <code>Prowser</code> instance.
     * 
     * @param tab
     *        {@link Tab} to be closed.
     * @return The closed {@link Tab} object.
     */
    public Tab closeTab(Tab tab) {
        if (tab == null)
            throw new IllegalArgumentException("Attempted to close a null tab");
        Tab closedTab = tab;
        synchronized (tabs) {
            if (!tab.isClosed()) {
                tab.markClosed();
                tabs.remove(tab);
            }
        }
        return closedTab;
    }

    /**************************************************************************
     * Closes all {@link Tab}'s associated with this <code>Prowser</code>
     * instance.
     * 
     * @return An array of the closed {@link Tab} objects.
     */
    public Tab[] closeTabs() {
        Tab[] closedTabs = null;
        synchronized (tabs) {
            closedTabs = getTabs();
            for (Tab tab : closedTabs) {
                closeTab(tab);
            }
        }
        return closedTabs;
    }
    
    /**************************************************************************
     * Opens a new {@link Tab} for this <code>Prowser</code> instance,
     * automatically loaded with the <code>Prowser</code>'s specified home
     * page (if any).
     * <p>
     * If a home page is loaded, it will be available via the
     * {@link Tab#getResponse()} method.
     * <p>
     * To prevent the home page from loading, use {@link #createTab(boolean)}
     * and pass it a value of <code>false</code>.
     * 
     * @return A newly-created {@link Tab} object that is owned and managed by
     *         this <code>Prowser</code>, automatically loaded with the
     *         <code>Prowser</code>'s specified home page (if any).
     * @see #createTab(boolean)
     */
    public Tab createTab() {
        return createTab(homePage != null ? true : false);
    }
    
    /**************************************************************************
     * Opens a new {@link Tab} for this <code>Prowser</code> instance. If
     * <code>loadHomePage</code> is true, the <code>Prowser</code>'s
     * specified home page (if any) is automatically loaded in the new
     * <code>Tab</code>; otherwise, a home page will not be loaded.
     * <p>
     * If a home page is loaded, it will be available via the
     * {@link Tab#getResponse()} method.
     * 
     * @param loadHomePage
     *        When <code>true</code>, causes the new <code>Tab</code> to
     *        automatically load the <code>Prowser</code>'s home page (if
     *        any).
     * @return A newly-created {@link Tab} object that is owned and managed by
     *         this <code>Prowser</code>, automatically loaded with the
     *         <code>Prowser</code>'s specified home page (if any) when the
     *         <code>loadHomePage</code> argument is <code>true</code>.
     * @see #createTab()
     */
    public Tab createTab(boolean loadHomePage) {
        Tab newTab = null;
        try {
            newTab = new Tab(this, loadHomePage);
        }
        catch (Exception e) {
            throw new IllegalStateException("Unable to create new tab ("
                    + e.getMessage() + ")");
        }
        tabs.add(newTab);
        return newTab;
    }
    
    /**************************************************************************
     * Returns the HttpState object that is used to maintain a common set of
     * cookies across all Prowser Tabs.
     * 
     * @return The HttpState object that is used to maintain a common set of
     *         cookies across all Prowser Tabs.
     */
    /* package */HttpState getHttpState() {
        return httpState;
    }
    
    /**************************************************************************
     * Returns the default HTTP version used by {@link Tab}s of this
     * <code>Prowser</code> instance whenever a {@link Request} doesn't
     * explicitly specify one.
     * 
     * @return The default HTTP version used by {@link Tab}s of this
     *         <code>Prowser</code> instance.
     */
    public String getDefaultHttpVersion() {
        return defaultHttpVersion;
    }
    
    /**************************************************************************
     * Returns the default maximum redirects allowed while processing a
     * request.
     * 
     * @return The default maximum redirects allowed while processing a
     *         request.
     */
    /* package */int getDefaultMaxRedirects() {
        return defaultMaxRedirects;
    }
    
    /**************************************************************************
     * Returns the default request timeout (in milliseconds) used by
     * {@link Tab}s of this <code>Prowser</code> instance whenever a
     * {@link Request} does not explicitly specify one.
     * 
     * @return The default request timeout (in milliseconds) used by
     *         {@link Tab}s of this <code>Prowser</code>.
     * @see #setDefaultTimeout(Integer)
     */
    public Integer getDefaultTimeout() {
        return defaultTimeout;
    }
    
    /**************************************************************************
     * Returns the default user-agent string used by {@link Tab}s of this
     * <code>Prowser</code> instance whenever a {@link Request} doesn't
     * explicitly specify one.
     * 
     * @return The default user-agent string used by {@link Tab}s of this
     *         <code>Prowser</code> instance.
     */
    public String getDefaultUserAgent() {
        return defaultUserAgent;
    }

    /**************************************************************************
     * Returns the home page {@link Request} object for this
     * <code>Prowser</code> instance.
     * 
     * @return The home page {@link Request} object for this
     *         <code>Prowser</code> instance, or <code>null</code> if no
     *         home page has been set.
     */
    public Request getHomePage() {
        return homePage;
    }
    
    /**************************************************************************
     * Returns the number of {@link Tab}'s currently open for this
     * <code>Prowser</code> instance.
     * 
     * @return The number of {@link Tab}'s currently open for this
     *         <code>Prowser</code> instance.
     */
    public int getTabCount() {
        return tabs.size();
    }
    
    /**************************************************************************
     * Returns an array of all the open tabs in this <code>Prowser</code>
     * instance.
     * 
     * @return An array of all the open tabs in this <code>Prowser</code>
     *         instance.
     */
    public Tab[] getTabs() {
        synchronized (tabs) {
            return tabs.toArray(new Tab[tabs.size()]);
        }
    }

    /**************************************************************************
     * Sets a new default HTTP version to be used by {@link Tab}s of this <code>Prowser</code>
     * instance whenever a {@link Request} doesn't explicitly specify one. If
     * this value is never set, a preset default is used.
     * 
     * @param httpVersion
     *        The new default HTTP version to be used by {@link Tab}s of this
     *        <code>Prowser</code>.
     * @throws IllegalArgumentException
     *         If <code>httpVersion</code> is not valid.
     */
    public void setDefaultHttpVersion(String httpVersion) {
        
        if (httpVersion == null)
            throw new IllegalArgumentException("Null default HTTP version");
        this.defaultHttpVersion = httpVersion;
        
        // Update the default HTTP version in the HttpClient of all tabs
        for (Tab tab : tabs) {
            tab.setHttpClientHttpVersion(httpVersion);
        }
    }
    
    /**************************************************************************
     * Sets a new default request timeout (in milliseconds) to be used by
     * {@link Tab}s of this <code>Prowser</code> instance whenever a
     * {@link Request} doesn't explicitly specify one. If this value is never
     * set, a preset default of {@link Request#TIMEOUT_INFINITE} is used.
     * <p>
     * A value of {@link Request#TIMEOUT_INFINITE} indicates that the calling
     * thread should wait indefinitely for requests to complete.
     * 
     * @param timeout
     *        The new default request timeout (in milliseconds) to be used by
     *        {@link Tab}s of this <code>Prowser</code>, or
     *        {@link Request#TIMEOUT_INFINITE} to indicate that the default
     *        behavior is to never time out requests that don't explicitly
     *        specify a timeout.
     * @throws IllegalArgumentException
     *         If the timeout value is <code>null</code> or less than zero.
     * @see Request#setTimeout(Integer) Setting an explicit request timeout.
     * @see Tab#stop(Integer) Setting a temporary request timeout.
     */
    public void setDefaultTimeout(Integer timeout) {
        if (timeout == null || timeout.intValue() < 0)
            throw new IllegalArgumentException(
                "Invalid default request timeout value: " + timeout);
        this.defaultTimeout = timeout;
    }

    /**************************************************************************
     * Sets a new default user-agent string to be used by {@link Tab}s of this
     * <code>Prowser</code> instance whenever a {@link Request} doesn't
     * explicitly specify one. If this value is never set, a preset default is
     * used.
     * 
     * @param userAgent
     *        The new default user-agent string to be used by {@link Tab}s of
     *        this <code>Prowser</code>.
     */
    public void setDefaultUserAgent(String userAgent) {

        if (userAgent == null)
            throw new IllegalArgumentException("Null default user-agent");
        this.defaultUserAgent = userAgent;
        
        // Update the default user-agent string in the HttpClient of all tabs
        for (Tab tab : tabs) {
            tab.setHttpClientUserAgent(userAgent);
        }
    }

    /**************************************************************************
     * Sets the home page {@link Request} object for this <code>Prowser</code>
     * instance.
     * 
     * @param homePage
     *        The home page {@link Request} object for this
     *        <code>Prowser</code> instance.
     */
    public void setHomePage(Request homePage) {
        this.homePage = homePage;
    }
}
