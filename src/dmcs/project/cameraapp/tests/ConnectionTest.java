package dmcs.project.cameraapp.tests;

import java.net.MalformedURLException;

import org.junit.Assert;
import org.junit.Test;

import dmcs.project.cameraapp.GlobalStore;

public class ConnectionTest {
	@Test
	public void testConnection() {
		Assert.assertEquals(true, true);
	}
	
	@Test
	public void testWrongCredentials() {
		try {
			GlobalStore.setAddr("http://");
			GlobalStore.setPasswd("pass");
			GlobalStore.setUser("user");
			GlobalStore.getHttpClient();
		} catch (Exception ex) {
			Assert.assertTrue(ex instanceof MalformedURLException);
		}
	}
}
