/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package it.dontesta.labs.liferay.portal.db;

import com.liferay.portal.kernel.dao.db.DB;
import com.liferay.portal.kernel.dao.db.DBType;

import it.dontesta.labs.liferay.portal.dao.db.SQLServerDB;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Miguel Pastor
 * @author Alberto Chaparro
 * @author Antonio Musarra
 */
public class SQLServerDBTest extends BaseDBTestCase {

	@Test
	public void testDBType() throws IOException {
		Assert.assertEquals(DBType.SQLSERVER, getDB().getDBType());
	}

	@Test
	public void testRewordAlterColumnType() throws Exception {
		Assert.assertEquals(
			"alter table user_ alter column screenName nvarchar(75);\n",
			buildSQL("alter_column_type user_ screenName VARCHAR(75);"));
	}

	@Test
	public void testRewordAlterColumnTypeNotNull() throws Exception {
		Assert.assertEquals(
			"alter table user_ alter column screenName nvarchar(75) not " +
				"null;\n",
			buildSQL(
				"alter_column_type user_ screenName VARCHAR(75) not null;"));
	}

	@Test
	public void testRewordAlterColumnTypeNull() throws Exception {
		Assert.assertEquals(
			"alter table user_ alter column screenName nvarchar(75) null;\n",
			buildSQL("alter_column_type user_ screenName VARCHAR(75) null;"));
	}

	@Test
	public void testRewordRenameTable() throws Exception {
		Assert.assertEquals(
			"exec sp_rename 'a', 'b';\n", buildSQL(RENAME_TABLE_QUERY));
	}

	@Override
	protected DB getDB() {
		return new SQLServerDB(0, 0);
	}

}