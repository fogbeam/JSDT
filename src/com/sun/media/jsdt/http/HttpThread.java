
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

import com.sun.media.jsdt.*;
import com.sun.media.jsdt.impl.*;
import java.net.*;
import java.io.*;
import java.util.*;

/**
 * JSDT HTTP thread class.
 *
 * @version     2.3 - 21st November 2017
 * @author      Rich Burridge
 */

class
HttpThread extends JSDTThread implements Runnable, httpDebugFlags {

    // The thread that will be used to ping for any asynchronous messages.
    private static HttpThread pingThread = null;

    // Send the current message now, or buffer it?
    protected boolean sendNow;

    // The session number for the current message being sent.
    private short currentSessionNo = 0;

    // The id number for the current message being sent.
    protected int currentId = 0;

    // The socket for this thread.
    Socket socket;

    // The input stream associated with this socket.
    protected InputStream in;

    // The output stream associated with this socket.
    protected OutputStream out;

    // The byte array input stream used for reading buffered messages.
    protected JSDTByteArrayInputStream byteIn;

    // The byte array output stream used for writing buffered messages.
    protected ByteArrayOutputStream byteOut;

    /* Hashtable of client thread currently suspended during the
     * authentication process.
     */
    private Hashtable<String, Thread> clientThreads = null;

    /* Hashtable of the results of the current authentication processes
     * keyed by thread.
     */
    private Hashtable<Thread, Boolean> authenticationResults = null;

    // The Sessions keyed by id, associated with this ping thread.
    protected Hashtable<Integer, Session> sessionsById = null;

    // The threads, keyed by id, associated with this ping thread.
    protected Hashtable<Integer, HttpThread> threadsById = null;

    // True if a new socket connection needs to be created.
    protected boolean needConnection = true;

    // An instance of the socket factory for creating sockets.
    private JSDTSocketFactory factory = null;

    // Indicates whether this socket should keep a permanent connection.
    protected boolean permanent = false;

    // Hashtable of client authentication id keys for thread values.
    protected final Hashtable<Integer, Thread> idThreads = new Hashtable<>();

    // Object used to synchronized client thread suspension.
    static final Object clientResponseLock = new Object();

    // Set true when we have a permanent connection with server.
    boolean havePermanent = false;

    // Set true when waiting for a T_Authenticate reply.
    private boolean authenticationWaitStatus = false;


    public
    HttpThread() {
        if (HttpThread_Debug) {
            debug("HttpThread: constructor.");
        }
    }


/**
 * <A NAME="SD_HTTPTHREAD"></A>
 * <EM>HttpThread</EM> the constructor on the server-side.
 *
 * @param socket
 */

    public
    HttpThread(Socket socket) throws SocketException, UnknownHostException {
        if (HttpThread_Debug) {
            debug("HttpThread: constructor:" +
                  " socket: " + socket);
        }

        setSocket(socket);
    }


/**
 * <A NAME="SD_HTTPTHREAD"></A>
 * <EM>HttpThread</EM> the constructor on the proxy-side.
 *
 * Initially JSDT will attempt to make a direct socket connection to the
 * given address/port. If that fails, it will try to use HTTP-tunneling.
 *
 * There are two forms of HTTP-tunneling, tried in order. The first is
 * http-to-port; the second is http-to-cgi.
 *
 * In http-to-port tunneling, we attempt an HTTP POST request to a http: URL
 * directed at the exact hostname and port number of the target server. The
 * HTTP request contains a single JSDT request. If the HTTP proxy accepts this
 * URL, it will forward the POST request to the listening JSDT server, which
 * will recognise the request and unwrap it. The result of the call is wrapped
 * in an HTTP reply, which is returned through the same proxy.
 *
 * Often, HTTP proxies will refuse to proxy requests to unusual port numbers.
 * In this case, we will fall back to http-to-cgi tunneling. The JSDT request
 * is encapsulated in a HTTP POST request as before, but the request URL is of
 * the form http://hostname:8080/cgi-bin/java-jsdt.cgi?port=n (where hostname
 * and n are the hostname and port number of the intended server). There must
 * be an HTTP server listening on port 8080 on the server host, which will run
 * the java-jsdt.cgi script (supplied with the JSDT distribution), which will
 * in turn forward the request to a JSDT server listening on port n. JSDT can
 * unwrap a HTTP-tunneled request without help from a http server, CGI script,
 * or any other external entity. So, if the client's HTTP proxy can connect
 * directly to the server's port, then you don't need a java-jsdt.cgi script
 * at all.
 *
 * @param address the server address to connect to.
 * @param port the port number to use on the server host.
 *
 * @exception UnknownHostException if the IP address of the host could not
 * be determined
 */

    public
    HttpThread(String address, int port, boolean permanent)
                throws UnknownHostException {
        String factoryClass = Util.getStringProperty("httpFactoryClass",
                                                     httpFactoryClass);

        if (HttpThread_Debug) {
            debug("HttpThread: constructor:" +
                  " address: "   + address +
                  " port: "      + port +
                  " permanent: " + permanent);
        }

        this.address          = address;
        this.port             = port;
        this.permanent        = permanent;
        byteIn                = new JSDTByteArrayInputStream();
        clientThreads         = new Hashtable<>();
        authenticationResults = new Hashtable<>();

        try {
            factory = (JSDTSocketFactory)
                Util.getClassForName(factoryClass).newInstance();

/* XXX: possible problem here if the JSDTMasterSocketFactory is forced to
 *      use a HttpToCGISocketFactory.
 */

            socketFactory = factory;

            needConnection = true;
        } catch (Exception e) {
            error("HttpThread: constructor: ", e);
        }
    }


/**
 * <A NAME="SD_CLEANUPCONNECTION"></A>
 * <EM>cleanupConnection</EM>
 */

    public void
    cleanupConnection() {
        if (HttpThread_Debug) {
            debug("HttpThread: cleanupConnection.");
        }

        try {
            if (socket != null) {
                socket.close();
            } else {
                in.close();
                out.close();
            }
        } catch (IOException e) {
            error("HttpThread: cleanupConnection: ", e);
        }
    }


    public final void
    finishMessage() {
        if (HttpThread_Debug) {
            debug("HttpThread: finishMessage.");
        }

        try {
            synchronized (this) {
                while (state == FOUND_REPLY) {
                    try {
                        wait();
                    } catch (InterruptedException ie) {
                    }
                }

                state = GET_MESSAGE;
                try {
                     if (sendNow && !isReusable() && socket != null) {
                         socket.close();
                     }
                 } catch (IOException ioe) {
                    error("HttpThread: finishMessage: ", ioe);
                 }
                 notifyAll();
                needConnection = true;
            }
        } catch (IllegalMonitorStateException e) {
            error("HttpThread: finishMessage: ", e);
        }
    }


    public final synchronized void
    finishReply() {
        if (HttpThread_Debug) {
            debug("HttpThread: finishReply.");
        }

        try {
            state = GET_MESSAGE;
              synchronized (waitValueLock) {
                  waitValue = 0;
              }
            try {
                 if (sendNow && !isReusable() && socket != null) {
                    socket.close();
                }
            } catch (IOException ioe) {
                error("HttpThread: finishReply: ", ioe);
            }
            notifyAll();
            needConnection = true;
        } catch (IllegalMonitorStateException e) {
            error("HttpThread: finishReply: ", e);
        }
    }


/**
 * <A NAME="SD_FLUSH"></A>
 * <EM>flush</EM> flush the message written to this connection.
 *
 * @exception IOException if an IO exception has occured.
 */

    public synchronized void
    flush() throws IOException {
        if (HttpThread_Debug) {
            debug("HttpThread: flush.");
        }

        dataOut.flush();

        try {
            byte[] bytes = byteOut.toByteArray();

            if (sendNow) {
                out.write(bytes);
            } else {
                if (mustPing()) {
                    putBufferedMessage(currentSessionNo, currentId, bytes);
                } else {
                    sendAsynchMessage(currentSessionNo, currentId, bytes);
                }
                byteOut = null;
            }
        } catch (IOException ioe) {
            cleanupConnection();
        }
    }


    boolean
    mustPing() {
        boolean haveToPing = Util.getBooleanProperty("alwaysPing", alwaysPing);

        if (HttpThread_Debug) {
            debug("HttpThread: mustPing.");
        }

        if (haveToPing) {
            return(true);
        } else if (socketFactory instanceof JSDTMasterSocketFactory) {
            return(false);
        } else if (socketFactory instanceof TCPSocketFactory) {
            return(false);
        } else if (socketFactory instanceof HttpToPortSocketFactory) {
            return(false);
        } else if (socketFactory instanceof HttpToCGISocketFactory) {
            return(true);
        } else {

/* Assume unknown factories have no permanent connection. */

            return(true);
        }
    }


/**
 * Determine if this connection can be used for multiple operations.
 * If the socket implements JSDTSocketInfo, then we can query it about
 * this; otherwise, assume that it does provide a full-duplex
 * persistent connection like java.net.Socket.
 */

    private boolean
    isReusable() {
        return (socket == null) || (!(socket instanceof JSDTSocketInfo)) ||
                (((JSDTSocketInfo) socket).isReusable());
    }


/**
 * <A NAME="SD_SETREUSABLE"></A>
 * <EM>setReusable</EM> set an indication of whether this socket is reusable.
 *
 * @param reusable true if socket is reusable.
 */

    void
    setReusable(boolean reusable) {
        if ((socket != null) && (socket instanceof JSDTSocketInfo)) {
            ((JSDTSocketInfo) socket).setReusable(reusable);
        }
    }


    private void
    makeNewConnection() throws IOException {
        if (HttpThread_Debug) {
            debug("HttpThread: makeNewConnection.");
        }

        if (factory != null) {
            socket  = factory.createSocket(address, port);
            in      = socket.getInputStream();
            out     = socket.getOutputStream();
            dataIn  = new DataInputStream(new BufferedInputStream(in));
            dataOut = new DataOutputStream(new BufferedOutputStream(out));
            if (permanent) {
                setReusable(true);
            }
        }
    }


/**
 * <A NAME="SD_SENDASYNCHMESSAGE"></A>
 * <EM>sendAsynchMessage</EM>
 *
 * @param sessionNo
 * @param id
 * @param byteArray the outgoing data message.
 */

    protected synchronized void
    sendAsynchMessage(short sessionNo, int id, byte[] byteArray) {
        if (HttpThread_Debug) {
            debug("HttpThread: sendAsynchMessage:" +
                  " session #: "  + sessionNo +
                  " id #: "       + id +
                  " byte array: " + byteArray);
        }

        error("HttpThread: sendAsynchMessage: ",
              "impl.subclass");
    }


/**
 * <A NAME="SD_PUTBUFFEREDMESSAGE"></A>
 * <EM>putBufferedMessage</EM> put a message in the vector of messages to
 * be sent, when the proxy-side pings for them.
 *
 * @param byteArray the outgoing data message.
 */

    protected synchronized void
    putBufferedMessage(short sessionNo, int id, byte[] byteArray) {
        if (HttpThread_Debug) {
            debug("HttpThread: putBufferedMessage:" +
                  " session #: "  + sessionNo +
                  " id #: "       + id +
                  " byte array: " + byteArray);
        }

        error("HttpThread: putBufferedMessage: ",
              "impl.subclass");
    }


/**
 * <A NAME="SD_GETSOCKETMESSAGE"></A>
 * <EM>getSocketMessage</EM> gets the next message off the connection.
 *
 * @return true if there is a valid message to be processed.
 */

    public synchronized boolean
    getSocketMessage() throws IOException {
        if (HttpThread_Debug) {
            debug("HttpThread: getSocketMessage.");
        }

        in      = socket.getInputStream();
        dataIn  = new DataInputStream(new BufferedInputStream(in));

        message.getMessageHeader(this);
        return(message.validMessageHeader());
    }


/**
 * <A NAME="SD_SETSOCKET"></A>
 * <EM>setSocket</EM> set the input/output streams associated with this
 * socket.
 *
 * @param socket
 */

    protected void
    setSocket(Socket socket) throws SocketException, UnknownHostException {
        if (HttpThread_Debug) {
            debug("HttpThread: setSocket:" +
                  " socket: " + socket);
        }

        try {
            in      = socket.getInputStream();
            out     = socket.getOutputStream();
            dataIn  = new DataInputStream(new BufferedInputStream(in));
            dataOut = new DataOutputStream(new BufferedOutputStream(out));
            byteIn  = new JSDTByteArrayInputStream();
        } catch (SocketException | UnknownHostException se) {
            throw se;
        } catch (Exception e) {
            error("HttpThread: setSocket: ", e);
        }

        this.socket = socket;
    }


/**
 * <A NAME="SD_SYNCINPUT"></A>
 * <EM>syncInput</EM> there has been an error on some kind while trying to
 * read the incoming data. Try to sync up with the beginning of the next
 * message.
 */

    final void
    syncInput() {
        int toRead, value;

        if (HttpThread_Debug) {
            debug("HttpThread: syncInput.");
        }

        try {
            toRead = dataIn.available();
            while (toRead > 0) {
                dataIn.mark(4);
                value = dataIn.read();
                toRead--;
                if (value == T_Version) {
                    value = dataIn.read();
                    toRead--;
                    if (value == version) {
                        value = dataIn.read();
                        toRead--;
                        if (value == T_Session_No) {
                            dataIn.reset();
                            break;
                        }
                    }
                }
            }
        } catch (IOException ioe) {
            error("HttpThread: syncInput: ", ioe);
        }
    }


    final boolean
    suspendClientThread(String key, Thread thread) {
        Boolean authenticationResult;

        if (HttpThread_Debug) {
            debug("HttpThread: suspendClientThread:" +
                  " key: "    + key +
                  " thread: " + thread);
        }

        synchronized (clientThreads) {
            clientThreads.put(key, thread);
        }

        thread.suspend();

        synchronized (authenticationResults) {
            authenticationResult = authenticationResults.get(thread);
        }

        return(authenticationResult);
    }


    final void
    resumeClientThread(String key, boolean result) {
        Thread thread;

        if (HttpThread_Debug) {
            debug("HttpThread: resumeClientThread:" +
                  " key: "    + key +
                  " result: " + result);
        }

        synchronized (clientThreads) {
            thread = clientThreads.get(key);
        }

        synchronized (authenticationResults) {
            authenticationResults.put(thread, result);
        }

        thread.resume();
    }


    final void
    setAuthenticateWaitStatus(boolean authenticationWaitStatus) {
        if (HttpThread_Debug) {
            debug("HttpThread: setAuthenticateWaitStatus:" +
                  " authentication wait state: " + authenticationWaitStatus);
        }

        this.authenticationWaitStatus = authenticationWaitStatus;
    }


    final boolean
    getAuthenticateWaitStatus() {
        if (HttpThread_Debug) {
            debug("HttpThread: getAuthenticateWaitStatus.");
        }


        return(authenticationWaitStatus);
    }


    final void
    waitForPermanentConnection() {
        if (HttpThread_Debug) {
            debug("HttpThread: waitForPermanentConnection.");
        }

        if (this instanceof SameVMThread || !permanent) {
            return;
        }

        while (!havePermanent) {
            synchronized (this) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                }
            }
        }
    }


