/*#############################################################################
 * Request.java
 *
 * $Source: D:/Development/cvsroot/Prowser/src/com/zenkey/net/prowser/Request.java,v $
 * $Revision: 1.2 $
 * $Date: 2006/02/21 19:41:49 $
 * $Author: Michael $
 *
 * This file contains Java source code for the following class:
 * 
 *     com.zenkey.net.prowser.Request
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;


/**
 * The <code>Request</code> class is a collection of properties that specify
 * a web page request to be made by a {@link Tab} instance. When a
 * <code>Request</code> is executed by a <code>Tab</code>, a
 * {@link Response} object is generated.
 * <p>
 * <a href="" name="reqprops"></a>
 * The properties of a <code>Request</code> are listed in the following
 * table. Note that only the <b>URI</b> property is required; the rest are
 * optional: <blockquote> <table border="1">
 * <tr>
 * <td><b>URI</b></td>
 * <td>The web address of the request.</td>
 * </tr>
 * <tr>
 * <td><b>Request&nbsp;Parameters&nbsp;&nbsp;&nbsp;</b></td>
 * <td>Extra data that is usually submitted via an HTML form in a URI query
 * string, or in <code>POST</code>ed data. (See <a href="#reqParamNote"><b>A
 * note about <em>request parameters</em></b></a> for more information.)</td>
 * </tr>
 * <tr>
 * <td><b>HTTP Method</b></td>
 * <td>The HTTP method for making the request. {@link Tab} accepts the
 * <code>{@link #HTTP_METHOD_GET GET}</code> and
 * <code>{@link #HTTP_METHOD_POST POST}</code> methods. (See <a
 * href="http://www.ietf.org/rfc/rfc2616.txt">RFC 2616</a> for more
 * information.)</td>
 * </tr>
 * <tr>
 * <td><b>HTTP Version</b></td>
 * <td>Version of HTTP to use. (<code>{@link #HTTP_VERSION_0_9 0.9}</code>,
 * <code>{@link #HTTP_VERSION_1_0 1.0}</code> or
 * <code>{@link #HTTP_VERSION_1_1 1.1}</code>)</td>
 * </tr>
 * <tr>
 * <td><b>Timeout</b></td>
 * <td>Time to wait before giving up on a web page request in progress.</td>
 * </tr>
 * <tr>
 * <td><b>User Agent String</b></td>
 * <td>Identifies the web client to the web server. (Passed to the web server
 * as an HTTP header field.)</td>
 * </tr>
 * </table> </blockquote>
 * <p>
 * Except for the URI, the properties of a <code>Request</code> object can be
 * specified either during the object's construction <i>or</i> via setter
 * methods. The URI can only be set during construction, and it cannot be
 * changed afterward.
 * <p>
 * There are two options for setting <code>Request</code> properties during
 * construction. The first option involves specifying only a URI. In this case,
 * the other properties receive default values. (See {@link #Request(URI)},
 * {@link #Request(String)}, and {@link #Request(String, boolean)}.) The
 * other option involves the specification of a <em>request file</em> that
 * contains the <code>Request</code>'s property values. (See
 * {@link #Request(File)}.)
 * <p>
 * After construction, various properties of the <code>Request</code> can be
 * manipulated with getter and setter methods.
 * <p>
 * When a <code>Request</code> is used by a {@link Tab} instance to
 * execute a page request, a response is generated and returned in the form of
 * a {@link Response} object. From this <code>Response</code> object, you can
 * obtain the contents of the web page as {@link Response#getPageSource() text}
 * or {@link Response#getPageBytes() binary} data. You can also obtain the
 * {@link Response#getError() error condition} of the request/repsonse
 * transaction, as well as the {@link Response#getStatus() HTTP status}
 * information of the respone. The <code>Response</code> object is returned
 * by the {@link Tab} method that executed the page request, but it is also
 * stored in the <code>Request</code> object and is available via the
 * {@link #getResponse()} method. <a name="reqParamNote"></a>
 * <p>
 * <a name="reqParamNote"></a><b>A note about <em>request parameters</em></b>:
 * <blockquote>
 * <p>
 * <em>Request parameters</em> are extra information that can be sent with an
 * HTTP request. They usually constitute the data submitted in an HTML form.
 * When using the HTTP <code>GET</code> method, parameters are transmitted in
 * a URI's query string. When using the HTTP <code>POST</code> method,
 * parameters are included in the message body of the request.
 * <p>
 * Keep in mind that a request can have multiple parameters with the same name.
 * For instance, if an HTML form has a <code>&lt;select&gt;</code> element
 * that is configured to allow more than one option selection at a time, each
 * selected option will be submitted as a parameter with the same name. So, for
 * example, if the <code>name</code> attribute of a
 * <code>&lt;select&gt;</code> element is <code>colors</code>, and the
 * user selects <code>red</code>, <code>orange</code>, and
 * <code>yellow</code>, the following three parameters would be submitted in
 * the request: <blockquote>
 * 
 * <pre>
 *colors=red
 *colors=orange
 *colors=yellow
 * </pre>
 * 
 * </blockquote> </blockquote>
 * <p>
 * <b><em>Basic</em> and <em>Digest Authentication</em></b>:
 * <p>
 * <blockquote> To request a page that requires <b>Basic</b> or <b>Digest
 * Authentication</b>, simply include the username and password as part of the
 * URI, like this:
 * 
 * <pre>
 *    http://username:password@www.site-requiring-auth.com/
 * </pre>
 * 
 * </blockquote>
 * </p>
 * <b>Thread Safety</b>:
 * <p>
 * <blockquote>A <code>Request</code> object is <b>not</b> thread-safe. It
 * maintains state information so that it may be used repeatedly. Multiple
 * threads using the same <code>Request</code> might corrupt this state
 * information.
 * <p>
 * </blockquote>
 * 
 * @version $Revision: 1.2 $, $Date: 2006/02/21 19:41:49 $
 * @see Tab
 * @see Response
 */

