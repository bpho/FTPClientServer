//Original Eclipse Version
//*Submitted Version*
import java.io.*;
import java.net.*;
public class FTPClient {
	public static String currentRequest = "";
	public static boolean connectSuccessful = false;
	public static boolean connectCleared = false;
	public static String inputHostName = "";
	public static int inputPortNum = 0;
	public static boolean connected = false;
	public static String sentence;
	public static String modifiedSentence;
	public static int portNumber = 9925;	//THIS WILL BE HARDCODED AS SS# 
	public static int defaultPort = 8080;	//DEFAULT PORT WILL BE ARGS[0] for actual program
	public static String portString = "";
	public static String retrString = "";
	public static boolean getAccepted = false;
	public static int generatedPortNum = 0;
	public static int retrCounter = 1;
	public static String fileString = "";

	public static Socket clientSocket;
	public static DataOutputStream outToServer;
	public static BufferedReader inFromServer;
	
	public static void main(String[] args) throws NumberFormatException, IOException{
		boolean checkReq = false;
		generatedPortNum = Integer.parseInt(args[0]);
		String hostName = "localhost";
		
		//Create buffered input stream using standard input
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

		while ((sentence = inFromUser.readLine()) != null) {
			String input = sentence;
			System.out.println(input);
			checkReq = checkRequest(input);
			
			if (checkReq == false){
				System.out.println("ERROR -- request");
			} else {
				if (currentRequest == "connect"){
					connectSuccessful = true;
					checkHost(input);
					//If the request is valid, use another method to print whatever follows
					if (connectCleared == true){
//						Socket clientSocket = new Socket(inputHostName, portNumber); //uncomment this, when time to use actual server
						try {
							newConnection();
							System.out.print("CONNECT accepted for FTP server at host " +inputHostName +" and port " +inputPortNum +"\r\n");
							
							outToServer.writeBytes(input +'\n');
							outToServer.flush();
							modifiedSentence = inFromServer.readLine();
							System.out.println(modifiedSentence);
							
							System.out.print("USER anonymous" +"\r\n");
							outToServer.writeBytes("USER anonymous" +'\n');
							outToServer.flush();
							modifiedSentence = inFromServer.readLine();
							System.out.println(modifiedSentence);
							
							System.out.print("PASS guest@" +"\r\n");
							outToServer.writeBytes("PASS guest@" +'\n');
							outToServer.flush();
							modifiedSentence = inFromServer.readLine();
							System.out.println(modifiedSentence);

							System.out.print("SYST" +"\r\n");
							outToServer.writeBytes("SYST" +'\n');
							outToServer.flush();
							modifiedSentence = inFromServer.readLine();
							System.out.println(modifiedSentence);

							System.out.print("TYPE I" +"\r\n");
							outToServer.writeBytes("TYPE I" +'\n');
							outToServer.flush();
							modifiedSentence = inFromServer.readLine();
							System.out.println(modifiedSentence);

							connected = true;
						} catch (IOException e){
							System.out.println("CONNECT failed");
						}
					}
				} else if (currentRequest == "quit"){
					if (connectSuccessful == true){
						if (input.length() > 4){
							if (input.charAt(4) != '\r'){
								System.out.println("ERROR -- request");
							}
						} else {
							System.out.println("QUIT accepted, terminating FTP client");
							System.out.print("QUIT" +"\r\n");
							outToServer.writeBytes("QUIT" +'\n');
							modifiedSentence = inFromServer.readLine();
							System.out.println(modifiedSentence);
							clientSocket.close();
							System.exit(0);
						}
					
					} else {
						System.out.println("ERROR -- expecting CONNECT");
					}
				} else if (currentRequest == "get"){
					//Check pathname
					checkPath(input);
//					defaultPort2++;	//use it with real input
					ServerSocket welcomeclientSocket = new ServerSocket(generatedPortNum);
//					System.out.println("*FTP-data welcome port created for " +generatedPortNum +"*");
					System.out.print(portString);
					outToServer.writeBytes(portString);
					outToServer.flush();
					modifiedSentence = inFromServer.readLine();
					System.out.println(modifiedSentence);

					//establishing the welcome port
					try {
						System.out.print(retrString);
						outToServer.writeBytes(retrString);
						outToServer.flush();
						Socket connectionSocket = welcomeclientSocket.accept();
//						System.out.println("*Connection socket accepted*");
						modifiedSentence = inFromServer.readLine();
						System.out.println(modifiedSentence);
						
						if (modifiedSentence.contains("150")){
							File newFile = new File ("file" +retrCounter);
							InputStream in = connectionSocket.getInputStream();
							OutputStream out = new FileOutputStream("retr_files/" +newFile);
							
							byte[] buffer = new byte[1024];
							int length;
							//copy the file into directory
							while ((length = in.read(buffer)) > 0){
								out.write(buffer, 0, length);
							}
							
							System.out.println("FTP reply 250 accepted. Text is: Requested file action completed.");
							in.close();
							out.close();
							retrCounter++;	
						}	
						connectionSocket.close();
					} catch (IOException e){
						System.out.println("GET failed, FTP-data port not allocated");
					}	
					defaultPort++;	//increment by one every time
					generatedPortNum++;
				}
			}
		}	
	}
	