/**
 * <A NAME="SD_WAITFORCLIENTRESPONSE"></A>
 * <EM>waitForClientResponse</EM> wait for an authentication response from
 * the Client for the given id.
 *
 * @param id the id key to use for Client authentication.
 *
 * @return the message containing that autentication response.
 */

    final Message
    waitForClientResponse(int id) {
        Integer idObject = id;

        if (HttpThread_Debug) {
            debug("HttpThread: waitForClientResponse:" +
                  " id: " + id);
        }

        state = GET_MESSAGE;
        synchronized (idThreads) {
            idThreads.put(id, Thread.currentThread());
        }

        while (idThreads.containsKey(idObject)) {
            synchronized (clientResponseLock) {
                try {
                    clientResponseLock.wait();
                } catch (InterruptedException ie) {
                }
            }
        }

        state = PROCESSING_REPLY;
        return(message);
    }


/**
 * <A NAME="SD_WAITFORREPLY"></A>
 * <EM>waitForReply</EM>
 *
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @return the current message that was being waited for.
 */

    public Message
    waitForReply() throws TimedOutException {
        long startTime    = System.currentTimeMillis();
        long currentTime;
        long timeoutValue = Util.getLongProperty("timeoutPeriod",
                                                  timeoutPeriod);

        if (HttpThread_Debug) {
            debug("HttpThread: waitForReply.");
        }

        synchronized (this) {
            while (state != FOUND_REPLY) {
                currentTime = System.currentTimeMillis();
                if ((currentTime - startTime) >= timeoutValue) {
                    finishReply();
                    throw new TimedOutException();
                }
                try {
                    if (getSocketMessage()) {
                        break;
                    }
                } catch (IOException ioe) {
                }
            }
            state = PROCESSING_REPLY;
        }

        return(message);
    }


