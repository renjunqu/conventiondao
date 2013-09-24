package com.rework.joss.persistence.test.biz;
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
	private String url;             
	private String usernmae;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getUsernmae() {
		return usernmae;
	}
	public void setUsernmae(String usernmae) {
		this.usernmae = usernmae;
	}    

  }
