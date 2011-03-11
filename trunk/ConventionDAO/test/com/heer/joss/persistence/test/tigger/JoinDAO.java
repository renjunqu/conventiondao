package com.heer.joss.persistence.test.tigger;

import com.heer.joss.persistence.test.biz.JoinTestDTO;
import com.rework.joss.persistence.convention.annotation.AbstractConventionDAO;
import com.rework.joss.persistence.convention.annotation.DAO;

/**
 * 
 * @author kevin zhang
 *
 */
@DAO(pojo=JoinTestDTO.class, table="join_test")
public class JoinDAO extends AbstractConventionDAO{


	
}
