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

import it.dontesta.labs.liferay.portal.dao.db.OracleDB;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Miguel Pastor
 * @author Alberto Chaparro
 * @author Antonio Musarra
 */
public class OracleDBTest extends BaseDBTestCase {

	@Test
	public void testDBType() throws IOException {
		Assert.assertEquals(DBType.ORACLE, getDB().getDBType());
	}

	@Test
	public void testRewordAlterColumnType() throws Exception {
		Assert.assertEquals(
			"alter table user_ modify screenName VARCHAR2(75 CHAR);\n",
			buildSQL("alter_column_type user_ screenName VARCHAR(75);"));
	}

	@Test
	public void testRewordAlterColumnTypeLowerCase() throws Exception {
		Assert.assertEquals(
			"alter table user_ modify screenName VARCHAR2(75 CHAR);\n",
			buildSQL("alter_column_type user_ screenName varchar(75);"));
	}

	@Test
	public void testRewordAlterColumnTypeNotNullWhenNotNull() throws Exception {
		Assert.assertEquals(
			"alter table user_ modify screenName VARCHAR2(75 CHAR);\n",
			buildSQL(
				"alter_column_type user_ screenName VARCHAR(75) not null;"));
	}

	@Test
	public void testRewordAlterColumnTypeNotNullWhenNull() throws Exception {
		_nullable = true;

		Assert.assertEquals(
			"alter table user_ modify screenName VARCHAR2(75 CHAR) not null;\n",
			buildSQL(
				"alter_column_type user_ screenName VARCHAR(75) not null;"));
	}

	@Test
	public void testRewordAlterColumnTypeNullWhenNotNull() throws Exception {
		Assert.assertEquals(
			"alter table user_ modify screenName VARCHAR2(75 CHAR) null;\n",
			buildSQL("alter_column_type user_ screenName VARCHAR(75) null;"));
	}

	@Test
	public void testRewordAlterColumnTypeNullWhenNull() throws Exception {
		_nullable = true;

		Assert.assertEquals(
			"alter table user_ modify screenName VARCHAR2(75 CHAR);\n",
			buildSQL("alter_column_type user_ screenName VARCHAR(75) null;"));
	}

	@Override
	protected DB getDB() {
		return new OracleDB(0, 0) {

			@Override
			protected boolean columnIsNullable(
				String tableName, String columnName) {

				return _nullable;
			}

		};
	}

	private boolean _nullable;

}