/**
 * <A NAME="SD_WRITEMESSAGEHEADER"></A>
 * <EM>writeMessageHeader</EM> write the "standard" header portion of a message.
 * Each client/server message contains an initial standard set of fields:
 *
 * T_Version        - char.
 * version          - char.
 * T_Session_No     - char
 * sessionNo        - short    (unique for each session name).
 * id               - integer  (unique for each sending thread).
 * type             - char     (message type).
 * action           - char     (message action).
 *
 * Each of these fields are sent over the connection via a DataOutputStream.
 *
 * @param stream the DataOutputStream to write the fields to.
 * @param sessionNo the unique session number for this message.
 * @param id the unique identifier for this sending thread.
 * @param type the message type.
 * @param action the message action.
 * @param toWait do we wait for a reply to this message?
 * @param sendNow if true, send this message out now, otherwise add it to a
 * buffer of outgoing messages that will be retrieved by the receiver.
 *
 * @exception IOException if an IO exception has occured.
 */

    public void
    writeMessageHeader(DataOutputStream stream, short sessionNo,
                       int id, char type, char action,
                       boolean toWait, boolean sendNow)
                throws IOException {
        if (HttpThread_Debug) {
            debug("HttpThread: writeMessageHeader:" +
                  " stream: "    + stream +
                  " session #: " + sessionNo +
                  " id: "        + id +
                  " type: "      + typeToString(type) +
                  " action: "    + actionToString(action) +
                  " wait?: "     + toWait +
                  " send now?: " + sendNow);
        }

        synchronized (this) {
            if (toWait) {
                try {
                    while (true) {
                          synchronized (waitValueLock) {
                              if (state == GET_MESSAGE && waitValue == 0) {
                                  break;
                            }
                        }
                        try {
                            wait();
                        } catch (InterruptedException ie) {
                        }
                    }
                } catch (Exception e) {
                    error("HttpThread: writeMessageHeader: ", e);
                }

                synchronized (waitValueLock) {
                      if (HttpThread_Debug) {
                        debug("HttpThread: writeMessageHeader:" +
                              " Old wait value" +
                              " for thread: " + this +
                              " was: "        + waitValue);
                    }

                    waitValue = (id   << 32) + (sessionNo << 16) +
                                (type <<  8) + action;

                    if (HttpThread_Debug) {
                        debug("HttpThread: writeMessageHeader:" +
                              " Changing wait value for:" +
                              " thread: "    + this +
                              " session #: " + sessionNo +
                              " id: "        + id +
                              " type: "   + typeToString(type) +
                              " action: " + actionToString(action));
                        debug("HttpThread: writeMessageHeader:" +
                              " New wait value" +
                              " for thread: " + this +
                              " is: "         + waitValue);
                    }
                }

                state = WAITING_FOR_REPLY;
            } else {
                state = SENDING_MESSAGE;
            }

            this.sendNow     = sendNow;
            currentSessionNo = sessionNo;
            currentId        = id;

            byteOut = new ByteArrayOutputStream();
            dataOut = new DataOutputStream(byteOut);

            if (needConnection) {
                makeNewConnection();
                needConnection = false;
            }

            stream = dataOut;
            stream.writeChar(T_Version);
            stream.writeChar(version);
            stream.writeChar(T_Session_No);
            stream.writeShort(sessionNo);
            stream.writeInt(id);
            stream.writeChar(type);
            stream.writeChar(action);
        }
    }


