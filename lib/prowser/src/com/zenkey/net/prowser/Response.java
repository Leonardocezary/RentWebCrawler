/*#############################################################################
 * Response.java
 *
 * $Source: D:/Development/cvsroot/Prowser/src/com/zenkey/net/prowser/Response.java,v $
 * $Revision: 1.2 $
 * $Date: 2006/02/21 19:37:18 $
 * $Author: Michael $
 *
 * This file contains Java source code for the following class:
 * 
 *     com.zenkey.net.prowser.Response
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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * The <code>Response</code> class represents the data resulting from a web
 * page request made by a {@link Tab} instance. A <code>Response</code>
 * object is generated when a <code>Tab</code> executes a {@link Request}.
 * <p>
 * The data available in a <code>Response</code> object includes:
 * <ul>
 * <li>Web page source code (encoded text).
 * <li>Web page in binary form (array of bytes).
 * <li>HTTP status code.
 * <li>HTTP status text accompanying the status code.
 * <li>HTTP version.
 * <li>Error code of the request/response transaction.
 * <li>Error text accompanying the error code.
 * <li>Duration of the request/response transaction (in milliseconds).
 * <li>Originating {@link Request} object.
 * </ul>
 * <p>
 * <code>Response</code> objects are only created by the {@link Tab}
 * class; there are no public constructors for them.
 * <p>
 * A <code>Response</code> object is thread-safe. Once created, it cannot be
 * altered.
 * 
 * @version $Revision: 1.2 $, $Date: 2006/02/21 19:37:18 $
 * @see Tab
 * @see Request
 */

public class Response {

    /*#########################################################################
     *                             CONSTANTS
     *#######################################################################*/

    //
    // Error Codes -----------------------------------------------------------
    //
    
    /**
     * No error condition exists.
     * 
     * @see #getError()
     * @see #getErrorText()
     */
    public static final int ERR_NONE = 0;

    /**
     * General error.
     * 
     * @see #getError()
     * @see #getErrorText()
     */
    public static final int ERR_GENERAL = -1;

    /**
     * HTTP protocol error.
     * 
     * @see #getError()
     * @see #getErrorText()
     */
    public static final int ERR_PROTOCOL = -2;

    /**
     * HTTP I/O (transport) error.
     * 
     * @see #getError()
     * @see #getErrorText()
     */
    public static final int ERR_IO = -3;

    /**
     * Max HTTP redirects exceeded.
     * 
     * @see #getError()
     * @see #getErrorText()
     */
    public static final int ERR_MAX_REDIRECTS = -4;

    /**
     * No "Location" field in header of redirection response.
     * 
     * @see #getError()
     * @see #getErrorText()
     */
    public static final int ERR_NO_LOCATION = -5;

    /**
     * URI error.
     * 
     * @see #getError()
     * @see #getErrorText()
     */
    public static final int ERR_URI = -6;

    /**
     * Request timed out.
     * 
     * @see #getError()
     * @see #getErrorText()
     */
    public static final int ERR_TIMEOUT = -7;

    //
    // HTTP Status Codes ------------------------------------------------------
    //

    /**
     * The value returned by {@link #getStatus()} when HTTP status information
     * has not been obtained for the response.
     * 
     * @see #getStatus()
     * @see #getStatusText()
     * @see #getError()
     */
    public static final int STATUS_NOT_OBTAINED = -1;

    /** Successful HTTP response. */
    /* package */ static final int STATUS_200_OK = 200;

    /** HTTP response indicating that the requested resource has been moved to a new permanent URI. */
    /* package */ static final int STATUS_301_MOVED_PERMANENTLY = 301;

    /** HTTP response indicating that the requested resource resides temporarily under a different URI. */
    /* package */ static final int STATUS_302_FOUND = 302;

    /** HTTP response indicating that the response to the request can be found under a different URI and SHOULD be retrieved using a GET method on that resource. */
    /* package */ static final int STATUS_303_SEE_OTHER = 303;

    /** HTTP response indicating that the requested resource resides temporarily under a different URI. */
    /* package */ static final int STATUS_307_TEMPORARY_REDIRECT = 307;

    
    /*#########################################################################
     *                         CLASS VARIABLES
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

    /** Code indicating the error condition of the request that generated this response. */
    private int error = ERR_NONE;

