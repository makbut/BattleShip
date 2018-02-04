import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;


public class BattleShipGame {
    static final int width = 6; //width of sea
    static final int height = 6; //height of sea
    static final String player1 = "Makis"; //player 1 name
    static final String player2 = "Efthymios"; //player 2 name

    public static void main(String args[]){

        Player p1 = new Player(player1,width,height); //player1
        p1.populateSea(createBoatsStatically());
        p1.play = true;

        Player p2 = new Player(player2,width,height); //player2
        p2.populateSea(createBoatsStatically());
        p2.play = false;

        Player playing = p1; //player 1 starts
        while(true) {//main loop of the program
            if(playing.plays() == 1){ // if the playing player sunk all ships, he won, exit game
                break;
            }else{ //else change the turn of players
                if(p1.play){
                    p1.play = false;
                    p2.play = true;
                    playing = p2;
                }else{
                    p2.play = false;
                    p1.play = true;
                    playing = p1;
                }
            }
        }

    }
    //this method creates a list of boats at fixed locations for each player
    private static ArrayList createBoatsStatically(){
        ArrayList<String> loc = new ArrayList();
        ArrayList<Boat> boats1 = new ArrayList();
        loc.add("11");
        loc.add("12");
        Boat b1 = new Boat(loc);
        boats1.add(b1);
        loc.clear();
        loc.add("31");
        loc.add("41");
        loc.add("51");
        Boat b2 = new Boat(loc);
        boats1.add(b2);
        loc.clear();
        loc.add("33");
        loc.add("43");
        loc.add("53");
        Boat b3 = new Boat(loc);
        boats1.add(b3);
        loc.clear();
        loc.add("14");
        loc.add("24");
        loc.add("34");
        loc.add("44");
        Boat b4 = new Boat(loc);
        boats1.add(b4);
        loc.clear();
        loc.add("61");
        loc.add("62");
        loc.add("63");
        loc.add("64");
        loc.add("65");
        loc.add("66");
        Boat b5 = new Boat(loc);
        boats1.add(b5);

        return boats1;
    }
}
/*
* Class Player: describes each player. Each player has a name and a sea
* */
class Player{
    private String name; //player name
    private Sea sea; //player's sea
    public boolean play; //indicates if it is his turn to play
    private Scanner scanner; //reads the keyboard

    //constructor
    Player(String name, int width, int height){
        this.name = name;
        sea = new Sea(width, height);
        scanner = new Scanner(System.in);
    }

    //add all the boats to the  player's sea
    void populateSea(ArrayList<Boat> boats){
        Iterator<Boat> boat_it = boats.iterator();
        while(boat_it.hasNext()){
            sea.addBoat(boat_it.next());
        }
    }

    //describes the behavior of how a player plays
    //returns 0 if the player lost his turn
    //returns 1 if the player won the game
    int plays(){
        String input;
        do{
            do{
                System.out.println(name + " choose where to fire! \n Give shot as xy coordinate. \n e.g. 34");
                sea.printSeaState();
                input = scanner.next();
            }while(!sea.checkShotValid(input)); //while player gives wrong input ask him continuously for input
        }while(sea.processShot(input) && !sea.boats.isEmpty());//while the player is succesfully shooting and while he has not yet sunk everythin
        if(sea.boats.isEmpty()){//if the player sunk every boat declare him winner
            System.out.println("Player " + name + " has won the game!");
            sea.printSeaState();
            return 1;
        }
        return 0;
    }
}

/*
* Class Sea: describes the state of the game
* */
class Sea{
    public ArrayList<Boat> boats; //list of boats in the sea
    private Shots shots; //list of shots in the sea
    private ArrayList< ArrayList<String> > sea_state; //state of the sea
    private int width; //width of the sea (x-coordinate)
    private int height; //height of the sea (y-coordinate)