/**
 * <A NAME="SD_HANDLEMESSAGE"></A>
 * <EM>handleMessage</EM>
 *
 * @param message
 */

    public void
    handleMessage(Message message) {
        if (HttpThread_Debug) {
            debug("HttpThread: handleMessage:" +
                  " message: " + message);
        }

        error("HttpThread: handleMessage: ",
              "impl.subclass");
    }


    protected void
    startPingThread(SessionImpl session) {
        if (HttpThread_Debug) {
            debug("HttpThread: startPingThread:" +
                  " session: " + session);
        }

        if (pingThread == null) {
            try {
                pingThread = new PingThread(address, port);
            } catch (UnknownHostException uhe) {
            }

            Util.startThread(pingThread,
                             "PingThread: " + session.getName(), true);
        }

        pingThread.addEntry(getId(), session, this);
    }


    void
    stopPingThread() {
        if (HttpThread_Debug) {
            debug("HttpThread: stopPingThread.");
        }

        pingThread.removeEntry(getId());
    }


/**
 * <A NAME="SD_ADDENTRY"></A>
 * <EM>addEntry</EM>
 *
 * @param id
 * @param session
 * @param thread
 */

    private void
    addEntry(int id, SessionImpl session, HttpThread thread) {
        Integer key = id;

        if (HttpThread_Debug) {
            debug("HttpThread: addEntry:" +
                  " id: "      + id +
                  " session: " + session +
                  " thread: "  + thread) ;
        }

        sessionsById.put(key, session);
        threadsById.put(key, thread);
    }


