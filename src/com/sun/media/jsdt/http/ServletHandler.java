
/*
 *  Copyright (c) 1996-2005 Sun Microsystems, Inc.
 *  All Rights Reserved.
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Library General Public License as
 *  published by the Free Software Foundation; either version 2, or (at
 *  your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU Library General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 *  02111-1307, USA.
 */

package com.sun.media.jsdt.http;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.servlet.http.*;
import javax.servlet.*;

/**
 * JSDT HTTP servlet handler class.
 *
 * (Based on the RMI servlet handler class in the rmiservlethandler package).
 *
 * @version     2.3 - 19th December 2017
 *
 * When a JSDT client initiates contact with a remote server, it attempts to
 * establish a connection using each of the following protocols in turn,
 * until one succeeds:
 *
 * 1. Direct HTTP connection.
 * 2. Connection on port 80 over HTTP to a CGI script.
 *
 * The JSDT ServletHandler can be used as replacement for the java-jsdt.cgi
 * script that comes with the JSDT distribution (and is invoked in #2).
 * The java-jsdt.cgi script and the ServletHandler both function as proxy
 * applications that forward remote calls embedded in HTTP to local JSDT
 * servers which service these calls. The JSDT ServletHandler enables JSDT
 * to tunnel JSDT calls over HTTP more efficiently than the existing
 * java-jsdt.cgi script.  The ServletHandler is only loaded once from
 * the servlet administration utility.  The script, java-jsdt.cgi, is
 * executed once every remote call.
 *
 * The ServletHandler class contains methods for executing as a Java
 * servlet extension.  Because JSDT only makes use of the HTTP post command,
 * the ServletHandler only supports the <code>doPost</code>
 * <code>HttpServlet</code> method.  The <code>doPost</code> method of this
 * class interprets a servlet request's query string as a command of the form
 * "&lt;command&gt;=&lt;parameters&gt;".  These commands are represented by the
 * abstract interface, <code>CommandHandler</code>.  Once the
 * <code>doPost</code> method has parsed the requested command, it
 * calls the execute method on one of several command handlers in the
 * <code>commands</code> array.
 *
 * The command that actually proxies remote calls is the
 * <code>ServletForwardCommand</code>.  When the execute method is invoked on
 * the ServletForwardCommand, the command will open a connection on a local
 * port specified by its <code>param</code> parameter and will proceed to
 * write the body of the relevant post request into this connection.  It is
 * assumed that a JSDT server is listening on the local port, "param."
 * The "forward" command will then read the JSDT server's response and send
 * this information back to the JSDT client as the body of the response to
 * the HTTP post method.
 *
 * Servlet documentation may be found at the following location:
 *
 * http://jserv.javasoft.com/products/java-server/documentation/
 *        webserver1.0.2/apidoc/Package-javax.servlet.http.html
 */