    //constructor
    Sea(int width, int height){
        boats = new ArrayList();
        shots = new Shots();
        sea_state = new ArrayList();
        this.width = width;
        this.height = height;

        //initialize the state of the sea at U = Uknown
        for(int y=0;y<this.height+1;y++){
            ArrayList<String> s = new ArrayList();
            for(int x=0;x<this.width+1;x++){
                if(y==0){
                    StringBuffer sb = new StringBuffer();
                    sb.append(x);
                    String number = sb.toString();
                    s.add(number);
                }else{
                    if(x==0){
                        StringBuffer sb = new StringBuffer();
                        sb.append(y);
                        String number = sb.toString();
                        s.add(number);
                    }else{
                        s.add("U");
                    }
                }
            }
            sea_state.add(s);
        }
    }

    //adds a new boat to the list of boats
    void addBoat(Boat boat){
        boats.add(boat);
    }

    //checks if shot is valid (typo error, inside bounds, already fired)
    boolean checkShotValid(String shot){
        int x;
        int y;
        if(shot.length()!=2){
            System.out.println("Invalid shot! Please type again!");
            return false;
        }else{
            x = Character.getNumericValue(shot.charAt(0));
            y = Character.getNumericValue(shot.charAt(1));
            if(x<=0 || x>width || y<=0 || y>width){
                System.out.println("Invalid shot! Coordinates are out of bounds! Please type again!");
                return false;
            }else{
                if(shots.checkFire(shot)){
                    shots.throwFire(shot);
                    return true;
                }else{
                    return false;
                }
            }
        }
    }

    //process the fired shot
    boolean processShot(String shot){
        boolean boat_shot;
        for(int i=0;i<boats.size();i++){ //for all boats
            if(!boats.get(i).sunk) { //if boat is not sunk
                boat_shot = boats.get(i).checkIfBoatShot(shot); //check if your shot hits the boat
                if (boat_shot) { //if boat is shot
                    System.out.println("A boat has been shot!");
                    if(boats.get(i).sunk){ //if the boat gets sunk
                        System.out.println("A boat has been sunk!");
                        boats.remove(boats.get(i)); //remove the boat from list of boats
                    }
                    updateSeaState("H", shot); //update the sea state
                    return true;
                }
            }
        }//if you exit for loop, it means no boat has been shot
        updateSeaState("M", shot); //update the sea state
        System.out.println("Your shot missed!");
        return false;
    }

    //updates the sea state, adds the replacement character to the shot coordinate
    void updateSeaState(String replacement, String shot){
        int x = Character.getNumericValue(shot.charAt(0));
        int y = Character.getNumericValue(shot.charAt(1));
        ArrayList<String> temp = sea_state.get(y);
        temp.set(x,replacement);
        sea_state.set(y,temp);
    }

    //prints the state of the sea
    void printSeaState(){
        Iterator<ArrayList<String>> it1;
        Iterator<String> it2;
        it1 = sea_state.iterator();
        while(it1.hasNext()){
            it2 = it1.next().iterator();
            while(it2.hasNext()){
                System.out.print(it2.next() + " ");
            }
            System.out.println();
        }
    }
}

/*
* Class Boat: describes the boat state and contains the boat location
* */
class Boat{
    ArrayList<String> location; //location of the boat in the sea
    boolean sunk; //state of boat

    //constructor
    Boat(ArrayList<String> location){
        this.location = new ArrayList();
        this.location.addAll(location);
        sunk = false;
    }

    //given the coordinates of the shot, check if this boat is shot
    boolean checkIfBoatShot(String shot){
        if(location.contains(shot)){
            location.remove(shot);
            if(location.isEmpty()){
                sunk=true;
            }
            return true;
        }else{
            return false;
        }
    }
}
/*
* Class Shots: stores the shots fired from each player and checks their validity
* */
class Shots{
    ArrayList<String> shots_fired; //contains the shots fired

    //constructor
    Shots(){
        shots_fired = new ArrayList();
    }

    //check if shot has already been fired
    boolean checkFire(String shot){
        if(shots_fired.contains(shot)){
            System.out.println("You already fired in that location!");
            return false;
        }else{
            return true;
        }
    }

    //add valid shot to the list
    void throwFire(String shot){
        shots_fired.add(shot);
    }

}