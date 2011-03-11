/* 
 * Copyright (c) 2004-2007 HEER Software, Inc. All rights reserved.
 *
 * This software consists of contributions made by many individuals
 * on behalf of Heer R&D.  For more information,
 * please see <http://www.heerit.com/>.
 *
 */
package com.heer.joss.persistence.test.tigger;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;

import com.heer.joss.persistence.test.BaseDAOTestCase;
import com.heer.joss.persistence.test.biz.DAOTestDTO;
import com.heer.joss.persistence.test.biz.JoinTestDTO;
import com.rework.joss.persistence.IBaseDAO;
import com.rework.joss.persistence.convention.ConventionUtils;

public class TiggerDAOTest extends BaseDAOTestCase {

	@Resource(type=TiggerDAO.class)
	TiggerDAO tiggerDAO;

	@Autowired
	JoinDAO joinDAO;
	
	public void testTpl(){
		DAOTestDTO data = new DAOTestDTO();
		data.setTestint(new Integer(20));
		data.setTestname("TESTNAME1");
		data.setId("test_id_000");
		data.setTestId("ttest");
		tiggerDAO.create(data);
		
		List list = tiggerDAO.queryByTpl(
				"select *from dao_test where testname = '${name}'", 
				ConventionUtils.toMap(new String[]{"name", "TESTNAME1"}));
		list.size();;
		
	}
	
	public void testPreLoad(){
		DAOTestDTO data = new DAOTestDTO();
		data.setTestint(new Integer(20));
		data.setTestname("TESTNAME1");
		data.setId("test_id_000");
		data.setTestId("ttest");
		data.setJoinTestId( "testtest" );
		tiggerDAO.create(data);
		
		JoinTestDTO	testdto = new JoinTestDTO();
		testdto.setJoinColumn( "jiontest" );
		testdto.setTestId( data.getJoinTestId() );
		joinDAO.create( testdto );

		data.setTestId("ttest2");
		data.setJoinTestId( "testtest2" );
		tiggerDAO.create(data);
		
		testdto.setTestId( data.getJoinTestId() );
		joinDAO.create( testdto );

		
		// List<DAOTestDTO> list = tiggerDAO.leftjoin(joinDAO, "testlist", "join_test_id").query( );
		List<DAOTestDTO> list = tiggerDAO.queryByTpl("", ConventionUtils.toMap( IBaseDAO.PARAM_FETCH_PROPERTIES, "testlist:order by test_id desc" ));
		// 更复杂的例子
		// List<DAOTestDTO> list2 = tiggerDAO.queryByTpl("", UtilMisc.toMap( IBaseDAO.PARAM_FETCH_PROPERTIES, "testlist:order by test_id desc|leftjoin:testlist2:order by test_id desc" ));
		list.get(0).getTestlist().size();
		
	}
	
}
