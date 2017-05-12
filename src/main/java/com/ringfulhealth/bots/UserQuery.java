package com.ringfulhealth.bots;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="userquery")
public class UserQuery implements Serializable {
	private String userId;
	private int lastQueryType;
	private String lastQueryText;
	
	public static final int POLLING_LOCATION = 1;
	public static final int WHO_ARE_REPRESENTATIVES = 2;
	public static final int CONTACT_REPRESENTATIVES = 3;
	
	public UserQuery(){
		userId = "";
		lastQueryType = -1;
		lastQueryText = "";
	}
	
	public String getUserId() { return userId; }
	public void setUserId(String uid) { userId = uid; }
	
	public int getLastQueryType() { return lastQueryType; }
	public void setLastQueryType(int lqt) { lastQueryType = lqt; }
	
	public String lastQueryText() { return lastQueryText; }
	public void setLastQueryText(String lqt) { lastQueryText = lqt; }
	
}
