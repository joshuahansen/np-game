package singlePlayer;

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;
import java.util.zip.*;
import java.util.logging.*;

/**
* Server Class
* @author Joshua Hansen
* Runs the main game allows client to connect and handels client responses
*/
class Server
{
    /**
    * main function starts server
    */
    public static void main(String[] args)
    {
        
       try {
            //set up logging to file and console
            SimpleFormatter formatter = new SimpleFormatter();
            Logger gameLog = Logger.getLogger("gameLog");
            Logger commLog = Logger.getLogger("communicationLog");
            FileHandler gamefh = new FileHandler("gameLog.log");
            FileHandler serverfh = new FileHandler("communicationLog.log");
            gameLog.addHandler(gamefh);
            commLog.addHandler(serverfh);
            gamefh.setFormatter(formatter);
            serverfh.setFormatter(formatter);
            gameLog.setLevel(Level.FINE);
            commLog.setLevel(Level.FINE);
    
            //get incoming client connection
            Socket clientSocket = serverSocket(commLog);
            //set up buffers to send data
            PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true);
            InputStream clientInputStream = clientSocket.getInputStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(clientInputStream));
                
            String inputLine;
            boolean close = false;
            //loop while user wants to play
            while(!close)
            {
                output.println("Please Enter your name: ");
                commLog.log(Level.INFO, "Name promt sent to client");
                    
                String clientName = input.readLine();
                commLog.log(Level.INFO, "Data recieved from client");
                    
                output.println("Welcome " + clientName);
                commLog.log(Level.INFO, "Welcome sent to client");
                    
                output.println("Please enter a code length");
                commLog.log(Level.INFO, "code length promt sent to client");
                    
                int codeLength = Integer.parseInt(input.readLine());
                commLog.log(Level.INFO, "Data recieved from client");
                gameLog.log(Level.INFO, "Code length: " + codeLength);
                    
                int code[] = generateCode(codeLength, gameLog);
                gameLog.log(Level.INFO, "Code generated");
                    

                int guessCount = 1;
                gameLog.log(Level.INFO, "Code: " + Arrays.toString(code));
                //loop while user has guesses
                while(!(inputLine = input.readLine()).equals("f"))
                {
                    gameLog.log(Level.INFO, "GUESS: " + inputLine);
                    GuessResponse newGuess = match(code, inputLine, gameLog);
                    if(newGuess == null)
                    {
                        gameLog.log(Level.INFO, "guess was to long");
                        output.println("Guess must only contain " + codeLength + " digits");
                        commLog.log(Level.INFO, "Guess size prompt sent to client");
                    }
                    else if(newGuess.correct == codeLength)
                    {
                        gameLog.log(Level.INFO, "Correct Guess");
                        gameLog.log(Level.INFO, "Guess count: " + guessCount);
                        output.println("Correct,"+guessCount);
                        commLog.log(Level.INFO, "result sent to client");
                        break;
                    }
                    else if(newGuess.correct != codeLength && guessCount > 9)
                    {
                        gameLog.log(Level.INFO, "Incorrect Guess Out of Guesses");
                        gameLog.log(Level.INFO, "Guess count: " + guessCount);
                        output.println("Incorrect,"+guessCount);
                        commLog.log(Level.INFO, "result sent to client");
                        break;
                    }
                    else
                    {
                        gameLog.log(Level.INFO, "Incorrect Guess Again");
                        gameLog.log(Level.INFO, newGuess.correct + "," + newGuess.incorrect);
                        output.println("Incorrect,"+newGuess.correct + "," + newGuess.incorrect);
                        commLog.log(Level.INFO, "result sent to client");
                        ++guessCount;
                    }
                }
                //proccess forfeit
                if(inputLine.equals("f"))
                {
                    gameLog.log(Level.INFO, "Game Forfeit score 11");
                    output.println("Game Forfeit,"+11);
                }
                //ask user if they want to play again
                output.println("End Game");
                commLog.log(Level.INFO, "end game sent to client");
                output.println("Do you wish to play again? (p)-play/(q)-quit");
                commLog.log(Level.INFO, "Prompt to play again sent to client");
                inputLine = input.readLine();
                commLog.log(Level.INFO, "Response received from client");
                //proccess results
                if(inputLine.equals("q"))
                {
                    output.println("close");
                    commLog.log(Level.INFO, "close connection sent to client");
                    close = true;
                }
                else
                {
                    output.println("play");
                    commLog.log(Level.INFO, "Keep connection alive and play again");
                }
            }
            clientSocket.close();
        }catch(SecurityException e)
        {
            System.out.println("Security Exception error: " + e);
        }catch(IOException ex)
        {
            System.out.println("Input output error: " + ex);
        }catch(NumberFormatException nfe)
        {
            System.out.println("Number Format Exception: " + nfe);
        }
    }

    /**
    * Open server socket for client to connect to
    */
    private static Socket serverSocket(Logger commLog) throws IOException
    {
        int serverPort = 19185;

        ServerSocket ss = new ServerSocket(serverPort);
        
        commLog.log(Level.INFO, "Server waiting for connection");
        Socket clientSocket = ss.accept();
        commLog.log(Level.INFO, "Connection between server and client established");
        return clientSocket;
    }
    /**
    * Generate Code
    * @param length : int; Length of code to be generated
    * @return String; return a unique code of inputed length
    */
    private static int[] generateCode(int length, Logger gameLog)
    {
        gameLog.log(Level.INFO, "Generate code of length: " + length);
        int code[] = new int[length];
        Random rand = new Random();
        for(int i = 0; i < length; ++i)
        {
            int newDigit = rand.nextInt(10);
            if(!inCode(newDigit, code, gameLog))
                code[i] = newDigit;
            else
                --i;
        }
        return code;
    }
    //chech if digit is in the code
    private static boolean inCode(int digit, int[] code, Logger gameLog)
    {
        gameLog.log(Level.INFO, "Make sure digit is unique in the code");
        for(int i = 0; i < code.length; ++i)
        {
            if(code[i] == digit)
                return true;
        }
        return false;
    }
    //chueck if the guess matches the code
    private static GuessResponse match(int[] code, String guess, Logger gameLog)
    {
        gameLog.log(Level.INFO, "check if guess matches code");
        GuessResponse newGuess = new GuessResponse();
        if(guess.length() > code.length)
            return null;
        for(int i = 0; i < guess.length(); ++i)
        {
            if(code[i] == Character.getNumericValue(guess.charAt(i)))
                newGuess.correct++;    
            else if(inCode(Character.getNumericValue(guess.charAt(i)), code, gameLog))
                newGuess.incorrect++;
        }
        return newGuess;
    }
}  
//global class to handle guess position results
class GuessResponse
{
    public int correct = 0;
    public int incorrect = 0;
    
}