public class Request {

    /*#########################################################################
     *                             CONSTANTS
     *#######################################################################*/

    //
    // HTTP Methods -----------------------------------------------------------
    //

    /** The HTTP <code>GET</code> method. */
    public static final String  HTTP_METHOD_GET     = "GET";

    /** The HTTP <code>POST</code> method. */
    public static final String  HTTP_METHOD_POST    = "POST";

    /** The default HTTP method to use for requests. */
    private static final String DEFAULT_HTTP_METHOD = HTTP_METHOD_GET;

    //
    // HTTP Versions ----------------------------------------------------------
    //

    /** HTTP version 0.9. */
    public static final String HTTP_VERSION_0_9 = "0.9";

    /** HTTP version 1.0. */
    public static final String HTTP_VERSION_1_0 = "1.0";

    /** HTTP version 1.1. */
    public static final String HTTP_VERSION_1_1 = "1.1";

    //
    // Request Property Names -------------------------------------------------
    //

    /** The URI property name. */
    private static final String PROP_URI          = "URI";

    /** The request parameter property name. */
    private static final String PROP_PARAMETER    = "Parameter";

    /** The HTTP method property name. */
    private static final String PROP_HTTP_METHOD  = "HttpMethod";

    /** The HTTP version property name. */
    private static final String PROP_HTTP_VERSION = "HttpVersion";

    /** The timeout property name. */
    private static final String PROP_TIMEOUT      = "Timeout";

    /** The user-agent property name. */
    private static final String PROP_USER_AGENT   = "UserAgent";

    //
    // Miscellaneous ----------------------------------------------------------
    //

    /**
     * A timeout value of zero (<code>0</code>), which should be
     * interpreted as an "infinite" timeout. This value is used for web page
     * requests. <b>Note that the use of this value may still result in a
     * finite timeout, depending on how it is handled by the {@link Tab}
     * object and the underlying protocol stack.</b>
     * 
     * @see #setTimeout(Integer)
     */
    public static final int     TIMEOUT_INFINITE = 0;

    /** An empty string. */
    private static final String EMPTY_STRING     = "";


    /*#########################################################################
     *                           CLASS VARIABLES
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

    /** The URI of the request. */
    private URI        uri              = null;

    /** The collection of parameters for the request. */
    private LinkedHashMap<String, ArrayList<String>> parameters = new LinkedHashMap<String, ArrayList<String>>();

    /** The HTTP method of the request */
    private String     httpMethod       = DEFAULT_HTTP_METHOD;

    /** The HTTP version of the request. */
    private String     httpVersion      = null;

    /** The user-agent header field value. */
    private String     userAgent        = null;

    /** The response to the request */
    private Response   response         = null;

    /** The number of milliseconds to wait before timing out a request. */
    private Integer    timeout          = null;

    
    /*#########################################################################
     *                           CONSTRUCTORS
     *#######################################################################*/

    /**************************************************************************
     * Protected, do-nothing constrcutor.
     */
    protected Request() {};

