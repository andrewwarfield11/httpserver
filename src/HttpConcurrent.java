import java.net.*;
import java.util.StringTokenizer;
import java.io.*;


public class HttpConcurrent implements Runnable {

	private int type;
	private static final int GET = 0;
	private static final int HEAD = 1;
	private String fileName;
	private int statusCode; 
	private static final String CLRF = "\r\n";
	private Socket sock;
	private PrintWriter printer;


	public HttpConcurrent(Socket socket) {
		sock = socket;
		printer = null;
	}

	public void run() {
		start();
	}

	public void start() {
		//System.out.println("New thread stuff happening");
		// initializing them
		String line = "";
		BufferedReader in = null;

		try {

			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));

			String firstLine = in.readLine();
			boolean hasHostLine = false;

			if(firstLine == null) {
				end();
			}

			line = firstLine;
			while(!line.isEmpty()) {
				System.out.println(line);
				if(line.startsWith("Host")) {
					hasHostLine = true;
				}
				line = in.readLine();
			}

			if(firstLine.startsWith("GET") || firstLine.startsWith("HEAD")) {
				if(firstLine.startsWith("GET"))
					type = GET;
				else
					type = HEAD;
				int slash1 = 0;
				int space = 0;
				boolean requestingFile;
				//boolean hasHostLine = false;
				boolean favicon = false;
				int fileLength = 0;
				String pHtmlFileRequested;
				for(int i = 0; i < firstLine.length(); i++) {
					// location of first /
					if(slash1 == 0 && firstLine.charAt(i) == '/') {
						slash1 = i;
					}
					// location of first space after first /
					else if(firstLine.charAt(i) == ' ' && slash1 != 0) {
						space = i;
					}
				}
				String fileRequested = firstLine.substring(slash1, space);
				if(fileRequested.length() > 1) {
					pHtmlFileRequested = "public_html/" + fileRequested;
					requestingFile = true;
					fileName = pHtmlFileRequested;

					favicon = false;
					File file = new File(fileName);
					if(file.exists() && !file.isDirectory()) {
						statusCode = 200;
						fileLength = (int)file.length(); 
						line = firstLine;
					}
					else {
						statusCode = 404;
					}
				}
				else {
					requestingFile = false;
					statusCode = 200;
				}

				if(!hasHostLine) {
					// bad request
					statusCode = 400;
				}

				writeHeader(fileLength, favicon);


			}
			else if(firstLine.startsWith("OPTIONS") || firstLine.startsWith("POST") || firstLine.startsWith("PUT") ||
					firstLine.startsWith("DELETE") || firstLine.startsWith("TRACE") || firstLine.startsWith("CONNECT")) {
				//System.err.println("Method requested is not implemented.");
				statusCode = 501;
				writeHeader(0, false);
			}
			else {
				// bad request
				statusCode = 400;
				writeHeader(0, false);
			}

			// else return error code

		} catch (IOException e) {e.printStackTrace();}

	}

	// these are the methods used to write the header
	public void writeHeader(int fileLength, boolean fav) {
		try {
			printer = new PrintWriter(sock.getOutputStream());
		} catch (IOException e) {e.printStackTrace();}

		//System.out.println("\nRequest to browser: " );
		printer.write(writeLineOne()); // status code line
		printer.write(writeLineTwo()); // server line
		printer.write(writeLineThree()); // content-type line
		printer.write(writeLineFour(fileLength)); // content-length line
		printer.write(CLRF);
		printer.flush();
		if(type == GET && fav == false) {
			writeBody(fileLength);
		}
	}

	public String writeLineOne() {
		if(statusCode == 200) {
			System.out.println("HTTP/1.1 200 OK");
			return "HTTP/1.1 200 OK" + CLRF;
		}
		else if(statusCode == 501) {
			System.out.println("HTTP/1.1 501 Not Implemented");
			return "HTTP/1.1 501 Not Implemented" + CLRF;
		}
		else if(statusCode == 400) {
			System.out.println("HTTP/1.1 400 Bad Request");
			return "HTTP/1.1 400 Bad Request" + CLRF;
		}
		else if(statusCode == 404) {
			System.out.println("HTTP/1.1 404 Not Found");
			return "HTTP/1.1 404 Not Found" + CLRF;
		}
		else 
			return "";
	}

	public String writeLineTwo() {
		System.out.println("Server: CS4333 Java Http Server/1.1");
		return "Server: Java Test Server" + CLRF;
	}
	public String writeLineThree() {
		System.out.println("Content-type: " + getContentType(fileName));
		return "Content-type: " + getContentType(fileName) +CLRF;
	}
	public String writeLineFour(int l) {
		System.out.println("Content-length: " + l);
		return "Content-length: " + l + CLRF;
	}


	public void writeBody(int fileLen) {

		FileInputStream fileStream = null;
		ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
		OutputStream out6 = null;
		byte[] data = new byte[fileLen];

		try {
			File file = new File(fileName);
			if(file.exists()) {
				fileStream = new FileInputStream(file);
				fileStream.read(data);
				fileStream.close();
			}
			out6 = sock.getOutputStream();

		} catch (FileNotFoundException e) {e.printStackTrace();} catch (IOException e) {e.printStackTrace();}
		try {

			byteOutput.write(data);
			byteOutput.writeTo(out6);
			out6.flush();
			byteOutput.close();
			out6.close();

		} catch (IOException e) {e.printStackTrace();}


	}
	public String getContentType(String fileName) {
		if(fileName.endsWith(".htm") || fileName.endsWith(".html")) 
			return "text/html";
		else if(fileName.endsWith(".gif"))
			return "image/gif";
		else if(fileName.endsWith(".jpeg") || fileName.endsWith(".jpg"))
			return "image.jpeg";
		else if(fileName.endsWith(".pdf"))
			return "application/pdf";
		else
			return ""; // this shouldn't ever happen, will probably add in a better error checking method later
	}

	public void end() {
		try {
			//System.out.println("ending this thread");
			sock.close();
		} catch (IOException e) {e.printStackTrace();}
	}

}
