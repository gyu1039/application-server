package webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.HttpRequestUtils;
import util.IOUtils;

public class HttpRequest {
	
	private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);

	private RequestLine requestLine;
	private Map<String, String> headers = new HashMap<String, String>();
	private Map<String, String> params = new HashMap<String, String>();
	
	public HttpRequest() {}
	
	public HttpRequest(InputStream is) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String line = br.readLine();
			if(line == null) return;
			
			requestLine = new RequestLine(line); 
			
			line = br.readLine();
			while(line != null && !line.equals("")) {
				log.debug("header : {}", line);
				String[] tokens = line.split(":");
				headers.put(tokens[0].trim(), tokens[1].trim());
				line = br.readLine();
			}
			
			if(HttpMethod.POST == getMethod()) {
				String body = IOUtils.readData(br, Integer.parseInt(headers.get("Content-Length")));
				params = HttpRequestUtils.parseQueryString(body);
			} else {
				params = requestLine.getParams();
			}
			
		} catch(IOException io) {
			log.error(io.getMessage());
		}
	}
	
	public HttpCookie getCookies() {
		return new HttpCookie(getHeader("Cookie"));
	}
	
	public HttpSession getSession() {
		return HttpSessions.getSession(getCookies().getCookie("JSESSIONID"));
	}
	
	public HttpMethod getMethod() {
		return requestLine.getMethod();
	}
	
	public String getPath() {
		return requestLine.getPath();
	}
	
	public String getHeader(String name) {
		return headers.get(name);
	}
	
	public String getParameter(String name) {
		return params.get(name);
	}
	
	
	
}
