package it.dontesta.labs.liferay.portal.db;

import it.dontesta.labs.liferay.portal.dao.db.SQLServerDB;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.liferay.portal.kernel.dao.db.DB;
import com.liferay.portal.kernel.dao.db.DBType;

/**
 * @author Antonio Musarra
 */
public class SQLServerDBTest extends BaseDBTestCase {

	@Test
	public void testDBType() throws IOException {
		Assert.assertEquals(
				DBType.SQLSERVER,
				getDB().getDBType());
	}

	@Override
	protected DB getDB() {
		return new SQLServerDB(0, 0);
	}

}
