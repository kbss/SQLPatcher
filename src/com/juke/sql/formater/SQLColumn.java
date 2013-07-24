package com.juke.sql.formater;

import java.sql.ResultSet;
import java.sql.SQLException;

/*******************************************************************************
 * SQLPatcher - <a
 * href="https://github.com/kbss/SQLPatcher">https://github.com/kbss
 * /SQLPatcher</a><br>
 * 
 * Copyright (C) 2013 Serhii Krivtsov<br>
 * 
 * SQLPatcher is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.<br>
 * <br>
 * SQLPatcher is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>. <br>
 * 
 * @author Serhii Krivtsov
 ******************************************************************************/
public interface SQLColumn {
    public int OBJECT_TYPE_UN_QOTED = 0;

    public int OBJECT_TYPE_QOTED = 1;

    public int OBJECT_TYPE_BINARY = 2;

    public int getType();

    public String getColumnName();

    public String getObjectStringValue(ResultSet resultSet) throws SQLException;

    public boolean isNotNull();

    public boolean isPrimaryKey();

    public boolean isUnique();

    public void setNotNull(boolean isNotNull);

    public void setPrimaryKey(boolean isPrimaryKey);

    public void setUnique(boolean isUnique);
}