public class
ServletHandler extends HttpServlet {

    /**
     * CommandHandler is the abstraction for an object that handles
     * a particular supported command (for example the "forward"
     * command "forwards" call information to a remote server on the
     * local machine).
     *
     * The command handler is only used by the ServletHandler so the
     * interface is protected.
     */
    protected interface CommandHandler {

    /**
     * Return the string form of the command to be recognized in the
     * query string. 
     */

        String
        getName();


    /**
     * Execute the command with the given string as parameter.
     */

        void
        execute(HttpServletRequest req, HttpServletResponse res, String param)
          throws ServletClientException, ServletServerException, IOException;
    }

    /**
     * List of handlers for supported commands. A new command will be
     * created for every service request.
     */

    private static final CommandHandler commands[] = new CommandHandler [] {
        new ServletForwardCommand(),
        new ServletGethostnameCommand(),
        new ServletPingCommand(),
        new ServletTryHostnameCommand()
    };

    // Construct table mapping command strings to handlers.

    private static final Hashtable<String, ServletHandler.CommandHandler> commandLookup;
    static {
        commandLookup = new Hashtable<>();
        for (int i = 0; i < commands.length; ++ i) {
            commandLookup.put(commands[i].getName(), commands[i]);
        }
    }


/**
 * Execute the command given in the servlet request query string.
 * The string before the first '=' in the queryString is
 * interpreted as the command name, and the string after the first
 * '=' is the parameters to the command.
 *
 * @param req  HTTP servlet request, contains incoming command and
 *             arguments
 * @param res  HTTP servlet response
 * @exception  ServletException and IOException when invoking
 *             methods of <code>req</code> or <code>res</code>.
 */

    public void
    doPost(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException {
        try {

/* Command and parameter for this POST request. */

            String queryString = req.getQueryString();
            String command, param;
            int    delim = queryString.indexOf("=");

            if (delim == -1) {
                command = queryString;
                param = "";
            } else {
                command = queryString.substring(0, delim);
                param = queryString.substring(delim + 1);
            }

            System.out.println("command: " + command);
            System.out.println("param: " + param);

// Lookup command to execute on the client's behalf.

            CommandHandler handler = commandLookup.get(command);

// Execute the command.

            if (handler != null) {
                try {
                    handler.execute(req, res, param);
                } catch (ServletClientException e) {
                    returnClientError(res, "client error: " +
                                      e.getMessage());
                    e.printStackTrace();
                } catch (ServletServerException e) {
                    returnServerError(res, "internal server error: " +
                                      e.getMessage());
                    e.printStackTrace();
                }
            } else {
                returnClientError(res, "invalid command: " + command);
            }
        } catch (Exception e) {
            returnServerError(res, "internal error: " + e.getMessage());
            e.printStackTrace();
        }
    }

/**
 * Provide more intelligible errors for methods that are likely to
 * be called.  Let unsupported HTTP "do*" methods result in an
 * error generated by the super class.
 *
 * @param req  http Servlet request, contains incoming command and
 *             arguments
 *
 * @param res  http Servlet response
 *
 * @exception  ServletException and IOException when invoking
 *             methods of <code>req</code> or <code>res</code>.
 */

    public void
    doGet(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException {
        returnClientError(res,
                          "GET Operation not supported: " +
                          "Can only forward POST requests.");
    }


    public void
    doPut(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException {
        returnClientError(res,
                          "PUT Operation not supported: " +
                          "Can only forward POST requests.");
    }


    public String
    getServletInfo() {
        return("JSDT Call Forwarding Servlet Servlet.<br>\n");
    }


/**
 * Return an HTML error message indicating there was error in
 * the client's request.
 *
 * @param res Servlet response object through which <code>message</code>
 *            will be written to the client which invoked one of
 *            this servlet's methods.
 * @param message Error message to be written to client.
 */

    private static void
    returnClientError(HttpServletResponse res, String message)
        throws IOException {
        res.sendError(HttpServletResponse.SC_BAD_REQUEST,
                      "<HTML><HEAD>" +
                      "<TITLE>Java JSDT Client Error</TITLE>" +
                      "</HEAD>" +
                      "<BODY>" +
                      "<H1>Java JSDT Client Error</H1>" +
                      message +
                      "</BODY></HTML>");

        System.err.println(HttpServletResponse.SC_BAD_REQUEST +
                           "Java JSDT Client Error" +
                           message);
    }


/**
 * Return an HTML error message indicating an internal error
 * occurred here on the server. 
 *
 * @param res Servlet response object through which <code>message</code>
 *            will be written to the servlet client.
 * @param message Error message to be written to servlet client.
 */

    private static void
    returnServerError(HttpServletResponse res, String message)
        throws IOException {
        res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                      "<HTML><HEAD>" +
                      "<TITLE>Java JSDT Server Error</TITLE>" +
                      "</HEAD>" +
                      "<BODY>" +
                      "<H1>Java JSDT Server Error</H1>" +
                      message + "</BODY></HTML>");

        System.err.println(HttpServletResponse.SC_INTERNAL_SERVER_ERROR +
                           "Java JSDT Server Error: " +
                           message);
    }


/*
 * The ServletHandler class is the only object that needs to access the
 * CommandHandler subclasses, so we write the commands internal to the
 * servlet handler.
 */

/**
 * Class that has an execute command to forward request body to
 * local port on the server and send server reponse back to client. 
 */

    protected static class
    ServletForwardCommand implements CommandHandler {

        public String getName() {
            return("forward");
        }


/**
 * Execute the forward command.  Forwards data from incoming servlet
 * request to a port on the local machine.  Presumably, a JSDT server
 * will be reading the data that this method sends.
 *
 * @param req   The servlet request.
 * @param res   The servlet response.
 * @param param Port to which data will be sent.
 */

        public void
        execute(HttpServletRequest req, HttpServletResponse res, String param)
          throws ServletClientException, ServletServerException, IOException {
            int              port;
            byte             buffer[];
            Socket           socket;
            DataInputStream  clientIn;
            DataInputStream  socketIn;
            DataOutputStream socketOut;

            try {
                port = Integer.parseInt(param);
            } catch (NumberFormatException e) {
                throw new ServletClientException("invalid port number: " +
                                                 param);
            }

            if (port <= 0 || port > 0xFFFF) {
                throw new ServletClientException("invalid port: " + port);
            }
            if (port < 1024) {
                throw new
                  ServletClientException("permission denied for port: "  +
                                          port);
            }

            try {
                socket = new Socket(InetAddress.getLocalHost(), port);
            } catch (IOException e) {
                throw new ServletServerException("could not connect to " +
                                                 "local port");
            }

// Read client's request body.

            clientIn = new DataInputStream(req.getInputStream());
            buffer = new byte[req.getContentLength()];
            try {
                clientIn.readFully(buffer);
            } catch (EOFException e) {
                throw new ServletClientException("unexpected EOF " +
                                                 "reading request body");
            } catch (IOException e) {
                throw new ServletClientException("error reading request" +
                                                 " body");
            }

// Send to local server in HTTP.

            try {
                socketOut = new DataOutputStream(socket.getOutputStream());
                socketOut.writeBytes("POST / HTTP/1.0\r\n");
                socketOut.writeBytes("Content-length: " +
                                     req.getContentLength() + "\r\n\r\n");
                socketOut.write(buffer);
                socketOut.flush();
            } catch (IOException e) {
                throw new ServletServerException("error writing to server");
            }

// Read response from local server.

            try {
                socketIn = new DataInputStream(socket.getInputStream());
            } catch (IOException e) {
                throw new ServletServerException("error reading from " +
                                                 "server");
            }

            String  key = "Content-length:".toLowerCase();
            boolean contentLengthFound = false;
            String  line;
            int     responseContentLength = -1;

            do {
                try {
                    line = socketIn.readLine();
                } catch (IOException e) {
                    throw new
                        ServletServerException("error reading from server");
                }
                if (line == null) {
                    throw new ServletServerException(
                                   "unexpected EOF reading server response");
                }

                if (line.toLowerCase().startsWith(key)) {
                    responseContentLength =
                        Integer.parseInt(line.substring(key.length()).trim());
                    contentLengthFound = true;
                }
            } while ((line.length() != 0) &&
                     (line.charAt(0) != '\r') && (line.charAt(0) != '\n'));

            if (!contentLengthFound || responseContentLength < 0) {
                throw new ServletServerException(
                      "missing or invalid content length in server response");
            }

            buffer = new byte[responseContentLength];
            try {
                socketIn.readFully(buffer);
            } catch (EOFException e) {
                throw new ServletServerException(
                            "unexpected EOF reading server response");
            } catch (IOException e) {
                throw new ServletServerException("error reading from server");
            }

// Send local server response back to servlet client.

            res.setStatus(HttpServletResponse.SC_OK);
            res.setContentType("application/octet-stream");
            res.setContentLength(buffer.length);

            try {
                OutputStream out = res.getOutputStream();

                out.write(buffer);
                out.flush();
            } catch (IOException e) {
                throw new ServletServerException("error writing response");
            } finally {
                socketOut.close();
                socketIn.close();
            }
        }
    }

/**
 * Class that has an execute method to return the host name of the
 * server as the response body.
 */

    protected static class
    ServletGethostnameCommand implements CommandHandler {

        public String getName() {
            return("gethostname");
        }

        public void
        execute(HttpServletRequest req, HttpServletResponse res, String param)
          throws ServletClientException, ServletServerException, IOException {
            byte[] getHostStringBytes = req.getServerName().getBytes();

            res.setStatus(HttpServletResponse.SC_OK);
            res.setContentType("application/octet-stream");
            res.setContentLength(getHostStringBytes.length);

            OutputStream out = res.getOutputStream();
            out.write(getHostStringBytes);
            out.flush();
        }
    }

/**
 * Class that has an execute method to return an OK status to
 * indicate that connection was successful. 
 */

    protected static class
    ServletPingCommand implements CommandHandler {

        public String getName() {
            return("ping");
        }

        public void
        execute(HttpServletRequest req, HttpServletResponse res, String param)
          throws ServletClientException, ServletServerException, IOException {
            res.setStatus(HttpServletResponse.SC_OK);
            res.setContentType("application/octet-stream");
            res.setContentLength(0);
        }
    }

/**
 * Class that has an execute method to return a human readable
 * message describing which host name is available to local Java
 * VMs. 
 */

    protected static class
    ServletTryHostnameCommand implements CommandHandler {

        public String getName() {
            return("hostname");
        }

        public void
        execute(HttpServletRequest req, HttpServletResponse res, String param)
          throws ServletClientException, ServletServerException, IOException {
            PrintWriter pw = res.getWriter();

            pw.println("");
            pw.println("<HTML>" +
                       "<HEAD><TITLE>Java JSDT Server Hostname Info" +
                       "</TITLE></HEAD>" +
                       "<BODY>");
            pw.println("<H1>Java JSDT Server Hostname Info</H1>");
            pw.println("<H2>Local host name available to Java VM:</H2>");
            pw.print("<P>InetAddress.getLocalHost().getHostName()");

            try {
                String localHostName = InetAddress.getLocalHost().getHostName();

                pw.println(" = " + localHostName);
            } catch (UnknownHostException e) {
                pw.println(" threw java.net.UnknownHostException");
            }

            pw.println("<H2>Server host information obtained through Servlet " +
                       "interface from HTTP server:</H2>");
            pw.println("<P>SERVER_NAME = " + req.getServerName());
            pw.println("<P>SERVER_PORT = " + req.getServerPort());
            pw.println("</BODY></HTML>");
        }
    }


/**
 * ServletClientException is thrown when an error is detected
 * in a client's request.
 */

    protected static class
    ServletClientException extends Exception {

        public
        ServletClientException(String s) {
            super(s);
        }
    }


/**
 * ServletServerException is thrown when an error occurs here on the server.
 */
    protected static class
    ServletServerException extends Exception {

        public ServletServerException(String s) {
            super(s);
        }
    }
}
