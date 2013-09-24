package com.rework.joss.persistence.test.biz;

import com.rework.joss.persistence.convention.annotation.AbstractConventionDAO;
import com.rework.joss.persistence.convention.annotation.DAO;

@DAO(table="auto_generate_id", pojo=AutoGenerateIdDTO.class)
public class AutoGenerateIdDAO extends AbstractConventionDAO {

}
