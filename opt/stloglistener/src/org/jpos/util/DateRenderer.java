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
import java.text.SimpleDateFormat;
import org.antlr.stringtemplate.AttributeRenderer;


public class DateRenderer implements AttributeRenderer {
    public String toString (Object o) {
        return toString (o, "yyyy.MM.dd");
    }
    public String toString (Object o, String format) {
        if (o instanceof Date) {
            SimpleDateFormat df = new SimpleDateFormat(format);
            return df.format ((Date)o);
        } else 
            return o.toString();
    }
}

