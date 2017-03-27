package it.dontesta.labs.liferay.portal.db;

import com.liferay.portal.kernel.dao.db.DB;
import com.liferay.portal.kernel.dao.db.DBType;

import it.dontesta.labs.liferay.portal.dao.db.DB2DB;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Antonio Musarra
 */
public class BD2DBTest extends BaseDBTestCase {

	@Test
	public void testDBType() throws IOException {
		Assert.assertEquals(
				DBType.DB2,
				getDB().getDBType());
	}

	@Override
	protected DB getDB() {
		return new DB2DB(0, 0);
	}

}