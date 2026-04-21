<%@page import="de.fraunhofer.iosb.ilt.frostserver.plugin.odata.PluginOData"%>
<%@page import="de.fraunhofer.iosb.ilt.frostserver.plugin.coremodelv2.PluginCoreServiceV2"%>
<%@page import="de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.PluginCoreService"%>
<%@page import="de.fraunhofer.iosb.ilt.frostserver.plugin.openapi.PluginOpenApi"%>
<%@page import="de.fraunhofer.iosb.ilt.frostserver.service.PluginManager"%>
<%@page import="de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    CoreSettings coreSettings = (CoreSettings) pageContext.getServletContext().getAttribute(CoreSettings.TAG_CORE_SETTINGS);
    PluginManager pm = coreSettings.getPluginManager();
    boolean hasOpenApi = pm.isPluginEnabled(PluginOpenApi.class);
    boolean hasV1 = pm.isPluginEnabled(PluginCoreService.class);
    boolean hasV2 = pm.isPluginEnabled(PluginCoreServiceV2.class);
    boolean hasOData = pm.isPluginEnabled(PluginOData.class);
%>
<!DOCTYPE html>
<html lang="en">
    <head>
        <title>Start Page</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <style>
            .flex-container {
                display: flex;
                flex-direction: row;
                width: 800px;
            }
            #data, #result {
                width: 800px;
                padding:5px 0px;
                margin:5px 0px;
            }
        </style>
        <script type="text/javascript">
            function execute() {
                let method = document.getElementById('method').value;
                let url = document.getElementById('url').value;
                let data = document.getElementById('data').value;
                document.getElementById('result').innerHTML = 'Executing ' + method + " to " + url;

                var request = new XMLHttpRequest();
                request.addEventListener("load", function (e) {
                    document.getElementById('result').innerHTML = 'Done';
                    if (request.readyState === 4) {
                        if (request.status >= 200 && request.status < 300) {
                            let location = request.getResponseHeader('Location');
                            if (location === null)
                                location = "";
                            try {
                                var data = JSON.parse(request.responseText);
                                document.getElementById('result').innerHTML = 'Done:<br><pre>' + JSON.stringify(data, null, '  ') + '</pre>' + location;
                            } catch (err) {
                                document.getElementById('result').innerHTML = 'Done: ' + request.responseText + "<br>"
                                        + location;
                            }
                        } else {
                            document.getElementById('result').innerHTML = 'Error ' + request.status + ": " + request.responseText + "";
                        }
                    }
                });
                request.addEventListener("error", function (e) {
                    document.getElementById('result').innerHTML = 'Error: ' + request.statusText;
                });
                request.open(method, url, true);
                request.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
                request.send(data);
            }
        </script>
    </head>
    <body>
        <h1>FROST-Server</h1>
        The Fraunhofer Open-source SensorThings Server.

        <h2>Some Links</h2>
        <table>
            <tr><th>Link</th><th>Description</th></tr>
            <% if (hasV1) { %>
                <tr><td><a href="v1.0">v1.0/</a></td><td>SensorThingsApi v1.0</td></tr>
                <tr><td><a href="v1.1">v1.1/</a></td><td>SensorThingsApi v1.1</td></tr>
                <% if (hasOpenApi) { %>
                    <tr><td><a href="v1.1/api">v1.1/api</a></td><td>OpenAPI definition for SensorThingsApi v1.1</td></tr>
                <% } %>
            <% } %>
            <% if (hasV2) { %>
                <tr><td><a href="v2.0">v2.0/</a></td><td>SensorThingsApi v2.0</td></tr>
                <tr><td><a href="v2.0/$metadata?$format=json">v2.0/$metadata</a></td><td>SensorThingsApi v2.0 Data Model Metadata</td></tr>
                <% if (hasOpenApi) { %>
                    <tr><td><a href="v2.0/api">v2.0/api</a></td><td>OpenAPI definition for SensorThingsApi v2.0</td></tr>
                <% } %>
            <% } %>
            <% if (hasOData) { %>
                <tr><td><a href="ODATA_4.01">ODATA_4.01/</a></td><td>OData version 4.01</td></tr>
                <tr><td><a href="ODATA_4.01/$metadata?$format=json">ODATA_4.01/$metadata</a></td><td>OData version 4.01 Data Model Metadata</td></tr>
                <% if (hasOpenApi) { %>
                    <tr><td><a href="ODATA_4.01/api">ODATA_4.01/api</a></td><td>OpenAPI definition for OData version 4.01</td></tr>
                <% } %>
                <tr><td><a href="ODATA_4.0">ODATA_4.0</a></td><td>OData version 4.0</td></tr>
                <tr><td><a href="ODATA_4.0/$metadata?$format=json">ODATA_4.0/$metadata</a></td><td>OData version 4.0 Data Model Metadata</td></tr>
                <% if (hasOpenApi) { %>
                    <tr><td><a href="ODATA_4.0/api">ODATA_4.0/api</a></td><td>OpenAPI definition for OData version 4.0</td></tr>
                <% } %>
            <% } %>
            <tr><td><a href="DatabaseStatus">DatabaseStatus</a></td><td>Database Status and Update</td></tr>
        </table>
        <a href="https://github.com/FraunhoferIOSB/FROST-Server">FROST-Server on GitHub</a>
        <h2>HTTP Tool</h2>
        <div class="flex-container">
            <select id="method">
                <option value="GET">GET</option>
                <option value="POST">POST</option>
                <option value="PATCH">PATCH</option>
                <option value="DELETE">DELETE</option>
            </select>
            <label for="url" style="padding: 5px">To URL:</label>
            <input type="text" id="url" name="url" style="flex-grow: 1" value="v1.1/Things"><input type="button" value="execute" onclick="execute();">
        </div>
        <div>
            <textarea id="data" name="content" rows="10" cols="80">
{
  "name" : "Kitchen",
  "description" : "The Kitchen in my house",
  "properties" : {
    "oven" : true,
    "heatingPlates" : 4
  }
}
            </textarea>
        </div>
        <div id="result"></div>
    </body>
</html>
