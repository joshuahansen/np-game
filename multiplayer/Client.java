package multiplayer;

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
            //connect to server
            Socket serverSocket = connectToServer();
        
            //create buffers for input output
            InputStream keyboardInputStream = System.in;
            BufferedReader keyboardInput = new BufferedReader(
                      new InputStreamReader(keyboardInputStream));
            BufferedReader serverInput = new BufferedReader(
                        new InputStreamReader(serverSocket.getInputStream()));
            PrintWriter clientOutput = new PrintWriter(serverSocket.getOutputStream(), true);
            
            String sr = "";
            //loop while user wishes to continue playing
            while(!sr.equals("close"))
            {
                //get user name propmts and send response
                System.out.println(getServerResponse(serverInput));
                clientOutput.println(getUserInput(keyboardInput));
                System.out.println(getServerResponse(serverInput));
    
                sr = getServerResponse(serverInput);
                System.out.println(sr);
                //set code length
                if(sr.equals("Please enter a code length"))
                {
                    setCodeLength(keyboardInput, clientOutput);
                }
                sr = getServerResponse(serverInput);
                //loop getting user guesses while valid turns
                while(!"End Game".equals(sr))
                {
                    boolean response = displayServerResponse(sr);
                    if(!response)
                    {
                        guess(keyboardInput, clientOutput);
                    }
                    sr = getServerResponse(serverInput);
                }   
                //get play agian/quit prompt
                sr = getServerResponse(serverInput);
                System.out.println(sr);
                clientOutput.println(getUserInput(keyboardInput));
                sr = getServerResponse(serverInput);
            }
            keyboardInput.close();
        }catch(IOException ex)
        {
            System.out.println("Failed to close connection: " + ex);
        }
    }
    /**
    * @param keyboardInput : BufferedReader; get input from keyboard.
    * @param clientOutput : PrintWriter; sends output to the server.
    * Validate the user input to make sure it only contains digits.
    */
    private static void guess(BufferedReader keyboardInput, PrintWriter clientOutput) throws IOException
    {
        System.out.print("Enter Guess: ");
        String userGuess = getUserInput(keyboardInput);
        clientOutput.println(userGuess);
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
        String[] response = serverResponse.split(",");
        if(response.length == 2 && response[0].equals("Correct"))
        {
            System.out.println("Correct!");
            System.out.println("Guesses: " + response[1]);
            return true;
        }
        else if(response.length == 2 && response[0].equals("Incorrect"))
        {
            System.out.println("Incorrect Guess. Out of guesses");
            System.out.println("Guesses: " + response[1]);
            return true;
        }
        else if(response.length == 2 && response[0].equals("Game Forfeit"))
        {
            System.out.println("Game Forfeit");
            System.out.println("Guesses: " + response[1]);
            return true;
        }
        else if(response.length == 3 && response[0].equals("Incorrect"))
        {
            System.out.println("Incorrect Guess");
            System.out.println("Correct Positions: " + response[1]);
            System.out.println("Incorrect Positions: " + response[2]);
            return false;
        }
        else
        {
            System.out.println(serverResponse);
            return false;   
        }
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

    
