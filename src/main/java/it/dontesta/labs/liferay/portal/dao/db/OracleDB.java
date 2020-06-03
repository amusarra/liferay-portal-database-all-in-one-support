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

package it.dontesta.labs.liferay.portal.dao.db;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.dao.db.BaseDB;
import com.liferay.portal.kernel.dao.db.DBInspector;
import com.liferay.portal.kernel.dao.db.DBType;
import com.liferay.portal.kernel.dao.db.Index;
import com.liferay.portal.kernel.dao.jdbc.DataAccess;
import com.liferay.portal.kernel.io.unsync.UnsyncBufferedReader;
import com.liferay.portal.kernel.io.unsync.UnsyncStringReader;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

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
	public String buildSQL(String template) throws IOException, SQLException {
		template = _preBuildSQL(template);
		template = _postBuildSQL(template);

		return template;
	}

	@Override
	public List<Index> getIndexes(Connection con) throws SQLException {
		List<Index> indexes = new ArrayList<>();

		StringBundler dbIndexesSB = new StringBundler(3);

		dbIndexesSB.append("select table_name, index_name, uniqueness from ");
		dbIndexesSB.append("user_indexes where index_name like 'LIFERAY_%' ");
		dbIndexesSB.append("or index_name like 'IX_%'");

		String sql = dbIndexesSB.toString();

		try (PreparedStatement preparedStatement = con.prepareStatement(sql);
			ResultSet resultSet = preparedStatement.executeQuery()) {

			while (resultSet.next()) {
				String tableName = resultSet.getString("table_name");
				String indexName = resultSet.getString("index_name");
				String uniqueness = resultSet.getString("uniqueness");

				boolean unique = true;

				if (StringUtil.equalsIgnoreCase(uniqueness, "NONUNIQUE")) {
					unique = false;
				}

				indexes.add(new Index(indexName, tableName, unique));
			}
		}

		return indexes;
	}

	@Override
	public String getPopulateSQL(String databaseName, String sqlContent) {
		StringBundler populateSqlSB = new StringBundler(5);

		populateSqlSB.append("connect &1/&2;\n");
		populateSqlSB.append("set define off;\n");
		populateSqlSB.append("\n");
		populateSqlSB.append(sqlContent);
		populateSqlSB.append("quit");

		return populateSqlSB.toString();
	}

	@Override
	public String getRecreateSQL(String databaseName) {
		StringBundler recreateSqlSB = new StringBundler(4);

		recreateSqlSB.append("drop user &1 cascade;\n");
		recreateSqlSB.append("create user &1 identified by &2;\n");
		recreateSqlSB.append("grant connect,resource to &1;\n");
		recreateSqlSB.append("quit");

		return recreateSqlSB.toString();
	}

	@Override
	public boolean isSupportsInlineDistinct() {
		return _SUPPORTS_INLINE_DISTINCT;
	}

	@Override
	protected String[] buildColumnTypeTokens(String line) {
		Matcher matcher = _varchar2CharPattern.matcher(line);

		StringBuffer sb = new StringBuffer();

		while (matcher.find()) {
			matcher.appendReplacement(
				sb, "VARCHAR2(" + matcher.group(1) + "%20CHAR)");
		}

		matcher.appendTail(sb);

		String[] template = super.buildColumnTypeTokens(sb.toString());

		template[3] = StringUtil.replace(template[3], "%20", StringPool.SPACE);

		return template;
	}

	/**
	 * Check if the column name of the specified table is nullable
	 *
	 * @param tableName The table name
	 * @param columnName The column name
	 * @return boolean true if column is nullable false otherwise
	 * @throws SQLException When occurs SQL Exception
	 */
	protected boolean columnIsNullable(String tableName, String columnName)
		throws SQLException {

		try (Connection connection = DataAccess.getConnection()) {
			DBInspector dbInspector = new DBInspector(connection);

			return dbInspector.isNullable(tableName, columnName);
		}
	}

	@Override
	protected int[] getSQLTypes() {
		return _SQL_TYPES;
	}

	@Override
	protected String[] getTemplate() {
		return _ORACLE;
	}

	@Override
	protected String replaceTemplate(String template) {

		// LPS-12048

		Matcher matcher = _varcharPattern.matcher(template);

		StringBuffer sb = new StringBuffer();

		while (matcher.find()) {
			int size = GetterUtil.getInteger(matcher.group(1));

			// Force size of the column to 4000 char

			if (size > 4000) {
				size = 4000;
			}

			matcher.appendReplacement(sb, "VARCHAR2(" + size + " CHAR)");
		}

		matcher.appendTail(sb);

		return super.replaceTemplate(sb.toString());
	}

	@Override
	protected String reword(String data) throws IOException, SQLException {
		try (UnsyncBufferedReader unsyncBufferedReader =
				new UnsyncBufferedReader(new UnsyncStringReader(data))) {

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

					String nullable = template[template.length - 1];

					if (!Validator.isBlank(nullable)) {
						boolean currentNullable = columnIsNullable(
							template[0], template[1]);

						if ((nullable.equalsIgnoreCase("null") &&
							 currentNullable) ||
							(nullable.equalsIgnoreCase("not null") &&
							 !currentNullable)) {

							nullable = StringPool.BLANK;
						}
					}

					line = StringUtil.replace(
						String.format(
							"alter table @table@ modify @old-column@ @type@ %s;",
							nullable),
						REWORD_TEMPLATE, template);

					line = StringUtil.replace(line, " ;", ";");
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

			return sb.toString();
		}
	}

	private String _postBuildSQL(String template) {
		return StringUtil.replace(template, "\\n", "'||CHR(10)||'");
	}

	private String _preBuildSQL(String template)
		throws IOException, SQLException {

		template = replaceTemplate(template);
		template = reword(template);
		template = StringUtil.replace(
			template, new String[] {"\\\\", "\\'", "\\\""},
			new String[] {"\\", "''", "\""});

		return template;
	}

	private static final String[] _ORACLE = {
		"--", "1", "0",
		"to_date('1970-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS')", "sysdate",
		" blob", " blob", " number(1, 0)", " timestamp", " number(30,20)",
		" number(30,0)", " number(30,0)", " varchar2(4000 char)", " clob",
		" varchar2", "", "commit"
	};

	private static final int[] _SQL_TYPES = {
		Types.BLOB, Types.BLOB, Types.NUMERIC, Types.TIMESTAMP, Types.NUMERIC,
		Types.NUMERIC, Types.NUMERIC, Types.VARCHAR, Types.CLOB, Types.VARCHAR
	};

	private static final boolean _SUPPORTS_INLINE_DISTINCT = false;

	private static final Pattern _varchar2CharPattern = Pattern.compile(
		"VARCHAR2\\((\\d+) CHAR\\)", 2);
	private static Pattern _varcharPattern = Pattern.compile(
		"VARCHAR\\((\\d+)\\)", 2);

}