package webserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.junit.Test;

public class HttpRequestTest {

	private String testDirectory = "./src/test/resources/";
	
	@Test
	public void request_GET() throws Exception {
		InputStream is = new FileInputStream(new File(testDirectory + "Http_GET.txt"));
		
		HttpRequest request = new HttpRequest(is);
	}
}
