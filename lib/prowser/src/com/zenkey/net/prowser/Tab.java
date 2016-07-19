/*#############################################################################
 * Tab.java
 *
 * $Source: D:/Development/cvsroot/Prowser/src/com/zenkey/net/prowser/Tab.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/02/21 19:55:15 $
 * $Author: Michael $
 *
 * This file contains Java source code for the following class:
 * 
 *     com.zenkey.net.prowser.Tab
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.ProtocolException;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;


/**
 * The <code>Tab</code> class represents a "tab window" that belongs to a
 * particular {@link Prowser} instance; <code>Tab</code> objects actually do
 * the work of executing web page requests and processing the responses.
 * Multiple <code>Tab</code> instances can be owned by a single
 * <code>Prowser</code> in the same way that a tabbed GUI web browser can
 * have more than one tab window open at any one time.
 * <p>
 * The <code>Tab</code> class makes it easy to retrieve web pages, including
 * encrypted https requests. You simply specify URLs and (when necessary) form
 * data. In lieu of actually being able to <i>see</i> the resulting pages,
 * their contents -- text or binary -- are easily retrievable. Also, all cookie
 * processing and all normal server-side redirections are handled
 * automatically. Even HTTP Basic and Digest Authentication are performed
 * seamlessly.
 * <p>
 * A <code>Tab</code> object saves various pieces of state information
 * between page requests. This is done to provide browser functionality like
 * {@link #getPageSource() source viewing} and
 * {@link #refresh() page refreshing}. Of particular note is the fact that for
 * each request/response transaction that a <code>Tab</code> executes, it
 * saves the {@link Request} and {@link Response} objects internally until the
 * next page request is processed. These <code>Request</code> and
 * <code>Response</code> objects are referred to as the "current"
 * <code>Request</code> and <code>Response</code> (sometimes collectively
 * called the "current page").
 * <p>
 * In keeping with <i>Prowser's</i> "GUI browser functionality" philosophy,
 * the <code>Tab</code> class provides methods that perform common browser
 * operations like {@link #goBack() going back} one page,
 * {@link #goForward() going forward} one page, {@link #refresh() refreshing}
 * the current page, returning to the {@link #goHome() home page}, and
 * {@link #stop(Integer) stopping} the current page request.
 * <p>
 * <h3>Usage</h3>
 * <p>
 * An object of the <code>Prowser</code> class is used to create and maintain
 * one or more <code>Tab</code> objects. A <code>Tab</code> object acts
 * upon a {@link Request} object in order to retrieve a web page. The request
 * results in a {@link Response} object that contains the page contents (and
 * other information related to the transaction).
 * <p>
 * For example, to print out the page source for <code>http://java.net/</code>,
 * you could do this:
 * 
 * <pre>
 *     Prowser prowser = new Prowser();
 *     Tab tab = prowser.createTab();
 *     Request request = new Request("http://java.net/");
 *     Response response = tab.go(request);
 *     String html = response.getPageSource();
 *     System.out.println(html);
 * </pre>
 * 
 * Or, you could forego most of the intermediate variables, and reduce the
 * above code to this:
 * 
 * <pre>
 *     Tab tab = new Prowser().createTab();
 *     System.out.println(
 *         tab.go("http://java.net/").getPageSource());
 * </pre>
 * 
 * <p>
 * <blockquote> <b>Note:</b> If you later needed the <code>Prowser</code>
 * object instantiated in the above code snippet, you could retrieve it with
 * the following statement:
 * 
 * <pre>
 *     Prowser prowser = tab.getProwser();
 * </pre>
 * 
 * </blockquote>
 * <p>
 * To download a file, you use the {@link #getPageBytes()} method instead of
 * {@link #getPageSource()}. For example (assuming that a <code>Tab</code>
 * object named <code>tab</code> already exists) you could obtain a PDF file
 * with the following code:
 * 
 * <pre>
 *     Request request =
 *         new Request("http://java.sun.com/xml/webservices.pdf");
 *     byte[] fileContents = tab.go(request).getPageBytes();
 * </pre>
 * 
 * To submit a web form and retrieve the resulting page (e.g., logging into
 * your bank account), you can use the pertinent methods to configure a
 * {@link Request} object with the proper URL, HTTP method (<code>POST</code>)
 * and form field values. However, you also have the option of specifying the
 * request's configuration in a <i>request file</i>, which can be set up once
 * and used repeatedly without having to configure the request
 * programmatically. See
 * {@link Request#Request(java.io.File) Request(java.io.File)} for more
 * information.
 * <p>
 * To request a page that requires <b>Basic</b> or <b>Digest Authentication</b>,
 * simply include the username and password as part of the URI, like this:
 * 
 * <pre>
 *     http://username:password@www.site-requiring-auth.com/
 * </pre>
 * 
 * <p>
 * <h3>Thread Safety</h3>
 * <p>
 * An individual <code>Tab</code> object is <b>not</b> thread-safe. This is
 * due to local state information (like "current" page and browsing history)
 * that each <code>Tab</code> maintains. However, multiple <code>Tab</code>
 * instances <b>can</b> be safely used in concurrent threads, as long as no
 * two threads are using the same <code>Tab</code>. Also, a single thread
 * can safely employ more than one <code>Tab</code>.
 * 
 * @version $Revision: 1.1 $, $Date: 2006/02/21 19:55:15 $
 * @see Prowser
 * @see Request
 * @see Response
 */

public class Tab {

    /*#########################################################################
     *                             CONSTANTS
     *#######################################################################*/

    //
    // Trace Levels -----------------------------------------------------------
    //
    
    /**
     * No tracing.
     * 
     * @see #setTraceLevel(int)
     */
    public static final int TRACE_OFF = 0;
    
    /**
     * Write only URIs to standard output.
     * 
     * @see #setTraceLevel(int)
     */
    public static final int TRACE_URI = 1;
    
    /**
     * Write HTTP request and response (status) lines to standard output. The
     * output will also include the URIs.
     * 
     * @see #setTraceLevel(int)
     */
    public static final int TRACE_REQUEST_RESPONSE_LINES = 2;
    
    /**
     * Write HTTP request and response headers to standard output. The output
     * will also include the request/response lines and the URIs.
     * 
     * @see #setTraceLevel(int)
     */
    public static final int TRACE_HEADERS = 3;
    
    /**
     * Write HTTP response bodies to standard output. The output will also
     * include the headers, the request/response lines and the URIs.
     * 
     * @see #setTraceLevel(int)
     */
    public static final int TRACE_BODY = 4;

    /**
     * Write all trace info. (Equivalent to the highest trace level.)
     * 
     * @see #setTraceLevel(int)
     */
    public static final int TRACE_ALL = Integer.MAX_VALUE;

