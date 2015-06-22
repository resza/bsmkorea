/*
 * MiniGL
 * Copyright (C) 2005 Alejandro Revilla
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.jpos.gl.rule;

import java.math.BigDecimal;

/**
 * Check that final account's balance (in reporting currency) is not less than the ruleinfo param.
 *
 * @author <a href="mailto:apr@jpos.org">Alejandro Revilla</a>
 * @see org.jpos.gl.JournalRule
 * @see org.jpos.gl.RuleInfo
 */
public class FinalMinBalance extends FinalBalance {
    protected String getRuleName() {
        return "FinalMinBalance";
    }
    protected boolean isError
        (BigDecimal balance, BigDecimal minBalance)
    {
        return balance.compareTo (minBalance) < 0;
    }
}

