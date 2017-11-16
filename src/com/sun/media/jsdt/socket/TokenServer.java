
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

package com.sun.media.jsdt.socket;

import com.sun.media.jsdt.*;
import com.sun.media.jsdt.impl.*;
import com.sun.media.jsdt.event.TokenEvent;
import java.io.*;
import java.util.*;

/**
 * JSDT Token Server-side class.
 *
 * @version     2.3 - 6th November 2017
 * @author      Rich Burridge
 */

public final class
TokenServer extends ManageableServer implements AbstractTokenServer {

    // The server-side session associated with this token server.
    private SessionImpl session;

    // The server-side token associated with this token server.
    private TokenImpl token;

    // The clients who have currently grabbed/inhibited this token.
    private Hashtable<String, Client> grabbedClients = null;

    // The client potentially giving this token (if any).
    private Client givingClient = null;

    // The client potentially being given this token (if any).
    private Client receivingClient = null;


/**
 * <A NAME="SD_INITSERVER"></A>
 * <EM>initServer</EM> initialise the "server-side" for this Token.
 *
 * @param name the name of the server Token being constructed.
 * @param session the server-side session the Token belongs to.
 * @param object the server-side Token.
 */

    public void
    initServer(String name, SessionImpl session, Object object) {
        if (TokenServer_Debug) {
            debug("TokenServer: initServer:" +
                  " name: "    + name +
                      " session: " + session);
        }

        this.name      = name;
        this.session   = session;
        this.token     = (TokenImpl) object;

        clients        = new Hashtable<>();
        grabbedClients = new Hashtable<>();
        super.initServer(name, session, object);
    }


/**
 * <A NAME="SD_GETSERVER"></A>
 * <EM>getServer</EM>
 *
 * @return
 */

    public Object
    getServer() {
        if (TokenServer_Debug) {
            debug("TokenServer: getServer.");
        }

        return(this);
    }


/**
 * <A NAME="SD_ADDGRABBEDCLIENT"></A>
 * <EM>addGrabbedClient</EM> add this client to the hashtable of clients
 * currently grabbing/inhibiting this token.
 *
 * @param client the client to be added.
 */

    private void
    addGrabbedClient(Client client) {
        if (TokenServer_Debug) {
            debug("TokenServer: addGrabbedClient:" +
                  " client: " + client);
        }

         grabbedClients.put(client.getName(), client);
    }


/**
 * <A NAME="SD_ADDJOINEDCLIENT"></A>
 * <EM>addJoinedClient</EM> add this client to the hashtable of clients
 * currently joined to this token. This token is also added to the vector
 * of tokens that this client is joined to for this session, to make any
 * later cleanup easier.
 *
 * @param client the client to be added.
 */

    private void
    addJoinedClient(ClientImpl client) {
        String        clientName   = client.getName();
        SessionServer ss           = (SessionServer) session.so.getServer();
        ClientImpl    ci           = ss.getClientByName(clientName);
        Vector<Token> clientTokens = ci.getTokens();

        if (TokenServer_Debug) {
            debug("TokenServer: addJoinedClient:" +
                  " client: " + client);
        }

        clients.put(client.getName(), client);
        clientTokens.addElement(token);
    }


/**
 * <A NAME="SD_GETGRABBEDCLIENTBYNAME"></A>
 * <EM>getGrabbedClientByName</EM>
 *
 * @param clientName
 *
 * @return
 */

    private Client
    getGrabbedClientByName(String clientName) {
        if (TokenServer_Debug) {
            debug("TokenServer: getGrabbedClientByName:" +
                  " client name: " + clientName);
        }

        return(grabbedClients.get(clientName));
    }


/**
 * <A NAME="SD_GETGIVINGCLIENT"></A>
 * <EM>getGivingClient</EM>
 *
 * @return
 */

    private Client
    getGivingClient() {
        if (TokenServer_Debug) {
            debug("TokenServer: getGivingClient.");
        }

        return(givingClient);
    }


/**
 * <A NAME="SD_GETRECEIVINGCLIENT"></A>
 * <EM>getReceivingClient</EM>
 *
 * @return
 */

    private Client
    getReceivingClient() {
        if (TokenServer_Debug) {
            debug("TokenServer: getReceivingClient.");
        }

        return(receivingClient);
    }


/**
 * <A NAME="SD_GETTOKENEXCLUSIVE"></A>
 * <EM>getTokenExclusive</EM>
 *
 * @return
 */

    private boolean
    getTokenExclusive() {
        if (TokenServer_Debug) {
            debug("TokenServer: getTokenExclusive:" +
                  " token name: " + token.getName());
        }

        return(token.isExclusive);
    }


/**
 * <A NAME="SD_GETTOKENSTATUS"></A>
 * <EM>getTokenStatus</EM>
 *
 * @return
 */

    int
    getTokenStatus() {
        if (TokenServer_Debug) {
            debug("TokenServer: getTokenStatus:" +
                  " token name: " + token.getName());
        }

        return(token.status);
    }


/**
 * <A NAME="SD_GIVE"></A>
 * <EM>give</EM>
 *
 * @param message
 */

    void
    give(Message message) {
        DataInputStream  in                  = message.thread.dataIn;
        String           givingClientName    = null;
        String           receivingClientName = null;
        SocketThread     thread              = null;
        Client           receivingClient     = null;
        Client           givingClient        = null;
        int              retval              = 0;

        if (TokenServer_Debug) {
            debug("TokenServer: give:" +
                  " message: " + message);
        }

        try {
            givingClientName    = in.readUTF();
            receivingClientName = in.readUTF();
            thread              = getThreadForClient(receivingClientName);
            receivingClient     = getClientByName(receivingClientName);
            givingClient        = getClientByName(givingClientName);
        } catch (IOException e) {
            error("TokenServer: give: ", e);
        }

        if (givingClient == null || receivingClient == null || thread == null) {
            retval = JSDTException.NO_SUCH_CLIENT;
        } else if (((ClientImpl) givingClient).getCheck() != message.thread) {
            retval = JSDTException.PERMISSION_DENIED;
        } else if (grabbedClients.size() != 1 ||
                   getGrabbedClientByName(givingClientName) == null) {
            retval = JSDTException.PERMISSION_DENIED;
        }

        try {
            if (retval == 0) {
                CheckToken ct = new CheckToken(token, getTokenStatus());

                Util.startThread(ct,
                                 "CheckTokenThread:" + token.getName(), false);

                thread.writeMessageHeader(thread.dataOut,
                              message.sessionNo, message.id,
                              ClientImpl.M_Client, T_TokenGiven, false, true);
                thread.dataOut.writeUTF(name);
                thread.dataOut.writeUTF(receivingClientName);
                thread.flush();
                thread.finishMessage();

                setTokenStatus(Token.GIVING);
                setGivingClient(givingClient);
                setReceivingClient(receivingClient);
            }

            message.thread.writeMessageHeader(message.thread.dataOut,
                                message.sessionNo, message.id,
                                message.type, message.action, false, true);
            message.thread.dataOut.writeInt(retval);
            message.thread.dataOut.writeInt(getTokenStatus());
            message.thread.flush();
            message.thread.finishMessage();
        } catch (IOException e) {
            error("TokenServer: give: ", e);
        }

        if (retval == 0) {
            informListeners(session.getName(), givingClientName,
                            name, TokenEvent.GIVEN, message.type);
        }
    }


    void
    grab(Message message) {
        DataInputStream  in               = message.thread.dataIn;
        Client           client           = null;
        Client           givingClient     = null;
        boolean          sendReleaseEvent = false;
        boolean          exclusive        = false;
        int              eventType        = 0;
        int              retval           = 0;
        int              status           = 0;

        if (TokenServer_Debug) {
            debug("TokenServer: grab:" +
                  " message: " + message);
        }

        try {
            client    = getClientByName(in.readUTF());
            exclusive = in.readBoolean();
        } catch (IOException e) {
            error("TokenServer: grab: ", e);
        }

        if (client == null ||
            !((SessionServer) session.so).validClient(client)) {
            retval = JSDTException.NO_SUCH_CLIENT;
        }

        if (((ClientImpl) client).getCheck() != message.thread) {
            retval = JSDTException.PERMISSION_DENIED;
        }

        if (retval == 0) {
            if (getTokenStatus() == Token.NOT_IN_USE) {
                if (exclusive) {
                    setTokenExclusive(true);
                    setTokenStatus(Token.GRABBED);
                    eventType = TokenEvent.GRABBED;
                } else {
                    setTokenStatus(Token.INHIBITED);
                    eventType = TokenEvent.INHIBITED;
                }
                addGrabbedClient(client);
                status = getTokenStatus();
            } else if (getTokenStatus() == Token.INHIBITED) {
                if (!exclusive) {
                    if (!grabbedClientsContains(client)) {
                        addGrabbedClient(client);
                        eventType = TokenEvent.INHIBITED;
                        status = Token.INHIBITED;
                    }
                } else {
                    retval = JSDTException.PERMISSION_DENIED;
                    status = Token.ALREADY_INHIBITED;
                }
            } else if (getTokenStatus() == Token.GRABBED) {
                retval = JSDTException.PERMISSION_DENIED;
                status = Token.ALREADY_GRABBED;
            } else if (getTokenStatus() == Token.GIVING) {
                if (client.equals(getReceivingClient())) {
                    givingClient = getGivingClient();
                    removeGrabbedClient(givingClient);
                    sendReleaseEvent = true;

                    if (exclusive) {
                        setTokenExclusive(true);
                        setTokenStatus(Token.GRABBED);
                        eventType = TokenEvent.GRABBED;
                    } else {
                        setTokenStatus(Token.INHIBITED);
                        eventType = TokenEvent.INHIBITED;
                    }
                    addGrabbedClient(client);
                }
                status = getTokenStatus();
            }
        }

        try {
            message.thread.writeMessageHeader(message.thread.dataOut,
                                message.sessionNo, message.id,
                                message.type, message.action, false, true);
            message.thread.dataOut.writeInt(retval);
            message.thread.dataOut.writeInt(status);
            message.thread.flush();
            message.thread.finishMessage();
        } catch (IOException e) {
            error("TokenServer: grab: ", e);
        }

        if (retval == 0) {
            if (sendReleaseEvent) {
                informListeners(session.getName(), givingClient.getName(),
                                name, TokenEvent.RELEASED, message.type);
            }
            informListeners(session.getName(), client.getName(),
                            name, eventType, message.type);
        }
    }


/**
 * <A NAME="SD_GRABBEDCLIENTSCONTAINS"></A>
 * <EM>grabbedClientsContains</EM>
 *
 * @param client
 *
 * @return
 */

    private boolean
    grabbedClientsContains(Client client) {
        if (TokenServer_Debug) {
            debug("TokenServer: grabbedClientsContains:" +
                  " client name: " + client.getName());
        }

        return(grabbedClients.contains(client));
    }


/**
 * <A NAME="SD_GRABBEDCLIENTSSIZE"></A>
 * <EM>grabbedClientsSize</EM>
 *
 * @return
 */

    private int
    grabbedClientsSize() {
        if (TokenServer_Debug) {
            debug("TokenServer: grabbedClientsSize.");
        }

        return(grabbedClients.size());
    }


/**
 * <A NAME="SD_JOIN"></A>
 * <EM>join</EM>
 *
 * @param message
 */

    void
    join(Message message) {
        DataInputStream  in         = message.thread.dataIn;
        int              retval     = 0;
        String           clientName = null;
        ClientImpl       client     = null;

        if (TokenServer_Debug) {
            debug("TokenServer: join:" +
                  " message: " + message);
        }

        try {
            clientName = in.readUTF();
            client     = new ClientImpl(clientName, message.thread);
        } catch (IOException e) {
            error("TokenServer: join: ", e);
        }

        if (!((SessionServer) session.so).validClient(client)) {
            retval = JSDTException.NO_SUCH_CLIENT;
        } else {
            if (clients.containsKey(clientName)) {
                retval = JSDTException.NAME_IN_USE;
            } else {
                addJoinedClient(client);
            }
        }

        try {
            message.thread.writeMessageHeader(message.thread.dataOut,
                                message.sessionNo, message.id,
                                message.type, message.action, false, true);
            message.thread.dataOut.writeInt(retval);
            message.thread.flush();
            message.thread.finishMessage();
        } catch (IOException e) {
            error("TokenServer: join: ", e);
        }

        if (retval == 0) {
            informListeners(session.getName(), clientName,
                            name, TokenEvent.JOINED, TokenImpl.M_Token);
            addClientThreadConnection(clientName, message.thread);
        }
    }


/**
 * <A NAME="SD_EXPEL"></A>
 * <EM>expel</EM>
 *
 * @param message
 * @param objectType
 */

    protected void
    expel(Message message, char objectType) {
        if (TokenServer_Debug) {
            debug("TokenServer: expel:" +
                  " message: "     + message +
                  " object type: " + objectType);
        }

        super.expel(message, objectType, this);
    }


/**
 * <A NAME="SD_LEAVE"></A>
 * <EM>leave</EM> the given client is leaving this token. If the token had
 * been previous grabbed or inhibited, make sure it is released.
 *
 * @param message the current message being processed.
 * @param clientName the name of the client leaving this token.
 */

    protected void
    leave(Message message, String clientName) {
        ClientImpl client;

        if (TokenServer_Debug) {
            debug("TokenServer: leave:" +
                  " message: "     + message +
                  " client name: " + clientName);
        }

        if ((client = getClientByName(clientName)) != null) {
            removeGrabbedClient(client);
            removeJoinedClient(client);
        }
        super.leave(message, session.getName(), TokenImpl.M_Token, clientName);
    }


/**
 * <A NAME="SD_LISTHOLDERNAMES"></A>
 * <EM>listHolderNames</EM> "server-side" method that provides a list
 *  of all the clients who are currently holding this Token.
 *
 * @param message
 */

    protected void
    listHolderNames(Message message) {
        int retval = 0;

        if (TokenServer_Debug) {
            debug("TokenServer: listHolderNames:" +
                  " message: " + message);
        }

        try {
            message.thread.writeMessageHeader(message.thread.dataOut,
                                  message.sessionNo, message.id,
                                  message.type, message.action, false, true);
            message.thread.dataOut.writeInt(retval);
            message.thread.dataOut.writeInt(grabbedClients.size());

            for (Enumeration k = grabbedClients.keys(); k.hasMoreElements();) {
                message.thread.dataOut.writeUTF((String) k.nextElement());
            }

            message.thread.flush();
            message.thread.finishMessage();
        } catch (IOException e) {
            error("TokenServer: listHolderNames: ", e);
        }
    }


/**
 * <A NAME="SD_RELEASE"></A>
 * <EM>release</EM>
 *
 * @param message
 */

    void
    release(Message message) {
        DataInputStream  in         = message.thread.dataIn;
        String           clientName = null;
        int              retval     = 0;
        Client           client;

        if (TokenServer_Debug) {
            debug("TokenServer: release:" +
                  " message: " + message);
        }

        try {
            clientName = in.readUTF();
        } catch (IOException e) {
            error("TokenServer: release: ", e);
        }

        if ((client = getGrabbedClientByName(clientName)) == null) {
            retval = JSDTException.CLIENT_NOT_GRABBING;
        }

        if (retval == 0) {
            if (((ClientImpl) client).getCheck() != message.thread) {
                retval = JSDTException.PERMISSION_DENIED;
            }
        }

        if (retval == 0) {
            if (!((SessionServer) session.so).validClient(client)) {
                retval = JSDTException.NO_SUCH_CLIENT;
            }
        }

        if (retval == 0) {
            if (getTokenExclusive()) {
                if (removeGrabbedClient(client)) {
                    setTokenStatus(Token.NOT_IN_USE);
                } else {
                    retval = JSDTException.CLIENT_NOT_RELEASED;
                }
            } else {
                if (removeGrabbedClient(client)) {
                    if (grabbedClientsSize() == 0) {
                        setTokenStatus(Token.NOT_IN_USE);
                    } else {
                        setTokenStatus(Token.INHIBITED);
                    }
                } else {
                    retval = JSDTException.CLIENT_NOT_RELEASED;
                }
            }
        }

        try {
            message.thread.writeMessageHeader(message.thread.dataOut,
                                message.sessionNo, message.id,
                                message.type, message.action, false, true);
            message.thread.dataOut.writeInt(retval);
            message.thread.dataOut.writeInt(getTokenStatus());
            message.thread.flush();
            message.thread.finishMessage();
        } catch (IOException e) {
            error("TokenServer: release: ", e);
        }

        if (retval == 0) {
            informListeners(session.getName(), clientName,
                            name, TokenEvent.RELEASED, message.type);
        }
    }


/**
 * <A NAME="SD_REMOVEGRABBEDCLIENT"></A>
 * <EM>removeGrabbedClient</EM> remove this client from the hashtable of
 * clients currently grabbing/inhibiting this token.
 *
 * @param client the client to be removed.
 *
 * @return true if the client successfully removed; false otherwise.
 */

    private boolean
    removeGrabbedClient(Client client) {
        Object reply;

        if (TokenServer_Debug) {
            debug("TokenServer: removeGrabbedClient:" +
                  " client: " + client);
        }

        if ((reply = grabbedClients.remove(client.getName())) != null) {
            if (grabbedClients.size() != 0) {
                setTokenStatus(Token.INHIBITED);
            } else {
                setTokenStatus(Token.NOT_IN_USE);
            }
        }

        return(reply != null);
    }


/**
 * <A NAME="SD_REMOVEJOINEDCLIENT"></A>
 * <EM>removeJoinedClient</EM> remove this token from the vector of token
 * that this client has joined.
 *
 * @param client the client to be removed.
 */

    private void
    removeJoinedClient(ClientImpl client) {
        String        clientName   = client.getName();
        SessionServer ss           = (SessionServer) session.so.getServer();
        ClientImpl    ci           = ss.getClientByName(clientName);
        Vector        clientTokens = ci.getTokens();

        if (TokenServer_Debug) {
            debug("TokenServer: removeJoinedClient:" +
                  " client: " + client);
        }

        clientTokens.removeElement(token);
    }


/**
 * <A NAME="SD_REQUEST"></A>
 * <EM>request</EM>
 *
 * @param message
 */

    void
    request(Message message) {
        DataInputStream  in         = message.thread.dataIn;
        String           clientName = null;
        int              retval     = 0;
        ClientImpl       client;

        if (TokenServer_Debug) {
            debug("TokenServer: request:" +
                  " message: " + message);
        }

        try {
            clientName = in.readUTF();
        } catch (IOException e) {
            error("TokenServer: request: ", e);
        }

        client = getClientByName(clientName);
        if (client == null) {
            retval = JSDTException.NO_SUCH_CLIENT;
        }

        if (client.getCheck() != message.thread) {
            retval = JSDTException.PERMISSION_DENIED;
        }

        try {
            message.thread.writeMessageHeader(message.thread.dataOut,
                                message.sessionNo, message.id,
                                message.type, message.action, false, true);
            message.thread.dataOut.writeInt(retval);
            message.thread.dataOut.writeInt(getTokenStatus());
            message.thread.flush();
            message.thread.finishMessage();
        } catch (IOException e) {
            error("TokenServer: request: ", e);
        }

        if (retval == 0 && grabbedClients != null) {
            informListeners(session.getName(), clientName,
                            name, TokenEvent.REQUESTED, message.type);
        }
    }


/**
 * <A NAME="SD_SETGIVINGCLIENT"></A>
 * <EM>setGivingClient</EM>
 *
 * @param givingClient
 */

    private void
    setGivingClient(Client givingClient) {
        if (TokenServer_Debug) {
            debug("TokenServer: setGivingClient:" +
                  " giving client: " + givingClient);
        }

        this.givingClient = givingClient;
    }


/**
 * <A NAME="SD_SETRECEIVINGCLIENT"></A>
 * <EM>setReceivingClient</EM>
 *
 * @param receivingClient
 */

    private void
    setReceivingClient(Client receivingClient) {
        if (TokenServer_Debug) {
            debug("TokenServer: setReceivingClient:" +
                  " receiving client: " + receivingClient);
        }

        this.receivingClient = receivingClient;
    }


/**
 * <A NAME="SD_SETTOKENEXCLUSIVE"></A>
 * <EM>setTokenExclusive</EM>
 *
 * @param value
 */

    private void
    setTokenExclusive(boolean value) {
        if (TokenServer_Debug) {
            debug("TokenServer: setTokenExclusive:" +
                  " token name: " + token.getName() +
                  " new value: "  + value);
        }

        token.isExclusive = value;
    }


/**
 * <A NAME="SD_SETTOKENSTATUS"></A>
 * <EM>setTokenStatus</EM>
 *
 * @param value
 */

    void
    setTokenStatus(int value) {
        if (TokenServer_Debug) {
            debug("TokenServer: setTokenStatus:" +
                  " token name: " + token.getName() +
                  " new status: " + value);
        }

        token.status = value;
    }


/**
 * <A NAME="SD_TEST"></A>
 * <EM>test</EM>
 *
 * @param message
 */

    void
    test(Message message) {
        if (TokenServer_Debug) {
            debug("TokenServer: test:" +
                  " message: " + message);
        }

        try {
            message.thread.writeMessageHeader(message.thread.dataOut,
                                message.sessionNo, message.id,
                                message.type, message.action, false, true);
            message.thread.dataOut.writeInt(0);
            message.thread.dataOut.writeInt(getTokenStatus());
            message.thread.flush();
            message.thread.finishMessage();
        } catch (IOException e) {
            error("TokenServer: test: ", e);
        }
    }
}
