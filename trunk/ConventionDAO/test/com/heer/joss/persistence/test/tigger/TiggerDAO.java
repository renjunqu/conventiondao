﻿package com.heer.joss.persistence.test.tigger;

import com.heer.joss.persistence.test.biz.DAOTestDTO;
import com.rework.joss.persistence.convention.annotation.AbstractConventionDAO;
import com.rework.joss.persistence.convention.annotation.DAO;

/**
 * 
 * @author kevin zhang
 *
 */
@DAO(pojo=DAOTestDTO.class, table="dao_test")
public class TiggerDAO extends AbstractConventionDAO{


	
}