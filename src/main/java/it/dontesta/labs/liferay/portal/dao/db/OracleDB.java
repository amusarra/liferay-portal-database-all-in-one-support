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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.liferay.portal.dao.db.BaseDB;
import com.liferay.portal.kernel.dao.db.DBType;
import com.liferay.portal.kernel.dao.db.Index;
import com.liferay.portal.kernel.dao.jdbc.DataAccess;
import com.liferay.portal.kernel.io.unsync.UnsyncBufferedReader;
import com.liferay.portal.kernel.io.unsync.UnsyncStringReader;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringUtil;

/**
 * @author Alexander Chow
 * @author Sandeep Soni
 * @author Ganesh Ram
 * @author Antonio Musarra
 * @author Javier Alpanez
 */
public class OracleDB extends BaseDB {

	public OracleDB(int majorVersion, int minorVersion) {
		super(DBType.ORACLE, majorVersion, minorVersion);
	}

	@Override
	public String buildSQL(String template) throws IOException {
		template = _preBuildSQL(template);
		template = _postBuildSQL(template);

		return template;
	}

	@Override
	public List<Index> getIndexes(Connection con) throws SQLException {
		List<Index> indexes = new ArrayList<Index>();

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			StringBundler sb = new StringBundler(3);

			sb.append("select index_name, table_name, uniqueness from ");
			sb.append("user_indexes where index_name like 'LIFERAY_%' or ");
			sb.append("index_name like 'IX_%'");

			String sql = sb.toString();

			ps = con.prepareStatement(sql);

			rs = ps.executeQuery();

			while (rs.next()) {
				String indexName = rs.getString("index_name");
				String tableName = rs.getString("table_name");
				String uniqueness = rs.getString("uniqueness");

				boolean unique = true;

				if (StringUtil.equalsIgnoreCase(uniqueness, "NONUNIQUE")) {
					unique = false;
				}

				indexes.add(new Index(indexName, tableName, unique));
			}
		}
		finally {
			DataAccess.cleanUp(null, ps, rs);
		}

		return indexes;
	}

	@Override
	public String getPopulateSQL( String s, String s1 ) {
		return null;
	}

	@Override
	public String getRecreateSQL( String s ) {
		return null;
	}

	@Override
	public boolean isSupportsInlineDistinct() {
		return _SUPPORTS_INLINE_DISTINCT;
	}

	@Override
	protected String[] getTemplate() {
		return _ORACLE;
	}

	@Override
	protected String reword(String data) throws IOException {
		UnsyncBufferedReader unsyncBufferedReader = new UnsyncBufferedReader(
			new UnsyncStringReader(data));

		StringBundler sb = new StringBundler();

		String line = null;

		while ((line = unsyncBufferedReader.readLine()) != null) {
			if (line.startsWith(ALTER_COLUMN_NAME)) {
				String[] template = buildColumnNameTokens(line);

				line = StringUtil.replace(
					"alter table @table@ rename column @old-column@ to " +
						"@new-column@;",
					REWORD_TEMPLATE, template);
			}
			else if (line.startsWith(ALTER_COLUMN_TYPE)) {
				String[] template = buildColumnTypeTokens(line);

				line = StringUtil.replace(
					"alter table @table@ modify @old-column@ @type@;",
					REWORD_TEMPLATE, template);
			}
			else if (line.startsWith(ALTER_TABLE_NAME)) {
				String[] template = buildTableNameTokens(line);

				line = StringUtil.replace(
					"alter table @old-table@ rename to @new-table@;",
					RENAME_TABLE_TEMPLATE, template);
			}
			else if (line.contains(DROP_INDEX)) {
				String[] tokens = StringUtil.split(line, ' ');

				line = StringUtil.replace(
					"drop index @index@;", "@index@", tokens[2]);
			}

			sb.append(line);
			sb.append("\n");
		}

		unsyncBufferedReader.close();

		return sb.toString();
	}

	@Override
	protected int[] getSQLTypes() {
		return _SQL_TYPES;
	}

	private void _convertToOracleCSV(String line, StringBundler sb) {
		int x = line.indexOf("values (");
		int y = line.lastIndexOf(");");

		line = line.substring(x + 8, y);

		line = StringUtil.replace(line, "sysdate, ", "20050101, ");

		sb.append(line);
		sb.append("\n");
	}

	private String _postBuildSQL(String template) throws IOException {
		//template = removeLongInserts(template);
		template = StringUtil.replace(template, "\\n", "'||CHR(10)||'");

		return template;
	}

	private String _preBuildSQL(String template) throws IOException {
		template = replaceTemplate(template);

		template = reword(template);
		template = StringUtil.replace(
			template,
			new String[] {"\\\\", "\\'", "\\\""},
			new String[] {"\\", "''", "\""});

		return template;
	}

	private static final String[] _ORACLE = {
		"--", "1", "0",
		"to_date('1970-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS')", "sysdate",
		" blob", " blob", " number(1, 0)", " timestamp", " number(30,20)",
		" number(30,0)", " number(30,0)", " varchar2(4000)", " clob",
		" varchar2", "", "commit"
	};

	private static final int[] _SQL_TYPES = {
			Types.BLOB, Types.BLOB, Types.NUMERIC, Types.TIMESTAMP, Types.NUMERIC,
			Types.NUMERIC, Types.NUMERIC, Types.VARCHAR, Types.CLOB, Types.VARCHAR
	};

	private static final boolean _SUPPORTS_INLINE_DISTINCT = false;

	private static Pattern _varcharPattern = Pattern.compile(
		"VARCHAR\\((\\d+)\\)");

}
