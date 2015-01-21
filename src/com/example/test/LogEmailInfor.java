package com.example.test;


/***
 * 发送错误日志model
 * @author Administrator
 *
 */
public class LogEmailInfor {
	private String emailAdd = "xxxx@163.com";
	private String emailPass = "xxxxx";
	private String emailPort = "25";
	private String emailFrom = "发送来自xxxx";
	private String emailCome = "接收来自xxxx";
	private String emailHost = "smtp.163.com";
	private String emailSubject = "xxxxx";
	public String getEmailAdd() {
		return emailAdd;
	}
	public void setEmailAdd(String emailAdd) {
		this.emailAdd = emailAdd;
	}
	public String getEmailPass() {
		return emailPass;
	}
	public void setEmailPass(String emailPass) {
		this.emailPass = emailPass;
	}
	public String getEmailPort() {
		return emailPort;
	}
	public void setEmailPort(String emailPort) {
		this.emailPort = emailPort;
	}
	public String getEmailFrom() {
		return emailFrom;
	}
	public void setEmailFrom(String emailFrom) {
		this.emailFrom = emailFrom;
	}
	public String getEmailCome() {
		return emailCome;
	}
	public void setEmailCome(String emailCome) {
		this.emailCome = emailCome;
	}
	public String getEmailHost() {
		return emailHost;
	}
	public void setEmailHost(String emailHost) {
		this.emailHost = emailHost;
	}
	public String getEmailSubject() {
		return emailSubject;
	}
	public void setEmailSubject(String emailSubject) {
		this.emailSubject = emailSubject;
	}
	
	
	
	
}
