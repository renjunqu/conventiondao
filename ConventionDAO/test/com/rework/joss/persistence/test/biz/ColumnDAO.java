package com.rework.joss.persistence.test.biz;

import com.rework.joss.persistence.convention.annotation.AbstractConventionDAO;
import com.rework.joss.persistence.convention.annotation.DAO;

@DAO(table="info_column", pojo=ColumnDTO.class)
public class ColumnDAO extends AbstractConventionDAO {

}
