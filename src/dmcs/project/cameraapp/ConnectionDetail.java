package dmcs.project.cameraapp;

public class ConnectionDetail {
	private String addr;
	private String user;
	private String passwd;
	
	public ConnectionDetail(String addr, String user, String passwd) {
		this.addr = addr;
		this.user = user;
		this.passwd = passwd;
	}
	
	public String getAddr() {
		return this.addr;
	}
	
	public String getUser() {
		return this.user;
	}
	
	public String getPasswd() {
		return this.passwd;
	}

	public void setAddr(String string) {
		this.addr = string;
		
	}

	public void setUser(String string) {
		this.user = string;
		
	}

	public void setPasswd(String string) {
		this.passwd = string;
		
	}
}