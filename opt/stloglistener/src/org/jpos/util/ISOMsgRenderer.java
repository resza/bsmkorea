/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2007 Alejandro P. Revilla
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.util;

import java.util.Date;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import org.antlr.stringtemplate.AttributeRenderer;

import org.jpos.iso.ISOMsg;

public class ISOMsgRenderer implements AttributeRenderer {
    public String toString (Object o) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream p = new PrintStream (baos);
        ((ISOMsg)o).dump (p, "");
        return baos.toString();
    }
    public String toString (Object o, String format) {
        if ("short".equals (format))
            return o.toString();
        else
            return toString(o);
    }
}

