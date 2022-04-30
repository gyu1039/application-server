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
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
        	
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "utf8"));
            String inputLine;
        	while((inputLine = br.readLine()) != null) {
        		
        		if(isRequestURL(br)) {
        			returnView(out); return;
        		}
        		if(inputLine.equals("")) break;
        	}
        	
        	DataOutputStream dos = new DataOutputStream(out);
            byte[] body = "Hello World".getBytes();
            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    // 요구사항 1
    private void printHTTPRequest(BufferedReader br) {
    	
    	String line;
    	List<String> tokens = new ArrayList<>();
    	while((line = getStringFromBR(br)) != null) {
    		if(line.equals("")) break;
    		tokens.add(line);
    	}
    	
    	System.out.println(tokens.toString());
    	return;
    }
    
    // 요구사항 2
    private boolean isRequestURL(BufferedReader br){
    	
    	List<String> firstLine = new ArrayList<>(List.of(getStringFromBR(br).split(" ")));
    	if(firstLine.contains("/index.html")) {
    		return true;
    	} else return false;
    	
    }
    
    // 요구사항 3
    private void returnView(OutputStream out) throws IOException {
    	
    	String url = "/index.html";
		byte[] body2 = Files.readAllBytes(new File("./webapp" + url).toPath());
		DataOutputStream dos = new DataOutputStream(out);
		response200Header(dos, body2.length);
		responseBody(dos, body2);
    }

    private String getStringFromBR(BufferedReader br){
    	
    	try {
    		return br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			
		}
		return null;
    }
    
    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
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