    /** Text describing the error condition of the request that generated this response. */
    private String errorText = null;
    
    /** HTTP status code returned in the response from the Web server. */
    private int status = STATUS_NOT_OBTAINED;
    
    /** HTTP status text accompanying the status code. */
    private String statusText = null;
    
    /** HTTP status version accompanying the status code. */
    private String statusVersion = null;
    
    /** Binary form of the response body. */
    private byte[] pageBytes = null;
    
    /** Encoded, textual form of the response body. */
    private String pageSource = null;
    
    /** Title of the the page (taken from the <title> tag) */
    private String pageTitle = null;

    /** Character set of the response body. */
    private String responseCharset = null;
    
    /** Content-type of the response */
    private String responseContentType = null;
    
    /** Milliseconds to complete the request that generated this response. */
    private long duration = -1;

    /** Request object that generated this response. */
    private Request request = null;
    
    /** Tab object that generated this response. */
    private Tab tab = null; 

    /** Final URI of the retrieved web page (after all server-side redirects). */
    private URI uri = null;
    
    /** Filename of the retrieved web page (or "" if no filename in URI). */
    private String filename = null;


    /*#########################################################################
     *                           CONSTRUCTORS
     *#######################################################################*/

    /**************************************************************************
     * Package-level, do-nothing constructor.
     */
    /* package */ Response() {};


    /*#########################################################################
     *                             METHODS
     *#######################################################################*/
    
    /**************************************************************************
     * Returns the value of the HTTP <code>Content-type</code> header
     * associated with this <code>Response</code>.
     * 
     * @return The value of the HTTP <code>Content-type</code> header
     *         associated with this <code>Response</code>.
     */
    public String getContentType() {
        return responseContentType;
    }
    
    /**************************************************************************
     * Returns the number of milliseconds that it took to complete the
     * request/response transaction that generated this <code>Response</code>.
     * 
     * @return The number of milliseconds that it took to complete the request
     *         that generated this response.
     */
    public long getDuration() {
        return duration;
    }

    /**************************************************************************
     * Returns a code indicating the error condition of the page request that
     * generated this <code>Response</code>. If no error condition exists, {@link #ERR_NONE}
     * is returned; otherwise, one of the other <a href="#field_summary"><code>ERR_<i>xxx</i></code></a>
     * values are returned.
     * 
     * @return A code representing the error condition of this request that
     *         generated this <code>Response</code>.
     * @see #ERR_NONE
     * @see #getErrorText()
     */
    public int getError() {
        return error;
    }

    /**************************************************************************
     * Returns text describing the error condition of the request that
     * generated this <code>Response</code>.
     * 
     * @return A <code>String</code> describing the error condition of the
     *         request that generated this <code>Response</code>, or
     *         <code>null</code> if no error condition exists (i.e.,
     *         {@link #getError()} returns {@link #ERR_NONE}).
     * @see #getError()
     * @see #ERR_NONE
     */
    public String getErrorText() {
        return errorText;
    }

    /**************************************************************************
     * Returns the name of the web page file associated with this
     * <code>Response</code>. If the URI did not contain a filename, then an
     * empty string is returned.
     * 
     * @return The name of the web page file associated with this
     *         <code>Response</code>, or an empty string if the URI did not
     *         contain a filename.
     */
    public String getFilename() {
        if (filename == null) {
            String[] pathComponents = uri.getPath().split("/");
            if (pathComponents.length > 0)
                filename = pathComponents[pathComponents.length - 1];
            else
                filename = "";
        }
        return filename;
    }
    
    /**************************************************************************
     * Returns the binary form of the retrieved web page associated with this
     * <code>Response</code>.
     * 
     * @return The web page as a byte array.
     * @see #getPageSource()
     */
    public byte[] getPageBytes() {
        return pageBytes;
    }

