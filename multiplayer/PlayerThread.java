package multiplayer;

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;
import java.util.zip.*;
import java.util.logging.*;

class PlayerThread extends Thread
{
    private Socket clientSocket;
    private Logger commLog;
    public String name;
    public int score;
    GameMsg msg;

    public PlayerThread(Socket socket, Logger commLog, GameMsg msg)
    {
        this.clientSocket = socket;
        this.commLog = commLog;
        this.score = 1;
        this.msg = msg;
    }
    @Override
    public void run()
    {
        try{
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
                    notify();
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
                while(!(inputLine = input.readLine()).equals("f"))
                {
                    receiveSend(inputLine, commLog, output);
                }
                if(inputLine.equals("f"))
                {
                    receiveSend(inputLine, commLog, output);
                }
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
    
    public void receiveSend(String inputLine, Logger commLog, PrintWriter output)
    {
        msg.input = inputLine;
        commLog.log(Level.INFO, "input received from client");
        notify();
        synchronized(this)
        {
            try{
                this.wait();
            }catch(InterruptedException e)
            {
                System.out.println("Interrupt error: " + e);
            }
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

