package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.Socket;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;
import sun.misc.IOUtils;

public class RequestHandler extends Thread {
	private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
	
	private Socket connection;

	public RequestHandler(Socket connectionSocket) {
		this.connection = connectionSocket;
	}

	public void run() {
		log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(), connection.getPort());
		
		try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
			// TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
			Reader reader = new InputStreamReader(in);
			BufferedReader buffer = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			
			String line = buffer.readLine();
			String[] tokens = line.split(" ");
			
			String url = tokens[1];
			if(url.startsWith("/create"))
			{
				String []accToken = line.split("=|&| ");
				User user = new User(accToken[2],accToken[4],accToken[6],accToken[8]);	
				
				System.out.println(user.getUserId());
				System.out.println(user.getPassword());
				System.out.println(user.getName());
				System.out.println(user.getEmail());
				DataBase.addUser(user);
				util.IOUtils.readData(buffer,tokens[1].length());
				
				url = "/index.html";
				DataOutputStream dos = new DataOutputStream(out);
				response302Header(dos);
			}
			else if(url.equals("/"))
			{
				url = "/index.html";
			}
			
			
			byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
			DataOutputStream dos = new DataOutputStream(out);
			response200Header(dos, body.length);
			responseBody(dos, body);
			
		} catch (IOException e) {
			log.error(e.getMessage());
		}	
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
	
	private void response302Header(DataOutputStream dos) {
		try {
			dos.writeBytes("HTTP/1.1 302 Found \r\n");
			dos.writeBytes("Loation: http://localhost:8080/index.html");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	
	private void responseBody(DataOutputStream dos, byte[] body) {
		try {
			dos.write(body, 0, body.length);
			dos.writeBytes("\r\n");
			dos.flush();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
}
