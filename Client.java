import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;
import java.util.zip.*;

/**
* Client Class
* @author Joshua Hansen
* Connects to the server
* Signs up for each game round
* gets code length from client and forwards to server
* get guesses from player and forwards to server
* display server's message to player
*/
class Client
{
    /**
    * @param args: String; command line arguments if any
    * main function called when clients starts game
    */
    public static void main(String[] args)
    {
        try {
            System.out.println("==========Welcome To Code Breaker==========");

            Socket serverSocket = connectToServer();

            InputStream keyboardInputStream = System.in;
            BufferedReader keyboardInput = new BufferedReader(
                      new InputStreamReader(keyboardInputStream));
            BufferedReader serverInput = new BufferedReader(
                        new InputStreamReader(serverSocket.getInputStream()));
            PrintWriter clientOutput = new PrintWriter(serverSocket.getOutputStream(), true);

            System.out.println("Send server you name:");
            clientOutput.println(getUserInput(keyboardInput));
    
            String serverResponse = getServerResponse(serverInput);
            if(serverResponse.equals("first player"))
            {
                setCodeLength(keyboardInput, clientOutput);
            }

            while(!"quit".equals(getServerResponse(serverInput)))
            {
            }   



            keyboardInput.close();
        }catch(IOException ex)
        {
            System.out.println("Failed to close connection: " + ex);
        }
    }
    /**
    * Connect to the server.
    * @return Socket; Return socket of server connection.
    */
    private static Socket connectToServer() throws IOException
    {
        try {
            String serverHostname = "m1-c33n1.csit.rmit.edu.au";
            int serverPort = 19185;
            System.out.println("Connecting to server: " + serverHostname);
            System.out.println("Server Port: " + serverPort);
            Socket server =  new Socket(serverHostname, serverPort);
            
            System.out.println("Connection Established");
            return server;            
        }catch(UnknownHostException ex)
        {
            System.out.println("Connection failed: " + ex);
            System.exit(0);
            return null;
        }
    }

    /**
    * @param serverResponse: String; Recievece string from server.
    * Use String.split to seperate each  server response.
    * Displays the response from the server.
    * @return boolean; return true if game is over otherwise return false.
    */
    public static boolean displayServerResponse(String serverResponse)
    {
        String response[] = serverResponse.split(",");
        if(response.length == 2 && response[0] == "Correct")
        {
            System.out.println("Correct!");
            System.out.println("Guesses: " + response[1]);
            return true;
        }
        else if(response.length == 2 && response[0] == "Incorrect")
        {
            System.out.println("Incorrect Guess. Out of guesses");
            System.out.println("Guesses: " + response[1]);
            return true;
        }
        else if(response.length == 3 && response[0] == "Incorrect")
        {
            System.out.println("Incorrect Guess");
            System.out.println("Correct Positions: " + response[1]);
            System.out.println("Incorrect Positions: " + response[2]);
            return false;
        }
        else
            return false;   
    }

    /**
    * Gets user input from keyboard and returns as a string.
    * @return String.
    */
    public static String getUserInput(BufferedReader input) throws IOException
    {
            return input.readLine();
    }
    
    /**
    * Get server response.
    * @return String.
    */
    public static String getServerResponse(BufferedReader input) throws IOException
    {
            return input.readLine();
    }
    
    /**
    * Set Code Length to be generated.
    * @param input : BufferedReader; user input buffer.
    * @param clientOutput : PrintWriter; used to send user input to server
    */
    public static void setCodeLength(BufferedReader input, PrintWriter clientOutput) throws IOException
    {
        System.out.println("Enter Length of code between 3 and 8:");
        int length = Integer.valueOf(getUserInput(input));
        while(!(length >= 3 && length <= 8))
        {
            System.out.println("Incorrect size please enter a length between 3 and 8:");
            length = Integer.valueOf(getUserInput(input));
        }
        clientOutput.println(length);
    }                
}

    