    /**************************************************************************
     * Constructs a new <code>Request</code> object whose property values
     * are specified in a <em>request file</em>. (<code>Request</code>
     * properties are explained <a href="#reqprops">here</a>. Default values
     * are listed <a href="#defaultpropvals">here</a>.)
     * <p>
     * Some notes on <em>request files</em>:
     * <p>
     * <ul>
     * <li>The only required property is the URI. All others are optional.
     * (See below for valid property names.)</li>
     * <li>It is the caller's responsibility to ensure that the URI's path and
     * query (if any) are URL-encoded.</li>
     * <li>It is the caller's responsibility to ensure that the parameters are
     * URL-encoded.</li>
     * <li>Timeout values are in milliseconds.</li>
     * <li>Property names are case-insensitive (e.g., <code>HttpVersion</code>
     * is the same as <code>HTTPVERSION</code>).</li>
     * <li>Comments are indicated by lines that begin with a hash mark (<code>#</code>).</li>
     * <li>Blank lines are permitted.</li>
     * </ul>
     * <p>
     * Here is an example of a <em>request file</em> that shows the names and
     * format of all valid property entries: <blockquote>
     * 
     * <pre>
     *# This is a request file for class com.zenkey.net.prowser.Request.
     *
     *URI http://onesearch.sun.com/search/onesearch/index.jsp
     * 
     *Parameter qt=java
     *Parameter charset=UTF-8
     *
     *HttpMethod GET
     * 
     *HttpVersion 1.1
     *
     *Timeout 10000
     *
     *UserAgent Mozilla/4.8 [en] (Windows NT 5.1; U)
     * </pre>
     * 
     * </blockquote> If the request is using the HTTP <code>GET</code>
     * method, then if the URI contains a query string, the parameters
     * contained in that string will be added to the request's parameter
     * collection. Also, the URI's query string will be updated to include any
     * parameters that are added separately via <code>Parameter</code>
     * properties in the <em>request file</em>.
     * 
     * @param requestFile
     *        The <em>request file</em> that contains the request's property
     *        values.
     * @throws Exception
     *         If errors occur related to the processing of the <em>request file</em>.
     */
    public Request(File requestFile) throws Exception {

        // Open the request file for reading
        BufferedReader fileReader = new BufferedReader(new InputStreamReader(
            new FileInputStream(requestFile)));

        // Process each line in the request file
        String fileLine;
        while ((fileLine = fileReader.readLine()) != null) {

            // Skip blank lines and comments
            if (fileLine.matches("^\\s*$") || fileLine.matches("^\\s*#.*"))
                continue;

            // Split a property's name-value pair at first chunk of whitespace
            String[] propertyComponents = fileLine.split("\\s+", 2);
            String propertyName;
            String propertyValue;
            if (propertyComponents.length == 2) {
                propertyName = propertyComponents[0];
                propertyValue = propertyComponents[1];
            } else
                throw new Exception("Invalid property in file "
                        + requestFile.toString() + " : " + fileLine);

            // Process a URI property
            if (propertyName.equalsIgnoreCase(PROP_URI))
                uri = new URI(propertyValue);

            // Process a request parameter property
            else if (propertyName.equalsIgnoreCase(PROP_PARAMETER)) {

                // Split the param's name-value pair at the first equal sign
                String parameterName = null;
                String parameterValue = null;
                String[] parameterElements = propertyValue.split("\\s*=\\s*",
                    2);
                if (parameterElements.length > 1) {
                    parameterName = parameterElements[0];
                    parameterValue = parameterElements[1];
                } else {
                    parameterName = parameterElements[0].trim();
                    parameterValue = EMPTY_STRING;
                }

                // Add the parameter to it's list of values in the map
                addParameterWithoutUpdate(parameterName, parameterValue);
            }

            // Process an HTTP method property
            else if (propertyName.equalsIgnoreCase(PROP_HTTP_METHOD))
                setHttpMethod(propertyValue);

            // Process an HTTP version property
            else if (propertyName.equalsIgnoreCase(PROP_HTTP_VERSION))
                setHttpVersion(propertyValue);

            // Process a timeout property
            else if (propertyName.equalsIgnoreCase(PROP_TIMEOUT))
                setTimeout(new Integer(propertyValue));

            // Process a user-agent property
            else if (propertyName.equalsIgnoreCase(PROP_USER_AGENT))
                setUserAgent(propertyValue);

            // Else process an invalid request property
            else
                throw new Exception("Invalid property in request file "
                        + requestFile.toString() + " : " + fileLine);
        }

        // Close the request file reader
        fileReader.close();

        // Make sure a URI was specified
        if (uri == null)
            throw new Exception("No URI property sepcified in request file "
                    + requestFile.toString());

        // If request is a GET, update params with values from query string
        if (getHttpMethod().equalsIgnoreCase(HTTP_METHOD_GET)) {
            addParameters(getQueryMap());
            // If this GET URI has no query string but does has parameter props,
            // update the query string here because it won't happen in the
            // above call to addParameters() due to getQueryMap() returning null.
            if (uri.getRawQuery() == null && parameters.size() > 0)
                updateQueryString();
        }
    }

    /**************************************************************************
     * Constructs a new <code>Request</code> object for the specified
     * URL-encoded URI string; all other request properties receive default
     * values. For a list of default property values, see
     * {@link #Request(URI)}.
     * <p>
     * It is the caller's responsibility to ensure that The URI's path and
     * query (if any) are URL-encoded.
     * 
     * @param uri
     *        The request's URI.
     * @throws URISyntaxException
     *         If string could not be parsed as a URI reference.
     */
    public Request(String uri) throws URISyntaxException {
        this(uri, true);
    }

    /**************************************************************************
     * Constructs a new <code>Request</code> object for the specified URI
     * string, which is assumed to be URL-encoded if the <code>isEncoded</code>
     * argument is <code>true</code>; all other request properties receive
     * default values. For a list of default property values, see
     * {@link #Request(URI)}.
     * <p>
     * If <code>isEncoded</code> is <code>false</code>, then this
     * constructor will URL-encode the path and query (if any).
     * 
     * @param uri
     *        The request's URI.
     * @param isEncoded
     *        <code>true</code> when the supplied URI is URL-encoded;
     *        <code>false</code> otherwise.
     * @throws URISyntaxException
     *         If string could not be parsed as a URI reference.
     */
    public Request(String uri, boolean isEncoded) throws URISyntaxException  {

        if (!isEncoded) {
            String encodedUri = null;
            try {
                encodedUri = URIUtil.encodePathQuery(uri, "UTF-8");
            } catch (URIException e) {
                // Never reached because UTF-8 is supported
            }
            this.uri = new URI(encodedUri);
        }
        else
            this.uri = new URI(uri);

        // Add parameters from the URI's query string to the request object
        addParameters(getQueryMap());
    }

    /**************************************************************************
     * Constructs a new <code>Request</code> object for the specified
     * <code>URI</code>; all other request properties receive default
     * values.
     * <p>
     * <a href="" name="defaultpropvals"></a>
     * The default property values are: <blockquote> <table>
     * <tr>
     * <td><b>Request Parameters:&nbsp;&nbsp;&nbsp;</b></td>
     * <td>&nbsp;<code>null</code></td>
     * <td><i>(No request parameters)</i></td>
     * </tr>
     * <tr>
     * <td><b>HTTP Method:</b></td>
     * <td>&nbsp;<code>GET</code></td>
     * </tr>
     * <tr>
     * <td><b>HTTP Version:</b></td>
     * <td>&nbsp;<code>null</code></td>
     * <td><i>({@link Tab} will govern the value)</i></td>
     * </tr>
     * <tr>
     * <td><b>Timeout:</b></td>
     * <td>&nbsp;<code>null</code></td>
     * <td><i>({@link Tab} will govern the value)</i></td>
     * </tr>
     * <tr>
     * <td><b>User Agent String:</b></td>
     * <td>&nbsp;<code>null&nbsp;&nbsp;&nbsp;</code></td>
     * <td><i>({@link Tab} will govern the value)</i></td>
     * </tr>
     * </table> </blockquote>
     * <p>
     * If the URI contains a query string, then the parameters contained in
     * that string will be added to the request's parameters.
     * 
     * @param uri
     *        The request's URI.
     */
    public Request(URI uri) {

        if (uri == null) {
            throw new IllegalArgumentException("Request URI is null");
        }

        this.uri = uri;

        // Add parameters from the URI's query string to the request object
        addParameters(getQueryMap());
    }


