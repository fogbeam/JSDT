
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

package examples.phone;

import com.sun.media.jsdt.*;
import com.sun.media.jsdt.event.*;
import java.awt.*;
import java.awt.event.*;
import java.applet.Applet;
import javax.sound.sampled.*;

/**
 * Simple Internet Phone.
 *
 * @version     2.3 - 20th November 2017
 * @author      Rich Burridge
 * @author      Pete Boysen
 */

public class
PhoneUser extends Applet
          implements ActionListener, ChannelListener, ItemListener,
                     PhoneDebugFlags, Runnable {

    private boolean        isBig;
    private TargetDataLine inputChannel;
    private SourceDataLine outputChannel;
    private AudioFormat    inputFormat      = null;
    private final int      inputBufferSize  = 4096;
    private AudioFormat    outputFormat     = null;
    private final int      outputBufferSize = inputBufferSize * 4;
    private final byte[]   data             = new byte[4096];

    private Label     setNameLabel;
    private Button    signOffButton;
    private TextField nameField;
    private List      list;
    private Checkbox inputBox, outputBox;

    // Are we running as an application (as opposed to an applet)?
    private boolean isApplication = false;

    // The name of this Client.
    protected String name;

    // The phone session that this client application will join.
    private Session session;

    // The client that will be joining the session and channel.
    private PhoneClient client;

    // The channel that this client application will use to send data.
    private com.sun.media.jsdt.Channel channel;

    // Indicates if the client is successfully connected to the server.
    private boolean connected = false;

// Default setup, will mostly be overriden by attributes.

    private boolean hasOutput = true;
    int     width     = 250;
    int     height    = 200;

    private String  hostname    = "localhost";
    private int     hostport    = 4469;
    private String  sessionType = "socket";
    private String  sessionName = "PhoneSession";
    private boolean hasInput    = true;


    private void
    getOptions(String args[]) {
        if (PhoneUser_Debug) {
            System.err.println("PhoneUser: getOptions:");
            for (int i = 0; i < args.length ; i++) {
                System.err.println("args[" + i + "]: " + args[i]);
            }
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

        if (getArg(args, "session") != null) {
            sessionName = getArg(args, "session");
        }

        if (getArg(args, "microphone") != null) {
            hasInput = getArg(args, "microphone").equals("yes");
        }

        if (getArg(args, "speakers") != null) {
            hasOutput = getArg(args, "speakers").equals("yes");
        }
    }


    private String
    getArg(String args[], String arg) {
        if (PhoneUser_Debug) {
            System.err.println("PhoneUser: getArg:" +
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
        if (PhoneUser_Debug) {
            System.err.println("PhoneUser: init.");
        }

        getOptions(null);
        initialize();
    }


    private void
    initialize() {
        Panel p;
        Font  lbl = new Font("Helvetica", Font.BOLD, 14);

        initPhone();
        setLayout(new BorderLayout());
        setBackground(Color.lightGray);

        p = new Panel();
        p.setLayout(new BorderLayout());
        p.add("West",   (setNameLabel  = new Label("Set Name")));
        p.add("Center", (nameField     = new TextField(20)));
        p.add("East",   (signOffButton = new Button("Disconnect")));
        nameField.setFont(lbl);
        nameField.setBackground(Color.white);
        nameField.addActionListener(this);
        signOffButton.setEnabled(false);
        signOffButton.addActionListener(this);
        add("North", p);

        p = new Panel();
        p.add(new Label("Callers: "));
        p.add(list = new List(5));
        list.setBackground(Color.white);
        add("Center", p);

        p = new Panel();
        p.add(new Label("Microphone"));
        p.add(inputBox = new Checkbox());
        inputBox.setForeground((hasInput ? Color.green : Color.red));
        inputBox.setState(hasInput);
        inputBox.addItemListener(this);

        p.add(new Label("Speakers"));
        p.add(outputBox = new Checkbox());
        outputBox.setForeground((hasOutput ? Color.green : Color.red));
        outputBox.setState(hasOutput);
        outputBox.addItemListener(this);
        add("South", p);

        resize(width, height);
        setVisible(true);
    }


    private void
    initPhone() {
        Thread inputThread;

        if (PhoneUser_Debug) {
            System.err.println("PhoneUser: initPhone.");
        }

/* Phone optimization - (Pete Boysen - 7th December 1999):
 *
 * I found these settings to work best on my system especially if you want to
 * transmit across 28.8K lines.  Note that choosing 8K for a frequency doesn't
 * work well because it causes distortion resulting from JavaSound up-sampling
 * 8K to 22K.
 */

        inputFormat = new AudioFormat(11025.0f, 8, 1, true, false);

//        inputFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
//                                      22050.0f, 16, 2, 4, 22050.0f,
//                                      false);

        outputFormat = inputFormat;

        if (hasInput) {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class,
                inputFormat);
            try {
                inputChannel = (TargetDataLine) AudioSystem.getLine(info);
                inputChannel.open(inputFormat, inputBufferSize);
                isBig = inputChannel.getFormat().isBigEndian();
                inputChannel.start();
                if (PhoneUser_Debug) {
                    System.err.println("got input channel!");
                }
            } catch(LineUnavailableException e) {
                   if (PhoneUser_Debug) {
                    System.err.println("PhoneUser: initPhone:" +
                                " unable to get input channel.");
                }
            }
        }

        if (hasOutput) {
            try {
                outputChannel = getOutputChannel();
                outputChannel.open(outputFormat, outputBufferSize);
                outputChannel.start();
                if (PhoneUser_Debug) {
                    System.err.println("got output channel!");
                }
            } catch (LineUnavailableException e) {
                if (PhoneUser_Debug) {
                    System.err.println("PhoneUser: initPhone:" +
                                       " unable to get output channel.");
                }
            }
        }

        inputThread = new Thread(this);
        inputThread.start();
    }


    private SourceDataLine
    getOutputChannel() throws LineUnavailableException {
           DataLine.Info info = new DataLine.Info(SourceDataLine.class,
                                               outputFormat);

        return((SourceDataLine) AudioSystem.getLine(info));
    }


    private TargetDataLine
    getInputChannel() throws LineUnavailableException {
           DataLine.Info info = new DataLine.Info(TargetDataLine.class,
                                               inputFormat);

        return((TargetDataLine) AudioSystem.getLine(info));
    }


    private void
    connect() {
        if (PhoneUser_Debug) {
            System.err.println("PhoneUser: connect.");
        }

        if (name == null) {
            throw new Error("need a non-null name to connect");
        }

        if (connected) {
            return;
        }

        try {
            // The consumer of all data sent over the channel.
            PhoneConsumer phoneConsumer;

            System.err.println("Creating a PhoneMember...");
            client = new PhoneClient(name);

            try {
                URLString url = URLString.createSessionURL(hostname, hostport,
                                                     sessionType, sessionName);

                session = SessionFactory.createSession(client, url, true);
                channel = session.createChannel(client, "PhoneChannel",
                                                false, true, true);
                channel.addChannelListener(this);

                String[] consumers = channel.listConsumerNames();
                for (int i = 0; i < consumers.length; i++) {
                    list.add(consumers[i]);
                }

                phoneConsumer = new PhoneConsumer(client.getName(),
                                          outputChannel, isBig);
                channel.addConsumer(client, phoneConsumer);

                repaint();
                connected = true;
            } catch (Exception e) {
                System.err.print("Caught exception in ");
                System.err.println("PhoneUser.connect: " + e);
                if (PhoneUser_Debug) {
                    e.printStackTrace();
                }
            }
        } catch (Throwable th) {
            System.err.println("PhoneUser: connect caught: " + th);
            if (PhoneUser_Debug) {
                th.printStackTrace();
            }
            throw new Error("SetClient.start failed : " + th);
        }
    }


    private void
    sendInput(boolean isBig, int length, byte inputData[]) {
        if (PhoneUser_Debug) {
            System.err.println("PhoneUser: sendInput:" +
                                " big endian: " + isBig +
                                " length: "     + length +
                                " input data: " + inputData);
        }

        if (connected) {
            try {
                SoundPacket soundPacket = new SoundPacket(isBig,
                                                          length, inputData);
                channel.sendToOthers(client, new Data(soundPacket));
            } catch (Exception e) {
                System.err.println("PhoneUser: sendInput: exception: " + e);
                if (PhoneUser_Debug) {
                    e.printStackTrace();
                }
            }
        }
    }


    void
    disconnect() {
        if (PhoneUser_Debug) {
            System.err.println("PhoneUser: disconnect.");
        }

        if (!connected) {
            return;
        }

        try {
            connected = false;
            channel.leave(client);
            session.leave(client);
        } catch (Exception e) {
            System.err.println("Caught exception while trying to " +
                                "leave the phone channel and session: " + e);
            if (PhoneUser_Debug) {
                e.printStackTrace();
            }
        }

        try {
            session.close(true);
            list.removeAll();
//            connected = false;
        } catch (Exception e) {
            System.err.println("Caught exception while trying to " +
                                "close the session: " + e);
            if (PhoneUser_Debug) {
                e.printStackTrace();
            }

        }
    }


    public void
    destroy() {
        if (PhoneUser_Debug) {
            System.err.println("PhoneUser: destroy.");
        }

        disconnect();
    }


    public void
    itemStateChanged(ItemEvent event) {
        Object source = event.getSource();

        if (PhoneUser_Debug) {
            System.err.println("PhoneUser: itemStateChanged:" +
                                " event: " + event);
        }

        if (source == inputBox) {
            hasInput = inputBox.getState();
            inputBox.setForeground((hasInput ? Color.green : Color.red));
        } else if (source == outputBox) {
            hasOutput = outputBox.getState();
            outputBox.setForeground((hasOutput ? Color.green : Color.red));
        }
    }


    public void
    actionPerformed(ActionEvent event) {
        Object source = event.getSource();

        if (PhoneUser_Debug) {
            System.err.println("PhoneUser: actionPerformed:" +
                                " event: " + event);
        }

        if (source == nameField) {
            name = nameField.getText();
            if (name == null) {
                nameField.setText("Need to give a name before \"Set Name\"");
            } else {
                connect();
                if (connected) {
                    nameField.setEditable(false);
                    setNameLabel.setEnabled(false);
                    signOffButton.setEnabled(true);
                }
            }
        } else if (source == signOffButton) {
            disconnect();
            if (!connected) {
                nameField.setEditable(true);
                setNameLabel.setEnabled(true);
                signOffButton.setEnabled(false);
            }
        }
    }


    public void channelJoined(ChannelEvent event)   {}
    public void channelLeft(ChannelEvent event)     {}
    public void channelInvited(ChannelEvent event)  {}
    public void channelExpelled(ChannelEvent event) {}


    public void channelConsumerAdded(ChannelEvent event) {
        String clientName = event.getClientName();

        if (PhoneUser_Debug) {
            System.err.println("PhoneUser: channelConsumerAdded:" +
                                " event: " + event);
        }

        list.add(clientName);
    }


    public void channelConsumerRemoved(ChannelEvent event) {
        String clientName = event.getClientName();

        if (PhoneUser_Debug) {
            System.err.println("PhoneUser: channelConsumerRemoved:" +
                                " event: " + event);
        }

        if (list.getItemCount() != 0) {
            list.remove(clientName);
        }
    }


/* Phone optimization - (Pete Boysen - 7th December 1999): */

    /** Sound level below which is considered silence. */
    private static final int BYTE_THRESHOLD   = 3;

/* End of phone optimization. */


    public void
    run() {
        if (PhoneUser_Debug) {
            System.err.println("PhoneUser: run.");
        }

/* Phone optimization - (Pete Boysen - 7th December 1999):
 *
 * Silent represents the number of silent buffers. When it exceeds
 * SILENT_THRESHOLD data will not be transmitted until sound is detected
 * again.
 */

        int silent = 0;

/* End of phone optimization. */

        while (true) {
            int bytes = data.length;

            if (connected) {
                if (PhoneUser_Debug) {
                    System.out.println(">>>CONNECTED<<<");
                }
                if (connected && inputChannel != null) {
                    if (PhoneUser_Debug) {
                        System.out.println("reading data");
                    }
                    bytes = inputChannel.read(data, 0, bytes);

                    if (PhoneUser_Debug) {
                        System.out.println("bytes = " + bytes);
                    }

/* Phone optimization - (Pete Boysen - 7th December 1999):
 *
 * Scan the buffer for data that is above the BYTE_THRESHOLD.
 */

                    for (int i = 0; i < bytes; i++) {
                        if (Math.abs(data[i]) > BYTE_THRESHOLD) {
                            silent = 0;
                            break;
                        }
                    }
                    if (silent++ < 3) {
                        sendInput(isBig, bytes, data);
                    } else {
                        silent = 3;
                    }

/* End of phone optimization. */

                } else {
                    System.err.println("Can't get input channel.");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                    }
                }
            } else {
                if (PhoneUser_Debug) {
                    System.out.println("***NOT CONNECTED***");
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                }
            }
        }
    }


    public static void
    main(String args[]) {
        com.sun.media.jsdt.impl.JSDTObject.showMessage = true;
        com.sun.media.jsdt.impl.JSDTObject.showStack   = true;

        PhoneUser phoneUser = new PhoneUser();

        if (PhoneUser_Debug) {
            System.err.println("PhoneUser: main.");
        }

        phoneUser.isApplication = true;
        phoneUser.getOptions(args);
        phoneUser.initialize();
        new PhoneUserFrame(phoneUser);
    }
}


class
PhoneUserFrame extends Frame implements WindowListener, PhoneDebugFlags {

    private final PhoneUser phoneUser;

    PhoneUserFrame(PhoneUser phoneUser) {
        if (PhoneUserFrame_Debug) {
            System.err.println("PhoneUserFrame: constructor.");
        }

        this.phoneUser = phoneUser;

        setTitle("Phone User");
        add(phoneUser);
        addWindowListener(this);
        pack();
        setSize(phoneUser.width, phoneUser.height);
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
        if (PhoneUserFrame_Debug) {
            System.err.println("PhoneUserFrame: windowClosing.");
        }

        phoneUser.disconnect();
        System.exit(0);
    }
}