    //
    // Miscellaneous ----------------------------------------------------------
    //

    private static final int MAX_HISTORY = 15;
    
    
    /*#########################################################################
     *                          CLASS VARIABLES
     *#######################################################################*/

    /** Retry handler for HTTP methods. */
    private static HttpMethodRetryHandler httpMethodRetryHandler
        = new ProwserMethodRetryHandler();


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

    /** Most recent request submitted. */
    private Request currentRequest = null;
    
    /** Most recent response generated. */
    private Response currentResponse = null;
    
    /** HttpClient instance used to transact the requests/responses */
    private HttpClient httpClient = null;

    /** History list */
    private LinkedList<Request> history = new LinkedList<Request>();

    /** History list index */
    private int historyIndex = -1;

    /** Trace level for writing request/response transaction info */
    private int traceLevel = TRACE_OFF;
    
    /** Stream for writing trace output. (Defaults to standard output.) */
    private PrintStream traceStream = System.out;
    
    /** Temporary timeout specified by the stop(Integer) method */
    private Integer tempTimeout = null;
    
    /** The Prowser instance that owns this Tab */
    private Prowser prowser = null;
    
    /** Indicates if the Tab is still "open" (i.e., usable) */
    private boolean isClosed = false;
    
    
    /*#########################################################################
     *                           CONSTRUCTORS
     *#######################################################################*/

    
    /**************************************************************************
     * Constructs and initializes a new <code>Tab</code> object for the
     * specified Prowser, and then visits the home page if requested.
     *
     * @param prowser
     * @param loadHomePage
     */
    /* package */ Tab(Prowser prowser, boolean loadHomePage) {
        
        // Record the prowser of this Tab instance
        this.prowser = prowser;

        // Create an HttpClient instance to handle this Tab's requests
        httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
        
        // Set the cookies that are shared across all tabs of owning Prowser 
        httpClient.setState(prowser.getHttpState());
            
        // Set the default HTTP version for the HttpClient
        setHttpClientHttpVersion(prowser.getDefaultHttpVersion());

        // Set the default HTTP user-agent for the HttpClient
        setHttpClientUserAgent(prowser.getDefaultUserAgent());

        // If the home page should be loaded first into this Tab, do it
        if (loadHomePage)
            goHome();
    }


    /*#########################################################################
     *                           CLASS METHODS
     *#######################################################################*/

    /**************************************************************************
     * Configures the HttpMethod object before use.
     * 
     * @param httpMethod
     *        The HTTP method object.
     * @param request
     *        The request object.
     * @param ioTimeout
     *        The timeout value to use for I/O operations (like making
     *        connections and socket reads). This value should be 1 second
     *        longer than the actual timeout used for the request, in order to
     *        allow the request to timeout cleanly without having an I/O
     *        exception get in the way.
     * @param httpMethodRetryHandler
     *        The retry handler object.
     * @throws ProtocolException
     *         If the HTTP version in the request object is invalid.
     */
    private static void prepareHttpMethod(HttpMethod httpMethod,
            Request request, HttpMethodRetryHandler httpMethodRetryHandler)
            throws ProtocolException {

        httpMethod.setFollowRedirects(false);

        httpMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
            httpMethodRetryHandler);

        String httpVersion = null;
        if ((httpVersion = request.getHttpVersion()) != null)
            httpMethod.getParams().setVersion(
                HttpVersion.parse("HTTP/" + httpVersion));

