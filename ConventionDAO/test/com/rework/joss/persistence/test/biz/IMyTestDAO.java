package com.rework.joss.persistence.test.biz;

import java.util.List;

import com.rework.joss.persistence.IBaseDAO;

/**
 * IBaseDAO扩展接口,如果需要扩展基本的DAO实现,自定义的DAO实现需要extends IBaseDAO
 * @author zhujj
 * @create 2007-11-3
 */
@ConventionDAO(table = "sns_block", vo = "com.rework.domain.SnsBlock")
public interface IMyTestDAO extends IBaseDAO{
	public List getTestByTestname(String testname);
	
	public DAOTestDTO getTestByTestId(String testId);
	
	public int getMaxTestInt();
	
	public List queryDAOTest(DAOTestDTO query);
}
