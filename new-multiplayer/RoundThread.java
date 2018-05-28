package multiplayer;

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;
import java.util.zip.*;
import java.util.logging.*;
//round thread handels game logic
class RoundThread extends Thread
{
    private Logger gameLog;
    private List<PlayerThread> players;
    GameMsg msg;
    //constructor to set up thread
    public RoundThread(List<PlayerThread> players, Logger gameLog, GameMsg msg)
    {
        this.players = players;
        this.gameLog = gameLog;
        this.msg = msg;
        //add this thread to player threads
        for(PlayerThread player : players)
        {
            player.addRoundThread(this);
        }
    }
    @Override
    public void run()
    {
        //loop while round is playing
        boolean endRound = false;
        while(!endRound)
        {
            //ask player 1 for the code length
            msg.output = "get code length";
            synchronized(players.get(0))
            {
                players.get(0).notify();
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
            int codeLength = Integer.parseInt(msg.input);
            gameLog.log(Level.INFO, "Code length: " + codeLength);
            //generate the code
            int code[] = generateCode(codeLength, gameLog);
            gameLog.log(Level.INFO, "Code generated");
            msg.code = code;
            msg.codeLength = codeLength;

            gameLog.log(Level.INFO, "Code: " + Arrays.toString(code));

            for(PlayerThread player : players)
            {
                synchronized(player)
                {
                    player.notify();
                }
            }
            boolean allPlayed = false;
            while(!allPlayed)
            {
                int count = 0;
                for(PlayerThread player : players)
                {
                    if(player.getState() == State.WAITING)
                        count++;
                }
                if(count == players.size())
                    break;
            }
            players.sort(Comparator.comparing(PlayerThread::getScore));
            int place = 1;
            msg.result = "Score Board\t";
            for(PlayerThread player : players)
            {
                msg.result += place+". " + player.toString()+ "\t";
                ++place;
            }
            for(PlayerThread player : players)
            {
                synchronized(player)
                {
                    player.notify();
                }
            }
            endRound = true;
        }
    }
    /**
    * Generate Code
    * @param length : int; Length of code to be generated
    * @return String; return a unique code of inputed length
    */
    private int[] generateCode(int length, Logger gameLog)
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
}

