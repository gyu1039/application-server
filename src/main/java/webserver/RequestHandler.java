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
import java.security.DigestOutputStream;
import java.util.Collection;
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

			BufferedReader br = new BufferedReader(new InputStreamReader(in, "utf8"));

			String line = br.readLine();

			if(line == null) {
				return;
			}
			
			String url = HttpRequestUtils.getUrl(line);
//			String method = HttpRequestUtils.getHTTPMethod(line);
			Map<String, String> requestHeader;

			DataOutputStream dos = new DataOutputStream(out);

			if(url.contains("/user/create")) {
				requestHeader = HttpRequestUtils.getHeaders(br, line);

				String requestBody = IOUtils.readData(br, Integer.parseInt(requestHeader.get("Content-Length")));
				log.debug("Request Body : {}", requestBody);
				Map<String, String> data = HttpRequestUtils.parseQueryString(requestBody);
				User user1 = new User(data.get("userId"), 
						data.get("password"), 
						data.get("name"),
						data.get("email"));
				
				DataBase.addUser(user1);
				log.debug("User : {} ", user1);
				url = "/index.html";
				response302Redirect(dos);
				
				return;
			} else if(url.equals("/user/login")) {
				requestHeader = HttpRequestUtils.getHeaders(br, line);

				String requestBody = IOUtils.readData(br, Integer.parseInt(requestHeader.get("Content-Length")));
				Map<String, String> param = HttpRequestUtils.parseQueryString(requestBody);
				User login = new User(param.get("userId"), 
						param.get("password"), 
						param.get("name"),
						param.get("email"));

				User checkingUser = DataBase.findUserById(param.get("userId"));

				if(checkingUser == null) {
					url = "/user/login_failed.html";
					responseResource(url, dos);
					log.debug("User Not Found!");
				} else if(login.getPassword().equals(checkingUser.getPassword())) {
					response302LoginSuccessHeader(dos);
					log.debug("login success!!");
				} else {
					url = "/user/login_failed.html";
					responseResource(url, dos);
					log.debug("Password Mismatch!");
				}

				return;
			} else if(url.contains("/user/list")) {
				requestHeader = HttpRequestUtils.getHeaders(br, line);
				String cookies = requestHeader.get("Cookie");
				if(cookies == null || !HttpRequestUtils.parseCookies(cookies).get("logined").equals("true")) {
					responseResource("/user/login.html", dos);
					return;
				}
				
				Collection<User> users = DataBase.findAll();
				StringBuilder sb = new StringBuilder();
				sb.append("<table border='1'>");
				for(User user : users) {
					sb.append("<tr>");
					sb.append("<td>" + user.getUserId() + "</td>");
					sb.append("<td>" + user.getName() + "</td>");
					sb.append("<td>" + user.getEmail() + "</td>");
					sb.append("</tr>");
				}
				sb.append("</table>");
				
				byte[] body = sb.toString().getBytes();
				response200Header(dos, body.length);
				responseBody(dos, body);
				return;
			}
			else if(url.endsWith(".css")) {
				responseCss(url, dos);
				return;
			}
			else responseResource(url, dos);


			

		} catch (IOException e) {
			log.error(e.getMessage());

		}
	}

	// 직접 구현해본 요구사항 3
	private void responseResource(String url, DataOutputStream dos) throws IOException {

		byte[] body;
		if(url.equals("/")) body = Files.readAllBytes(new File("./webapp/index.html").toPath());  
		else body = Files.readAllBytes(new File("./webapp" + url).toPath());
		response200Header(dos, body.length);
		responseBody(dos, body);
	}

	private void responseCss(String url, DataOutputStream dos) throws IOException {

		byte[] body;
		dos.writeBytes("HTTP/1.1 200 OK \r\n");
		dos.writeBytes("Content-Type: text/css;charset=	utf-8\r\n");

		if(url.equals("/")) body = Files.readAllBytes(new File("./webapp/index.html").toPath());  
		else body = Files.readAllBytes(new File("./webapp" + url).toPath());
		dos.writeBytes("Content-Length: " + body.length + "\r\n");
		dos.writeBytes("\r\n");
		responseBody(dos, body);
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
	
	private void response302Redirect(DataOutputStream dos) {
		try {
			dos.writeBytes("HTTP/1.1 302 Found \r\n");
			dos.writeBytes("Location: /index.html \r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void response302LoginSuccessHeader(DataOutputStream dos) {
		try {
			dos.writeBytes("HTTP/1.1 302 Found \r\n");
			dos.writeBytes("Location: /index.html \r\n");
			dos.writeBytes("Set-Cookie: logined = true \r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}


}
