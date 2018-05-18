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
    public String name;
    public int score;
    RoundThread round;
    GameMsg msg;
//constructor to set up thread
    public PlayerThread(Socket socket, Logger commLog, GameMsg msg)
    {
        this.clientSocket = socket;
        this.commLog = commLog;
        this.score = 1;
        this.msg = msg;
    }
    //add RoundThread to call notify on
    public void addRoundThread(RoundThread thread)
    {
        this.round = thread;
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
                    output.println("Please enter a code length");
                    commLog.log(Level.INFO, "code length promt sent to client");
                    msg.input = input.readLine();
                    synchronized(this)
                    {
                        try{
                            this.wait();
                        }catch(InterruptedException e)
                        {
                            System.out.println("Interrupt error: " + e);
                        }
                        round.notify();
                    }
                    synchronized(round)
                    {
                        round.notify();
                    }
                }
                //loop while guessing 
                while(!(inputLine = input.readLine()).equals("f"))
                {
                    receiveSend(inputLine, commLog, output);
                }
                if(inputLine.equals("f"))
                {
                    receiveSend(inputLine, commLog, output);
                }
                //send play again/ quit prompt and handle response
                output.println("End Game");
                commLog.log(Level.INFO, "end game sent to client");
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
                }
            }
            this.clientSocket.close();
        }catch(IOException ioe)
        {
            System.out.println("Input output exception: " + ioe);
        }
    }
    //send and receive data between thread and client and notify Round thread when data has been recieved
    public void receiveSend(String inputLine, Logger commLog, PrintWriter output)
    {
        msg.input = inputLine;
        commLog.log(Level.INFO, "input received from client");
        synchronized(this)
        {
            try{
                this.wait();
            }catch(InterruptedException e)
            {
                System.out.println("Interrupt error: " + e);
            }
        }
        synchronized(round)
        {
            round.notify();
        }
        
        output.println(msg.output);
        commLog.log(Level.INFO, "Game result sent to client");
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
}

