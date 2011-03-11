package com.rework.joss.persistence.convention.db;

import com.rework.joss.persistence.convention.db.model.Container;

public interface IDBFacade {

	/**
	 * Populate the column and key information for the one table.
	 * 
	 * @param container
	 *            the table container
	 * @param tableName
	 * @throws DataAccessException
	 */
	public abstract void populateTableData(Container container, String tableName)
			throws DataAccessException;


}