    /*#########################################################################
     *                             METHODS
     *#######################################################################*/
    
    /**************************************************************************
     * Adds a parameter to this <code>Request</code>.
     * <p>
     * If the request is using the HTTP <code>GET</code> method, then the
     * URI's query string will be updated accordingly.
     * 
     * @param name
     *        The parameter's name.
     * @param value
     *        The parameter's value.
     */
    public void addParameter(String name, String value) {

        // Add the parameter to the request
        addParameterWithoutUpdate(name, value);

        // If request is using the GET method, update the URI's query string
        if (getHttpMethod().equalsIgnoreCase(HTTP_METHOD_GET))
            updateQueryString();
    }

    /**************************************************************************
     * Adds parameters from a <code>java.util.Map</code> to this
     * <code>Request</code>. The keys of the map are parameter names; the
     * values of the map are arrays of parameter values associated with each
     * parameter name.
     * <p>
     * If the request is using the HTTP <code>GET</code> method, then the
     * URI's query string will be updated accordingly.
     * 
     * @param parameterMap
     *        Map containing parameters to be added to the request. A
     *        <code>null</code> value is allowed.
     */
    public void addParameters(Map<String, String[]> parameterMap) {

        // If the parameter map is null, do nothing
        if (parameterMap == null)
            return;

        // Add the mapped parameters to the request
        for (String name : parameterMap.keySet()) {
            String[] values = parameterMap.get(name);
            for (String value : values)
                addParameterWithoutUpdate(name, value);
        }

        // If request is using the GET method, update the URI's query string
        if (getHttpMethod().equalsIgnoreCase(HTTP_METHOD_GET))
            updateQueryString();
    }

    /**************************************************************************
     * Adds parameters with the spcecified name and values to this
     * <code>Request</code>.
     * <p>
     * If the request is using the HTTP <code>GET</code> method, then the
     * URI's query string will be updated accordingly.
     * 
     * @param name
     *        The parameters' name.
     * @param values
     *        The parameter values.
     */
    public void addParameters(String name, String[] values) {

        // Add the parameters to the request
        for (String value : values)
            addParameterWithoutUpdate(name, value);

        // If request is using the GET method, update the URI's query string
        if (getHttpMethod().equalsIgnoreCase(HTTP_METHOD_GET))
            updateQueryString();
    }

    /**************************************************************************
     * Changes the value of an existing parameter in this <code>Request</code>.
     * <p>
     * If the specified name-value pair of <em>name=oldValue</em> does not
     * exist, no changes or additions are made. If more than one parameter
     * exists with the same <em>name</em> and <em>oldValue</em>, all of
     * them will be changed to <em>newValue</em>.
     * <p>
     * If the request is using the HTTP <code>GET</code> method, then the
     * URI's query string will be updated accordingly.
     * 
     * @param name
     *        The parameter's name.
     * @param oldValue
     *        The parameter value to be changed.
     * @param newValue
     *        The parameter value to replace <em>oldValue</em>
     * @return <code>true</code> if the parameter's <em>oldValue</em>
     *         existed and was changed to <em>newValue</em>;
     *         <code>false</code> otherwise.
     */
    public boolean changeParameter(String name, String oldValue,
            String newValue) {

        // Get any values for the specified parameter name
        String[] oldValues = getParameterValues(name);

        // If any params with the specified name exist, check for the value
        boolean changed = false;
        if (oldValues != null) {

            // Make new value list for the param, changing the specified value
            ArrayList<String> newValues = new ArrayList<String>();
            for (String currentOldValue : oldValues) {
                if (!currentOldValue.equals(oldValue))
                    newValues.add(currentOldValue);
                else {
                    newValues.add(newValue);
                    changed = true;
                }
            }

            // If the value was changed, replace the parameter's value list
            if (changed)
                parameters.put(name, newValues);

            // If request is using the GET method, update URI's query string
            if (getHttpMethod().equalsIgnoreCase(HTTP_METHOD_GET))
                updateQueryString();
        }

        // Return an indication of whether or not the value was changed
        return changed;
    }

    /**************************************************************************
     * Factory method that returns a new <code>Request</code> object whose
     * property values are specified in a <em>request file</em>.
     * <p>
     * See {@link #Request(File)} for more information on the usage and
     * format of <em>request files</em>.
     * 
     * @param requestFile
     *        The <em>request file</em> that contains the request's property
     *        values.
     * @return A new <code>Request</code> object for the specified
     *         <em>request file</em>, or <code>null</code> if the request
     *         can not be created.
     */
    public static Request createRequest(File requestFile) {
        try {
            return new Request(requestFile);
        } catch (Exception e) {
            return null;
        }
    }

