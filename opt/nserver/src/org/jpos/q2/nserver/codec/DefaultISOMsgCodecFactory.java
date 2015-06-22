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

package org.jpos.q2.nserver.codec;

import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.core.session.IoSession;
import org.jpos.q2.nserver.ISOMsgCodecFactory;
import org.jpos.q2.nserver.ProtocolHandler;

/**
 * @author Victor Salaman (vsalaman@gmail.com)
 */
public class DefaultISOMsgCodecFactory implements ISOMsgCodecFactory
{
    DefaultISOMsgDecoder decoder;
    DefaultISOMsgEncoder encoder;

    public void init(ProtocolHandler handler)
    {
        encoder = new DefaultISOMsgEncoder(handler);
        decoder = new DefaultISOMsgDecoder(handler);
    }

    public void setDecoder(DefaultISOMsgDecoder decoder)
    {
        this.decoder = decoder;
    }

    public void setEncoder(DefaultISOMsgEncoder encoder)
    {
        this.encoder = encoder;
    }

    public ProtocolEncoder getEncoder(IoSession ioSession) throws Exception
    {
        return encoder;
    }

    public ProtocolDecoder getDecoder(IoSession ioSession) throws Exception
    {
        return decoder;
    }

    public ProtocolEncoder getEncoder()
    {
        return encoder;
    }

    public ProtocolDecoder getDecoder()
    {
        return decoder;
    }
}
