package multiplayer;

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
    public static GameMsg msg = new GameMsg();
    public static List<PlayerThread> players = new ArrayList<PlayerThread>();
    public static void main(String[] args)
    {
        LinkedList<PlayerThread> lobbyQueue = new LinkedList<PlayerThread>();
        try {
            //set up loggers for game and communication
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
            
            int serverPort = 19185;
            //start server socket
            ServerSocket ss = new ServerSocket(serverPort);

            boolean queuing = true;
            //continuously loop to allow clients to connect 
            while(queuing)
            {
                Socket clientSocket = connect(ss, commLog);
                //start a new thread to handle client
                PlayerThread newPlayer = new PlayerThread(clientSocket, commLog, msg);
                //add to lobbyQueue
                lobbyQueue.add(newPlayer);
                newPlayer.start();
                //start game once 3 players have connected
                if(lobbyQueue.size() >= 3)
                {
                    players.clear();
                    //get first 3 players
                    players.add(lobbyQueue.poll());
                    players.add(lobbyQueue.poll());
                    players.add(lobbyQueue.poll());
                    //start round
                    RoundThread round = new RoundThread(players, gameLog, msg);
                    round.start();
                }
            }
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
    private static Socket connect(ServerSocket ss, Logger commLog) throws IOException
    {
        commLog.log(Level.INFO, "Server waiting for connection");
        Socket clientSocket = ss.accept();
        commLog.log(Level.INFO, "Connection between server and client established");
        return clientSocket;
    }
}
