/*
 * Copyright (C) 2016 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
 * Karlsruhe, Germany.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.fraunhofer.iosb.ilt.sta;

import de.fraunhofer.iosb.ilt.sta.model.core.Entity;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.sta.parser.path.ParseException;
import de.fraunhofer.iosb.ilt.sta.parser.path.PathParser;
import de.fraunhofer.iosb.ilt.sta.parser.query.QueryParser;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.path.ResourcePath;
import de.fraunhofer.iosb.ilt.sta.persistence.PersistenceManager;
import de.fraunhofer.iosb.ilt.sta.persistence.PersistenceManagerFactory;
import de.fraunhofer.iosb.ilt.sta.query.Query;
import de.fraunhofer.iosb.ilt.sta.serialize.EntityFormatter;
import de.fraunhofer.iosb.ilt.sta.util.VisibilityHelper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scf
 */
@WebServlet(name = "StaTest", urlPatterns = {"/v0.0", "/v0.0/*"})
public class ServletTest extends HttpServlet implements ServletContextListener {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ServletTest.class);
    private static final Charset ENCODING = Charset.forName("UTF-8");
    private static final String API_VERSION = "v0.0";
    private static final String USE_ABSOLUTE_NAVIGATION_LINKS_TAG = "useAbsoluteNavigationLinks";
    private boolean useAbsoluteNavigationLinks = true;

    private Properties getDbProperties(ServletContext sc) {
        Properties props = new Properties();
        Enumeration<String> names = sc.getInitParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            props.put(name, sc.getInitParameter(name));
        }
        return props;
    }

    private Settings getSettings() {
        Settings settings = new Settings();
        ServletContext sc = getServletContext();
        String defaultCount = sc.getInitParameter("defaultCount");
        if (defaultCount != null) {
            settings.setCountDefault(Boolean.valueOf(defaultCount));
        }
        String defaulTop = sc.getInitParameter("defaultTop");
        if (defaulTop != null) {
            try {
                settings.setTopDefault(Integer.parseInt(defaulTop));
            } catch (NumberFormatException e) {
                LOGGER.error("Could not parse default top value. Not a number: " + defaulTop, e);
            }
        }
        String maxTop = sc.getInitParameter("maxTop");
        if (maxTop != null) {
            try {
                settings.setTopMax(Integer.parseInt(maxTop));
            } catch (NumberFormatException e) {
                LOGGER.error("Could not parse max top value. Not a number: " + maxTop, e);
            }
        }
        return settings;
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processGetRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        String pathInfo = request.getPathInfo();
        String queryInfo = request.getQueryString();
        URL serviceRootUrl = new URL(request.getScheme(), request.getLocalName(), request.getLocalPort(), request.getContextPath() + "/" + API_VERSION);
        String serviceRoot = serviceRootUrl.toExternalForm();

        PersistenceManager pm = null;
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>SensorThingsApi Test Servlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet " + getClass().getName() + " at " + request.getContextPath() + "</h1>");
            out.println("<div style=\"float:right;\">");
            out.println("<p>");
            String urlBase = request.getContextPath() + "/" + API_VERSION;
            for (EntityType et : EntityType.values()) {
                out.println("<a href='" + urlBase + "/" + et.plural + "'>" + et.plural + "</a><br/>");
            }
            out.println("<a href='" + urlBase + "/Things(1)/Datastreams'>Things(1)/Datastreams</a><br/>");
            out.println("<a href='" + urlBase + "/Datastreams?$select=Sensor'>Datastreams?$select=Sensor</a><br/>");
            out.println("<a href='" + urlBase + "/Datastreams?$expand=Sensor'>Datastreams?$expand=Sensor</a><br/>");
            out.println("<a href='" + urlBase + "/Datastreams?$select=Sensor&$expand=Sensor'>Datastreams?$select=Sensor&$expand=Sensor</a><br/>");
            out.println("<a href='" + urlBase + "/Datastreams?$select=description&$expand=Sensor'>Datastreams?$select=description&$expand=Sensor</a><br/>");
            out.println("<a href='" + urlBase + "/Things?$select=properties&$expand=Datastreams($select=description;$expand=Sensor($select=metadata))'>complex expand</a><br/>");
            out.println("<a href='" + urlBase + "/Observations?$orderby=length(result)%20desc,result%20desc&$select=result'>complex order</a><br/>");

            out.println("</p>");
            out.println("<dl><dt>PathInfo</dt><dd>");
            out.println(pathInfo);
            out.println("</dd><dt>QueryString</dt><dd>");
            out.println(queryInfo);
            out.println("</dd></dl>");

            out.println("<h2>Parsing path</h2>");
            out.println("<p>Path: " + pathInfo + "</p>");
            if (pathInfo == null || pathInfo.equals("/")) {
                out.println("<p>No path to parse.</p>");
                Map<String, List<Map<String, String>>> capabilities = getCapabilities(request);
                String capabilitiesJsonString = new EntityFormatter().writeObject(capabilities);
                out.print(capabilitiesJsonString);
                return;
            }

            String pathString = URLDecoder.decode(pathInfo, ENCODING.name());
            InputStream is = new ByteArrayInputStream(pathString.getBytes(ENCODING));
            de.fraunhofer.iosb.ilt.sta.parser.path.Parser pp = new de.fraunhofer.iosb.ilt.sta.parser.path.Parser(is, ENCODING.name());
            try {
                de.fraunhofer.iosb.ilt.sta.parser.path.ASTStart n = pp.Start();
                de.fraunhofer.iosb.ilt.sta.parser.path.DumpVisitorHtml v = new de.fraunhofer.iosb.ilt.sta.parser.path.DumpVisitorHtml(out);
                n.jjtAccept(v, null);
            } catch (ParseException ex) {
                LOGGER.error("Failed to parse because (Set loglevel to trace for stack): {}", ex.getMessage());
                LOGGER.trace("Exception: ", ex);
                out.println("<p>Invalid path!</p>");
            }

            out.println("<h2>Parsing query</h2>");
            if (queryInfo == null) {
                out.println("<p>No query to parse.</p>");
            } else {
                String queryString = URLDecoder.decode(queryInfo, ENCODING.name());
                is = new ByteArrayInputStream(queryString.getBytes(ENCODING));
                de.fraunhofer.iosb.ilt.sta.parser.query.Parser qp = new de.fraunhofer.iosb.ilt.sta.parser.query.Parser(is, ENCODING.name());
                try {
                    de.fraunhofer.iosb.ilt.sta.parser.query.ASTStart n = qp.Start();
                    de.fraunhofer.iosb.ilt.sta.parser.query.DumpVisitorHtml v = new de.fraunhofer.iosb.ilt.sta.parser.query.DumpVisitorHtml(out);
                    n.jjtAccept(v, null);
                } catch (Exception ex) {
                    LOGGER.error("Failed to parse because (Set loglevel to trace for stack): {}", ex.getMessage());
                    LOGGER.trace("Exception: ", ex);
                    out.println("<p>Invalid query!</p>");
                }
            }
            out.println("</div>");

            ResourcePath path;
            try {
                path = PathParser.parsePath(serviceRoot, pathString);
            } catch (NumberFormatException | IllegalStateException e) {
                out.println("<p>404 Nothing found.</p>");
                return;
            }
            String queryString;
            if (queryInfo == null) {
                queryString = null;
            } else {
                queryString = URLDecoder.decode(queryInfo, ENCODING.name());
            }
            Query query = null;
            try {
                query = QueryParser.parseQuery(queryString, getSettings());
            } catch (IllegalStateException e) {
                out.println("<p>400 Invalid Query.</p>");
                return;
            }
            pm = PersistenceManagerFactory.getInstance().create();
            if (!pm.validatePath(path)) {
                out.println("<p>404 Nothing found.</p>");
                pm.commitAndClose();
                return;
            }
            Object object;
            try {
                object = pm.get(path, query);
            } catch (UnsupportedOperationException e) {
                LOGGER.error("Unsupported operation.", e);
                out.println("Unsupported operation.");
                pm.rollbackAndClose();
                return;
            }
            if (object == null) {
                out.println("<p>404 Nothing found.</p>");
                pm.commitAndClose();
                return;
            }
            String entityJsonString;
            if (Entity.class.isAssignableFrom(object.getClass())) {
                Entity entity = (Entity) object;
                VisibilityHelper.applyVisibility(entity, path, query, useAbsoluteNavigationLinks);
                entityJsonString = new EntityFormatter().writeEntity(entity);
            } else if (EntitySet.class.isAssignableFrom(object.getClass())) {
                EntitySet entitySet = (EntitySet) object;
                VisibilityHelper.applyVisibility(entitySet, path, query, useAbsoluteNavigationLinks);
                entityJsonString = new EntityFormatter().writeEntityCollection((EntitySet) object);
            } else {
                entityJsonString = new EntityFormatter().writeObject(object);
            }

            out.println("<h2>Output</h2>");
            out.println("<pre>");
            out.println(entityJsonString);
            out.println("</pre>");

            out.println("</body>");
            out.println("</html>");
            pm.commitAndClose();
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            if (pm != null) {
                pm.rollbackAndClose();
            }
        }
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processGetRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processGetRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "SensorThingsApi v1.0 Testservlet";
    }

    private Map<String, List<Map<String, String>>> getCapabilities(HttpServletRequest request) {
        Map<String, List<Map<String, String>>> result = new HashMap<>();
        List< Map<String, String>> capList = new ArrayList<>();
        result.put("value", capList);
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        try {
            URL servletUrl = new URL(scheme, serverName, serverPort, request.getContextPath() + "/");
            URL baseUrl = new URL(servletUrl, API_VERSION + "/");
            for (EntityType entityType : EntityType.values()) {
                capList.add(createCapability(entityType.plural, new URL(baseUrl, entityType.plural)));
            }
        } catch (MalformedURLException ex) {
            LOGGER.error("Failed to build url.", ex);
            return result;
        }
        return result;
    }

    private Map<String, String> createCapability(String name, URL url) {
        Map<String, String> val = new HashMap<>();
        val.put("name", name);
        val.put("url", url.toString());
        return Collections.unmodifiableMap(val);
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        if (sce != null && sce.getServletContext() != null) {
            if (sce.getServletContext().getInitParameter(USE_ABSOLUTE_NAVIGATION_LINKS_TAG) != null) {
                useAbsoluteNavigationLinks = Boolean.parseBoolean(sce.getServletContext().getInitParameter(USE_ABSOLUTE_NAVIGATION_LINKS_TAG)
                );
            }
            PersistenceManagerFactory.init(getDbProperties(sce.getServletContext()), null);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
