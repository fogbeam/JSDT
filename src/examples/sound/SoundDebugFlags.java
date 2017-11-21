
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

/**
 * Sound example debug flags interface.
 *
 * @version     2.3 - 29th October 2017
 * @author      Rich Burridge
 */

public
interface SoundDebugFlags {

    boolean AudioClick_Debug      = false;
    boolean AudioConsumer_Debug   = false;
    boolean AudioFileReader_Debug = false;
    boolean CommandConsumer_Debug = false;
    boolean PlayAudioFile_Debug   = false;
    boolean SendSoundInfo_Debug   = false;
    boolean SoundCanvas_Debug     = false;
    boolean SoundClient_Debug     = false;
    boolean SoundListener_Debug   = false;
    boolean SoundServer_Debug     = false;
    boolean SoundUser_Debug       = true;
    boolean SoundUserFrame_Debug  = false;
    boolean Speaker_Debug         = false;
}