    /**************************************************************************
     * Factory method that returns a new <code>Request</code> object for
     * the specified URL-encoded URI string; all other request properties
     * receive default values. For a list of default property values, see
     * {@link #Request(URI)}.
     * <p>
     * It is the caller's responsibility to ensure that The URI's path and
     * query (if any) are URL-encoded.
     * 
     * @param uri
     *        The request's URI.
     * @return A new <code>Request</code> object for the specified
     *         URL-encoded URI string, or <code>null</code> if the request
     *         can not be created.
     */
    public static Request createRequest(String uri) {
        try {
            return new Request(uri);
        } catch (URISyntaxException e) {
            return null;
        }
    }
    
    /**************************************************************************
     * Factory method that returns a new <code>Request</code> object for
     * the specified URI string, which is assumed to be URL-encoded if the
     * <code>isEncoded</code> argument is <code>true</code>; all other
     * request properties receive default values. For a list of default
     * property values, see {@link #Request(URI)}.
     * <p>
     * If <code>isEncoded</code> is <code>false</code>, then this
     * method will URL-encode the path and query (if any).
     * 
     * @param uri
     *        The request's URI.
     * @param isEncoded
     *        <code>true</code> when the supplied URI is URL-encoded;
     *        <code>false</code> otherwise.
     * @return A new <code>Request</code> object for the specified URI
     *         string, or <code>null</code> if the request can not be
     *         created.
     */
    public static Request createRequest(String uri, boolean isEncoded) {
        try {
            return new Request(uri, isEncoded);
        } catch (URISyntaxException e) {
            return null;
        }
    }
    
    /**************************************************************************
     * Returns the HTTP method of this <code>Request</code>.
     * <p>
     * If this property is not set, the request uses a default value of
     * <code>{@link #HTTP_METHOD_GET GET}</code>.
     * 
     * @return The request's HTTP method.
     */
    public String getHttpMethod() {
        return httpMethod;
    }

    /**************************************************************************
     * Returns the explicit HTTP version of this <code>Request</code>.
     * <p>
     * If this property is not set, the value will be governed by the
     * {@link Tab} object that executes this request.
     * 
     * @return The request's explicit HTTP version, or <code>null</code> if
     *         one has not been set.
     */
    public String getHttpVersion() {
        return httpVersion;
    }

    /**************************************************************************
     * Returns a <code>java.util.Map</code> of the parameters in this
     * <code>Request</code>. The keys of the map are parameter names; the
     * values of the map are arrays of parameter values associated with each
     * parameter name.
     * 
     * @return A <code>java.util.Map</code> of the request's parameters, or
     *         <code>null</code> if the request has no parameters.
     */
    public Map<String, String[]> getParameterMap() {

        // Declare a parameter map for the return value
        LinkedHashMap<String, String[]> returnParameterMap = null;

        // If the request has parameters, create the return map
        if (!parameters.isEmpty()) {
            returnParameterMap = new LinkedHashMap<String, String[]>();
            for (String name : parameters.keySet()) {
                String[] values = getParameterValues(name);
                returnParameterMap.put(name, values);
            }
        }

        // Return the parameter map (or null if there were no parameters)
        return returnParameterMap;
    }

    /**************************************************************************
     * Returns an array of values for this <code>Request</code>'s parameters
     * that have the specified name.
     * <p>
     * If there is only one parameter with the specified name, the array will
     * have a length of 1.
     * 
     * @param name
     *        The parameter's name.
     * @return An array of values associated with the specified parameter name,
     *         or <code>null</code> if the parameter does not exist.
     */
    public String[] getParameterValues(String name) {

        // Get a list of values for the specified paramter name
        ArrayList<String> values = parameters.get(name);

        // If the parameter doesn't exist, return null
        if (values == null)
            return null;

        // Else return an array of values for the parameter
        else
            return values.toArray(new String[values.size()]);
    }

    /**************************************************************************
     * Returns the most recent {@link Response} that resulted from this
     * <code>Request</code>.
     * 
     * @return The most recent {@link Response} that resulted from this
     *         <code>Request</code>, or <code>null</code> if the
     *         <code>Request</code> has not been submitted yet.
     */
    public Response getResponse() {
        return response;
    }
    
    /**************************************************************************
     * Returns the explicit timeout value (in milliseconds) for this
     * <code>Request</code>.
     * 
     * @return The request's explicit timeout value, in milliseconds, or
     *         <code>null</code> if the request does not specify a timeout
     *         value itself (i.e., the timeout [if any] will be governed by the
     *         {@link Tab} instance that executes the request).
     * @see #setTimeout(Integer)
     */
    public Integer getTimeout() {
        return timeout;
    }

    /**************************************************************************
     * Returns the URI of this <code>Request</code>.
     * 
     * @return The request's URI.
     */
    public URI getUri() {
        return uri;
    }

    /**************************************************************************
     * Returns the explicit user-agent string of this <code>Request</code>.
     * <p>
     * If this property is not set, the value will be governed by the
     * {@link Tab} object that executes this request.
     * 
     * @return The request's explicit user-agent string, or <code>null</code>
     *         if one has not been not set.
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**************************************************************************
     * Removes a specified parameter with a specified value from this
     * <code>Request</code>. Other parameters with the same name but
     * different values will not be removed. If more than one occurrence of the
     * name-value pair exists, the superfluous parameters will also be removed.
     * To remove all parameters with a specified name (regardless of value),
     * use {@link #removeParameters(String name)}.
     * <p>
     * If the request is using the HTTP <code>GET</code> method, then the
     * URI's query string will be updated accordingly.
     * 
     * @param name
     *        The parameter's name.
     * @param value
     *        The parameter's value.
     * @return <code>true</code> if the parameter was removed;
     *         <code>false</code> otherwise.
     */
    public boolean removeParameter(String name, String value) {

        // If the parameter doesn't exist, return false
        String[] oldValues = getParameterValues(name);
        if (oldValues == null)
            return false;

        // Create a new list of values for the param, minus the specified value
        ArrayList<String> newValues = new ArrayList<String>();
        boolean removed = false;
        for (String currentOldValue : oldValues) {
            if (!currentOldValue.equals(value))
                newValues.add(currentOldValue);
            else
                removed = true;
        }

        // If the specified parameter will now have no values, remove it
        if (newValues.size() == 0)
            removeParametersWithoutUpdate(name);

        // Else replace the parameter's list of values with the new list
        else
            parameters.put(name, newValues);

        // If request is using the GET method, update the URI's query string
        if (getHttpMethod().equalsIgnoreCase(HTTP_METHOD_GET))
            updateQueryString();

        // Return an indication of whether or not the value was removed
        return removed;
    }

