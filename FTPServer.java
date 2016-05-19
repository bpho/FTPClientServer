//Original Eclipse Version
//*Submitted Version*
import java.io.*;
import java.net.*;
public class FTPServer {

	public static int portNumber = 9925;
	public static String portReturn = "";
	public static String IPAddress = "";
	public static int portNumber2 = 0;
	public static int portNumber3 = 8080;
	public static Socket clientSocket;
	public static DataOutputStream outToServer;
	public static BufferedReader inFromServer;
	public static String retrFile = "";
	public static String fileStatus = "";
	public static int retrCounter = 1;
	public static Socket portSocket;
	public static boolean dataConnectMade = false;
	public static boolean fileRequested = false;

	
	public static void main(String[] args) throws IOException{
		// String portNumberString = args[0];
		int portNumberString = Integer.parseInt(args[0]);
		String clientSentence;
		
		try {
			// ServerSocket welcomeSocket = new ServerSocket(portNumber);
			ServerSocket welcomeSocket = new ServerSocket(portNumberString);
//			System.out.println("Server Ready for Connection");
			Socket connectionSocket = welcomeSocket.accept();

			while(true){
				BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
				DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
				
				clientSentence = inFromClient.readLine();
//				System.out.println("Client sent:" +clientSentence);
				
				String parserReply = checkParser(clientSentence);
				outToClient.writeBytes(parserReply +"\r\n");
				outToClient.flush();
			}
			
		} catch (IOException e){
			System.out.println("Server failed");
		}
	}

	
	public static void checkPort(String input1) throws IOException{
		boolean noErrors = false;
		int portNumber = 0;
		
		//Gets rid of the command and just takes in the host-port numbers
		String blankString = "";
		StringBuilder parameter = new StringBuilder(blankString);
		for (int i = 5; i < input1.length(); i++){
			char currentChar = input1.charAt(i);
			parameter.append(currentChar);
		}
		String newString = parameter.toString();
		
		//Check if it's valid
		String[] splitNums = newString.split(",");
		if (splitNums.length == 6){
			for (int i = 0; i < splitNums.length; i++){
				int intValue = Integer.parseInt(splitNums[i]);
				if ((splitNums[i].length() > 1 && splitNums[i].charAt(0) == '0') || (intValue < 0) || (intValue > 255)){
					System.out.print("501 Syntax error in parameter." +"\r\n");
				} else {
					noErrors = true;
				}
			}
		} else {
			System.out.print("501 Syntax error in parameter." +"\r\n");
		}
		
		if (noErrors == true){
			String hostAddress = "";
			StringBuilder builder = new StringBuilder(hostAddress);
			int commaCounter = 0; //if it reaches 4 then break
			for (int i = 0; i < newString.length(); i++){
				char currentChar = newString.charAt(i);
				if (currentChar == ','){
					commaCounter++;
					if (commaCounter == 4){
						break;
					}
					builder.append('.');
				} else {
					builder.append(currentChar);
				}
			}
			
			//calculate splitNums[4] and splitNums[5] for port-number
			int leftNum = Integer.parseInt(splitNums[4]);
			int rightNum = Integer.parseInt(splitNums[5]);
			portNumber = (leftNum * 256) + rightNum;
			
			String hostAddress2 = "(" +builder.toString() + "," +portNumber +").";
			System.out.print("200 Port command successful " +hostAddress2 +"\r\n");
			IPAddress = builder.toString();
			portNumber2 = portNumber;
			//send this back to client
			portReturn = "FTP reply 200 accepted. Text is: Port command successful " + "(" +builder.toString() + "," +portNumber +").";			
		}
	}
	
	//establishing the "FTP-data" socket
	public static void createSocket() throws IOException {
		if (dataConnectMade == false && fileRequested == false){
			try {
				portSocket = new Socket(IPAddress, portNumber2);
//				System.out.println("*FTP-port ready on Server side for " +portNumber2 +"*");
				outToServer = new DataOutputStream(portSocket.getOutputStream());
//				System.out.println("*Data Output Stream working*");
				inFromServer = new BufferedReader(new InputStreamReader(portSocket.getInputStream()));
//				System.out.println("*Input Stream Reader working*");
			} catch (IOException e){
				System.out.println("425 Can not open data connection.");
			}
			dataConnectMade = true;
		} else if (dataConnectMade == true && fileRequested == true){
			try {
				File file = new File(retrFile);
				byte[] buffer = new byte[1024];
				int length;
				InputStream in = new FileInputStream(file);
				OutputStream out = portSocket.getOutputStream();
				
				while ((length = in.read(buffer)) > 0){
					out.write(buffer, 0, length);
				}
				
				System.out.print("250 Requested file action completed." +"\r\n");
				in.close();
				out.close();
				portSocket.close();

			} catch (IOException e) {
				System.out.println("425 Can not open data connection.");
			}
			fileRequested = false;
			dataConnectMade = false;
		}
	}
	
