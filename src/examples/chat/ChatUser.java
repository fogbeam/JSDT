
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

package examples.chat;

import com.sun.media.jsdt.*;
import java.awt.*;
import java.awt.event.*;
import java.applet.Applet;

/**
 * This is just a simple server/client based proof-of-concept
 * implementation to make sure that the Shared Data classes have
 * all the required API in them.
 *
 * It's based on the chat demonstration which is part of the
 * west-coast rmi distribution.
 *
 * @version     2.3 - 20th November 2017
 * @author      Rich Burridge
 */

public class
ChatUser extends Applet implements ActionListener, ChatDebugFlags {
    private Label     setNameLabel;
    private Label     sayLabel;
    private Button    signOffButton;
    private TextField nameField;
    private TextField typeField;
    private TextArea  messageArea;

    // Are we running as an application (as opposed to an applet)?
    private boolean isApplication = false;

    // The name of this Client.
    protected String name;

    // The chat session that this client application will join.
    private Session session;

    // The client that will be joining the session and channel.
    private ChatClient client;

    // The channel that this client application will use to send data.
    private Channel channel;

    // Indicates if the client is successfully connected to the server.
    private boolean connected = false;

// Default setup, will mostly be overriden by attributes.

    int             width       = 250;
    int             height      = 250;
    private String  hostname    = "localhost";
    private int     hostport    = 4461;
    private String  sessionType = "socket";


    private void
    getOptions(String args[]) {
        if (ChatUser_Debug) {
            System.err.println("ChatUser: getOptions:");
            if (args != null) {
                for (int i = 0; i < args.length ; i++) {
                    System.err.println("args[" + i + "]: " + args[i]);
                }
            }
        }

        if (getArg(args, "width") != null) {
            width = Integer.parseInt(getArg(args, "width"));
        } else {
            width = 250;
        }

        if (getArg(args, "height") != null) {
            height = Integer.parseInt(getArg(args, "height"));
        } else {
            height = 250;
        }

        if (getArg(args, "server") != null) {
            hostname = getArg(args, "server");
        }

        if (getArg(args, "port") != null) {
            hostport = Integer.parseInt(getArg(args, "port"));
        }

        if (getArg(args, "type") != null) {
            sessionType = getArg(args, "type");
        }
    }


    private String
    getArg(String args[], String arg) {
        if (ChatUser_Debug) {
            System.err.println("ChatUser: getArg:" +
                                " arg: " + arg);
        }

        if (isApplication) {
            String option = "-" + arg;
            String retval = null;

            for (int i = 0 ; i < args.length ; i++) {
                if (args[i].equals(option)) {
                    if (++i < args.length) {
                        retval = args[i];
                    }
                    break;
                }
            }
            return(retval);
        } else {
            return(getParameter(arg));
        }
    }


    public void
    init() {
        if (ChatUser_Debug) {
            System.err.println("ChatUser: init.");
        }

        getOptions(null);
        initialize();
    }


    private void
    initialize() {
        Panel p;
        Font  lbl = new Font("Helvetica", Font.BOLD, 14);

        setLayout(new BorderLayout());

        p = new Panel();
        p.setLayout(new BorderLayout());
        p.add("West",   (setNameLabel  = new Label("Set Name")));
        p.add("Center", (nameField     = new TextField(20)));
        p.add("East",   (signOffButton = new Button("SignOff")));
        nameField.setFont(lbl);
        nameField.addActionListener(this);
        signOffButton.setEnabled(false);
        signOffButton.addActionListener(this);
        add("North", p);

        p = new Panel();
        p.add(new Label("Messages: "));
        p.add(messageArea = new TextArea(6, 30));
        add("Center", p);

        p = new Panel();
        p.add(sayLabel = new Label("Say"));
        sayLabel.setEnabled(false);
        p.add(typeField = new TextField(20));
        typeField.addActionListener(this);
        add("South", p);

        resize(width, height);
        setVisible(true);
    }


    private void
    connect() {
        String    sessionName   = "ChatSession";
        boolean   sessionExists = false;
        URLString url;

        // The consumer of all data sent over the channel.
        ChatConsumer chatConsumer;

        if (ChatUser_Debug) {
            System.err.println("ChatUser: connect.");
        }

        if (name == null) {
            throw new Error("need a non-null name to connect");
        }

        if (connected) {
            return;
        }

        try {
            try {
                url = URLString.createSessionURL(hostname, hostport,
                                                 sessionType, sessionName);
                System.out.println("ChatUser: connect: checking: url: " + url);

                while (!sessionExists) {
                    try {
                        if (SessionFactory.sessionExists(url)) {
                            System.out.println("ChatUser: connect:" +
                                               " found Session.");
                            sessionExists = true;
                        }
                    } catch (NoRegistryException nre) {
                        System.out.println("ChatUser: connect:" +
                                           " no registry: sleeping.");
                        Thread.sleep(1000);
                    } catch (ConnectionException ce) {
                        System.out.println("ChatUser: connect:" +
                                           " connection exception: sleeping.");
                        Thread.sleep(1000);
                    }
                }

// Create a chat client.

                System.err.println("Creating a ChatMember...");
                client = new ChatClient(name);

// Resolve the chat session.

                session = SessionFactory.createSession(client, url, true);
                channel = session.createChannel(client, "ChatChannel",
                                                true, true, true);
                chatConsumer = new ChatConsumer(client.getName(), messageArea);
                channel.addConsumer(client, chatConsumer);

                connected = true;
                nameField.setEditable(false);
                setNameLabel.setEnabled(false);
                signOffButton.setEnabled(true);
                sayLabel.setEnabled(true);
                repaint();
            } catch (Exception e) {
                System.err.print("Caught exception in ");
                System.err.println("ChatUser.connect: " + e);
                if (ChatUser_Debug) {
                    e.printStackTrace();
                }
            }
        } catch (Throwable th) {
            System.err.println("ChatUser: connect caught: " + th);
            if (ChatUser_Debug) {
                th.printStackTrace();
            }
            throw new Error("ChatUser.connect failed : " + th);
        }
    }


//  Convert the user's message to bytes and send it over the channel.

    private void
    writeLine(String message) {

        // The data that will be sent over the channel.
        Data data;

        if (ChatUser_Debug) {
            System.err.println("ChatUser: writeLine: " + message);
        }

        if (!connected) {
            System.out.println("ChatUser: writeLine: reconnecting...");
            connect();
        }

        try {
            data = new Data(message);
            data.setPriority(Channel.HIGH_PRIORITY);
            channel.sendToAll(client, data);
        } catch (ConnectionException | TimedOutException ce) {
            System.out.println("ChatUser: writeLine: exception: " + ce);
            System.out.println("ChatUser: writeLine: disconnecting...");
            disconnect();
        } catch (Exception e) {
            System.err.print("Caught exception in ");
            System.err.println("ChatUser.writeLine(): " + e);
            if (ChatUser_Debug) {
                e.printStackTrace();
            }
        }
    }


    void
    disconnect() {
        if (ChatUser_Debug) {
            System.err.println("ChatUser: disconnect.");
        }

        if (!connected) {
            return;
        }

        try {
            session.close(true);
        } catch (Exception e) {
            System.err.println("Caught exception while trying to " +
                                "disconnect from chat server: " + e);
            if (ChatUser_Debug) {
                e.printStackTrace();
            }
        }

        connected = false;
        nameField.setEditable(true);
        setNameLabel.setEnabled(true);
        signOffButton.setEnabled(false);
        sayLabel.setEnabled(false);
    }


    public void
    destroy() {
        if (ChatUser_Debug) {
            System.err.println("ChatUser: destroy.");
        }

        disconnect();
    }


    public void
    actionPerformed(ActionEvent event) {
        Object source = event.getSource();

        if (ChatUser_Debug) {
            System.err.println("ChatUser: action.");
        }

        if (source == nameField) {

// Set the name.

            name = nameField.getText();
            if (name == null) {
                nameField.setText("Need to give a name before \"Set Name\"");
            } else {
                connect();
            }
        } else if (source == signOffButton) {
            disconnect();
        } else if (source == typeField) {

// Say the message field info to all chat users.

            if (session != null) {
                try {
                    writeLine(typeField.getText());
                } catch (Exception e) {
                    System.err.println("Exception while invoking Say: " + e);
                    if (ChatUser_Debug) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    public static void
    main(String args[]) {
        ChatUser chatUser = new ChatUser();

        if (ChatUser_Debug) {
            System.err.println("ChatUser: main.");
        }

        chatUser.isApplication = true;
        chatUser.getOptions(args);
        chatUser.initialize();
        new ChatUserFrame(chatUser);
    }
}


class
ChatUserFrame extends Frame implements WindowListener, ChatDebugFlags {

    private final ChatUser chatUser;

    ChatUserFrame(ChatUser chatUser) {
        if (ChatUserFrame_Debug) {
            System.err.println("ChatUserFrame: constructor.");
        }

        this.chatUser = chatUser;

        setTitle("Chat User");
        add(chatUser);
        addWindowListener(this);
        pack();
        setSize(chatUser.width, chatUser.height);
        setVisible(true);
    }


    public void windowClosed(WindowEvent event) {}

    public void windowDeiconified(WindowEvent event) {}

    public void windowIconified(WindowEvent event) {}

    public void windowActivated(WindowEvent event) {}

    public void windowDeactivated(WindowEvent event) {}

    public void windowOpened(WindowEvent event) {}

    public void
    windowClosing(WindowEvent event) {
        if (ChatUserFrame_Debug) {
            System.err.println("ChatUserFrame: windowClosing.");
        }

        chatUser.disconnect();
        System.exit(0);
    }
}
