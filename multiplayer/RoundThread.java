package multiplayer;

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;
import java.util.zip.*;
import java.util.logging.*;

class RoundThread extends Thread
{
    private Logger gameLog;
    private List<PlayerThread> players;
    GameMsg msg;

    public RoundThread(List<PlayerThread> players, Logger gameLog, GameMsg msg)
    {
        this.players = players;
        this.gameLog = gameLog;
        this.msg = msg;
    }
    @Override
    public void run()
    {
        boolean endRound = false;
        while(!endRound)
        {
            msg.output = "get code length";
            players.get(0).notify();
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

            int code[] = generateCode(codeLength, gameLog);
            gameLog.log(Level.INFO, "Code generated");

            gameLog.log(Level.INFO, "Code: " + Arrays.toString(code));

            for(PlayerThread player : players)
            {
                player.notify();
                synchronized(this)
                {
                   try{
                       this.wait();
                   }catch(InterruptedException e)
                   {
                       System.out.println("Interrupt error: " + e);
                   }
                }

                if(msg.input.equals("f"))
                {
                    gameLog.log(Level.INFO, "Game Forfeit score 11");
                    msg.output = "Game Forfeit,"+11;
                }
                else
                {
                    gameLog.log(Level.INFO, "GUESS: " + msg.input);
                    GuessResponse newGuess = match(code, msg.input, gameLog);
                    if(newGuess == null)
                    {
                        gameLog.log(Level.INFO, "guess was to long");
                        msg.output = "Guess must only contain " + codeLength + " digits";
                    }
                    else if(newGuess.correct == codeLength)
                    {
                        gameLog.log(Level.INFO, "Correct Guess");
                        gameLog.log(Level.INFO, "Guess count: " + player.score);
                        msg.output = "Correct,"+player.score;
                        break;
                    }
                    else if(newGuess.correct != codeLength && player.score > 9)
                    {
                        gameLog.log(Level.INFO, "Incorrect Guess Out of Guesses");
                        gameLog.log(Level.INFO, "Guess count: " + player.score);
                        msg.output = "Incorrect,"+player.score;
                        break;
                    }
                    else
                    {
                        gameLog.log(Level.INFO, "Incorrect Guess Again");
                        gameLog.log(Level.INFO, newGuess.correct + "," + newGuess.incorrect);
                        player.score++;
                        msg.output = "Incorrect,"+newGuess.correct + "," + newGuess.incorrect    ;
                    }
                    player.notify();
                }
            }
            for(PlayerThread player : players)
            {
                msg.output = "End Game";
                player.notify();
            }
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
    class GuessResponse
    {
        public int correct;
        public int incorrect;
    }
}

