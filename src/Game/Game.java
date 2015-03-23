package Game;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javafx.event.Event;
import com.google.gson.Gson;

import javax.swing.*;


enum gameStatus{ONGOING, OVER, CHIPMENU}

public class Game implements Runnable, ActionListener {
    private Timer timer;

	private Player p1;
	private Player p2;
	private Arena arena;
	private ArrayList<Action> actions = new ArrayList<Action>();
	
	private gameStatus status;
    private int menuTimer;

    public Game(Connection c1, Connection c2)
	{
		p1 = new Player(c1, arena, 0, 1); //starts on left
		p2 = new Player(c2, arena, 5, 1); //starts on right
		status = gameStatus.CHIPMENU;
        menuTimer = 900;
	}
	
	public void run() {
        //setup thread timer
        System.out.println("Setting up Timer");
        timer = new Timer(1000, this);
        timer.start();
        while (this.status != gameStatus.OVER){
            //do nothing, keep the thread busy
        }
	}

    @Override
    public void actionPerformed(ActionEvent e) {
        // Check to see if the game is still active
        if(status != gameStatus.OVER)
            this.update();
//        else
//        	if both players have left then delete self

    }

    private void update(){
        System.out.println("Updating game state");
        //Grab and process current inputs
        Input i1 = p1.getPendingInput();
        Input i2 = p2.getPendingInput();

        //Change state based inputs/state
        switch (status){
            case ONGOING:
                if (i1.event == "menu" || i2.event == "menu")
                    menuTimer = 900; //30 ticks/second * 30 seconds
                    status = gameStatus.CHIPMENU;
                if (p1.isDead() || p2.isDead())
                    status = gameStatus.OVER;
                break;
            case OVER:
                //Nothing to see here :P
                break;
            case CHIPMENU:
                //if time has hit limit, or both players have locked in
               status = gameStatus.ONGOING;
                break;
        }

        //Update game based on game state
        switch (status){
            case ONGOING:
                //For all game objects
                //Create Actions based upon inputs and current game state
                //Hand the object an input, and get an action back
                //Add Actions to queue
                actions.add(p1.handleInput(i1));
                actions.add(p2.handleInput(i2));
                //Update all valid actions
                for(Action a : actions) {
                    a.update();
                    //Remove actions that have expired
                    if(a.isEventComplete())
                        actions.remove(a);
                }
                //Package up world info

                break;
            case OVER:
                break;
            case CHIPMENU:

                break;
        }

    }
	
	public void handleEvent(String playerName, String message)
	{
        System.out.println();
		if(p1.isPlayer(playerName))
			p1.addAction(message);
		else if(p2.isPlayer(playerName))
			p2.addAction(message);
	}
	
	public String getState(String playerName){
		GameState gs = new GameState();
		gs.state = status;
		if(p1.isPlayer(playerName)){
			gs.enemyPlayerStatus = p2.getStatus();
			gs.enemyPlayerPosition = "(" + p2.getXPos() + ", " + p2.getYPos() + ")";
			gs.myStatus = p1.getStatus();
			gs.myPosition = "(" + p1.getXPos() + ", " + p1.getYPos() + ")";
		}
		else if(p2.isPlayer(playerName))
		{
			gs.myStatus = p2.getStatus();
			gs.myPosition = "(" + p2.getXPos() + ", " + p2.getYPos() + ")";
			gs.enemyPlayerStatus = p1.getStatus();
			gs.enemyPlayerPosition = "(" + p1.getXPos() + ", " + p1.getYPos() + ")";
		}
		
		gs.actions = formatActions();
		Gson gson = new Gson();
		String message = gson.toJson(gs);
//		System.out.println(message);
		return message;
	}
	
	private String formatActions(){
		String actionsJSON = "";
		Gson gson = new Gson();
		for(Action a : actions){
			ActionState aState = new ActionState();
			aState.xPos = a.getTile().getXPos();
			aState.yPos = a.getTile().getYPos();
			aState.spriteMap = a.getSpritePath();
			aState.isComplete = a.isEventComplete();
			aState.index = a.getIndex();
			actionsJSON += gson.toJson(aState) + "\n";
		}
		
		return actionsJSON;
	}
	
	public void playerLeft(String playerName){
//		System.out.println(playerName + "Has left the game!");
//		status = gameStatus.OVER;
	}

    public class GameState{
		public gameStatus state;
		public playerStatus enemyPlayerStatus;
		public String enemyPlayerPosition;
		public playerStatus myStatus;
		public String myPosition;
		public String actions;
	}
    
    public class ActionState{
    	public int xPos;
    	public int yPos;
    	public String spriteMap;
    	public boolean isComplete;
    	public int index;
    }
}
