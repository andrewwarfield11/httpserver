
import java.net.*;
import java.util.Date;
import java.util.StringTokenizer;
import java.io.*;

public class HttpServer {

	private int portNumber;
	private Socket sock;
	private ServerSocket serverSock;

	public static void main(String[] args) {
		int portNum = 0;

		if(args.length == 1) {
			try {
				portNum = Integer.parseInt(args[0]);
			} 
			catch (NumberFormatException e) {
				System.err.println("Invalid portnumber");
				System.exit(0);
			}
		}
		else {
			System.err.println("Wrong number of arguments. The only argument must be the port number.");
			System.exit(0);
		}
		System.out.println("Port number is: " + portNum);
		HttpServer http = new HttpServer(portNum);
		http.start();
	}

	public HttpServer(int port) {
		portNumber = port;
		try {
			serverSock = new ServerSocket(portNumber);
		} catch (IOException e) {e.printStackTrace();}

	}

	public void start() {

		while(true) {
			try {
				sock = serverSock.accept();
				//System.out.println("Starting new thread");
				new Thread(new HttpConcurrent(sock)).start();

			} catch (IOException e) {e.printStackTrace();}
			
		}

	}
	
}
