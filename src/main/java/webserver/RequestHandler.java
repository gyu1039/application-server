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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.HttpRequestUtils;

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
      
        	br.mark(262144);
        	printRequestHeader(br);
        	br.reset();
        	
        	// Extracting URL 
        	String line = br.readLine();
        	String url = HttpRequestUtils.getUrl(line);
        	
        	if(url.equals("/index.html")) {
        		returnView(out, url, dos);
        		return ;
        	}
        	
            byte[] body = "Hello World".getBytes();
            response200Header(dos, body.length);
            responseBody(dos, body);
            
        } catch (IOException e) {
            log.error(e.getMessage());
            
        }
    }
    
    // 직접 구현해본 요구사항 1
    private void printRequestHeader(BufferedReader br){
    	
    	String line;
    	try {
			while((line = br.readLine()) != null) {
				if(line.equals("") || line == null) break;
				log.debug("Request Header = {}", line);
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return;
    }
    
    // 직접 구현해본 요구사항 2
	
    private boolean isRequestURL(BufferedReader br){

    	String firstLine;
		try {
			firstLine = (br.readLine().split(" "))[1];
			if(firstLine.equals("/index.html")) return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return false;

    }

    
    // 직접 구현해본 요구사항 3
    private void returnView(OutputStream out, String url, DataOutputStream dos) throws IOException {
    	
		byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
		response200Header(dos, body.length);
		responseBody(dos, body);
    }

    
    private void Requirement_1(BufferedReader br) throws IOException {
    	
    	String line = br.readLine();
    	if(line == null) return;
    	
    	while(!"".equals(line)) {
    		log.debug("header : {} ", line);
    		line = br.readLine();
    	}
    	
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

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
