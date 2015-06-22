/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2009 Alejandro P. Revilla
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

package org.jpos.q2.nserver;

import org.apache.mina.core.buffer.IoBuffer;
import org.jpos.core.Configurable;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOHeader;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.util.LogSource;

import java.io.IOException;

/**
 * Defines the contract for our protocol handlers.
 *
 * @author Victor Salaman (vsalaman@gmail.com)
 */
public interface ProtocolHandler extends LogSource, Configurable
{
    void setPackager(ISOPackager packager);

    ISOPackager getPackager();

    int getMaxPacketLength();

    ISOMsg createISOMsg();

    boolean isLengthEncoded();

    boolean containsHeader();

    int readMessageLength(IoBuffer in);

    int getHeaderLength();

    int getMessageLengthByteSize();

    void unpack(ISOMsg msg, byte[] b) throws ISOException;

    byte[] readHeader(IoBuffer in, int len);

    byte[] readPayload(IoBuffer in, int len);

    byte[] readStream(IoBuffer in) throws IOException;

    ISOHeader getDynamicHeader(byte[] image);

    void writeMessageLength(IoBuffer out, int len);

    void writeHeader(IoBuffer out, ISOMsg msg);

    void writePayload(IoBuffer out, byte[] b);

    void writeTrailer(IoBuffer out, byte[] b);

    public boolean isUseZeroLengthAsKeepalive();

    public void setUseZeroLengthAsKeepalive(boolean useZeroLengthAsKeepalive);
}
