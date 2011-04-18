package com.rework.joss.persistence.test.biz;
import java.sql.Clob;

import com.rework.core.dto.BaseObject;
 
public class MessageDTO extends BaseObject {
    public static final String FieldXm = "Message";
    public static final String FieldXb = "CreateDate";
    public static final String FieldZy = "CreateUser";
    public static final String FieldKsrq = "AccessUser";
    public static final String FieldNj = "ValidDate";
    public static final String FieldKslb = "Title";

    private String id;

	private String  message;      
	private String createDate;  
	private String createUser;            
	private String accessUser;            
	private String validDate;             
	private String title;    

	public MessageDTO() {
		super();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * @return
	 */
	public String getAccessUser()
	{
		return accessUser;
	}

	/**
	 * @return
	 */
	public String getCreateDate()
	{
		return createDate;
	}

	/**
	 * @return
	 */
	public String getCreateUser()
	{
		return createUser;
	}

	/**
	 * @return
	 */
	public String getMessage()
	{
		return message;
	}

	/**
	 * @return
	 */
	public String getTitle()
	{
		return title;
	}

	/**
	 * @return
	 */
	public String getValidDate()
	{
		return validDate;
	}

	/**
	 * @param string
	 */
	public void setAccessUser(String string)
	{
		accessUser = string;
	}

	/**
	 * @param string
	 */
	public void setCreateDate(String string)
	{
		createDate = string;
	}

	/**
	 * @param string
	 */
	public void setCreateUser(String string)
	{
		createUser = string;
	}

	/**
	 * @param string
	 */
	public void setMessage(String  string)
	{
		message = string;
	}
//	public void setMessage(String  string)
//	{
//		message = new Clob(string);
//	}

	/**
	 * @param string
	 */
	public void setTitle(String string)
	{
		title = string;
	}

	/**
	 * @param string
	 */
	public void setValidDate(String string)
	{
		validDate = string;
	}

  }
