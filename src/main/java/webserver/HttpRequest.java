package webserver;

import java.io.InputStream;

public class HttpRequest {

	InputStream inputStream;
	
	public HttpRequest() {}
	
	public HttpRequest(InputStream is) {
		this.inputStream = is;
	}
}
