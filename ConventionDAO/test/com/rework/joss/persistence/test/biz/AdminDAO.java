package com.rework.joss.persistence.test.biz;

import com.rework.joss.persistence.convention.annotation.AbstractConventionDAO;
import com.rework.joss.persistence.convention.annotation.DAO;

@DAO(table="admin", pojo=AdminDTO.class)
public class AdminDAO extends AbstractConventionDAO {

}
