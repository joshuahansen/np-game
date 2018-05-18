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

            ServerSocket ss = new ServerSocket(serverPort);

            boolean queuing = true;            
            while(queuing)
            {
                Socket clientSocket = connect(ss, commLog);
                PlayerThread newPlayer = new PlayerThread(clientSocket, commLog, msg);
                lobbyQueue.add(newPlayer);
                newPlayer.start();
                if(lobbyQueue.size() >= 3) //change to 3 after testing
                {
                    players.clear();
                    players.add(lobbyQueue.poll());
                    players.add(lobbyQueue.poll());
                    players.add(lobbyQueue.poll());
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
