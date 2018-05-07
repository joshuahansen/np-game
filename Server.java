import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;
import java.util.zip.*;

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
            Socket clientSocket = serverSocket();
            PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true);
            InputStream clientInputStream = clientSocket.getInputStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(clientInputStream));

            output.println("Please Enter your name: ");
            String clientName = input.readLine();
            output.println("Welcome " + clientName);
            output.println("Please enter a code length");
            int codeLength = Integer.parseInt(input.readLine());
            System.out.println("Code length: " + codeLength);
            int code[] = generateCode(codeLength);
            String inputLine;

            int guessCount = 0;
            System.out.println(Arrays.toString(code));
            
            while((inputLine = input.readLine()) != null)
            {
                System.out.println("GUESS: " + inputLine);
                GuessResponse newGuess = match(code, inputLine);
                if(newGuess.correct == codeLength)
                {
                    System.out.println("CORRECT");
                    output.println("Correct,"+guessCount);
                    break;
                }
                else if(newGuess.correct != codeLength && guessCount > 9)
                {
                    System.out.println("INCORRECT OUT OF GUESSES");
                    output.println("Incorrect,"+guessCount);
                    break;
                }
                else
                {
                    System.out.println("INCORRECT GUESS AGAIN");
                    System.out.println("Incorrect,"+newGuess.correct + "," + newGuess.incorrect);
                    output.println("Incorrect,"+newGuess.correct + "," + newGuess.incorrect);
                    ++guessCount;
                }
            }
            output.println("quit");

            clientSocket.close();
        
        }catch(IOException ex)
        {
            System.out.println("Input output error: " + ex);
        }
    }

    /**
    * Open server socket for client to connect to
    */
    private static Socket serverSocket() throws IOException
    {
        int serverPort = 19185;

        ServerSocket ss = new ServerSocket(serverPort);

        System.out.println("Server Waiting for connection");
        Socket clientSocket = ss.accept();
        System.out.println("Connection between server and client established"); 
        return clientSocket;
    }
    /**
    * Generate Code
    * @param length : int; Length of code to be generated
    * @return String; return a unique code of inputed length
    */
    private static int[] generateCode(int length)
    {
        int code[] = new int[length];
        Random rand = new Random();
        for(int i = 0; i < length; ++i)
        {
            int newDigit = rand.nextInt(10);
            if(!inCode(newDigit, code))
                code[i] = newDigit;
            else
                --i;
        }
        return code;
    }
    private static boolean inCode(int digit, int[] code)
    {
        for(int i = 0; i < code.length; ++i)
        {
            if(code[i] == digit)
                return true;
        }
        return false;
    }
    private static GuessResponse match(int[] code, String guess)
    {
        GuessResponse newGuess = new GuessResponse();
        for(int i = 0; i < guess.length(); ++i)
        {
            if(code[i] == Character.getNumericValue(guess.charAt(i)))
                newGuess.correct++;    
            else
                newGuess.incorrect++;
        }
        return newGuess;
    }
}  

class GuessResponse
{
    public int correct = 0;
    public int incorrect = 0;
    
}