        String userAgent = null;
        if ((userAgent = request.getUserAgent()) != null)
            httpMethod.getParams().setParameter(HttpMethodParams.USER_AGENT,
                userAgent);
    }
    
    /**************************************************************************
     * Adds form data to a POST method object.
     * 
     * @param request The request object.
     * @param postMethod The POST method object.
     */
    private static void addPostParameters(Request request,
            PostMethod postMethod) {

        // Convert the request's parameter map to an array of name-value pairs
        Map<String, String[]> parameterMap = request.getParameterMap();
        ArrayList<NameValuePair> parameterList = new ArrayList<NameValuePair>();
        for (String name : parameterMap.keySet()) {
            String[] values = request.getParameterValues(name);
            for (String value : values) {
                parameterList.add(new NameValuePair(name, value));
            }
        }
        NameValuePair[] parameterPairs = parameterList
            .toArray(new NameValuePair[parameterList.size()]);

        // Add the parameters to the POST method
        postMethod.addParameters(parameterPairs);
    }
    
    /**************************************************************************
     * Writes tracing information that traces the request/response activity.
     * 
     * @param traceLevel
     *        Indicates how much trace info to produce.
     * @param traceStream
     *        An output stream where trace statements will be written.
     * @param httpMethod
     *        The HttpMethod object of the request.
     */
    private static void writeTrace(int traceLevel, PrintStream traceStream,
            HttpMethod httpMethod) {

        try {
            if (traceLevel >= TRACE_URI) {
                // Show trace output of the request URI
                traceStream
                    .println("-------------------------------------------------------------------------------");
                traceStream.println(httpMethod.getURI() + "\n");
            }

            if (traceLevel >= TRACE_REQUEST_RESPONSE_LINES) {
                // Show trace output of the HTTP request line
                traceStream.println(httpMethod.getName()
                        + " "
                        + httpMethod.getPath()
                        + (httpMethod.getQueryString() == null ? "" : "?"
                                + httpMethod.getQueryString()) + " "
                        + httpMethod.getParams().getVersion().toString());
            }

            if (traceLevel >= TRACE_HEADERS) {
                // Show trace output of the HTTP request headers
                for (Header header : httpMethod.getRequestHeaders()) {
                    traceStream.println(header.getName() + ": "
                            + header.getValue());
                }
                // Show trace of request entity body
                if (httpMethod instanceof PostMethod) {
                    NameValuePair[] parameters = ((PostMethod)httpMethod)
                        .getParameters();
                    if (parameters != null) {
                        // StringBuffer parameterString = new StringBuffer();
                        // for (NameValuePair parameter : parameters) {
                        //       parameterString.append(parameter.getName() + "=" + parameter.getValue() + "&");
                        // }
                        // parameterString.deleteCharAt(parameterString.length() - 1);
                        String parameterString = new String(
                            ((ByteArrayRequestEntity)((PostMethod)httpMethod)
                                .getRequestEntity()).getContent(), "UTF-8");
                        traceStream.println("    |");
                        traceStream.println("    +-- " + parameterString);
                    }
                }
                traceStream.println();
            }

            if (traceLevel >= TRACE_REQUEST_RESPONSE_LINES) {
                // Show trace output of the HTTP status line
                traceStream.println(httpMethod.getStatusLine().toString());
            }

            if (traceLevel >= TRACE_HEADERS) {
                // Show trace output of the HTTP response headers
                for (Header header : httpMethod.getResponseHeaders()) {
                    traceStream.println(header.getName() + ": "
                            + header.getValue());
                }
                traceStream.println();
            }

            if (traceLevel >= TRACE_BODY) {
                // Show trace output of the HTTP response body
                traceStream.println(httpMethod.getResponseBodyAsString());
                traceStream.println();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    /*#########################################################################
     *                          INSTANCE METHODS
     *#######################################################################*/

    /**************************************************************************
     * Empties out the history list of this <code>Tab</code> so that methods
     * like {@link #goBack()} and {@link #goForward()} will have no effect
     * (until new pages are retrieved).
     * <p>
     * Note that the history list has a maximum capacity of 15 items. These
     * items are currently "cached" in-memory, although future versions of the
     * <b>Prowser</b> library may implement a disk cacheing mechanism and/or
     * an API for sizing the history list according to an application's needs.
     * 
     * @see #goBack()
     * @see #goForward()
     * @see #jumpBack(int)
     * @see #jumpForward(int)
     * @see #getHistoryIndex()
     * @see #getHistorySize()
     */
    public void clearHistory() {
        history.clear();
        historyIndex = -1;
    }
    
    /**************************************************************************
     * Performs finalization for this <code>Tab</code> instance when it is
     * garbage collected.
     * 
     * @throws Throwable
     *         The <code>Exception</code> raised by this method.
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable {
        if (traceStream != System.out) {
            traceStream.close();
        }
    }

    /**************************************************************************
     * Convenience method equivalent to calling
     * {@link #getResponse() getResponse()}.{@link Response#getError() getError()}.
     * 
     * @return A code representing the error condition of the request that
     *         generated the most recent response.
     * @see #getErrorText()
     * @throws IllegalStateException
     *         If no response has been generated yet.
     */
    public int getError() {
        if (currentRequest == null)
            throw new IllegalStateException(
                "No Tab response has been generated yet");
        return currentResponse.getError();
    }
    
    /**************************************************************************
     * Convenience method equivalent to calling
     * {@link #getResponse() getResponse()}.{@link Response#getErrorText() getErrorText()}.
     * 
     * @return A <code>String</code> describing the error condition of the
     *         request that generated the most recent response, or
     *         <code>null</code> if no error condition exists (i.e.,
     *         {@link #getError()} returns {@link Response#ERR_NONE}).
     * @see #getError()
     * @throws IllegalStateException
     *         If no response has been generated yet.
     */
    public String getErrorText() {
        if (currentRequest == null)
            throw new IllegalStateException("No Tab response has been generated yet");
        return currentResponse.getErrorText();
    }

    /**************************************************************************
     * Returns the "current" page's position in the history list. (The first
     * item in the history list is at index 0.) 
     * <p>
     * Note that the history list has a maximum capacity of 15 items. These
     * items are currently "cached" in-memory, although future versions of the
     * <b>Prowser</b> library may implement a disk cacheing mechanism and/or
     * an API for sizing the history list according to an application's needs.
     * 
     * @return The "current" page's position in the history list.
     */
    public int getHistoryIndex() {
        return historyIndex;
    }
    
    /**************************************************************************
     * Returns a Response from the history, as specified by an offset from the
     * current history index. This method is used by all history methods that
     * return Responses (e.g., goBack(), jumpBack(), etc.).
     * 
     * @param offset
     *        The offset from the current history index. This value can be
     *        positive or negative.
     * @return The Response object from history that is offset from the current
     *         history index by the specified amount.
     */
    private Response getHistoryItem(int offset) {
        if (historyIndex < 0)
            throw new IllegalStateException("No history");
        else {
            int newHistoryIndex = historyIndex + offset;
            if (newHistoryIndex > -1 && newHistoryIndex < history.size()) {
                historyIndex = newHistoryIndex;
                currentRequest = history.get(historyIndex);
                currentResponse = currentRequest.getResponse();
                return currentResponse;
            }
            else
                return null;
        }
    }
    
    /**************************************************************************
     * Returns the current size of the history list.
     * <p>
     * Note that the history list has a maximum capacity of 15 items. These
     * items are currently "cached" in-memory, although future versions of the
     * <b>Prowser</b> library may implement a disk cacheing mechanism and/or
     * an API for sizing the history list according to an application's needs.
     * 
     * @return The current size of the history list.
     */
    public int getHistorySize() {
        return history.size();
    }
    
    /**************************************************************************
     * Convenience method equivalent to calling
     * {@link #getResponse() getResponse()}.{@link Response#getPageBytes() getPageBytes()}.
     * 
     * @return The most recently retrieved web page as a byte array.
     * @throws IllegalStateException
     *         If no response has been generated yet.
     */
    public byte[] getPageBytes() {
        if (currentResponse == null)
            throw new IllegalStateException(
                "No Tab response has been generated yet");
        return currentResponse.getPageBytes();
    }
    
    /**************************************************************************
     * Convenience method equivalent to calling
     * {@link #getResponse() getResponse()}.{@link Response#getPageSource() getPageSource()}.
     * 
     * @return The most recently retrieved web page as an encoded string.
     * @throws IllegalStateException
     *         If no response has been generated yet.
     */
    public String getPageSource() {
        if (currentResponse == null)
            throw new IllegalStateException(
                "No Tab response has been generated yet");
        return currentResponse.getPageSource();
    }

    /**************************************************************************
     * Returns the {@link Prowser} that owns this <code>Tab</code>
     * instance.
     * 
     * @return The {@link Prowser} that owns this <code>Tab</code>
     *         instance.
     */
    public Prowser getProwser() {
        return prowser;
    }
    
    /**************************************************************************
     * Returns the most recently executed {@link Request}.
     * 
     * @return The most recently executed {@link Request}.
     * @throws IllegalStateException
     *         If no request has been submitted yet
     */
    public Request getRequest() {
        if (currentRequest == null)
            throw new IllegalStateException("No Tab request has been submitted yet");
        return currentRequest;
    }
    
    /**************************************************************************
     * Returns the most recently generated {@link Response}.
     * 
     * @return The most recently generated {@link Response}.
     * @throws IllegalStateException
     *         If no response has been generated yet.
     */
    public Response getResponse() {
        if (currentResponse == null)
            throw new IllegalStateException("No Tab response has been generated yet");
        return currentResponse;
    }
    
    /**************************************************************************
     * Convenience method equivalent to calling
     * {@link #getResponse() getResponse()}.{@link Response#getStatus() getStatus()}.
     * 
     * @return The HTTP status code associated with the most recent response,
     *         or {@link Response#STATUS_NOT_OBTAINED} if an error occurred
     *         during the request.
     * @see #getError()
     * @throws IllegalStateException
     *         If no response has been generated yet.
     */
    public int getStatus() {
        if (currentResponse == null)
            throw new IllegalStateException(
                "No Tab response has been generated yet");
        return currentResponse.getStatus();
    }
    
    /**************************************************************************
     * Convenience method equivalent to calling
     * {@link #getResponse() getResponse()}.{@link Response#getStatusText()() getStatusText()}.
     * 
     * @return The HTTP status text associated with the most recent response,
     *         or <code>null</code> if an error occurred during the request
     *         (i.e., {@link #getStatus()} returns
     *         {@link Response#STATUS_NOT_OBTAINED}).
     * @see #getError()
     * @throws IllegalStateException
     *         If no response has been generated yet.
     */
    public String getStatusText() {
        if (currentResponse == null)
            throw new IllegalStateException(
                "No Tab response has been generated yet");
        return currentResponse.getStatusText();
    }
    
    /**************************************************************************
     * Executes the specified {@link Request} and returns a new
     * {@link Response} object.
     * <p>
     * At the end of the transaction, the <code>Request</code> argument and
     * <code>Response</code> result are saved as the "current" request and
     * response in order to provide browser functionality like
     * {@link #refresh() page refreshing} and
     * {@link #getPageSource() source viewing}.
     * 
     * @param request
     *        A <code>Request</code> object representing the page request to
     *        be made.
     * @return A new <code>Response</code> object.
     * @throws IllegalArgumentException
     *         If <code>request</code> is <code>null</code>.
     */
    public Response go(Request request) {
        
        // Invalidate the forward history due to this new request
        if (historyIndex < history.size() - 1 ) {
            int itemsToRemove = history.size() - (historyIndex + 1);
            for (int i = 0; i < itemsToRemove; i++)
                history.removeLast();
        }
        
        // Add the new request to the end of the history
        history.addLast(request);
        historyIndex++;
        if (history.size() > MAX_HISTORY) {
            history.removeFirst();
            historyIndex--;
        }
        
        // Process the rquest
        return processRequest(request);
    }
    
    /**************************************************************************
     * Creates a new {@link Request} object from the specified URI string,
     * executes the request, and then returns a new {@link Response} object.
     * <p>
     * At the end of the transaction, the newly-created <code>Request</code>
     * and the <code>Response</code> result are saved as the "current"
     * request and response in order to provide browser functionality like
     * {@link #refresh() page refreshing} and
     * {@link #getPageSource() source viewing}.
     * 
     * @param uri
     *        A URI string representing the page request to be made.
     * @return A new <code>Response</code> object.
     * @throws IllegalArgumentException
     *         If <code>uri</code> is not valid.
     */
    public Response go(String uri) {
        return go(Request.createRequest(uri));
    }
    
    /**************************************************************************
     * Creates a new {@link Request} object from the specified URI object,
     * executes the request, and then returns a new {@link Response} object.
     * <p>
     * At the end of the transaction, the newly-created <code>Request</code>
     * and the <code>Response</code> result are saved as the "current"
     * request and response in order to provide browser functionality like
     * {@link #refresh() page refreshing} and
     * {@link #getPageSource() source viewing}.
     * 
     * @param uri
     *        A <code>URI</code> object representing the page request to be
     *        made.
     * @return A new <code>Response</code> object.
     * @throws IllegalArgumentException
     *         If <code>uri</code> is <code>null</code>.
     */
    public Response go(URI uri) {
        return go(new Request(uri));
    }

    /**************************************************************************
     * Simulates the action of a web browser's <b>Back</b> button by returning
     * the previous page in the history list. Note that a "cached"
     * <code>Response</code> object is returned by this method, not a
     * newly-generated one.
     * <p>
     * As a side effect of calling this method, the "current" request and
     * response of this <code>Tab</code> instance are set to be the
     * {@link Request} and {@link Response} objects associated with the
     * returned page. Therefore, if desired, the "cached" page that is returned
     * by this method can be easily refreshed with code similar to the
     * following:
     * 
     * <pre>
     *     Response oldResponse = tab.goBack();
     *     Response newResponse = tab.refresh();
     * </pre>
     * 
     * Obviously, another outcome of calling this method is that the history
     * index will subsequently point to the previous page. So, for example,
     * calling {@link #goForward()} immediately after calling this method will
     * set the history index -- along with the "current" request and response
     * for this <code>Tab</code> instance -- back to the state that existed
     * before this method was called.
     * <p>
     * Note that the history list has a maximum capacity of 15 items. These
     * items are currently "cached" in-memory, although future versions of the
     * <b>Prowser</b> library may implement a disk cacheing mechanism and/or
     * an API for sizing the history list according to an application's needs.
     * 
     * @return The "cached" <code>Response</code> object associated with the
     *         previous page in the history list, or <code>null</code> if the
     *         "current" page is already at the beginning of the history list.
     * @throws IllegalStateException
     *         If no page history exists.
     * @see #goForward()
     * @see #jumpBack(int)
     * @see #jumpForward(int)
     * @see #getHistoryIndex()
     * @see #getHistorySize()
     * @see #clearHistory()
     */
    public Response goBack() {
        return getHistoryItem(-1);
    }

    /**************************************************************************
     * Simulates the action of a web browser's <b>Forward</b> button by
     * returning the next page in the history list. Note that a "cached"
     * <code>Response</code> object is returned by this method, not a
     * newly-generated one.
     * <p>
     * As a side effect of calling this method, the "current" request and
     * response of this <code>Tab</code> instance are set to be the
     * {@link Request} and {@link Response} objects associated with the
     * returned page. Therefore, if desired, the "cached" page that is returned
     * by this method can be easily refreshed with code similar to the
     * following:
     * 
     * <pre>
     *     Response oldResponse = tab.goForward();
     *     Response newResponse = tab.refresh();
     * </pre>
     * 
     * Obviously, another outcome of calling this method is that the history
     * index will subsequently point to the next page. So, for example, calling
     * {@link #goBack()} immediately after calling this method will set the
     * history index -- along with the "current" request and response for this
     * <code>Tab</code> instance -- back to the state that existed before
     * this method was called.
     * <p>
     * Note that the history list has a maximum capacity of 15 items. These
     * items are currently "cached" in-memory, although future versions of the
     * <b>Prowser</b> library may implement a disk cacheing mechanism and/or
     * an API for sizing the history list according to an application's needs.
     * 
     * @return The "cached" <code>Response</code> object associated with the
     *         next page in the history list, or <code>null</code> if the
     *         "current" page is already at the end of the history list.
     * @throws IllegalStateException
     *         If no page history exists.
     * @see #goBack()
     * @see #jumpForward(int)
     * @see #jumpBack(int)
     * @see #getHistoryIndex()
     * @see #getHistorySize()
     * @see #clearHistory()
     */
    public Response goForward() {
        return getHistoryItem(1);
    }
    
    /**************************************************************************
     * Simulates the action of a web browser's <b>Home</b> button. The home
     * page {@link Request} is re-submitted and a new {@link Response} is
     * generated.
     * 
     * @return A new <code>Response</code> object.
     * @throws IllegalStateException
     *         If no home page has been set, or the home page is invalid.
     */
    public Response goHome() {
        Response response = null;
        try {
            response = go(prowser.getHomePage());
        }
        catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid home page");
        }
        return response;
    }
    
    /**************************************************************************
     * Indicates if this <code>Tab</code> has been closed.
     * 
     * @return A boolean value indicating if this <code>Tab</code> has been
     *         closed.
     */
    public boolean isClosed() {
        return isClosed;
    }

    /**************************************************************************
     * Returns (from the history list) a previously visited page that was
     * originally requested <i><b>prior</b></i> to the "current" page. (The
     * number of pages to jump back is indicated by the <code>pagesBack</code>
     * argument.) This method simulates the action of clicking the little arrow
     * on the right side of a browser's <b>Back</b> button to select a page
     * from a drop-down menu of recent history. Note that a "cached"
     * <code>Response</code> object is returned by this method, not a
     * newly-generated one.
     * <p>
     * As a side effect of calling this method, the "current" request and
     * response of this <code>Tab</code> instance are set to be the
     * {@link Request} and {@link Response} objects associated with the
     * returned page. Therefore, if desired, the "cached" page that is returned
     * by this method can be easily refreshed with code similar to the
     * following:
     * 
     * <pre>
     *     Response oldResponse = tab.jumpBack(3);
     *     Response newResponse = tab.refresh();
     * </pre>
     * 
     * Obviously, another outcome of calling this method is that the history
     * index will subsequently point to the selected page. So, for example,
     * calling {@link #jumpForward(int)} immediately after calling this method
     * can set the history index -- along with the "current" request and
     * response for this <code>Tab</code> instance -- back to the state that
     * existed before this method was called (assuming that both calls use the
     * same value for the number of pages to jump).
     * <p>
     * Note that the history list has a maximum capacity of 15 items. These
     * items are currently "cached" in-memory, although future versions of the
     * <b>Prowser</b> library may implement a disk cacheing mechanism and/or
     * an API for sizing the history list according to an application's needs.
     * 
     * @return The "cached" <code>Response</code> object associated with the
     *         page that is <code>pagesBack</code> pages back in the history
     *         list, or <code>null</code> if the "current" page is less than
     *         <code>pagesBack</code> pages from the beginning of the history
     *         list.
     * @throws IllegalArgumentException
     *         If <code>pagesBack</code> is not a positive value.
     * @throws IllegalStateException
     *         If no page history exists.
     * @see #jumpForward(int)
     * @see #goBack()
     * @see #goForward()
     * @see #getHistoryIndex()
     * @see #getHistorySize()
     * @see #clearHistory()
     */
    public Response jumpBack(int pagesBack) {
        if (pagesBack < 1)
            throw new IllegalArgumentException(
                "Number of pages to jump back is not a positive value");
        return getHistoryItem(-pagesBack);
    }

    /**************************************************************************
     * Returns (from the history list) a previously visited page that was
     * originally requested <i><b>after</b></i> to the "current" page. (The
     * number of pages to jump forward is indicated by the
     * <code>pagesForward</code> argument.) This method simulates the action
     * of clicking the little arrow on the right side of a browser's <b>Forward</b>
     * button to select a page from a drop-down menu of recent history. Note
     * that a "cached" <code>Response</code> object is returned by this
     * method, not a newly-generated one.
     * <p>
     * As a side effect of calling this method, the "current" request and
     * response of this <code>Tab</code> instance are set to be the
     * {@link Request} and {@link Response} objects associated with the
     * returned page. Therefore, if desired, the "cached" page that is returned
     * by this method can be easily refreshed with code similar to the
     * following:
     * 
     * <pre>
     *     Response oldResponse = tab.jumpForward(3);
     *     Response newResponse = tab.refresh();
     * </pre>
     * 
     * Obviously, another outcome of calling this method is that the history
     * index will subsequently point to the selected page. So, for example,
     * calling {@link #jumpBack(int)} immediately after calling this method can
     * set the history index -- along with the "current" request and response
     * for this <code>Tab</code> instance -- back to the state that existed
     * before this method was called (assuming that both calls use the same
     * value for the number of pages to jump).
     * <p>
     * Note that the history list has a maximum capacity of 15 items. These
     * items are currently "cached" in-memory, although future versions of the
     * <b>Prowser</b> library may implement a disk cacheing mechanism and/or
     * an API for sizing the history list according to an application's needs.
     * 
     * @return The "cached" <code>Response</code> object associated with the
     *         page that is <code>pagesFoward</code> pages forward in the
     *         history list, or <code>null</code> if the "current" page is
     *         less than <code>pagesForward</code> pages from the end of the
     *         history list.
     * @throws IllegalArgumentException
     *         If <code>pagesForward</code> is not a positive value.
     * @throws IllegalStateException
     *         If no page history exists.
     * @see #jumpBack(int)
     * @see #goForward()
     * @see #goBack()
     * @see #getHistoryIndex()
     * @see #getHistorySize()
     * @see #clearHistory()
     */
    public Response jumpForward(int pagesForward) {
        if (pagesForward < 1)
            throw new IllegalArgumentException(
                "Number of pages to jump forward is not a positive value");
        return getHistoryItem(pagesForward);
    }
    
    /**************************************************************************
     * Called by this Tab's owning Prowser to mark the Tab as closed.
     */
    /* package */ void markClosed() {
        isClosed = true;
        prowser = null;
    }

    /**************************************************************************
     * Simulates the action of a web browser's <b>Refresh</b> button. The most
     * recent {@link Request} is re-submitted and a new {@link Response} is
     * generated.
     * 
     * @return A new <code>Response</code> object.
     * @throws IllegalStateException
     *         If no initial <code>Request</code> exists to be refreshed.
     */
    public Response refresh() {
        Response response = null;
        try {
            response = processRequest(currentRequest);
        }
        catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                "No Tab request exists to be refreshed");
        }
        return response;
    }

    /**************************************************************************
     * Specifies the file to use for the trace output of this
     * <code>Tab</code> instance. Trace output will be written to this
     * location provided that the {@link #setTraceLevel(int)} method has been
     * called with a value higher than {@link #TRACE_OFF}. If no file is ever
     * specified, standard output is used by default. To set the output
     * location back to standard output, pass <code>null</code> for the
     * <code>traceFile</code> argument.
     * 
     * @param traceFile
     *        File to receive trace output, or <code>null</code> to use
     *        standard output.
     * @param append
     *        If <code>true</code>, trace output will be appended to the
     *        file (assuming that the file existed before this
     *        <code>Tab</code> instance was created); otherwise, the file
     *        will be overwritten.
     * @throws FileNotFoundException
     *         If the attempt to open the specified file fails.
     */
    public void setTraceFile(File traceFile, boolean append)
            throws FileNotFoundException {

        // If a trace file had been previously specified, close it
        if (traceFile != null && traceStream != System.out)
            traceStream.close();

        // If trace file is specified as null, use standard output
        if (traceFile == null) {
            traceStream = System.out;
            return;
        }
        
        // Else set up the trace output stream to write to the specified file
        else {
            FileOutputStream fileOutputStream = new FileOutputStream(
                traceFile, append);
            traceStream = new PrintStream(fileOutputStream, true);
        }
    }
    
    /**************************************************************************
     * Sets the trace level of this <code>Tab</code> instance. The trace
     * level dictates how much information from each request/response
     * transaction is written to the trace file (or standard output if no trace
     * file has been specified). See {@link #setTraceFile(File, boolean)} for
     * information on specifying a trace file.
     * <p>
     * See the <a href="#field_summary"><code>TRACE_<i>xxx</i></code></a>
     * constants for valid values and their meanings.
     * 
     * @param traceLevel
     *        The trace level of this <code>Tab</code> instance.
     */
    public void setTraceLevel(int traceLevel) {
        this.traceLevel = traceLevel;
    }

    /**************************************************************************
     * Simulates the action of a web browser's <b>Stop</b> button by enforcing
     * a timeout that is applied to the <i>next</i> page request <i>only</i>.
     * This method returns the <code>Tab</code> object that it acts upon so
     * that a page request method call can be chained onto it, like this:
     * 
     * <pre>
     *     response = tab.stop(5000).go("http://java.sun.com/");
     * </pre>
     * 
     * This differs from {@link Request#setTimeout(Integer)}, which sets an
     * explicit timeout that is inherrent to a {@link Request}, and it is also
     * different than {@link Prowser#setDefaultTimeout(Integer)}, which sets a
     * new default timeout used for any {@link Request} that does not
     * explicitly specify one. The difference is that this method sets a
     * <i>temporary</i> timeout; it only applies to the next request
     * processed. Subsequent requests are not affected. Note that the above
     * code could also be written as follows, with the same effect, but with
     * less clarity:
     * 
     * <pre>
     *     tab.stop(5000);   // Sets 5-sec timeout for *next* request *only*
     *     response = tab.go(" http://java.sun.com/");
     * </pre>
     * 
     * The temporary timeout set by this method will apply to any page request
     * method, e.g., {@link #go(Request)}, {@link #refresh()},
     * {@link #goBack()}, etc.
     * 
     * @return The <code>Tab</code> object for which the temporary timeout is
     *         being set (i.e., this <code>Tab</code>).
     * @param timeout
     *        The number of milliseconds to wait before timing out the <i>next</i>
     *        reqeust <i>only</i>, or {@link Request#TIMEOUT_INFINITE} to
     *        indicate that the next request should never time out.
     * @throws IllegalArgumentException
     *         If the timeout value is <code>null</code> or less than zero.
     * @see Request#setTimeout(Integer) Setting an explicit request timeout.
     * @see Prowser#setDefaultTimeout(Integer) Setting a default request
     *      timeout.
     */
    public Tab stop(Integer timeout) {
        if (timeout == null || timeout.intValue() < 0)
            throw new IllegalArgumentException(
                "Invalid temporary timeout value: " + timeout);
        this.tempTimeout = timeout;
        return this;
    }
    
    /* package */ void setHttpClientUserAgent(String userAgent) {
        httpClient.getParams().setParameter("http.useragent", userAgent);
    }
    
    /* package */ void setHttpClientHttpVersion(String httpVersion) {
        try {
            httpClient.getParams().setVersion(
                HttpVersion.parse("HTTP/" + httpVersion));
        }
        catch (ProtocolException e) {
            throw new IllegalArgumentException("Invalid HTTP version argument");
        }
    }
    
    /**************************************************************************
     * Executes the specified {@link Request} and returns a new
     * {@link Response} object.
     * <p>
     * At the end of the transaction, the <code>Request</code> argument and
     * <code>Response</code> result are saved as the "current" request and
     * response in order to provide browser functionality like
     * {@link #refresh() page refreshing} and
     * {@link #getPageSource() source viewing}.
     * 
     * @param request
     *        The <code>Request</code> object representing the page request
     *        to be made.
     * @return A new <code>Response</code> object for the specified
     *         <code>Request</code>.
     * @throws IllegalArgumentException
     *         If <code>request</code> is <code>null</code>.
     */
    private Response processRequest(Request request) {

        // Mark the start time for calculating the request's duration
        long requestStartTime = System.currentTimeMillis();

        // Make sure the tab is still open
        if (isClosed)
            throw new IllegalStateException("Tab is closed");
        
        // Validate request argument
        if (request == null) {
            history.remove(historyIndex--);
            throw new IllegalArgumentException("Tab request is null");
        }

        // If defined, use the temp timeout (set in a call to stop()), else if
        // the Request object explictly specifies a timeout, use it, else use
        // the Tab's default request timeout
        Integer timeout;
        if (tempTimeout != null) {
            timeout = tempTimeout;
            tempTimeout = null;
        }
        else if (request.getTimeout() != null)
            timeout = request.getTimeout();
        else
            timeout = prowser.getDefaultTimeout();
        
        // Set up an object to run the request
        RequestRunnable requestRunnable = new RequestRunnable(this, request,
            timeout);
        
        // If timeout is not "infinite", run request in a timed thread
        Thread requestThread = null;
        if (timeout.intValue() != Request.TIMEOUT_INFINITE) {

            // Start the request in a different thread
            requestThread = new Thread(requestRunnable);
            requestThread.setDaemon(true);
            requestThread.start();
    
            // Wait as long as the timeout for the request thread to complete
            try {
                requestThread.join(timeout);
            } catch (InterruptedException e) {
                throw new IllegalStateException("Request thread was unexpectedly interrupted");
            }
            
            // If timeout occurred, stop the request thread and set resulting
            // values for this special case
            if (!requestRunnable.requestCompleted) {

                // Tell the request thread to stop processing the request
                requestRunnable.requestTimedOut = true;

                // Create an exception and error state for the timeout
                requestRunnable.exception = new TimeoutException(
                    "Tab request timed out after " + timeout
                            + " milliseconds [" + request.getUri().toString()
                            + "]");
                requestRunnable.error = Response.ERR_TIMEOUT;
                requestRunnable.errorText = "Request timeout. "
                        + requestRunnable.exception.getClass().getName()
                        + ": " + requestRunnable.exception.getMessage();

                // Specify values for response status and content
                requestRunnable.status = Response.STATUS_NOT_OBTAINED;
                requestRunnable.statusText = null;
                requestRunnable.statusVersion = null;
                requestRunnable.pageBytes = null;
                requestRunnable.responseCharset = null;
            }
        }
        
        // Else an "infinite" timeout means request doesn't need a timed thread
        else
            requestRunnable.run();        

        // Build the new response
        Response response = new Response();
        response.setError(requestRunnable.error);
        response.setErrorText(requestRunnable.errorText);
        response.setStatus(requestRunnable.status);
        response.setStatusText(requestRunnable.statusText);
        response.setStatusVersion(requestRunnable.statusVersion);
        response.setPageBytes(requestRunnable.pageBytes);
        response.setPageCharset(requestRunnable.responseCharset);
        response.setPageContentType(requestRunnable.responseContentType);
        response.setUri(requestRunnable.uriFinal);
        response.setTab(this);

        // Save the request in the response, and vice versa
        response.setRequest(request);
        request.setResponse(response);

        // Save the request and response as the current request and response
        currentRequest = request;
        currentResponse = response;

        if (traceLevel > TRACE_OFF && requestRunnable.exception != null) {
            requestRunnable.exception.printStackTrace();
        }

        // Record the request's duration in the new response
        response.setDuration(System.currentTimeMillis() - requestStartTime);
        
        // Return the response resulting from the request
        return response;
    }


    /*#########################################################################
     *                          INNER CLASSES
     *#######################################################################*/

    /**
     * The <code>RequestRunnable</code> class provides a mechanism for
     * executing page requests either in a separate thread (for implementing
     * finite request timeouts), or in the same thread (for implementing
     * "infinite" timeouts).
     * <p>
     * Threaded execution is made possible by the fact that the
     * <code>RequestRunnable</code> class implements the
     * <code>Runnable</code> interface.
     */
    private static class RequestRunnable implements Runnable {
        
        // Variables passed to constructor
        private Tab tab = null;
        private Request request = null;
        private Integer timeout = null;

        // Variables for communicating with a calling thread
        private int error = Response.ERR_NONE;
        private String errorText = null;
        private int status = Response.STATUS_NOT_OBTAINED;
        private String statusText = null;
        private String statusVersion = null;
        private byte[] pageBytes = null;
        private String responseCharset = null;
        private String responseContentType = null;
        private Throwable exception = null;
        private boolean requestCompleted = false;
        private boolean requestTimedOut = false;
        private URI uriFinal = null;

        /**************************************************************************
         * Constructs a new <code>RequestRunnable</code> object.
         *
         * @param tab
         * @param request
         * @param timeout
         */
        private RequestRunnable(Tab tab, Request request, Integer timeout) {
            this.tab = tab;
            this.request = request;
            this.timeout = timeout;
        }
        
        /**************************************************************************
         * Mandatory run() method for Runnable implementations. This method
         * executes a page request. 
         *
         * @see java.lang.Runnable#run()
         */
        public void run() {
            
            // Declare local variables
            HttpMethod httpMethod = null;
            URI uri = null;
            URI previousUri = null;
            Integer ioTimeout = null;

            // Set the I/O timeout to be 1 second longer than request timeout,
            // (unless the request timeout is infinite). This allows the request
            // to timeout cleanly without an I/O exception getting in the way.
            ioTimeout = timeout > 0 ? timeout + 1000 : 0;

            try {
                // Create and configure the HTTP method object
                uri = request.getUri();
                if (request.getHttpMethod().equalsIgnoreCase(
                    Request.HTTP_METHOD_GET)) {
                    httpMethod = new GetMethod(uri.toString());
                }
                else if (request.getHttpMethod().equalsIgnoreCase(
                    Request.HTTP_METHOD_POST)) {
                    httpMethod = new PostMethod(uri.toString());
                    addPostParameters(request, (PostMethod)httpMethod);
                }
                prepareHttpMethod(httpMethod, request, httpMethodRetryHandler);
                
                // Set up Basic or Digest Authentication (if user-info provided in URI)
                String userInfo = request.getUri().getUserInfo();
                if (userInfo != null) {
                    String[] userInfoArray = userInfo.split(":");
                    if (userInfoArray.length == 2) {
                        String username = userInfoArray[0];
                        String password = userInfoArray[1];
                        tab.httpClient.getState().setCredentials(
                            AuthScope.ANY,
                            new UsernamePasswordCredentials(username, password));
                    }
                }

                // Set the connection and socket timeouts to match the request
                // timeout (plus 1 second). This guards against lingering
                // requests that have been abandoned by Tab due to a
                // request timeout, but not abandoned by the HttpClient yet.
                httpMethod.getParams().setParameter("http.socket.timeout",
                    ioTimeout);
                tab.httpClient.getHttpConnectionManager().getParams()
                    .setParameter("http.connection.timeout", ioTimeout);
                
                // Get current state (e.g., cookies) from owning Prowser
                tab.httpClient.setState(tab.prowser.getHttpState());

                // Loop to process redirects (if any)
                int redirects = 0;
                boolean transactionComplete = false;
                while (!transactionComplete) {
                    
                    // Save the previous URI (for use in redirects)
                    previousUri = uri;

                    // Execute the HttpClient method
                    status = tab.httpClient.executeMethod(httpMethod);
                    
                    // Process the HTTP status of the response
                    if (status != Response.STATUS_NOT_OBTAINED) {
                        statusText = httpMethod.getStatusText();
                        HttpVersion httpVersionObject = HttpVersion
                            .parse(httpMethod.getStatusLine().getHttpVersion());
                        statusVersion = httpVersionObject.getMajor() + "."
                                + httpVersionObject.getMinor();
                    }
                    else {
                        statusText = null;
                        statusVersion = null;
                    }

                    // If this request has been timed out by a calling thread,
                    // don't finish processing it. In this case, the calling
                    // calling thread has abandoned this request and moved on.
                    if (requestTimedOut) {
                        try {
                            // Clear out any body bytes and release the conn.
                            pageBytes = httpMethod.getResponseBody();
                            httpMethod.releaseConnection();
                        }
                        catch (Exception e) {
                            // Do nothing
                        }
                        return;
                    }

                    if (tab.traceLevel > TRACE_OFF)
                        writeTrace(tab.traceLevel, tab.traceStream, httpMethod);

                    // Act on the status code of the HTTP response
                    switch (status) {

                        // Handle redirects
                        case Response.STATUS_301_MOVED_PERMANENTLY:
                        case Response.STATUS_302_FOUND:
                        case Response.STATUS_303_SEE_OTHER:
                        case Response.STATUS_307_TEMPORARY_REDIRECT:

                            // If max redirects reached, stop processing request
                            if (++redirects > tab.prowser.getDefaultMaxRedirects()) {
                                error = Response.ERR_MAX_REDIRECTS;
                                errorText = "Maxiumum redirects exceeded (max = "
                                        + tab.prowser.getDefaultMaxRedirects() + ").";
                                transactionComplete = true;
                                break;
                            }

                            // Get the location URI for the redirect
                            if (httpMethod.getResponseHeader("location")
                                .getValue() == null) {
                                error = Response.ERR_NO_LOCATION;
                                errorText = "No \"Location:\" field in response header.";
                                transactionComplete = true;
                                break;
                            } else
                                uri = new URI(httpMethod.getResponseHeader(
                                    "location").getValue());

                            // Handle relative URL in redirect header field
                            if (uri.getHost() == null) {
                                uri = new URI(previousUri.getScheme(), null,
                                    previousUri.getHost(), previousUri.getPort(),
                                    uri.getRawPath(), uri.getRawQuery(), uri
                                        .getRawFragment());
                            }

                            // Clear out any body bytes and release the connection
                            pageBytes = httpMethod.getResponseBody();
                            httpMethod.releaseConnection();

                            // If status 301 or 307 from POST, use POST in redirect
                            if ((status == Response.STATUS_301_MOVED_PERMANENTLY || status == Response.STATUS_307_TEMPORARY_REDIRECT)
                                    && httpMethod instanceof PostMethod) {
                                httpMethod = new PostMethod(uri.toString());
                                addPostParameters(request, (PostMethod)httpMethod);
                            }

                            // Else use GET in redirect
                            else {
                                httpMethod = new GetMethod(uri.toString());
                            }

                            // Finish configuring the HTTP method for redirect
                            prepareHttpMethod(httpMethod, request,
                                httpMethodRetryHandler);

                            break;

                        // Handle all other status codes
                        default:
                            // Get response body/charset and exit the loop
                            pageBytes = httpMethod.getResponseBody();
                            responseCharset = ((HttpMethodBase)httpMethod)
                                .getResponseCharSet();
                            responseContentType = ((HttpMethodBase)httpMethod)
                                .getResponseHeader("Content-type").getValue();
                            httpMethod.releaseConnection();
                            error = Response.ERR_NONE;
                            errorText = null;
                            transactionComplete = true;
                            break;
                    }
                }
            }

            catch (URISyntaxException e) {
                exception = e;
                error = Response.ERR_URI;
                errorText = "URI error. " + e.getClass().getName() + ": "
                        + e.getMessage();
            }

            catch (URIException e) {
                exception = e;
                error = Response.ERR_URI;
                errorText = "URI error. " + e.getClass().getName() + ": "
                        + e.getMessage();
            }

            catch (HttpException e) {
                exception = e;
                error = Response.ERR_PROTOCOL;
                errorText = "Protocol error. " + e.getClass().getName() + ": "
                        + e.getMessage();
            }

            catch (IOException e) {
                exception = e;
                error = Response.ERR_IO;
                errorText = "I/O error. " + e.getClass().getName() + ": "
                        + e.getMessage();
            }

            catch (Exception e) {
                exception = e;
                error = Response.ERR_GENERAL;
                errorText = "General error. " + e.getClass().getName() + ": "
                        + e.getMessage();
            }

            finally {
                
                // If an HTTP status was obtained, finish up the resquest 
                if (status != Response.STATUS_NOT_OBTAINED) {
                    
                    // Add new cookie's back to owning Prowser's state
                    tab.prowser.getHttpState().addCookies(
                        tab.httpClient.getState().getCookies());

                    // Get the final URI of the retrieved page
                    try {
                        try {
                            uriFinal = new URI(httpMethod.getURI().toString());
                        }
                        catch (URIException e) {
                            uriFinal = new URI(request.getUri().toString());
                        }
                    }
                    catch (URISyntaxException e) {
                        uriFinal = null;
                    }
                }
                
                // Let calling thread (if any) know that the request completed
                requestCompleted = true;
            }
        }
    }

    // ------------------------------------------------------------------------
    
    /**
     * The <code>ProwserMethodRetryHandler</code> class implements the
     * <code>HttpMethodRetryHandler</code> interface in order to provide a
     * custom mechanism for handling the retrying of failed requests by the
     * Jakarta HttpClient module.
     */
    private static class ProwserMethodRetryHandler implements
            HttpMethodRetryHandler {

        /**********************************************************************
         * Automatically called from within the HttpClient code whenever an
         * error occurs that allows for retrying an HTTP request.
         * 
         * @param httpMethod
         *        The HttpMethod object used during the failed request.
         * @param exception
         *        The exception that indicates the error.
         * @param executionCount
         *        The number of times the request has been retried.
         * @return <code>true</code> when the request should be retired;
         *         <code>false</code> otherwise.
         * @see org.apache.commons.httpclient.HttpMethodRetryHandler#retryMethod(org.apache.commons.httpclient.HttpMethod,
         *      java.io.IOException, int)
         */
        public boolean retryMethod(@SuppressWarnings("unused")
                final HttpMethod httpMethod, @SuppressWarnings("unused")
                final IOException exception, @SuppressWarnings("unused")
                int executionCount) {

            // Never retry automatically
            return false;
            
//            // Do not retry if the max retry count has been exceeded
//            if (executionCount >= 3) {
//                return false;
//            }
//
//            // If HTTP method is GET, determine if we should retry
//            if (httpMethod instanceof GetMethod) {
//
//                // Retry if the request has not been sent fully
//                if (!httpMethod.isRequestSent()) {
//                    return true;
//                }
//
//                // Retry if connection timed out or server dropped connection
//                if (exception instanceof ConnectTimeoutException
//                        || exception instanceof NoHttpResponseException
//                        || exception instanceof java.net.ConnectException) {
//                    return true;
//                }
//
//                // Otherwise do not retry
//                return false;
//            }
//
//            // Else HTTP method is not GET, so don't retry
//            else
//                return false;
        }
    }
}