	public static String checkParser(String input) throws IOException{
		if (input.toLowerCase().contains("port ")){
			System.out.print(input +"\r\n");
			checkPort(input);
			createSocket();
			return portReturn;
		} else if (input.toLowerCase().contains("retr ")){
			fileRequested = true;
			System.out.print(input +"\r\n");
			checkRetr(input);
			createSocket();
			return fileStatus;
		} else if (input.toLowerCase().contains("quit")){
			System.out.print(input +"\r\n");
			System.out.print("221 Goodbye." +"\r\n");
			return "FTP reply 221 accepted. Text is: Goodbye.";
		}else if (input.toLowerCase().contains("connect")){
			System.out.print("220 COMP 431 FTP server ready." +"\r\n");
			return "FTP reply 220 accepted. Text is: COMP 431 FTP server ready.";
		} else if (input.toLowerCase().contains("user")){
			String username = "";
			String[] splitString = input.split(" ");
			username = splitString[1].trim();
			System.out.print("USER " +username +"\r\n");
			System.out.print("331 Guest access OK, send password." +"\r\n");
			return "FTP reply 331 accepted. Text is: Guest access OK, send password.";
		} else if (input.toLowerCase().contains("pass")){
			String password = "";
			String[] splitString = input.split(" ");
			password = splitString[1].trim();
			System.out.print("PASS " +password +"\r\n");
			System.out.print("230 Guest login OK." +"\r\n");
			return "FTP reply 230 accepted. Text is: Guest login OK.";
		} else if (input.toLowerCase().contains("syst")){
			System.out.print("SYST" +"\r\n");
			System.out.print("215 UNIX Type: L8." +"\r\n");
			return "FTP reply 215 accepted. Text is: UNIX Type: L8.";
		} else if (input.toLowerCase().contains("type")){
			System.out.print("TYPE I" + "\r\n");
			System.out.print("200 Type set to I." +"\r\n");
			return "FTP reply 200 accepted. Text is: Type set to I.";
		} else {
			return "NOT ACCEPTED APPARENTLY";
		}
	}
	
	public static void checkRetr(String input1) throws IOException{
		boolean validFile = false;
		boolean fileExists = false;
		
		//Converts to use the file name
		String blankString = "";
		StringBuilder builder = new StringBuilder(blankString);
		for (int i = 5; i < input1.length(); i++){
			char currentChar = input1.charAt(i);
			builder.append(currentChar);
		}
		String fileName = builder.toString();
		
		//Test the fileName
		String testFile = fileName +"\r\n";
		for (int i = 0; i < testFile.length(); i++){
			int asc = testFile.charAt(i);
			boolean innerLoop = false;
			char currentChar = testFile.charAt(i);
			if (asc > 0x7f || asc == 0x0A || (currentChar == '\r' && testFile.charAt(i+1) != '\n')) {	//newline, return line or anything not ASCII
				validFile = false;
				break;
			} else if (currentChar == '\r' && testFile.charAt(i+1) == '\n'){
				validFile = true;
				break;
			} 

			//Tests to see if rest of the username is just all blanks, returning an error if innerLoop returns true
			if (testFile.charAt(i) == ' ' && i == 0){
				for (int j = i; j < testFile.length(); j++){
					char currentChar2 = testFile.charAt(j);
					if (currentChar2 != ' '){
						if (currentChar2 == '\r' && testFile.charAt(j+1) == '\n'){
							innerLoop = true;
							break;
						}
						break;
					} 
					}
				}
			if (innerLoop == true){
				validFile = false;
				break;
			}
		}
		
		//Now that the file is valid check if it's in the directory, if not, print syntax error
		if (validFile == true){
			//Test if file exists in directory
			String currentDirectory;
			File file = new File(".");
			currentDirectory = file.getAbsolutePath();
			boolean check = new File(currentDirectory, fileName).exists();
			if (check == true){
				//tests for the slash or backslash
				if (fileName.charAt(0) == '/' || fileName.charAt(0) == '\\'){
					fileName = fileName.substring(1);
				}
				System.out.print("150 File status okay." +"\r\n");
				retrFile = fileName;
				fileStatus = "FTP reply 150 accepted. Text is : File status okay.";
				File source = new File(fileName);

			} else {
				System.out.print("550 File not found or access denied." +"\r\n");
				fileStatus = "FTP reply 550 accepted. Text is : File not found or access denied.";
			}
		} else {
			System.out.print("501 Syntax error in the parameter." +"\r\n");
		}
	}
}
