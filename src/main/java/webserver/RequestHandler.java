package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); 
        		OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
        	
        	BufferedReader br = new BufferedReader(new InputStreamReader(in, "utf8"));
        	DataOutputStream dos = new DataOutputStream(out);
//      
//        	br.mark(262144);
//        	printRequestHeader(br);
//        	br.reset();
        	
        	// Extracting URL 
        	String line = br.readLine();
        	log.debug("line : {}", line);
        	if(line == null) {
        		return;
        	}
        	
        	String url = HttpRequestUtils.getUrl(line);
        	String method = HttpRequestUtils.getHTTPMethod(line);
        	Map<String, String> headers = getHeaders(br, line);
        	
        		
        	if(url.contains("/user/create")) {
        		String requestBody = IOUtils.readData(br, Integer.parseInt(headers.get("Content-Length")));
        		log.debug("Request Body : {}", requestBody);
        		Map<String, String> data = HttpRequestUtils.parseQueryString(requestBody);
        		User user1 = new User(data.get("userId"), 
        				data.get("password"), 
        				data.get("name"),
        				data.get("email"));
        		DataBase.addUser(user1);
        		log.debug("User : {} ", user1);
        		response302Header(dos);
        		return;
        	}
        	
        	if(method.equals("POST") && url.contains("/user/login")) {
        		String requestBody = IOUtils.readData(br, Integer.parseInt(headers.get("Content-Length")));
        		log.debug("Request Body : {}", requestBody);
        		Map<String, String> param = HttpRequestUtils.parseQueryString(requestBody);
        		User login = new User(param.get("userId"), 
        				param.get("password"), 
        				param.get("name"),
        				param.get("email"));
        		log.debug("user : {}, password: {} ", param.get("userId"), param.get("password"));
        		
        		User checkingUser = DataBase.findUserById(param.get("userId"));
        		
        		if(checkingUser == null) {
        			response302HeaderWithCookie(dos, "logined=false", "/user/login_failed.html");
        			log.debug("User Not Found!");
        		} else if(login.getPassword().equals(checkingUser.getPassword())) {
        			response302HeaderWithCookie(dos, "logined=true", "/index.html");
        			log.debug("login success!!");
        		} else {
        			response302HeaderWithCookie(dos, "logined=false", "/user/login_failed.html");
        			log.debug("Password Mismatch!");
        		}
        		
        		return;
        	}
        	
        	
//        	String cookies;
//        	Map<String, String> getCookie = null;
//        	
//        	if(headers.get("cookie") != null) {
//        		cookies = headers.get("cookie");
//            	getCookie = HttpRequestUtils.parseCookies(cookies);
//        	}
//        	
//        	
//        	if(getCookie.get("logined").equals("true") && url.contains("/user/list")) {
//        		url = "/user/list.html";
//        		
//        	} else if(url.contains("/user/list") && getCookie.get("logined").equals("false")) {
//        		
//        	}
//        	
        	if(url.endsWith(".css")) {
        		css(url, dos);
        		return;
        	}
    		viewPage(url, dos);
        	
        } catch (IOException e) {
            log.error(e.getMessage());
            
        }
    }
  
    // 직접 구현해본 요구사항 3
    private void viewPage(String url, DataOutputStream dos) throws IOException {
    	
    	byte[] body;
    	if(url.equals("/")) body = Files.readAllBytes(new File("./webapp/index.html").toPath());  
    	else body = Files.readAllBytes(new File("./webapp" + url).toPath());
    	response200Header(dos, body.length);
    	responseBody(dos, body);
    }
    
    private void css(String url, DataOutputStream dos) throws IOException {

    	byte[] body;
    	dos.writeBytes("HTTP/1.1 200 OK \r\n");
		dos.writeBytes("Content-Type: text/css;charset=	utf-8\r\n");
		
    	if(url.equals("/")) body = Files.readAllBytes(new File("./webapp/index.html").toPath());  
    	else body = Files.readAllBytes(new File("./webapp" + url).toPath());
    	
    	dos.writeBytes("Content-Length: " + body.length + "\r\n");
        dos.writeBytes("\r\n");
    	responseBody(dos, body);
    }
    
    private Map<String, String> getHeaders(BufferedReader br,String line) throws IOException {
    	Map<String, String> headers = new HashMap<String, String>();
    	
    	while(!"".equals(line)) {
    		log.debug("header : {}", line);

    		line = br.readLine();
    		String[] headerTokens = line.split(": ");
    		if(headerTokens.length == 2) {
    			headers.put(headerTokens[0], headerTokens[1]);        			
    		}
    	}
    	
    	return headers;
    }


    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
    	try {
    		dos.writeBytes("HTTP/1.1 200 OK \r\n");
    		dos.writeBytes("Content-Type: text/html;charset=	utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
 
    private void response302Header(DataOutputStream dos) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: http://localhost:8080/index.html\r\n");
            dos.writeBytes("Content-Type: text/html;charset=	utf-8\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    private void response302HeaderWithCookie(DataOutputStream dos, String cookie, String url) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: http://localhost:8080" + url + "\r\n");
            dos.writeBytes("Set-Cookie: " + cookie + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
 
//    private boolean isLogin(String line) {
//    	String[] headerTokens = line.split(":");
//    	Map<String, String> cookies = HttpRequestUtils.parseCookies(headerTokens[1].trim());
//    	String value = cookies.get("logined");
//    }
}
