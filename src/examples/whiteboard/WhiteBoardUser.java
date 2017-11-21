
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

package examples.whiteboard;

import com.sun.media.jsdt.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.applet.Applet;
import java.net.*;

/**
 * This is just a simple server/client based proof-of-concept
 * implementation to make sure that the Shared Data classes have
 * all the required API in them.
 *
 * It's based on WhiteBoard; a test applet for the game server from
 * daniel@vpro.nl. It's been rewritten to use JSDT and to make it
 * easier to modify and maintain.
 *
 * @version     2.3 - 21st November 2017
 * @author      daniel@vpro.nl
 * @author      Rich Burridge
 */

public class
WhiteBoardUser extends Applet implements WhiteBoardDebugFlags {

    // Are we running as an application (as opposed to an applet)?
    private boolean isApplication = false;

    // The name of this User.
    protected String name;

    // The unique number associated with this client.
    private int clientNo;

    // The whiteboard session that this client application will join.
    private Session session;

    // The client that will be joining the session and channel.
    private WhiteBoardClient client;

    // The channel that this client application will use to send data.
    private Channel channel;

    // Indicates if the client is successfully connected to the server.
    private boolean connected = false;

    // The white board drawing area.
    DrawingArea drawingArea;

    // The white board menu controls.
    Controls controls;

// Default setup, will mostly be overriden by attributes.

    int             width       = 600;
    int             height      = 250;
    private String  hostname    = "localhost";
    private int     hostport    = 4466;
    private String  sessionType = "socket";
    String          commandLine = null;

    Hashtable<Integer, Image> brushes   = null;

    int     pointx1     = 0;
    int     pointy1     = 0;
    int     coll_offset = 0;
    int     bs_offset   = 0;
    boolean textMode    = false;
    String  textLine    = "";
    int     drawType    = 0;

    final Image  bs[] = new Image[40];


    private void
    getOptions(String args[]) {
        if (WhiteBoardUser_Debug) {
            System.err.println("WhiteBoardUser: getOptions:");
            if (args != null) {
                for (int i = 0; i < args.length ; i++) {
                    System.err.println("args[" + i + "]: " + args[i]);
                }
            }
        }

        if (getArg(args, "width") != null) {
            width = Integer.parseInt(getArg(args, "width"));
        } else {
            width = 600;
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
        if (WhiteBoardUser_Debug) {
            System.err.println("WhiteBoardUser: getArg:" +
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


    public synchronized void
    init() {
        if (WhiteBoardUser_Debug) {
            System.err.println("WhiteBoardUser: init.");
        }

        getOptions(null);
        initialize();
    }


    private void
    initialize() {
        URL docBase = null;

        try {
            if (!isApplication) {
                docBase = getDocumentBase();
            } else {
                docBase = WhiteBoardUser.class.getResource("");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        getImages(docBase, 0,  8, "black");   // Get needed brush pictures.
        getImages(docBase, 8,  8, "red");
        getImages(docBase, 16, 8, "green");
        getImages(docBase, 24, 8, "blue");
        getImages(docBase, 32, 8, "yellow");

        setLayout(new BorderLayout());
        drawingArea = new DrawingArea(width, height - 80, this);
        add("Center", drawingArea);
        controls = new Controls(width, 80, this);
        add("South", controls);
        resize(width, height);
        setVisible(true);

        name = "WhiteBoardUser" + ":" + (rrange(0, 1000) * rrange(0, 1000));
        connect();
        brushes = new Hashtable<>();
        brushes.put(clientNo, bs[0]);
    }


    private void
    getImages(URL docBase, int position, int count, String name) {
        int i;
        MediaTracker tracker = new MediaTracker(this);

        if (WhiteBoardUser_Debug) {
            System.err.println("WhiteBoardUser: getImage: " + name);
        }

        for (i = 0; i < count; i++) {
            String imageName = "../../images/" + name + (i+1) + ".gif";

            if (docBase == null) {
                bs[position+i] = getToolkit().getImage(imageName);
            } else {
                try {
                    bs[position+i] = getToolkit().getImage(
                                                new URL(docBase, imageName));
                } catch (MalformedURLException me) {
                }
            }
            tracker.addImage(bs[position+i], i);
        }

        if (WhiteBoardUser_Debug) {
            System.err.println("Waiting for images to be loaded.");
        }
        try {
            tracker.waitForAll();
        } catch (InterruptedException e) { }
    }


// Generate a random number between low and high.

    private int
    rrange(int low, int high) {
        return((int)(Math.random() * (high - low + 1) + low));
    }


    public void
    writeLine(String line) {
        String message = clientNo + " " + line;

        if (WhiteBoardUser_Debug) {
            System.err.println("WhiteBoardUser: writeLine: " + message);
        }

        if (connected) {
            try {
                // The data that will be sent over the channel.
                Data data = new Data(message);

                data.setPriority(Channel.HIGH_PRIORITY);
                channel.sendToAll(client, data);
            } catch (Exception e) {
                System.err.print("Caught exception in ");
                System.err.println("WhiteBoardUser.writeLine(): " + e);
                if (WhiteBoardUser_Debug) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void
    connect() {
        if (WhiteBoardUser_Debug) {
            System.err.println("WhiteBoardUser: connect.");
        }

        if (connected) {
            return;
        }

        try {
            String sessionName = "WBSession";

// Create a whiteboard client.

            System.err.println("Creating a WhiteBoard Client...");
            client = new WhiteBoardClient(name);

// Resolve the whiteboard session.

            try {
                // Names of clients that are currently joined to this session.
                String clientNames[];

                // The consumer of all data sent over the channel.
                WBConsumer wbConsumer;

                URLString url = URLString.createSessionURL(hostname, hostport,
                                                    sessionType, sessionName);

                session = SessionFactory.createSession(client, url, true);

/*  Need to setup a unique number for this client. A simple cheap way is to
 *  determine how many clients are joined to the session, and use that.
 */

                clientNames = session.listClientNames();
                clientNo    = clientNames.length;

                channel = session.createChannel(client, "WBChannel",
                                                true, true, true);
                wbConsumer = new WBConsumer(client.getName(), this);
                channel.addConsumer(client, wbConsumer);
                connected = true;
            } catch (Exception e) {
                System.err.print("Caught exception in ");
                System.err.println("WhiteBoardUser.connect: " + e);
                if (WhiteBoardUser_Debug) {
                    e.printStackTrace();
                }
            }
        } catch (Throwable th) {
            System.err.println("WhiteBoardUser: connect caught: " + th);
            if (WhiteBoardUser_Debug) {
                th.printStackTrace();
            }
            throw new Error("WhiteBoardUser: connect: failed : " + th);
        }
    }


    void
    disconnect() {
        if (WhiteBoardUser_Debug) {
            System.err.println("WhiteBoardUser: disconnect.");
        }

        if (!connected) {
            return;
        }

        try {

// Leave the channel and leave the session.

            channel.leave(client);
            session.leave(client);
        } catch (Exception e) {
            System.err.print("Caught exception while trying to ");
            System.err.println("leave the whiteboard channel and session: " + e);
            if (WhiteBoardUser_Debug) {
                e.printStackTrace();
            }
        }

// Close the session.

        try {
            session.close(true);
            connected = false;
        } catch (Exception e) {
            System.err.println("Caught exception while trying to " +
                                "close the session: " + e);
            if (WhiteBoardUser_Debug) {
                e.printStackTrace();
            }
        }
    }


    public void
    destroy() {
        if (WhiteBoardUser_Debug) {
            System.err.println("WhiteBoardUser: destroy.");
        }

        disconnect();
    }


    public boolean
    handleKey(int key) {
        char keyChar = (char) (key & 0xFF);
        String colorline;

        if (WhiteBoardUser_Debug) {
            System.err.println("WhiteBoardUser: keyDown: " + key);
        }

        if (textMode) {
            if (keyChar == 8) {
                int offset = textLine.length() - 1;

                if (offset != -1) {
                    textLine = textLine.substring(0, offset);
                }
            }
            else if (keyChar != '\n' && keyChar != '\r') {
                Character t = keyChar;

                textLine = textLine + t.toString();
            } else {
                textLine = textLine.replace(' ', '|');
                colorline = "BLACK";
                if (coll_offset == 0) {
                    colorline = "BLACK";
                } else if (coll_offset == 8) {
                    colorline = "RED";
                } else if (coll_offset == 16) {
                    colorline = "GREEN";
                } else if (coll_offset == 24) {
                    colorline = "BLUE";
                } else if (coll_offset == 32) {
                    colorline = "YELLOW";
                }
                writeLine("TEXT " + colorline + " " + textLine + " " +
                          pointx1 + " " + pointy1 + "\n");
                textLine = "";
                pointx1 = 0;
                textMode = false;
            }
        }

        controls.repaint();
        drawingArea.repaint();
        return(true);
    }


    public static void
    main(String args[]) {
        WhiteBoardUser      whiteboardUser = new WhiteBoardUser();

        if (WhiteBoardUser_Debug) {
            System.err.println("WhiteBoardUser: main.");
        }

        whiteboardUser.isApplication = true;
        whiteboardUser.getOptions(args);
        whiteboardUser.initialize();
        new WhiteBoardUserFrame(whiteboardUser);
    }
}


class
WhiteBoardUserFrame extends Frame
                    implements WindowListener, WhiteBoardDebugFlags {

    private final WhiteBoardUser whiteboardUser;

    WhiteBoardUserFrame(WhiteBoardUser whiteboardUser) {
        if (WhiteBoardUserFrame_Debug) {
            System.err.println("WhiteBoardUserFrame: constructor.");
        }

        this.whiteboardUser = whiteboardUser;

        setTitle("WhiteBoard User");
        add(whiteboardUser);
        addWindowListener(this);
        pack();
        setSize(whiteboardUser.width, whiteboardUser.height);
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
        if (WhiteBoardUserFrame_Debug) {
            System.err.println("WhiteBoardUserFrame: windowClosing.");
        }

        whiteboardUser.disconnect();
        System.exit(0);
    }
}
