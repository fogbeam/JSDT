
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

package examples.stock;

import com.sun.media.jsdt.*;
import java.awt.*;
import java.awt.event.*;
import java.applet.Applet;
import java.net.InetAddress;
import java.util.*;

/**
 * Stock quote viewer.
 * Server uses www.marketwatch.com to get stock quote information.
 *
 * @version     2.3 - 21st November 2017
 * @author      Amith Yamasani
 * @author      Rich Burridge
 */

public class
StockViewer extends Applet implements ActionListener, StockDebugFlags {

    private StockViewerFrame frame = null;

    private Button       bnQuit, bnUpdate;
    private Label        timeLabel;
    private Panel        titlePanel = null;
    private TextField    tfAdd, tfRemove;

    private int commandPanelHeight;

    private Hashtable<String, StockPanel> panels = null;

    private String tickers = null;

    // Are we running as an application (as opposed to an applet)?
    private boolean isApplication = false;

    // The stock session that this client application will join.
    private Session     stockSession;

    // The stock channel that is used to request updates from the server.
    private Channel     stockChannel;

    // The client that will be joining the session and channel.
    private StockClient stockClient;

    // Indicates if the client is successfully connected to the server.
    private boolean connected = false;

// Default setup, will mostly be overriden by attributes.

    private int     width       = 800;
    private String  hostname    = "localhost";
    private int     hostport    = 4464;
    private String  sessionType = "socket";


    private void
    getOptions(String args[]) {
        int x      = 80;
        int y      = 0;
        int height = 35;

        if (StockViewer_Debug) {
            System.err.println("StockViewer: getArgs:");
            if (args != null) {
                for (int i = 0; i < args.length ; i++) {
                    System.err.println("args[" + i + "]: " + args[i]);
                }
            }
        }

        if (getArg(args, "x") != null) {
            x = Integer.parseInt(getArg(args, "x"));
        }

        if (getArg(args, "y") != null) {
            y = Integer.parseInt(getArg(args, "y"));
        }

        if (getArg(args, "width") != null) {
            width = Integer.parseInt(getArg(args, "width"));
        }

        if (getArg(args, "height") != null) {
            height = Integer.parseInt(getArg(args, "height"));
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

        if (getArg(args, "stocks") != null) {
            tickers = getArg(args, "stocks");
        }
    }


    private int
    rrange(int low, int high) {
        return((int)(Math.random() * (high - low + 1) + low));
    }


    private String
    getName(Object object) {
        int    n       = rrange(20, 100) * rrange(1, 10000);
        String newName = null;

        if (StockViewer_Debug) {
            System.err.println("StockViewer: getName.");
        }

        try {
            newName = "StockViewer" + '\t' + n +
                                      '\t' + InetAddress.getLocalHost() +
                                      '\t' + object.hashCode();
        } catch (Exception e) {
            System.err.println("StockViewer: getName: exception " + e);
            if (StockViewer_Debug) {
                e.printStackTrace();
            }
        }
        return(newName);
    }


    private void
    initialize() {
        Label label;
        Panel commandPanel;

        if (StockViewer_Debug) {
            System.err.println("StockViewer: init.");
        }

        panels = new Hashtable<>();

        setLayout(new GridLayout(0, 1));
        setForeground(Color.black);
        setBackground(Color.black);

        commandPanel = new Panel();
        commandPanel.setLayout(new GridLayout(1, 6));
        commandPanel.setForeground(Color.black);
        commandPanel.setBackground(Color.lightGray);

        commandPanel.add((bnUpdate = new Button("Update")));
        bnUpdate.addActionListener(this);
        bnUpdate.setBackground(Color.gray);

        label = new Label("Add:");
        label.setAlignment(Label.RIGHT);
        commandPanel.add(label);

        commandPanel.add((tfAdd = new TextField(6)));
        tfAdd.addActionListener(this);

        label = new Label("Remove:");
        label.setAlignment(Label.RIGHT);
        commandPanel.add(label);

        commandPanel.add((tfRemove = new TextField(6)));
        tfRemove.addActionListener(this);

        if (isApplication) {
            commandPanel.add((bnQuit = new Button("Quit")));
            bnQuit.addActionListener(this);
            bnQuit.setBackground(Color.gray);
        }

        add(commandPanel);
        setVisible(true);
        commandPanelHeight = 35;
        connect();
    }


    private String
    getArg(String args[], String arg) {
        if (StockViewer_Debug) {
            System.err.println("StockViewer: getArg:" +
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


    public int
    getWidth() {
        if (StockViewer_Debug) {
            System.err.println("StockViewer: getWidth.");
        }

        return(width);
    }


    public int
    getHeight() {
        int height = commandPanelHeight;

        if (StockViewer_Debug) {
            System.err.println("StockViewer: getHeight.");
        }

        height += (panels.size() * commandPanelHeight);
        if (panels.size() != 0) {
            height += commandPanelHeight;
        } else {
            height = 55;
        }
        return(height);
    }


    public synchronized void
    init() {
        if (StockViewer_Debug) {
            System.err.println("StockViewer: init.");
        }

        getOptions(null);
        initialize();
    }


    private void
    getNewData() {
        Data data = new Data("UPDATE");

        if (StockViewer_Debug) {
            System.err.println("StockViewer: getNewData.");
        }

        try {
            stockChannel.sendToClient(stockClient, "Server", data);
        } catch (NoSuchConsumerException nsc) {
        } catch (Exception e) {
            System.err.println("StockViewer: getNewData: exception: " + e);
            if (StockViewer_Debug) {
                e.printStackTrace();
            }
        }
    }


    public void
    actionPerformed(ActionEvent event) {
        Object source = event.getSource();

        if (StockViewer_Debug) {
            System.err.println("StockViewer: actionPerformed.");
        }

        if (source == bnUpdate) {
            getNewData();
        } else if (source == tfAdd) {
            addStockName(tfAdd.getText());
        } else if (source == tfRemove) {
            removeStockName(tfRemove.getText());
        } else if (source == bnQuit) {
            System.exit(0);
        }
    }


    private void
    addStockName(String name) {
        String capName;
        StockPanel p;

        if (StockViewer_Debug) {
            System.err.println("StockViewer: addStockName:" +
                                " name: " + name);
        }

        tfAdd.setText("");
        if (panels.size() == 0) {
            makeTitleBar();
        }

        capName = name.toUpperCase();
        if (!panels.containsKey(capName)) {
            p = new StockPanel(this, stockSession, stockClient,
                               timeLabel, capName, isApplication);
            add(p);

            panels.put(capName, p);
            getNewData();

            if (isApplication && frame != null) {
                frame.setSize(getWidth(), getHeight());
            }
        }

        validate();
    }


    private void
    removeStockName(String name) {
        StockPanel p;

        if (StockViewer_Debug) {
            System.err.println("StockViewer: removeStockName:" +
                                " name: " + name);
        }

        tfRemove.setText("");
        p = panels.remove(name.toUpperCase());
        if (p != null) {
            p.removeListener();
            remove(p);

            if (panels.size() == 0) {
                removeTitleBar();
            }

            if (isApplication && frame != null) {
                frame.setSize(getWidth(), getHeight());
            }
        }

        validate();
    }


    private void
    makeTitleBar() {
        Label label;

        if (StockViewer_Debug) {
            System.err.println("StockViewer: makeTitleBar.");
        }

        if (titlePanel == null) {
            titlePanel = new Panel();
            titlePanel.setLayout(null);
            titlePanel.setBackground(Color.gray);

            label = new Label("Symbol");
            titlePanel.add(label);
            label.setSize(100, 30);
            label.setLocation(0, 0);

            label = new Label("Value");
            titlePanel.add(label);
            label.setSize(100, 30);
            label.setLocation(110, 0);

            label = new Label("Change");
            titlePanel.add(label);
            label.setSize(100, 30);
            label.setLocation(220, 0);

            label = new Label("Percent Change");
            titlePanel.add(label);
            label.setSize(120, 30);
            label.setLocation(330, 0);

            timeLabel = new Label(" ");
            titlePanel.add(timeLabel);
            timeLabel.setSize(175, 30);
            timeLabel.setLocation(getWidth() - 175, 0);
            timeLabel.setBackground(Color.black);
            timeLabel.setForeground(Color.yellow);

            titlePanel.setSize(getWidth(), commandPanelHeight);
        }
        add(titlePanel);
    }


    private void
    removeTitleBar() {
        if (StockViewer_Debug) {
            System.err.println("StockViewer: removeTitleBar.");
        }

        remove(titlePanel);
    }


    private void
    connect() {
        if (StockViewer_Debug) {
            System.err.println("StockViewer: connect.");
        }

        if (connected) {
            return;
        }

        try {
            String sessionName = "StockSession";

            stockClient = new StockClient(getName(this));
            try {
                URLString url = URLString.createSessionURL(hostname, hostport,
                                                    sessionType, sessionName);

                stockSession = SessionFactory.createSession(stockClient,
                                                            url, true);
                stockChannel = stockSession.createChannel(stockClient,
                                            "StockChannel", true, true, true);

                makeTickerList();
                connected = true;
            } catch (Exception e) {
                System.err.print("Caught exception in ");
                System.err.println("StockViewer.connect: " + e);
                if (StockViewer_Debug) {
                    e.printStackTrace();
                }
            }
        } catch (Throwable th) {
            System.err.println("StockViewer: connect caught: " + th);
            if (StockViewer_Debug) {
                th.printStackTrace();
            }
            throw new Error("SetClient.start failed : " + th);
        }
    }


    void
    disconnect() {
        if (StockViewer_Debug) {
            System.err.println("StockViewer: disconnect.");
        }

        if (!connected) {
            return;
        }

        try {
            stockSession.leave(stockClient);
        } catch (Exception e) {
            System.err.println("Caught exception while trying to " +
                                "leave the stock session: " + e);
            if (StockViewer_Debug) {
                e.printStackTrace();
            }
        }

        try {
            stockSession.close(true);
            connected = false;
        } catch (Exception e) {
            System.err.println("Caught exception while trying to " +
                                "close the session: " + e);
            if (StockViewer_Debug) {
                e.printStackTrace();
            }
        }
    }


    public void
    destroy() {
        if (StockViewer_Debug) {
            System.err.println("StockViewer: destroy.");
        }

        disconnect();
    }


    private String
    getNextWord(String s) {
        String out = "";
        int i = s.indexOf("+");

        if (StockViewer_Debug) {
            System.err.println("StockViewer: getNextWord:" +
                                " s: " + s);
        }

        if (i <= 5 && i > 2) {
            out = s.substring(0, i);
        }
        return(out);
    }


    private void
    makeTickerList() {
        String ticker;

        if (StockViewer_Debug) {
            System.err.println("StockViewer: makeTickerList.");
        }

        if (tickers == null) {
            return;
        }

        ticker = tickers + "%2C";
        while (ticker.length() > 2) {
            String t = getNextWord(ticker);

            if (t.length() > 2) {
                if (StockViewer_Debug) {
                    System.err.println("StockViewer: makeTickerList:" +
                                        " adding: " + t.toUpperCase());
                }

                addStockName(t);
                ticker = ticker.substring(t.length() + 1, ticker.length());
            } else {
                return;
            }
        }
    }


    public static void
    main(String args[]) {
        StockViewer      stockViewer = new StockViewer();

        if (StockViewer_Debug) {
            System.err.println("StockViewer: main:");
            for (int i = 0; i < args.length ; i++) {
                System.err.println("args[" + i + "]: " + args[i]);
            }
        }

        stockViewer.isApplication = true;
        stockViewer.getOptions(args);
        stockViewer.initialize();
        stockViewer.frame = new StockViewerFrame(stockViewer);
    }
}


class
StockViewerFrame extends Frame
              implements ComponentListener, WindowListener, StockDebugFlags {

    private final StockViewer stockViewer;

    StockViewerFrame(StockViewer stockViewer) {
        if (StockViewerFrame_Debug) {
            System.err.println("StockViewerFrame: constructor.");
        }

        this.stockViewer = stockViewer;

        setTitle("Stock User");
        add(stockViewer);
        addComponentListener(this);
        addWindowListener(this);
        setSize(stockViewer.getWidth(), stockViewer.getHeight());
        setVisible(true);
    }


    public void componentMoved(ComponentEvent event) {}

    public void componentShown(ComponentEvent event) {}

    public void componentHidden(ComponentEvent event) {}

    public void
    componentResized(ComponentEvent event) {
        if (StockViewerFrame_Debug) {
            System.err.println("StockViewerFrame: componentResized.");
        }

        validate();
    }


    public void windowClosed(WindowEvent event) {}

    public void windowDeiconified(WindowEvent event) {}

    public void windowIconified(WindowEvent event) {}

    public void windowActivated(WindowEvent event) {}

    public void windowDeactivated(WindowEvent event) {}

    public void windowOpened(WindowEvent event) {}

    public void
    windowClosing(WindowEvent event) {
        if (StockViewerFrame_Debug) {
            System.err.println("StockViewerFrame: windowClosing.");
        }

        stockViewer.disconnect();
        System.exit(0);
    }
}
