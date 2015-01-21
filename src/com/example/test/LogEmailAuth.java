package com.example.test;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class LogEmailAuth extends Authenticator{
	
	private LogEmailInfor emailInfor;
	public LogEmailAuth(){
		if (emailInfor == null) {
			emailInfor = new LogEmailInfor();
		}
	}
	
	public PasswordAuthentication getPasswordAuthentication(){
		/**
    	 * 这个地方需要添加上自己的邮箱的账号和密码
    	 */
        String username = emailInfor.getEmailAdd(); 
        String pwd = emailInfor.getEmailPass();
        return new PasswordAuthentication(username, pwd); 
	}
	
}