    /**************************************************************************
     * Returns the textual form of the retrieved web page associated with this
     * <code>Response</code>. The returned value is a <code>String</code>
     * encoded with the character set specified in the response header. If no
     * encoding was supplied, the default encoding is used.
     * 
     * @return The web page as an encoded string.
     * @see #getPageBytes()
     */
    public String getPageSource() {
        if (pageSource == null && pageBytes != null) {
            try {
                pageSource = new String(pageBytes, responseCharset);
            } catch (UnsupportedEncodingException e) {
                pageSource = new String(pageBytes);
            } 
        }
        return pageSource;
    }
    
    /**************************************************************************
     * Returns the {@link Request} object that generated this
     * <code>Response</code>.
     * 
     * @return The {@link Request} object that generated this
     *         <code>Response</code>.
     * @see Request
     * @see Tab
     */
    public Request getRequest() {
        return request;
    }
    
    /**************************************************************************
     * Returns the HTTP status code associated with this <code>Response</code>.
     * If the response was not completed due to an error during the request,
     * {@link #STATUS_NOT_OBTAINED} will be returned.
     * 
     * @return The HTTP status code associated with this <code>Response</code>,
     *         or {@link #STATUS_NOT_OBTAINED} if an error occurred during the
     *         request.
     * @see #getStatusText()
     * @see #getStatusVersion()
     * @see Response#getError()
     */
    public int getStatus() {
        return status;
    }
    
    /**************************************************************************
     * Returns the HTTP status text associated with this <code>Response</code>.
     * 
     * @return The HTTP status text associated with this <code>Response</code>,
     *         or <code>null</code> if an error occurred during the request
     *         (i.e., {@link #getStatus()} returns {@link #STATUS_NOT_OBTAINED}).
     * @see #getStatus()
     * @see #getStatusVersion()
     * @see Response#getError()
     */
    public String getStatusText() {
        return statusText;
    }

    /**************************************************************************
     * Returns the HTTP status version associated with this
     * <code>Response</code>.
     * 
     * @return The HTTP status version associated with this
     *         <code>Response</code>, or <code>null</code> if an error
     *         occurred during the request (i.e., {@link #getStatus()} returns
     *         {@link #STATUS_NOT_OBTAINED}).
     * @see #getStatus()
     * @see #getStatusText()
     * @see Response#getError()
     */
    public String getStatusVersion() {
        return statusVersion;
    }
    
    /**************************************************************************
     * Returns the {@link Tab} that generated this <code>Response</code>.
     * 
     * @return The {@link Tab} that generated this <code>Response</code>.
     */
    public Tab getTab() {
        return tab;
    }

    /**************************************************************************
     * Returns the title of the web page associated with this
     * <code>Response</code>, as specified in the <code>&lt;title&gt;</code>
     * tag of the page's HTML source code. If the page is not HTML source
     * (i.e., it's a binary file), then the filename is returned instead.
     * <p>
     * Note that all leading and trailing whitespace is trimmed from the title
     * string before it is returned.
     * 
     * @return The title of the web page associated with this
     *         <code>Response</code>, or the filename of the retrieved page
     *         if it is not in the form of HTML source code.
     */
    public String getTitle() {

        // If page title hasn't been determined yet, do it
        if (pageTitle == null) {

            // If Content-type is not some form of HTML, use filename as title
            if (responseContentType == null || !responseContentType.matches("(?i).*HTML.*")) {
                pageTitle = getFilename();
            }
            
            // Else get title from page source (use filename if title not found)
            else {
                getPageSource();
                Pattern pattern = Pattern
                    .compile("(?s)(?i).*<TITLE(?:\\s+.*)?>(.*)</TITLE>");
                Matcher matcher = pattern.matcher(pageSource);
                if (matcher.lookingAt())
                    pageTitle = matcher.group(1).trim();
                else
                    pageTitle = getFilename();
            }
        }
        
        // Return the page title
        return pageTitle;
    }
    
    /**************************************************************************
     * Returns the final URI of this <code>Response</code>. Note that this can
     * be different from the {@link Request} URI because of server-side
     * redirects that can occur during request processing. Think of this value
     * as the URI that appears in a web browser's address bar after the page
     * has completed loading.  
     * 
     * @return The the final URI of this <code>Response</code>.
     */
    public URI getUri() {
        return uri;
    }

