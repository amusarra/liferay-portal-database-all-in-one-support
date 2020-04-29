/**
 * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
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

package it.dontesta.labs.liferay.portal.dao.db;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.liferay.portal.dao.db.BaseDB;
import com.liferay.portal.kernel.dao.db.DBType;
import com.liferay.portal.kernel.dao.db.Index;
import com.liferay.portal.kernel.io.unsync.UnsyncBufferedReader;
import com.liferay.portal.kernel.io.unsync.UnsyncStringReader;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

/**
 * @author Alexander Chow
 * @author Sandeep Soni
 * @author Ganesh Ram
 * @author Antonio Musarra
 * @author Javier Alpanez
 */
public class SQLServerDB extends BaseDB {

	public SQLServerDB(int majorVersion, int minorVersion) {
		super(DBType.SQLSERVER, majorVersion, minorVersion);
	}

	@Override
	public String buildSQL(String template) throws IOException {
		template = replaceTemplate(template);

		template = reword(template);
		template = StringUtil.replace(
			template,
			new String[] {"\\\\", "\\'", "\\\"", "\\n", "\\r"},
			new String[] {"\\", "''", "\"", "\n", "\r"});

		return template;
	}

	@Override
	public List<Index> getIndexes(Connection con) throws SQLException {
		List<Index> indexes = new ArrayList<>();

		DatabaseMetaData databaseMetaData = con.getMetaData();

		if (databaseMetaData.getDatabaseMajorVersion() <=
				_SQL_SERVER_2000) {

			return indexes;
		}

		StringBundler sb = new StringBundler(200);

		sb.append("select sys.tables.name as table_name, ");
		sb.append("sys.indexes.name as index_name, is_unique from ");
		sb.append("sys.indexes inner join sys.tables on ");
		sb.append("sys.tables.object_id = sys.indexes.object_id where ");
		sb.append("sys.indexes.name like 'LIFERAY_%' or sys.indexes.name ");
		sb.append("like 'IX_%'");

		String sql = sb.toString();

		try ( PreparedStatement ps = con.prepareStatement( sql ); ResultSet rs = ps.executeQuery() ) {
			while (rs.next()) {
				String indexName = rs.getString("index_name");
				String tableName = rs.getString("table_name");
				boolean unique = rs.getBoolean("is_unique");

				indexes.add(new Index(indexName, tableName, unique));
			}
		}

		return indexes;
	}

	@Override
	public String getNewUuidFunctionName() {
		return "NEWID()";
	}

	@Override
	public String getPopulateSQL(String databaseName, String sqlContent) {
		throw new UnsupportedOperationException();
//		StringBundler sb = new StringBundler(100);
//
//		sb.append("use ");
//		sb.append(databaseName);
//		sb.append(" GO\n\n");
//		sb.append(sqlContent);
//
//		return sb.toString();
	}

	@Override
	public String getRecreateSQL( String databaseName ) {
		throw new UnsupportedOperationException();
//		StringBundler sb = new StringBundler(100);
//
//		sb.append("IF EXISTS(select * from sys.databases where name='");
//		sb.append(databaseName);
//		sb.append("') drop database ");
//		sb.append(databaseName);
//		sb.append(" GO\n");
//		sb.append("create database ");
//		sb.append(databaseName);
//		//sb.append(" character set utf8");
//		sb.append(" GO\n");
//
//		return sb.toString();
	}

	@Override
	public boolean isSupportsNewUuidFunction() {
		return true;
	}

	@Override
	public boolean isSupportsStringCaseSensitiveQuery() {
		return false;
	}

	@Override
	protected int[] getSQLTypes() {
		return _SQL_TYPES;
	}

	@Override
	protected String[] getTemplate() {
		return _SQL_SERVER;
	}

	@Override
	protected String reword(String data) throws IOException {
		try (UnsyncBufferedReader unsyncBufferedReader =
				new UnsyncBufferedReader(new UnsyncStringReader(data))) {

			StringBundler sb = new StringBundler();

			String line;

			while ((line = unsyncBufferedReader.readLine()) != null) {
				if (line.startsWith(ALTER_COLUMN_NAME)) {
					String[] template = buildColumnNameTokens(line);

					line = StringUtil.replace(
						"exec sp_rename '@table@.@old-column@', " +
							"'@new-column@', 'column';",
						REWORD_TEMPLATE, template);
				}
				else if (line.startsWith(ALTER_COLUMN_TYPE)) {
					String[] template = buildColumnTypeTokens(line);

					String nullable = template[template.length - 1];

					if ( Validator.isBlank(nullable)) {
						line = StringUtil.replace(
							"alter table @table@ alter column @old-column@ @type@ ",
							REWORD_TEMPLATE, template);
					}
					else {
						line = StringUtil.replace(
							"alter table @table@ alter column @old-column@ @type@ " +
								"@nullable@;",
							REWORD_TEMPLATE, template);
					}
				}
				else if (line.startsWith(ALTER_TABLE_NAME)) {
					String[] template = buildTableNameTokens(line);

					line = StringUtil.replace(
						"exec sp_rename '@old-table@', '@new-table@';",
						RENAME_TABLE_TEMPLATE, template);
				}

				sb.append(line);
				sb.append("\n");
			}

			return sb.toString();
		}
	}

	private static final String[] _SQL_SERVER = {
		"--", "1", "0", "'19700101'", "GetDate()", " image", " image",
		" bit", " datetime", " float", " int", " bigint",
		" nvarchar(4000)", " nvarchar(max)", " nvarchar", "  identity(1,1)", "go"
	};

	private static final int _SQL_SERVER_2000 = 8;

	private static final int[] _SQL_TYPES = {
			Types.LONGVARBINARY, Types.LONGVARBINARY, Types.BIT, Types.TIMESTAMP,
			Types.DOUBLE, Types.INTEGER, Types.BIGINT, Types.NVARCHAR,
			Types.NVARCHAR, Types.NVARCHAR
	};
}
