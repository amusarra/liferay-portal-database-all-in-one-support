package it.dontesta.labs.liferay.portal.db;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.db.DB;
import com.liferay.portal.kernel.util.StringBundler;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Miguel Pastor
 * @author László Csontos
 */
public abstract class BaseDBTestCase {

	@Test
	public void testReplaceTemplate() throws IOException, SQLException {
		StringBundler sb = new StringBundler(5);

		sb.append("select * from SomeTable where someColumn1 = ");
		sb.append(_db.getTemplateFalse());
		sb.append(" and someColumn2 = ");
		sb.append(_db.getTemplateTrue());
		sb.append(StringPool.NEW_LINE);

		Assert.assertEquals(sb.toString(), buildSQL(_BOOLEAN_LITERAL_QUERY));
		Assert.assertEquals(
			_BOOLEAN_PATTERN_QUERY + StringPool.NEW_LINE,
			buildSQL(_BOOLEAN_PATTERN_QUERY));
	}

	protected String buildSQL(String query) throws IOException, SQLException {
		return _db.buildSQL(query);
	}

	protected abstract DB getDB();

	protected static final String RENAME_TABLE_QUERY = "alter_table_name a b";

	private static final String _BOOLEAN_LITERAL_QUERY =
		"select * from SomeTable where someColumn1 = FALSE and someColumn2 = " +
			"TRUE";

	private static final String _BOOLEAN_PATTERN_QUERY =
		"select * from SomeTable where someColumn1 = [$FALSE$] and " +
			"someColumn2 = [$TRUE$]";

	private final DB _db = getDB();

}
