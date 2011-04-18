package com.rework.joss.persistence.test.tigger;

import com.rework.joss.persistence.convention.annotation.AbstractConventionDAO;
import com.rework.joss.persistence.convention.annotation.DAO;
import com.rework.joss.persistence.test.biz.DAOTestDTO;
import com.rework.joss.persistence.test.biz.DAOTestDateStringDTO;

/**
 * 
 * @author kevin zhang
 *
 */
@DAO(pojo=DAOTestDateStringDTO.class, table="dao_test")
public class TiggerStringDAO extends AbstractConventionDAO{


	
}
