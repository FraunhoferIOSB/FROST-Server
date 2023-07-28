/*
 * Copyright (C) 2023 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.http.common;

import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.TAG_CORE_SETTINGS;

import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManagerFactory;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.LiquibaseUser;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.UpgradeFailedException;
import java.io.IOException;
import java.io.PrintWriter;
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
@WebServlet(name = "DatabaseStatus", urlPatterns = {"/DatabaseStatus"})
public class DatabaseStatus extends HttpServlet {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseStatus.class);
    private static final String DESCRIPTION = "Database status and upgrade servlet.";

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     */
    protected void processGetRequest(HttpServletRequest request, HttpServletResponse response) {
        CoreSettings coreSettings = (CoreSettings) request.getServletContext().getAttribute(TAG_CORE_SETTINGS);
        PersistenceManagerFactory.init(coreSettings);

        response.setContentType("text/html;charset=UTF-8");
        LOGGER.info("DatabaseStatus Servlet called.");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet DatabaseStatus</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet DatabaseStatus at " + request.getContextPath() + "</h1>");
            out.println("<p>Checking Database status.</p>");
            out.println("<p><form action='DatabaseStatus' method='post' enctype='application/x-www-form-urlencoded'>");
            out.println("<button name='doupdate' value='Do Update' type='submit'>Do Update</button>");
            out.println("</form></p>");
            out.println("<p><a href='.'>Back...</a></p>");

            try (PersistenceManager pm = PersistenceManagerFactory.getInstance(coreSettings).create()) {
                if (pm instanceof LiquibaseUser) {
                    checkForUpgrades(out, (LiquibaseUser) pm);
                }
                for (LiquibaseUser user : coreSettings.getLiquibaseUsers()) {
                    checkForUpgrades(out, user);
                }
            }

            out.println("<p>Done. Click the button to execute the listed updates.</p>");
            out.println("</body>");
            out.println("</html>");
        } catch (IOException exc) {
            LOGGER.error("Error writing output to client", exc);
        }
    }

    public void checkForUpgrades(final PrintWriter out, LiquibaseUser user) {
        out.print("<h2>");
        out.print(user.getClass().getName());
        out.println("</h2>");
        out.println("<textarea rows=\"10\" style=\"width:95%;\">");
        String log = checkForUpgrades(user);
        out.println(log);
        out.println("</textarea>");
    }

    private String checkForUpgrades(final LiquibaseUser user) {
        return user.checkForUpgrades();
    }

    protected void processPostRequest(HttpServletRequest request, HttpServletResponse response) {
        CoreSettings coreSettings = (CoreSettings) request.getServletContext().getAttribute(TAG_CORE_SETTINGS);
        PersistenceManagerFactory.init(coreSettings);

        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet DatabaseStatus</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet DatabaseStatus at " + request.getContextPath() + "</h1><p>Updating Database</p>");

            try (PersistenceManager pm = PersistenceManagerFactory.getInstance(coreSettings).create()) {
                if (pm instanceof LiquibaseUser) {
                    processUpgrade(out, (LiquibaseUser) pm);
                }
                for (LiquibaseUser user : coreSettings.getLiquibaseUsers()) {
                    processUpgrade(out, user);
                }
            }

            out.println("<p>Done. <a href='DatabaseStatus'>Back...</a></p>");
            out.println("</body>");
            out.println("</html>");
        } catch (IOException exc) {
            LOGGER.error("Error writing output to client", exc);
        }
    }

    public void processUpgrade(final PrintWriter out, LiquibaseUser user) throws IOException {
        out.print("<h2>");
        out.print(user.getClass().getName());
        out.println("</h2>");
        out.println("<textarea rows=\"10\" style=\"width:95%;\">");
        processUpgrade(user, out);
        out.println("</textarea>");
    }

    private void processUpgrade(final LiquibaseUser user, final PrintWriter out) throws IOException {
        try {
            user.doUpgrades(out);
        } catch (UpgradeFailedException ex) {
            LOGGER.error("Could not initialise database.", ex);
        }
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        processGetRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        processPostRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return DESCRIPTION;
    }

}