    /**************************************************************************
     * Removes all parameters from this <code>Request</code>.
     * <p>
     * If the request is using the HTTP <code>GET</code> method, then the
     * URI's query string will be updated accordingly.
     * 
     * @return A <code>java.util.Map</code> of the request's former
     *         parameters, or <code>null</code> if the request had no
     *         parameters. The structure of the map is the same as the one
     *         returned by {@link #getParameterMap()}.
     */
    public Map<String, String[]> removeParameters() {

        // Get a map of the current parameters
        Map<String, String[]> removedParameterMap = getParameterMap();

        // If there are parameters in the request, remove them all
        if (removedParameterMap != null) {

            // Remove all current parameters by creating an empty parameter map
            parameters.clear();

            // If request is using the GET method, update URI's query string
            if (getHttpMethod().equalsIgnoreCase(HTTP_METHOD_GET))
                updateQueryString();
        }

        // Return a map of the removed parameters (or null if none removed)
        return removedParameterMap;
    }

    /**************************************************************************
     * Removes all parameters with the specified name from this
     * <code>Request</code>. To remove a single parameter with a specified
     * name and value, use {@link #removeParameter(String name, String value)}.
     * <p>
     * If the request is using the HTTP <code>GET</code> method, then the
     * URI's query string will be updated accordingly.
     * 
     * @param name
     *        The parameter's name.
     * @return An array of the values removed for the specified parameter name,
     *         or <code>null</code> if the parameter does not exist.
     */
    public String[] removeParameters(String name) {

        // If the parameter name doesn't exist in the map, return null
        String[] oldValues = removeParametersWithoutUpdate(name);
        if (oldValues == null)
            return null;

        // If request is using the GET method, update the URI's query string
        if (getHttpMethod().equalsIgnoreCase(HTTP_METHOD_GET))
            updateQueryString();

        // Return values for the removed parameter (or null if none existed)
        return oldValues;
    }

    /**************************************************************************
     * Sets the HTTP method for this <code>Request</code>.
     * <p>
     * If the <code>GET</code> method is specified, then the URI's query
     * string will be updated to include any parameters that have already been
     * added to the request.
     * <p>
     * If this property is not set, the request uses a default value of
     * <code>{@link #HTTP_METHOD_GET GET}</code>.
     * 
     * @param httpMethod
     *        The request's HTTP httpMethod.
     */
    public void setHttpMethod(String httpMethod) {

        this.httpMethod = httpMethod.toUpperCase();

        // If request is using the GET method, update the URI's query string
        if (getHttpMethod().equalsIgnoreCase(HTTP_METHOD_GET))
            updateQueryString();
    }
    
    /**************************************************************************
     * Sets an explicit HTTP version for this <code>Request</code>.
     * <p>
     * If this property is not set, the value will be governed by the
     * {@link Tab} object that executes this request.
     * 
     * @param httpVersion
     *        The request's HTTP version.
     */
    public void setHttpVersion(String httpVersion) {
        this.httpVersion = httpVersion;
    }

    /**************************************************************************
     * Sets a parameter with the spcecified value for this <code>Request</code>.
     * <p>
     * If any parameters with the specified name already exist, they will all
     * be replaced by a single parameter with the spcecfied value. If no
     * parameters with the specified name exist, a new parameter with the
     * specified value will be added to the request.
     * <p>
     * When parameters are replaced by this method, an array of the replaced
     * values is returned.
     * <p>
     * If the request is using the HTTP <code>GET</code> method, then the
     * URI's query string will be updated accordingly.
     * 
     * @param name
     *        The parameter's name.
     * @param value
     *        The parameter's value.
     * @return An array of replaced parameter values for the specified name, or
     *         <code>null</code> if no parameters were replaced.
     */
    public String[] setParameter(String name, String value) {

        // Get the current list of all values for the specified param name
        String[] oldValues = getParameterValues(name);

        // Replace all old values with the new new value
        ArrayList<String> newValues = new ArrayList<String>();
        newValues.add(value);
        parameters.put(name, newValues);

        // If request is using the GET method, update the URI's query string
        if (getHttpMethod().equalsIgnoreCase(HTTP_METHOD_GET))
            updateQueryString();

        // Return the list of old values (or null if none existed)
        return oldValues;
    }

