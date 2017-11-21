
/*
 *  Copyright (c) 1996-2005 James Begole.
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

package examples.ppong;

import java.awt.Button;

/**
 * Description: ToggleButton extends the Button class. It provides a method
 *              by which one may toggle the label between two strings.
 *              Might be enhanced to resize button based on size of current
 *              label string.
 *
 * @version     2.3 - 20th November 2017
 * @author      James "Bo" Begole
 * @author      Rich Burridge
 */

public class
ToggleButton extends Button implements PpongDebugFlags {

    private final String label1, label2;


    public
    ToggleButton(String label1, String label2) {
        super((label1.length() >= label2.length()) ? label1: label2);
        this.validate();
        this.label1 = label1;
        this.label2 = label2;
        setLabel(this.label1);
    }


    public void
    toggle() {
        String buttonLabel = getLabel();

        if (buttonLabel.equals(label1)) {
            setLabel(label2);
        } else {
            setLabel(label1);
        }
        if (ToggleButton_Debug) {
            System.err.println("Toggling to: " + getLabel());
        }
    }
}
