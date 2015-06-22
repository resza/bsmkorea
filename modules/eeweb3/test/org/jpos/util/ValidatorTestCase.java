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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ValidatorTestCase extends TestCase {
    public void testName() throws Exception {
        assertTrue  (V.isName ("Tito Puente"));
        assertTrue  (V.isName ("Length equals 40 XXXXXXXXXXXXXXXXXXXXXXX"));
        assertFalse (V.isName ("Length equals 41 XXXXXXXXXXXXXXXXXXXXXXXX"));
        assertTrue  (V.isAlpha("Test"));
        assertTrue  (V.isAlpha("',-#"));
        assertTrue  (V.isAlpha("Test", 4));
        assertFalse (V.isAlpha("Test", 3));
    }
    public void testNick () throws Exception {
        assertTrue  (V.isNick("abc"));
        assertTrue  (V.isNick("abce"));
        assertTrue  (V.isNick("abc_def"));
        assertTrue  (V.isNick("32CharsLongNickxxxxxxxxxxxxxxxxx"));
        assertFalse (V.isNick("33CharsLongNickxxxxxxxxxxxxxxxxxx"));
        assertFalse (V.isNick("abc def"));
        assertFalse (V.isNick("abc\\"));
        assertFalse (V.isNick("abc#def"));
        assertFalse (V.isNick("abc-def"));
    }
    public void testLong() throws Exception {
        assertTrue  (V.isLong("1"));
        assertTrue  (V.isLong("1234"));
        assertTrue  (V.isLong("123456789012345678"));
        assertTrue  (V.isLong("1234567890123456789"));
        assertFalse (V.isLong("a b c"));
        assertFalse (V.isLong("123a"));
    }
    public void testState () throws Exception {
        assertTrue  (V.isState("NY"));
        assertTrue  (V.isState("FL"));
        assertFalse (V.isState("XY"));
        assertFalse (V.isState(""));
        assertFalse (V.isState(null));
        assertFalse (V.isState("Any Text"));
        assertFalse (V.isState("NY Any Text starting with a valid state"));
    }
    public void testBoolean() throws Exception {
        assertTrue  (V.isBoolean("yes"));
        assertTrue  (V.isBoolean("no"));
        assertTrue  (V.isBoolean("true"));
        assertTrue  (V.isBoolean("false"));
        assertFalse (V.isBoolean("true2"));
    }
    private void assertTrue (String id, String value) throws Exception {
        assertTrue (Validator.validate (id, value));
    }
    private void assertFalse (String id, String value) throws Exception {
        assertFalse (Validator.validate (id, value));
    }
}