    /**************************************************************************
     * Sets parameters from a <code>java.util.Map</code> for this
     * <code>Request</code>. The keys of the map are parameter names; the
     * values of the map are arrays of parameter values associated with each
     * parameter name.
     * <p>
     * For each entry in the map argument, if any parameters with the specified
     * name already exist, they will all be replaced by parameters with the
     * spcecfied values. If no parameters with the specified name exist, new
     * parameters with the specified values will be added to the request.
     * <p>
     * If the request is using the HTTP <code>GET</code> method, then the
     * URI's query string will be updated accordingly.
     * 
     * @param parameterMap
     *        Map containing parameters to be set for the request. A
     *        <code>null</code> value is allowed.
     * @return A <code>java.util.Map</code> of any request parameters that
     *         were replaced, or <code>null</code> if no request parameters
     *         were replaced. The structure of the map is the same as the one
     *         returned by {@link #getParameterMap()}.
     */
    public Map<String, String[]> setParameters(
            Map<String, String[]> parameterMap) {

        // If the parameter map is null, just return null
        if (parameterMap == null)
            return null;

        // Create a map to hold the parameters that will be replaced
        LinkedHashMap<String, String[]> replacedParameterMap = new LinkedHashMap<String, String[]>();

        // Set the specified parameters
        for (String name : parameterMap.keySet()) {

            // Save off parameters that are being replaced
            String[] oldValues = null;
            if ((oldValues = getParameterValues(name)) != null)
                replacedParameterMap.put(name, oldValues);

            // Remove parameters that are being replaced
            removeParametersWithoutUpdate(name);

            // Add replacement parameters
            String[] newValues = parameterMap.get(name);
            for (String newValue : newValues)
                addParameterWithoutUpdate(name, newValue);
        }

        // If request is using the GET method, update the URI's query string
        if (getHttpMethod().equalsIgnoreCase(HTTP_METHOD_GET))
            updateQueryString();

        // Return a map of any parameters that were replaced
        if (replacedParameterMap.isEmpty())
            return null;
        else
            return replacedParameterMap;
    }

    /**************************************************************************
     * Sets parameters with the spcecified name and values for this
     * <code>Request</code>.
     * <p>
     * If any parameters with the specified name already exist, they will all
     * be replaced by parameters with the spcecfied values. If no parameters
     * with the specified name exist, new parameters with the specified values
     * will be added to the request.
     * <p>
     * If the request is using the HTTP <code>GET</code> method, then the
     * URI's query string will be updated accordingly.
     * 
     * @param name
     *        The parameters' name.
     * @param values
     *        The parameter values.
     * @return An array of replaced parameter values for the specified name, or
     *         <code>null</code> if no parameters were replaced.
     */
    public String[] setParameters(String name, String[] values) {

        // Get the current list of all values for the specified param name
        String[] oldValues = getParameterValues(name);

        // Replace all old values with the new new values
        ArrayList<String> newValues = new ArrayList<String>();
        for (String value : values)
            newValues.add(value);
        parameters.put(name, newValues);

        // If request is using the GET method, update the URI's query string
        if (getHttpMethod().equalsIgnoreCase(HTTP_METHOD_GET))
            updateQueryString();

        // Return the list of old values (or null if none existed)
        return oldValues;
    }

    /**************************************************************************
     * Sets an explicit timeout (in milliseconds) for this <code>Request</code>.
     * <p>
     * Use a value of {@link #TIMEOUT_INFINITE} to indicate that the calling
     * thread should wait indefinitely for the request to complete.
     * <p>
     * Use a value of <code>null</code> to indicate that the request itself
     * does not specify a timeout. In this case, the timeout (if any) will be
     * governed by the {@link Tab} instance that executes the request.
     * <p>
     * If the request's timeout is never explicitly set, the default timeout
     * value is <code>null</code>.
     * 
     * @param timeout
     *        The number of milliseconds to wait before timing out a
     *        reqeust, or <code>null</code> to indicate that the request
     *        itself does not specify a timeout. A value of
     *        {@link #TIMEOUT_INFINITE} means that the calling thread should
     *        wait indefinitely for the request to complete.
     * @throws IllegalArgumentException
     *         If the timeout value is less than zero.
     */
    public void setTimeout(Integer timeout) {
        if (timeout != null && timeout.intValue() < 0)
            throw new IllegalArgumentException(
                "Invalid Request timeout value: " + timeout);
        this.timeout = timeout;
    }
    
    /**************************************************************************
     * Sets an explicit user-agent string for this <code>Request</code>.
     * <p>
     * If this property is not set, the value will be governed by the
     * {@link Tab} object that executes this request.
     * 
     * @param userAgent
     *        The request's user-agent string.
     */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    /**************************************************************************
     * Returns a <code>String</code> representation of the
     * <code>Request</code>.
     * 
     * @return A <code>String</code> representation of the
     *         <code>Request</code>.
     */
    public String toString() {

        String eol = System.getProperty("line.separator");

        StringBuffer requestString = new StringBuffer();

        requestString.append(PROP_URI + ": " + getUri() + eol);
        requestString.append(PROP_HTTP_METHOD + ": " + getHttpMethod() + eol);
        for (String name : getParameterMap().keySet()) {
            String[] values = getParameterValues(name);
            for (String value : values) {
                requestString.append(PROP_PARAMETER + ": " + name + "="
                        + value + eol);
            }
        }
        requestString
            .append(PROP_HTTP_VERSION + ": " + getHttpVersion() + eol);
        requestString.append(PROP_USER_AGENT + ": " + getUserAgent() + eol);
        requestString.append(PROP_TIMEOUT + ": " + getTimeout() + eol);

        return requestString.toString();
    }

    /**************************************************************************
     * Adds a parameter to this <code>Request</code> without updating the
     * query string.
     * <p>
     * This is useful for methods such as {@link #addParameters(Map)} that are
     * made more efficient by being able to add multiple parameters without
     * having to update the query string after each addition.
     * 
     * @param name
     *        The parameter's name.
     * @param value
     *        The parameter's value.
     */
    private void addParameterWithoutUpdate(String name, String value) {

        // If the parameter name doesn't exist in the map, create a map entry
        ArrayList<String> values = parameters.get(name);
        if (values == null) {
            values = new ArrayList<String>();
            parameters.put(name, values);
        }

        // Add the specified value to the list of values for the paramter
        values.add(value);
    }

