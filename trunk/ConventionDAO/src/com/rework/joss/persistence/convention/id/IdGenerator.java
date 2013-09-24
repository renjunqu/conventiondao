package com.rework.joss.persistence.convention.id;

import javax.sql.DataSource;

public interface IdGenerator {

	public Object id(DataSource ds, String dbo);
}
