
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

package com.sun.media.jsdt;

import com.sun.media.jsdt.impl.JSDTObject;
import java.io.*;

/**
 * The Data class.
 *
 * @version     2.3 - 26th October 2017
 * @author      Rich Burridge
 * @since       JSDT 1.0
 */

public class
Data extends JSDTObject {

    /** The length (in bytes) of the raw data.
     *
     *  @serial
     */
    private int length;

    /** The raw data.
     *
     *  @serial
     */
    private byte[] data;

    /** The priority that this data is being sent/received at.
     *
     *  @serial
     */
    private int priority = Channel.MEDIUM_PRIORITY;

    /** The name of the sender.
     *
     *  @serial
     */
    private String senderName = null;

    /** The channel that the data is being sent over.
     *
     *  @serial
     */
    private Channel channel = null;


/**
 * <A NAME="SD_DATA"></A>
 * constructor for the Data class. An array of bytes is turned into a
 * Data object. The length of the data array is the length of the data object.
 *
 * @param data the data (an array of bytes).
 */

    public
    Data(byte[] data) {
        if (Data_Debug) {
            debug("Data: constructor:" +
                  " data: " + data);
        }

        this.data = data;
        length = data.length;
    }


/**
 * <A NAME="SD_DATAWITHOFFSETANDLENGTH"></A>
 * constructor for the Data class. An array of bytes is turned into a Data
 * object. The length of the data object is provided. If this value is
 * invalid, then the length of the data object is the length of the array
 * of bytes.
 *
 * @param data the data (an array of bytes).
 * @param length the length of the data.
 */

    public
    Data(byte[] data, int length) {
        if (Data_Debug) {
            debug("Data: constructor:" +
                  " data: "   + data +
                  " length: " + length);
        }


        this.data = data;
        if (length > 0 || length <= data.length) {
            this.length = length;
        } else {
            this.length = data.length;
        }
    }


/**
 * <A NAME="SD_DATAFROMSTRING"></A>
 * constructor for the Data class. A String is turned into a Data object.
 * The length of the data array is the length of the String object.
 *
 * @param string the data (a String).
 */

    public
    Data(String string) {
        this((Object) string);

        if (Data_Debug) {
            debug("Data: constructor:" +
                  " string: " + string);
        }
    }


/**
 * <A NAME="SD_DATAFROMOBJECT"></A>
 * constructor for the Data class. A Java object is turned into a Data object.
 * The length of the data array is the length of the serialized Java object.
 *
 * @param object the data (a Java object).
 *
 * @since       JSDT 1.5
 */

    public
    Data(Object object) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream    oos;

        if (Data_Debug) {
            debug("Data: constructor:" +
                  " object: " + object);
        }

        try {
            oos = new ObjectOutputStream(bos);
            oos.writeObject(object);
            length = bos.size();
            data   = new byte[length];
            data   = bos.toByteArray();
        } catch (IOException ioe) {
            error("Data: constructor: ", ioe);
        }
    }


/**
 * <A NAME="SD_GETDATAASBYTES"></A>
 * get the data associated with this Data object, as an array of bytes.
 *
 * @return the data contained in this Data object, as an array of bytes.
 *
 * @since       JSDT 1.5
 */

    public byte[]
    getDataAsBytes() {
        if (Data_Debug) {
            debug("Data: getDataAsBytes.");
        }

        return(data);
    }


/**
 * <A NAME="SD_GETDATAASOBJECT"></A>
 * get the data associated with this Data object, as a Java Object.
 *
 * @exception ClassNotFoundException if the class for this object cannot
 * be found.
 * @exception StreamCorruptedException if this Data object does not contain
 * a serialized object.
 *
 * @return the data contained in this Data object, as a Java Object.
 *
 * @since       JSDT 1.5
 */

    public Object
    getDataAsObject()
                throws ClassNotFoundException, StreamCorruptedException {
        ByteArrayInputStream bis    = new ByteArrayInputStream(data);
        ObjectInputStream    ois;
        Object               object = null;

        if (Data_Debug) {
            debug("Data: getDataAsObject.");
        }

        try {
            ois    = new ObjectInputStream(bis);
            object = ois.readObject();
            ois.close();
            bis.close();
        } catch (IOException ioe) {
            error("Data: getDataAsObject: ", ioe);
        }

        return(object);
    }


/**
 * <A NAME="SD_GETDATAASSTRING"></A>
 * get the data associated with this Data object, as a String object.
 *
 * @return the data contained in this Data object, as a String object.
 *
 * @since       JSDT 1.5
 */

    public String
    getDataAsString() {
        String s = null;

        if (Data_Debug) {
            debug("Data: getDataAsString.");
        }

        try {
            s = (String) getDataAsObject();
        } catch (Exception e) {
            error("Data: getDataAsString: ", e);
        }

        return(s);
    }


/**
 * <A NAME="SD_GETLENGTH"></A>
 * get the length of the data in this Data object.
 *
 * @return the length (in bytes) of the data in this Data object.
 */

    public int
    getLength() {
        if (Data_Debug) {
            debug("Data: getLength.");
        }

        return(length);
    }


/**
 * <A NAME="SD_GETSENDERNAME"></A>
 * get the name of the Data sender.
 *
 * @return the name of the sender of this Data.
 */

    public String
    getSenderName() {
        if (Data_Debug) {
            debug("Data: getSenderName.");
        }

        return(senderName);
    }


/**
 * <A NAME="SD_GETPRIORITY"></A>
 * gets the priority that this Data was sent at.
 *
 * @return the priority that this Data was sent at.
 */

    public int
    getPriority() {
        if (Data_Debug) {
            debug("Data: getPriority.");
        }

        return(priority);
    }


/**
 * <A NAME="SD_SETPRIORITY"></A>
 * set a new priority value for this data.
 *
 * @param priority the new priority value.
 */

    public void
    setPriority(int priority) {
        if (Data_Debug) {
            debug("Data: setPriority:" +
                  " priority: " + priority);
        }

        if (priority < Channel.TOP_PRIORITY) {
            this.priority = Channel.TOP_PRIORITY;
        } else if (priority > Channel.LOW_PRIORITY) {
            this.priority = Channel.LOW_PRIORITY;
        } else {
            this.priority = priority;
        }
    }


/**
 * <A NAME="SD_GETCHANNEL"></A>
 * get the Channel that this data was sent over.
 *
 * @return the Channel that this Data was sent over.
 */

    public Channel
    getChannel() {
        return(channel);
    }


/**
 * <A NAME="SD_SETCHANNEL"></A>
 * set a new channel value for this data.
 *
 * @param channel the new channel value.
 */

    public void
    setChannel(Channel channel) {
        if (Data_Debug) {
            debug("Data: setChannel:" +
                  " channel: " + channel);
        }

        this.channel = channel;
    }


/**
 * <A NAME="SD_SETSENDERNAME"></A>
 * set a new sender name for this data.
 *
 * @param senderName the new sender name.
 */

    public void
    setSenderName(String senderName) {
        if (Data_Debug) {
            debug("Data: setSenderName:" +
                  " sender name: " + senderName);
        }

        this.senderName = senderName;
    }
}
