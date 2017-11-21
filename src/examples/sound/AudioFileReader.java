
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

package examples.sound;

/* Read from a Sun au audio file and produce AudioClick objects.
 *
 * @version     2.3 - 29th October 2017
 * @author	Behfar Razavi
 */

import java.io.RandomAccessFile;
import java.io.IOException;

public class
AudioFileReader implements SoundDebugFlags {

    String filename = null;
    int clickSize = 50;                /* Audio chunk size. */
    int magic;
    int sampleRate;                    /* Au file characteristics. */
    int fileLength;
    int hdrSize;
    int channels;
    int encoding;
    int length = 0;                    /* Length of track in seconds. */
    RandomAccessFile audioFile;        /* Stream from the au file. */


    public
    AudioFileReader(String filename, int clickSize) throws IOException {
        if (AudioFileReader_Debug) {
            System.err.println("AudioFileReader: constructor: " +
                               " filename: " + filename +
			       " click size: " + clickSize);
        }

        this.filename  = filename;
	this.clickSize = clickSize;  /* Size of audio data to read. */

	getHeader(filename);
    }


    void
    getHeader(String filename) throws IOException {
        if (AudioFileReader_Debug) {
            System.err.println("AudioFileReader: getHeader: " +
                               " filename: " + filename);
        }

/* Open audiofile and read the header to determine sample rate, length, etc. */

        audioFile = new RandomAccessFile(filename, "r");

        magic      = audioFile.readInt();
        hdrSize    = audioFile.readInt();
        fileLength = audioFile.readInt();
        encoding   = audioFile.readInt();
        sampleRate = audioFile.readInt();
        channels   = audioFile.readInt();

/* Determine the length of the audio track in seconds. Since the fileLength
 * field is optional, it may be set to ~0.  If so, deduce the length from
 * the length of audio file itself.
 */

        if (fileLength <= 0) {
            length = (int) (audioFile.length() - hdrSize) /
                           (sampleRate * channels);
        } else {
            length = fileLength / (sampleRate * channels);
        }
        audioFile.seek(hdrSize);
    }


/*  Return the next chunk of audio data. Once the end of the file is
 *  reached, return an empty AudioClick to signal the end of the Media.
 */

    public AudioClick
    get() {
        int BUFSIZE = clickSize * sampleRate / 1000;   /* buffer size. */
        byte buf[]  = new byte[BUFSIZE];
        int n;

        if (AudioFileReader_Debug) {
            System.err.println("AudioFileReader: get.");
        }

        try {
            n = audioFile.read(buf, 0, BUFSIZE);
            if (n <= 0) {                         /* Reached end of file. */
                audioFile.close();
		getHeader(filename);
		n = audioFile.read(buf, 0, BUFSIZE);
	    }
        } catch (IOException e) {
	    return(new AudioClick(new byte[0], 0, 0));
        }

        return(new AudioClick(buf, 0, n));
    }


    public int
    getLength() {
        if (AudioFileReader_Debug) {
            System.err.println("AudioFileReader: getLength.");
        }

        return(length);
    }


    public int
    getSampleRate() {
        if (AudioFileReader_Debug) {
            System.err.println("AudioFileReader: getSampleRate.");
        }

        return(sampleRate);
    }


    public int
    getChannels() {
        if (AudioFileReader_Debug) {
            System.err.println("AudioFileReader: getChannels.");
        }

        return(channels);
    }


    public int
    getEncoding() {
        if (AudioFileReader_Debug) {
            System.err.println("AudioFileReader: getEncoding.");
        }

        return(encoding);
    }
}