/**
 * <A NAME="SD_REMOVEENTRY"></A>
 * <EM>removeEntry</EM>
 *
 * @param id
 */

    private void
    removeEntry(int id) {
        Integer key = id;

        if (HttpThread_Debug) {
            debug("HttpThread: removeEntry:" +
                  " id: " + id);
        }

        synchronized (sessionsById) {
            sessionsById.remove(key);
            threadsById.remove(key);
        }
    }


    protected void
    pingForMessage(SessionImpl session, HttpThread thread, int id)
                throws ConnectionException, NoSuchSessionException,
                       TimedOutException {
        Message      message;
        int          retval;
        SessionProxy sp     = (SessionProxy) session.po.getProxy();
        int          length = 0;

        if (HttpThread_Debug) {
            debug("HttpThread: pingForMessage:" +
                  " session: " + session +
                  " thread: "  + thread +
                  " id: "      + id);
        }

        try {
            writeMessageHeader(dataOut, sp.getSessionNo(), getId(),
                       SessionImpl.M_Session, T_GetMessage, true, true);
            dataOut.writeInt(id);
            flush();
            message = waitForReply();

            in      = message.thread.dataIn;
            retval  = message.thread.dataIn.readInt();
            if (retval == 0) {
                length  = message.thread.dataIn.readInt();
                if (length != 0) {
                    byteIn.setByteArray(message.thread.getData(length), 0, length);
                    dataIn = new DataInputStream(byteIn);
                }
            }

            message.thread.finishReply();

            if (retval != 0) {
                if (retval == JSDTException.NO_SUCH_SESSION) {
                    pingThread.removeEntry(id);
                } else {
                    error("HttpThread: pingForMessage: ",
                          "impl.unknown.exception.type", retval);
                }
            }

            if (length != 0) {
                message.getMessageHeader(this);
                thread.handleMessage(message);
                dataIn = new DataInputStream(new BufferedInputStream(in));
            }
        } catch (IOException e) {
            finishReply();
            throw new ConnectionException();
        }
    }


    protected void
    sendPermMessage(SessionImpl session, HttpThread thread, int id) {
        if (HttpThread_Debug) {
            debug("HttpThread: sendPermMessage:" +
                  " session: " + session +
                  " thread: "  + thread +
                  " id: "      + id);
        }

        try {
            SessionProxy sp = (SessionProxy) session.po.getProxy();

            thread.writeMessageHeader(thread.dataOut, sp.getSessionNo(), id,
                                      SessionImpl.M_Session, T_Permanent,
                                      true, true);
            thread.dataOut.flush();
        } catch (IOException ioe) {
            thread.finishMessage();
        } catch (Exception e) {
            error("HttpThread: sendPermMessage: ", e);
        }
    }


/**
 * <A NAME="SD_RUN"></A>
 * <EM>run</EM>
 */

    public void
    run() {
        if (HttpThread_Debug) {
            debug("HttpThread: run.");
        }

        try {
            while (running) {
                synchronized (this) {
                    try {
                        if (running) {
                            wait();
                        }
                    } catch (InterruptedException ie) {
                    }
                }
            }
        } catch (Exception e) {
            error("HttpThread: run: ", e);
        }
    }
}