	public static void newConnection(){
		if (connected == false){
			try {
				// clientSocket = new Socket("localhost", portNumber);
				clientSocket = new Socket(inputHostName, inputPortNum);
				outToServer = new DataOutputStream(clientSocket.getOutputStream());
				inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));		
			} catch (IOException e){
				System.out.println("CONNECT failed");
			}
		}
		else if (connected == true && currentRequest == "connect"){
			//set new clientSocket (outToServer and inFromServer) stays the same
			try {
//				clientSocket.close();
				// clientSocket = new Socket("localhost", portNumber);
				clientSocket = new Socket(inputHostName, inputPortNum);
				System.out.println("New Socket");	//debugger
			} catch (IOException e){
				System.out.println("CONNECT failed");
			}	
		}	
	}
	
	public static boolean checkRequest(String input){
		boolean requestFound = false;
		String request = input.toLowerCase();
		String testString = "";
		String result = "";
		StringBuilder builder = new StringBuilder(testString);
		
		for (int i = 0; i < request.length(); i++){
			if (requestFound == true){
				break;
			}
			char currentChar = request.charAt(i);
			builder.append(currentChar);
			if (i == 3 && currentChar == ' '){
				String possibleGet = builder.toString();
				if (possibleGet.equals("get ")){
					result = "get";
					requestFound = true;
					break;
				}
			} else if (i == 3 && currentChar != ' '){
				String possibleQuit = builder.toString();
				if (possibleQuit.equals("quit")){
					result = "quit";
					requestFound = true;
					break;
				}
			} else if (i == 7 && currentChar == ' '){
				String possibleConnect = builder.toString();
				if (possibleConnect.equals("connect ")){
					result = "connect";
					requestFound = true;
					break;
				}
			} else if (i > 7){
				//result = "ERROR -- request";
				requestFound = false;
				break;
			}
		}
		
		currentRequest = result;
		return requestFound;
	}
	
	public static void checkHost(String input){
		boolean serverhostError = false;
		boolean stringSpotted = false;
		int serverhostLength = 0;
		String blankString = "";
		StringBuilder builder = new StringBuilder(blankString);
		for (int i = 7; i < input.length(); i++){
			char currentChar = input.charAt(i);
			int asc = input.charAt(i);
			serverhostLength++;
			if (currentChar != ' ' || stringSpotted == true){
				//if stringspotted is still false, meaning it's the first character
				//and it's not a letter, return serverhost error
				if (stringSpotted == false){
					if (!(asc >= 0x41 && asc <= 0x5A) && !(asc >= 0x61 && asc <= 0x7A)){
						serverhostError = true;
						break;
					}
				}
				builder.append(currentChar);
				stringSpotted = true;
				//a space means the end of the hostName/else if string is spotted, but ends before a white space is detected
				//error -- server-host
				if (stringSpotted == true && currentChar == ' '){
					break;
				} else if (stringSpotted == true && currentChar != ' '){
					if (i == input.length() - 1){
						serverhostError = true;
						break;
					} else if (currentChar == '.'){	//if a '.' is spotted, treat as if it's testing <element> again
						stringSpotted = false;
					}
				} 
				//if any of the characters contain non-ascii, set serverhostError = true and break
				if (!(asc >= 0x41 && asc <= 0x5A) && !(asc >= 0x61 && asc <= 0x7A) && !(asc >= 0x30 && asc <= 0x39) && currentChar != '.'){
					serverhostError = true;
					break;
				}
			}
		}
		String serverhostName = builder.toString();
		String trimmedhostName = serverhostName.trim();
		inputHostName = trimmedhostName;
		int finalLength = (7+serverhostLength); //checkport starts at char position (finalLength+1)
		if (serverhostError == true){
			System.out.println("ERROR -- server-host");
		} else {
			//call checkPort next
			checkPort(trimmedhostName, input, finalLength);
		}
		
	}
	public static void checkPort(String hostName, String input, int Start){
		boolean serverportError = false;
		boolean stringSpotted = false;
		String blankString = "";
		StringBuilder builder = new StringBuilder(blankString);
		for (int i = Start; i < input.length(); i++){
			char currentChar = input.charAt(i);
			int asc = input.charAt(i);
			if (currentChar != ' '){
				builder.append(currentChar);
				stringSpotted = true;
				
				//If any of the characters are NOT decimal values
				if (!(asc >= 0x30 && asc <= 0x39)){
					serverportError = true;
					break;
				}
			}
			
			//If a decimal value has already been spotted, and you come across a whitespace
			if (currentChar == ' ' && stringSpotted == true){
				serverportError = true;
				break;
			}
		}
		//If the entire port is white spaces
		if (stringSpotted == false){
			serverportError = true;
		}
		
		//If the final decimal value exceeds or falls below 0
		String serverportNumber = builder.toString();
		String newserverPort = serverportNumber.trim();
		//int decimalValue = Integer.parseInt(newserverPort);
		int decimalValue = 0;
	    boolean parsable = true;
	    //Check if the string is even parseable or if it's an invalid String
	    try{
	        decimalValue = Integer.parseInt(newserverPort);
	    }catch(NumberFormatException e){
	        parsable = false;
	    }
	    if (parsable == false){
	    	serverportError = true;
	    }
		if (!(decimalValue >= 0 && decimalValue <= 65535)){
			serverportError = true;
		}
		
		if (serverportError == true){
			System.out.println("ERROR -- server-port");
		} else {
			inputPortNum = Integer.parseInt(newserverPort);
			connectCleared = true;
		}
	}
	
	public static void checkPath(String input) throws UnknownHostException{
		//Check if pathname consists of non-ascii, if it does, throw error in pathname
		//or if it contains \r or \n
		boolean stringSpotted = false;
		boolean pathnameError = false;
		String blankString = "";
		StringBuilder builder = new StringBuilder(blankString);
		for (int i = 3; i < input.length(); i++){
			char currentChar = input.charAt(i);
			int asc = input.charAt(i);
			if (currentChar != ' ' || stringSpotted == true){
				stringSpotted = true;
				if (asc > 0x7f || asc == 0x0A || currentChar == '\r'){
					pathnameError = true;
					break;
				}
				builder.append(currentChar);
			} else if (currentChar == ' ' && i == input.length()-1){ //checks if it's just all whitespace
				pathnameError = true;
				break;
			}
		}
		String pathName = builder.toString();
		if (pathName.charAt(0) == '/' || pathName.charAt(0) == '\\'){
			pathName = pathName.substring(1);
		}
		fileString = pathName; 	//used to copy file over
		
		if (stringSpotted = false){
			pathnameError = true;
		}
		
		if (pathnameError == true){
			System.out.println("ERROR -- pathname");
		} else {
			if (connectSuccessful == true){
				getAccepted = true;
				System.out.println("GET accepted for " +pathName);
				//PORT
				String portNumber = generatePort();
				portString = "PORT " +portNumber + "\r\n";
				retrString = "RETR " +pathName +"\r\n";	
			} else {
				System.out.println("ERROR -- expecting CONNECT");
			}
		}
	}
	
	public static String generatePort() throws UnknownHostException{
		// int portVariable = defaultPort;
		int portVariable = generatedPortNum;
		String myIP;
		InetAddress myInet;
		myInet = InetAddress.getLocalHost();
		myIP = myInet.getHostAddress();
		int difference = portVariable - 8000;
		String newIP = myIP.replace('.', ',');
		String leftNum = "31";
		int rightNum = 64 + difference;
		String rightStr = Integer.toString(rightNum);
//		defaultPort++; //increment for next port
		generatedPortNum = (Integer.parseInt(leftNum) * 256) + rightNum;
//		System.out.println("NEW SOCKET PORT NUMBER: " +generatedPortNum);
		return newIP +',' +leftNum +',' +rightStr;
		
	}
	
}