    /**************************************************************************
     * Sets the number of milliseconds that it took to complete the
     * request/response transaction that generated this <code>Response</code>.
     * 
     * @param duration
     *        The number of milliseconds that it took to complete the request
     *        that generated this response.
     */
    /* package */ void setDuration(long duration) {
        this.duration = duration;
    }

    /**************************************************************************
     * Sets a code indicating the error condition of the request that generated
     * this <code>Response</code>.
     * 
     * @param error
     *        A code indicating the error condition of the request that
     *        generated this <code>Response</code>.
     */
    /* package */ void setError(int error) {
        this.error = error;
    }

    /**************************************************************************
     * Sets the text describing the error condition of the request that
     * generated this <code>Response</code>.
     * 
     * @param errorText
     *        The text describing the error condition of the request that
     *        generated this <code>Response</code>.
     */
    /* package */ void setErrorText(String errorText) {
        this.errorText = errorText;
    }
    
    /**************************************************************************
     * Sets the binary form of this <code>Response</code>'s body, as an
     * array of bytes.
     * 
     * @param body
     *        A byte array representing the binary form of the response body.
     */
    /* package */ void setPageBytes(byte[] body) {
        this.pageBytes = body;
    }
    
    /**************************************************************************
     * Sets the character set indentifier returned in the HTTP header for this
     * <code>Response</code>. This value is used to generate the page source
     * in the {@link #getPageSource()} method.
     * 
     * @param responseCharset
     *        The character set indentifier returned in the header of the HTTP
     *        response.
     * @see #getPageSource()
     */
    /* package */ void setPageCharset(String pageCharset) {
        this.responseCharset = pageCharset;
    }

    /**************************************************************************
     * Sets the value returned in the HTTP Content-type header for this
     * <code>Response</code>. This value is used when generating the page title
     * in the {@link #getPageTitle()} method.
     * 
     * @param responseContentType
     *        The value returned in the Content-type header of the HTTP
     *        response.
     * @see #getPageTitle()
     */
    /* package */ void setPageContentType(String pageContentType) {
        this.responseContentType = pageContentType;
    }

    /**************************************************************************
     * Sets the {@link Request} object that was used to generate this
     * <code>Response</code>.
     * 
     * @param request
     *        The {@link Request} object that was used to generate this
     *        <code>Response</code>.
     */
    /* package */ void setRequest(Request request) {
        this.request = request;
    }

    /**************************************************************************
     * Sets the HTTP status code associated with this <code>Response</code>.
     *
     * @param status The HTTP status code associated with this response.
     * @see #setStatusText(String)
     * @see #setStatusVersion(String)
     */
    /* package */ void setStatus(int status) {
        this.status = status;
    }
    
    /**************************************************************************
     * Sets the HTTP status text associated with this <code>Response</code>.
     *
     * @param statusText The HTTP status text associated with this response.
     * @see #setStatus(int)
     * @see #setStatusVersion(String)
     */
    /* package */ void setStatusText(String statusText) {
        this.statusText = statusText;
    }
    
    /**************************************************************************
     * Sets the HTTP status version associated with this <code>Response</code>.
     * 
     * @param statusVersion
     *        The HTTP status version associated with this response.
     * @see #setStatus(int)
     * @see #setStatusText(String)
     */
    /* package */ void setStatusVersion(String statusVersion) {
        this.statusVersion = statusVersion;
    }

    /**************************************************************************
     * Sets the Tab object that generated this <code>Response</code>.
     * 
     * @param tab
     *        The Tab object that generated this <code>Response</code>.
     */
    /* package */ void setTab(Tab tab) {
        this.tab = tab;
    }
    
    /**************************************************************************
     * Sets the final URI of this <code>Response</code>. Note that this can
     * be different from the {@link Request} URI because of server-side
     * redirects that can occur during request processing. Think of this value
     * as the URI that appears in a web browser's address bar after the page
     * has completed loading.  
     * 
     * @param tab
     *        The Tab object that generated this <code>Response</code>.
     */
    /* package */ void setUri(URI uri) {
        this.uri = uri;
    }
}
