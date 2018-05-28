package multiplayer;

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;
import java.util.zip.*;
import java.util.logging.*;
//player thread handles single client
class PlayerThread extends Thread
{
    private Socket clientSocket;
    private Logger commLog;
    private Logger gameLog;
    private LinkedList<PlayerThread> lobbyQueue;
    public String name;
    public int score;
    RoundThread round;
    GameMsg msg;
//constructor to set up thread
    public PlayerThread(Socket socket, Logger commLog, Logger gameLog,  GameMsg msg, LinkedList<PlayerThread> lobbyQueue)
    {
        this.clientSocket = socket;
        this.commLog = commLog;
        this.gameLog = gameLog;
        this.score = 1;
        this.msg = msg;
        this.lobbyQueue = lobbyQueue;
    }
    //add RoundThread to call notify on
    public void addRoundThread(RoundThread thread)
    {
        this.round = thread;
    }
    public int compareTo(PlayerThread comparePlayer)
    {
        int compareScore = ((PlayerThread) comparePlayer).score;

        return this.score - compareScore;
    }
    public String toString()
    {
        return "Name: " + this.name + " Score: " + this.score;
    }
    public int getScore()
    {
        return this.score;
    }
    //start Thread
    @Override
    public void run()
    {
        try{
            //buffers to send and recieve data from clinet
            PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true);
            InputStream clientInputStream = clientSocket.getInputStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(clientInputStream));
 
            String inputLine;

            output.println("Please Enter your name: ");
            commLog.log(Level.INFO, "Name promt sent to client");
    
            this.name = input.readLine();
            commLog.log(Level.INFO, "Data recieved from client");
    
            output.println("Welcome " + this.name);
            commLog.log(Level.INFO, "Welcome sent to client");
            boolean close = false;
            //loop while client wishes to play
            while(!close)
            {
                synchronized(this)
                {
                    try{
                        output.println("Waiting for other players to join...");
                        commLog.log(Level.INFO, "waiting promt sent to client");
                        this.wait();
                    }catch(InterruptedException e)
                    {
                        System.out.println("Interrupt error: " + e);
                    }
                }
                if(msg.output.equals("get code length"))
                {
                    msg.output = "";
                    output.println("Please enter a code length");
                    commLog.log(Level.INFO, "code length promt sent to client");
                    msg.input = input.readLine();
                    commLog.log(Level.INFO, "code length received from client");
                    synchronized(round)
                    {
                        round.notify();
                    }
                    synchronized(this)
                    {
                        try{
                            this.wait();
                        }catch(InterruptedException e)
                        {
                            System.out.println("Interrupt error: " + e);
                        }
                    }
                }
                else
                {
                    output.println("Start Game");
                }
                //loop while guessing 
                while(!(inputLine = input.readLine()).equals("f"))
                {    
                    commLog.log(Level.INFO, "input received from client");
                    //handle clients guess
                    gameLog.log(Level.INFO, "GUESS: " + inputLine);
                    GuessResponse newGuess = match(msg.code, inputLine, gameLog);
                    if(newGuess == null)
                    {
                        gameLog.log(Level.INFO, "guess was to long");
                        output.println("Guess must only contain " + msg.codeLength + " digits");
                        commLog.log(Level.INFO, "Game result sent to client");
                    }
                    else if(newGuess.correct == msg.codeLength)
                    {
                        gameLog.log(Level.INFO, "Correct Guess");
                        gameLog.log(Level.INFO, "Guess count: " + this.score);
                        output.println("Correct,"+this.score);
                        commLog.log(Level.INFO, "Game result sent to client");
                        break;
                    }
                    else if(newGuess.correct != msg.codeLength && this.score > 9)
                    {
                        gameLog.log(Level.INFO, "Incorrect Guess Out of Guesses");
                        gameLog.log(Level.INFO, "Guess count: " + this.score);
                        output.println("Incorrect,"+this.score);
                        commLog.log(Level.INFO, "Game result sent to client");
                        break;
                    }
                    else
                    {
                        gameLog.log(Level.INFO, "Incorrect Guess Again");
                        gameLog.log(Level.INFO, newGuess.correct + "," + newGuess.incorrect);
                        this.score++;
                        output.println("Incorrect,"+newGuess.correct + "," + newGuess.incorrect    );
                        commLog.log(Level.INFO, "Game result sent to client");
                    }
                }
                if(inputLine.equals("f"))
                {
                    gameLog.log(Level.INFO, "Game Forfeit score 11");
                    this.score = 11;
                    output.println("Game Forfeit,"+11);
                }
                //send play again/ quit prompt and handle response
                output.println("End Game");
                commLog.log(Level.INFO, "end game sent to client");
                synchronized(this)
                {
                    try {
                        output.println("Waiting for other players to finish");
                        this.wait();
                    }catch(InterruptedException e)
                    {
                        System.out.println("Interupt error: " + e);
                    }
                }
                output.println(msg.result);
                output.println("Do you wish to play again? (p)-play/(q)-quit");
                commLog.log(Level.INFO, "Prompt to play again sent to client");
                inputLine = input.readLine();
                commLog.log(Level.INFO, "Response received from client");
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
                    System.out.println("DEBUG: LobbyQueue Size: " + this.lobbyQueue.size());
                    this.lobbyQueue.add(this);
                    System.out.println("DEBUG: LobbyQueue Size: " + this.lobbyQueue.size());
                }
            }
            this.clientSocket.close();
        }catch(IOException ioe)
        {
            System.out.println("Input output exception: " + ioe);
        }
    }
    //check if digit is in the code
    private boolean inCode(int digit, int[] code, Logger gameLog)
    {
        gameLog.log(Level.INFO, "Make sure digit is unique in the code");
        for(int i = 0; i < code.length; ++i)
        {
            if(code[i] == digit)
                return true;
        }
        return false;
    }
    //check if guess matches the code
    private GuessResponse match(int[] code, String guess, Logger gameLog)
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

