
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

package examples.synth;

import java.io.*;
import javax.sound.midi.*;
import com.sun.media.jsdt.*;

/**
 * Class for sharing channel, instrument and synthesizer information
 * using JSDT.
 *
 * @version     2.3 - 21st November 2017
 * @author      Rich Burridge
 */

public class
Share implements ChannelConsumer, Client, SynthDebugFlags {

    // Message types.

    private static final char T_Channels           = '\u00A0';
    private static final char T_Synthesizer        = '\u00A1';

    // Message actions.

    private static final char A_AllNotesOff        = '\u00B0';
    private static final char A_LoadInstrument     = '\u00B1';
    private static final char A_NoteOff            = '\u00B2';
    private static final char A_NoteOn             = '\u00B3';
    private static final char A_ControlChange      = '\u00B4';
    private static final char A_ProgramChange      = '\u00B5';
    private static final char A_SetChannelPressure = '\u00B6';
    private static final char A_SetMono            = '\u00B7';
    private static final char A_SetMute            = '\u00B8';
    private static final char A_SetPitchBend       = '\u00B9';
    private static final char A_SetSolo            = '\u00BA';

    // The byte array output stream used for writing buffered messages.
    private ByteArrayOutputStream byteOut;

    // The data output stream associated with this connection.
    private DataOutputStream dataOut;

    // The name of this user.
    private String userName;

    // JSDT session.
    private Session session;

    // JSDT multi-point data channel.
    private Channel channel;

    // Indicates if the client is successfully connected to the server.
    private boolean connected = false;

    // JavaSound MIDI synthesizer, instruments and channels.
    private final Synthesizer synthesizer;
    private final Instrument instruments[];
    private final MidiChannel channels[];


    public
    Share(String sessionType, String hostname, int hostport,
          Synthesizer synthesizer, Instrument instruments[],
          MidiChannel channels[]) {
        URLString url;
        String    sessionName = "MidiSession";

        if (Share_Debug) {
            System.err.println("Share: constructor:" +
                               " session type: " + sessionType +
                               " host name: "    + hostname +
                               " host port: "    + hostport +
                               " synthesizer: "  + synthesizer);
            for (int i = 0; i < instruments.length ; i++) {
                System.err.println("instruments[" + i + "]: " + instruments[i]);
            }
            for (int i = 0; i < channels.length ; i++) {
                System.err.println("channels[" + i + "]: " + channels[i]);
            }
        }

        this.synthesizer = synthesizer;
        this.instruments = instruments;
        this.channels    = channels;

        userName = System.getProperties().getProperty("user.name");
        if (Share_Debug) {
            System.err.println("userName: " + userName);
        }

        url = URLString.createSessionURL(hostname, hostport,
                                         sessionType, sessionName);

        try {
            boolean clientNameOK = false;
            while (!clientNameOK) {
                try {
                    session = SessionFactory.createSession(this, url, true);
                    clientNameOK = true;
                } catch (NameInUseException niu) {
                    userName = userName.concat("+");
                    if (Share_Debug) {
                        System.err.println("Share: userName now: " + userName);
                    }
                }
            }

            channel = session.createChannel(this, "MidiChannel",
                                            true, true, true);
            channel.addConsumer(this, this);
            connected = true;
        } catch (JSDTException e) {
            System.err.println("Share: constructor: exception: " + e);
            e.printStackTrace();
        }
    }


    public Object
    authenticate(AuthenticationInfo info) {
        if (Share_Debug) {
            System.err.println("Share: authenticate:" +
                               " info: " + info);
        }

        return(null);
    }


    public String
    getName() {
        if (Share_Debug) {
            System.err.println("Share: getName");
        }

        return(userName);
    }


    public synchronized void
    dataReceived(Data data) {
        ByteArrayInputStream bais = new
                                ByteArrayInputStream(data.getDataAsBytes());
        DataInputStream      dis  = new DataInputStream(bais);
        char                 type;
        char                 action;

        if (Share_Debug) {
            System.err.println("Share: dataReceived:" +
                               " data: " + data);
        }

        try {
            type   = dis.readChar();
            action = dis.readChar();
            if (type == T_Synthesizer) {
                int instrumentNum = dis.readInt();

                if (Share_Debug) {
                    System.err.println("LoadInstrument.");
                }
                synthesizer.loadInstrument(instruments[instrumentNum]);

            } else if (type == T_Channels) {
                int     channelNum, controller, kNum, velocity;
                int     value, programNum;
                boolean state;

                switch (action) {
                    case A_AllNotesOff:
                        if (Share_Debug) {
                            System.err.println("AllNotesOff.");
                        }
                        channelNum = dis.readInt();
                        channels[channelNum].allNotesOff();
                        break;

                    case A_NoteOff:
                        if (Share_Debug) {
                            System.err.println("NoteOff.");
                        }
                        channelNum = dis.readInt();
                        kNum       = dis.readInt();
                        velocity   = dis.readInt();
                        channels[channelNum].noteOff(kNum, velocity);
                        break;

                    case A_NoteOn:
                        if (Share_Debug) {
                            System.err.println("NoteOn.");
                        }
                        channelNum = dis.readInt();
                        kNum       = dis.readInt();
                        velocity   = dis.readInt();
                        channels[channelNum].noteOn(kNum, velocity);
                        break;

                    case A_ControlChange:
                        if (Share_Debug) {
                            System.err.println("ControlChange.");
                        }
                        channelNum = dis.readInt();
                        controller = dis.readInt();
                        value      = dis.readInt();
                        channels[channelNum].controlChange(controller, value);
                        break;

                    case A_ProgramChange:
                        if (Share_Debug) {
                            System.err.println("ProgramChange.");
                        }
                        channelNum = dis.readInt();
                        programNum = dis.readInt();
                        channels[channelNum].programChange(programNum);
                        break;

                    case A_SetChannelPressure:
                        if (Share_Debug) {
                            System.err.println("SetChannelPressure.");
                        }
                        channelNum = dis.readInt();
                        value      = dis.readInt();
                        channels[channelNum].setChannelPressure(value);
                        break;

                    case A_SetMono:
                        if (Share_Debug) {
                            System.err.println("SetMono.");
                        }
                        channelNum = dis.readInt();
                        state      = dis.readBoolean();
                        channels[channelNum].setMono(state);
                        break;

                    case A_SetMute:
                        if (Share_Debug) {
                            System.err.println("SetMute.");
                        }
                        channelNum = dis.readInt();
                        state      = dis.readBoolean();
                        channels[channelNum].setMute(state);
                        break;

                    case A_SetPitchBend:
                        if (Share_Debug) {
                            System.err.println("SetPitchBend.");
                        }
                        channelNum = dis.readInt();
                        value      = dis.readInt();
                        channels[channelNum].setPitchBend(value);
                        break;

                    case A_SetSolo:
                        if (Share_Debug) {
                            System.err.println("SetSolo.");
                        }
                        channelNum = dis.readInt();
                        state      = dis.readBoolean();
                        channels[channelNum].setSolo(state);
                        break;
                }
            } else {
                System.err.println("Share: dataReceived:" +
                                   " unexpected type: " + type);
            }
        } catch (IOException ioe) {
            System.err.println("Share: dataReceived: exception: " + ioe);
            ioe.printStackTrace();
        }
    }


    public int
    getNoUsers() {
        int noUsers = 1;

        if (Share_Debug) {
            System.err.println("Share: getNoUsers.");
        }

        try {
            String names[] = channel.listClientNames();

            noUsers = names.length;
        } catch (JSDTException e) {
            System.err.println("Share: getNoUsers: exception: " + e);
            e.printStackTrace();
        }

        return(noUsers);
    }


    public void
    noteOn(int channelNum, int kNum, int velocity) {
        if (Share_Debug) {
            System.err.println("Share: noteOn:" +
                               " channel num: " + channelNum +
                               " kNum: "        + kNum +
                               " velocity: "    + velocity);
        }

        try {
            writeHeader(T_Channels, A_NoteOn);
            dataOut.writeInt(channelNum);
            dataOut.writeInt(kNum);
            dataOut.writeInt(velocity);
            send();
        } catch (IOException ioe) {
            System.err.println("Share: noteOn: exception: " + ioe);
            ioe.printStackTrace();
        }
    }


    public void
    noteOff(int channelNum, int kNum, int velocity) {
        if (Share_Debug) {
            System.err.println("Share: noteOff:" +
                               " channel num: " + channelNum +
                               " kNum: "        + kNum +
                               " velocity: "    + velocity);
        }

        try {
            writeHeader(T_Channels, A_NoteOff);
            dataOut.writeInt(channelNum);
            dataOut.writeInt(kNum);
            dataOut.writeInt(velocity);
            send();
        } catch (IOException ioe) {
            System.err.println("Share: noteOn: exception: " + ioe);
            ioe.printStackTrace();
        }
    }


    public void
    allNotesOff(int channelNum) {
        if (Share_Debug) {
            System.err.println("Share: allNotesOff:" +
                               " channel num: " + channelNum);
        }

        try {
            writeHeader(T_Channels, A_AllNotesOff);
            dataOut.writeInt(channelNum);
            send();
        } catch (IOException ioe) {
            System.err.println("Share: noteOn: exception: " + ioe);
            ioe.printStackTrace();
        }
    }


    public void
    controlChange(int channelNum, int controller, int value) {
        if (Share_Debug) {
            System.err.println("Share: constructor:" +
                               " channel num: " + channelNum +
                               " controller: "  + controller +
                               " value: "       + value);
        }

        try {
            writeHeader(T_Channels, A_ControlChange);
            dataOut.writeInt(channelNum);
            dataOut.writeInt(controller);
            dataOut.writeInt(value);
            send();
        } catch (IOException ioe) {
            System.err.println("Share: noteOn: exception: " + ioe);
            ioe.printStackTrace();
        }
    }


    public void
    programChange(int channelNum, int programNum) {
        if (Share_Debug) {
            System.err.println("Share: programChange:" +
                               " channel num: " + channelNum +
                               " programNum: "  + programNum);
        }

        try {
            writeHeader(T_Channels, A_ProgramChange);
            dataOut.writeInt(channelNum);
            dataOut.writeInt(programNum);
            send();
        } catch (IOException ioe) {
            System.err.println("Share: noteOn: exception: " + ioe);
            ioe.printStackTrace();
        }
    }


    public void
    setChannelPressure(int channelNum, int value) {
        if (Share_Debug) {
            System.err.println("Share: setChannelPressure:" +
                               " channel num: " + channelNum +
                               " value: "       + value);
        }

        try {
            writeHeader(T_Channels, A_SetChannelPressure);
            dataOut.writeInt(channelNum);
            dataOut.writeInt(value);
            send();
        } catch (IOException ioe) {
            System.err.println("Share: noteOn: exception: " + ioe);
            ioe.printStackTrace();
        }
    }


    public void
    setMono(int channelNum, boolean state) {
        if (Share_Debug) {
            System.err.println("Share: setMono:" +
                               " channel num: " + channelNum +
                               " state: "       + state);
        }

        try {
            writeHeader(T_Channels, A_SetMono);
            dataOut.writeInt(channelNum);
            dataOut.writeBoolean(state);
            send();
        } catch (IOException ioe) {
            System.err.println("Share: noteOn: exception: " + ioe);
            ioe.printStackTrace();
        }
    }


    public void
    setMute(int channelNum, boolean state) {
        if (Share_Debug) {
            System.err.println("Share: setMute:" +
                               " channel num: " + channelNum +
                               " state: "       + state);
        }

        try {
            writeHeader(T_Channels, A_SetMute);
            dataOut.writeInt(channelNum);
            dataOut.writeBoolean(state);
            send();
        } catch (IOException ioe) {
            System.err.println("Share: noteOn: exception: " + ioe);
            ioe.printStackTrace();
        }
    }


    public void
    setSolo(int channelNum, boolean state) {
        if (Share_Debug) {
            System.err.println("Share: setSolo:" +
                               " channel num: " + channelNum +
                               " state: "       + state);
        }

        try {
            writeHeader(T_Channels, A_SetSolo);
            dataOut.writeInt(channelNum);
            dataOut.writeBoolean(state);
            send();
        } catch (IOException ioe) {
            System.err.println("Share: noteOn: exception: " + ioe);
            ioe.printStackTrace();
        }
    }


    public void
    setPitchBend(int channelNum, int value) {
        if (Share_Debug) {
            System.err.println("Share: setPitchBend:" +
                               " channel num: " + channelNum +
                               " value: "       + value);
        }

        try {
            writeHeader(T_Channels, A_SetPitchBend);
            dataOut.writeInt(channelNum);
            dataOut.writeInt(value);
            send();
        } catch (IOException ioe) {
            System.err.println("Share: noteOn: exception: " + ioe);
            ioe.printStackTrace();
        }
    }


    public void
    loadInstrument(int instrumentNum) {
        if (Share_Debug) {
            System.err.println("Share: loadInstrument:" +
                               " instrument num: " + instrumentNum);
        }

        try {
            writeHeader(T_Synthesizer, A_LoadInstrument);
            dataOut.writeInt(instrumentNum);
            send();
        } catch (IOException ioe) {
            System.err.println("Share: noteOn: exception: " + ioe);
            ioe.printStackTrace();
        }
    }


    private void
    send() {
        if (Share_Debug) {
            System.err.println("Share: send");
        }

        try {
            dataOut.flush();
            channel.sendToAll(this, new Data(byteOut.toByteArray()));
        } catch (IOException | JSDTException e) {
            System.err.println("Share: noteOn: exception: " + e);
            e.printStackTrace();
        }
    }


    private void
    writeHeader(char type, char action) {
        if (Share_Debug) {
            System.err.println("Share: writeHeader.");
        }

        try {
            byteOut = new ByteArrayOutputStream();
            dataOut = new DataOutputStream(byteOut);

            dataOut.writeChar(type);
            dataOut.writeChar(action);
        } catch (IOException ioe) {
            System.err.println("Share: noteOn: exception: " + ioe);
            ioe.printStackTrace();
        }
    }


    void
    disconnect() {
        if (Share_Debug) {
            System.err.println("Share: disconnect.");
        }

        if (!connected) {
            return;
        }

        try {
            session.close(true);
        } catch (Exception e) {
            System.err.println("Caught exception while trying to " +
                                "disconnect from synth server: " + e);
            if (Share_Debug) {
                e.printStackTrace();
            }
        }

        connected = false;
    }
}
