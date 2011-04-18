package com.rework.joss.persistence.test.tigger;

import com.rework.joss.persistence.convention.annotation.AbstractConventionDAO;
import com.rework.joss.persistence.convention.annotation.DAO;
import com.rework.joss.persistence.test.biz.JoinTestDTO;

/**
 * 
 * @author kevin zhang
 *
 */
@DAO(pojo=JoinTestDTO.class, table="join_test")
public class JoinDAO extends AbstractConventionDAO{


	
}