    /**************************************************************************
     * Returns a <code>java.util.Map</code> of the parameters contained
     * within the query string of the URI for this <code>Request</code>. The
     * keys of the map are parameter names; the values of the map are arrays of
     * parameter values associated with each parameter name.
     * 
     * @return A <code>java.util.Map</code> of the URI's query string
     *         parameters, or <code>null</code> if the URI has no query
     *         string.
     */
    private Map<String, String[]> getQueryMap() {

        // If the URI is null, don't do anything
        if (uri == null)
            return null;
        
        // Declare maps for temp work and the return value
        LinkedHashMap<String, ArrayList<String>> tempQueryMap = null;
        LinkedHashMap<String, String[]> queryMap = null;

        // If the URI has a query string, build a temp map of its parameters
        String rawQueryString = uri.getRawQuery();
        if (rawQueryString != null) {

            // Parse out the name value pairs
            String[] queryPairsArray = rawQueryString.split("&");

            // Create a temp map of query parameters
            tempQueryMap = new LinkedHashMap<String, ArrayList<String>>();
            String name = null;
            String value = null;
            for (String rawQueryPairString : queryPairsArray) {

                // Split the query param name-value pair into a name and value
                String[] queryPairArray = rawQueryPairString.split("=", 2);
                name = queryPairArray[0];
                if (queryPairArray.length > 1)
                    value = queryPairArray[1];
                else
                    value = EMPTY_STRING;
                if (value == null)
                    value = EMPTY_STRING;

                // If the query parameter doesn't exist, create it
                ArrayList<String> values = tempQueryMap.get(name);
                if (values == null) {
                    values = new ArrayList<String>();
                    tempQueryMap.put(name, values);
                }

                // Add the specified value to the list of values for the param
                values.add(value);
            }

            // Build the query parameter map to return
            queryMap = new LinkedHashMap<String, String[]>();
            for (String tempName : tempQueryMap.keySet()) {
                String[] tempValues = tempQueryMap.get(tempName).toArray(
                    new String[tempQueryMap.get(tempName).size()]);
                queryMap.put(tempName, tempValues);
            }
        }

        // Return a map of the query's parameters (or null if URI has no query)
        return queryMap;
    }

    /**************************************************************************
     * Removes all parameters with the specified name from this
     * <code>Request</code>, but without updating the query string.
     * <p>
     * This is useful for methods such as {@link #setParameters(Map)} that are
     * made more efficient by being able to remove multiple groups of
     * parameters without having to update the query string after each removal.
     * 
     * @param name
     *        The parameter's name.
     * @return An array of the values removed for the specified parameter name,
     *         or <code>null</code> if the parameter does not exist.
     */
    private String[] removeParametersWithoutUpdate(String name) {

        // If the parameter name doesn't exist in the map, return null
        String[] oldValues = getParameterValues(name);
        if (oldValues == null)
            return null;

        // Remove the parameter from the request
        parameters.remove(name);

        // Return values for the removed parameter (or null if none existed)
        return oldValues;
    }

    /**************************************************************************
     * Uses this <code>Request</code> object's current collection of
     * parameters to build a query string for the request's URI.
     * <p>
     * This method should be called internally by any method of the
     * <code>Request</code> class that adds, changes or removes a parameter.
     * However, this method should <b>only</b> be called for a request that
     * uses the HTTP <code>GET</code> method because the <code>POST</code>
     * method does not use query strings within URIs.
     */
    private void updateQueryString() {

        String newQueryString = null;

        // If request parameters exist, process them
        Map<String, String[]> parameterMap;
        if ((parameterMap = getParameterMap()) != null) {

            // Build a query string from the parameters
            StringBuffer newQueryStringBuffer = new StringBuffer(EMPTY_STRING);
            for (String name : parameterMap.keySet()) {
                String[] values = getParameterValues(name);
                for (String value : values) {
                    newQueryStringBuffer.append(name + "=" + value + "&");
                }
            }

            // Remove the trailing "&" from the new query string
            if (newQueryStringBuffer.length() > 0)
                newQueryStringBuffer
                    .setLength(newQueryStringBuffer.length() - 1);

            // Convert the StringBuffer to a String
            newQueryString = newQueryStringBuffer.toString();
        }

        // Rebuild the URI with the new query string
        try {
            String encodedUri =
                (uri.getScheme()       == null ? EMPTY_STRING : uri.getScheme()) +
                (uri.getRawAuthority() == null ? EMPTY_STRING : "://" + uri.getRawAuthority()) +
                (uri.getRawPath()      == null ? EMPTY_STRING : uri.getRawPath()) +
                (newQueryString        == null ? EMPTY_STRING : "?" + newQueryString) +
                (uri.getRawFragment()  == null ? EMPTY_STRING : "#" + uri.getRawFragment());
            uri = new URI(encodedUri);
        }
        
        // This catch block should never be reached because this method is only
        // called after a valid URI established
        catch (URISyntaxException e) {
            throw new IllegalStateException(
                "URI syntax exception occurred during update of request query " +
                "string (" +e.getMessage() + ")");
        }
    }
    
    /**************************************************************************
     * Sets a {@link Response} that resulted from this <code>Request</code>.
     * This should always be the most recent Response generated via this
     * Request.
     *
     * @param response
     */
    /* package */ void setResponse(Response response) {
        this.response = response;
    }
}
