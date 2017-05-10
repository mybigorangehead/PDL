/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pdlclient;

import java.awt.List;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author Josh
 */
public class PDLMasterClient{
    
    //array list of players in the game
    private ArrayList<PlayerThread> _players;
    
    //thread for listening for new players joining
    private LobbyWait _lobby;
    
    //thread to the server
    private ServerThread _toServer;    
    
    //socket to listen for connecting players
    private ServerSocket _master;
    
    //room code for our game
    private String _roomCode;
    
    //port to listen on
    private int _listenPort = 50001;
    
    private boolean _inGame = false;
    
    //GAME stuff
    private int _submissions = 0;
    private int _curRound = 0;
    private int _playerCount;
    private boolean _isDrawRound = true;
    private PictureLane [] _picLanes;
    private phraseGenerator _generator;
    private int maxPlayers = 5;
    
    
    public PDLMasterClient(String room, Socket toServer) throws IOException{
        _roomCode = room;
        _generator = new phraseGenerator();
        PDLClient.instance.setCurrentRoom(_roomCode);
        PDLClient.instance.addPlayer(PDLClient.instance.getPlayerName(), PDLClient.instance.getPlayerIcon());
        _players = new ArrayList<>();
        _master = new ServerSocket(_listenPort);
        _lobby = new LobbyWait();
        _lobby.start();
        _toServer = new ServerThread(toServer);
    } 
    
    /*
    * this thread listens using the _master ServerSocket for players joining/quiting the game
    */
    public class LobbyWait extends Thread{
         @Override
        public void run() {
            Socket incomingPlayer = null;
           // BufferedReader socketReader = new BufferedReader(new InputStreamReader(PDLClient.instance.toServer.getInputStream()));
            System.out.println("Waiting for players");
            while(!_inGame){
                try {
                    incomingPlayer = _master.accept();
                    addPlayerToGame(incomingPlayer);
                } catch (IOException ex) {
                    Logger.getLogger(PDLMasterClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    final String BYE = "BYE";
    /*
    * method to add a new player to the game
    */
    public void addPlayerToGame(Socket s){
        try{
            DataInputStream socketReader = new DataInputStream(s.getInputStream());
            DataOutputStream socketWriter = new DataOutputStream(s.getOutputStream());
            //ask for name and icon, recieve in respective order
            socketWriter.writeUTF("JOIN");
            socketWriter.flush();
            
            //read player name
            String playerName = socketReader.readUTF();
           // System.out.println(playerName);
            
            BufferedImage playerIcon = recieveImageTwo(socketReader);
            //add player to my players
            PDLClient.instance.addPlayer(playerName, playerIcon);
            WaitingRoomGUI.instance.updateDisplay();
            
            //send the new player everyone
            ArrayList<String> names = PDLClient.instance.getPlayerList();
            ArrayList<BufferedImage> icons = PDLClient.instance.getPlayerIcons();
            
            for(int i =0; i <names.size(); i++){
                //send name
                socketWriter.writeUTF(names.get(i));
                socketWriter.flush();
                sendImage(icons.get(i), socketWriter);
            
            }
            
            //send everyone else only the new player using their threads
            for(int i =0; i<_players.size(); i++){
                _players.get(i).sendNewPlayer(playerName, playerIcon);
            }
            //start thread for the new player, master client is player 0
            PlayerThread newPlayer = new PlayerThread(s, _players.size()+1);
            _players.add(newPlayer);
           
            socketWriter.writeUTF(BYE);
            socketWriter.flush();
            //if our lobby is full, tell the server to not send us any more players
            if(_players.size() == maxPlayers){
                _toServer.ChangeGameState("DENY");
            }
            
        }catch(Exception e){
            System.out.println("error");
        }
    }
    /*
    * start the game and tell all other players and server its starting
    */
    public void startGame() throws IOException{
        _toServer.ChangeGameState("DENY");
        //do a countdown here probs
        _picLanes = new PictureLane[_players.size()+1];
        _playerCount = _players.size()+1;
         for(int i = 0; i<_playerCount; i++){ 
            _picLanes[i] = new PictureLane();
        }
        String randomP = _generator.randomizer();
        //0 is the master clients ID, he does the 0 lane
        _picLanes[0].addPhrase(randomP);
        //tell everyone the game is starting
        for(int i =0; i<_players.size(); i++){
            _players.get(i).startGame();
        }
        //tell server game has started
        _inGame = true;
        _curRound = 0;
        _submissions = 0;
        voteCount = 0;
        _isDrawRound = true;
        //+1 for mastr
        DrawingPageGUI.instance.clearImage();
        EndGameGUI.instance.getPanel().removeAll();
        ScreenManager.instance.changeScreen(ScreenManager.DRAW);
        //  WaitingRoomGUI.instance.frame.setVisible(false);
        //  DrawingPageGUI.instance.frame.setVisible(true);
        DrawingPageGUI.instance.setPhrase(randomP);
       
    }
    int [] votes = new int[6];
    int voteCount = 0;
    /*
    * count a vote locally
    */
    public void recieveVote(int id) throws IOException{
        votes[id]++;
        voteCount++;
        //+1 to include the master client
        if(voteCount == _players.size() + 1){
            int maxID = 0;
            for(int i = 0; i<votes.length; i++){
                if(votes[i] > votes[maxID]){
                    maxID = i;
                }
            }
            //show winner!
            showWinner(maxID);
            //loop over other players telling them the winner
            for(int i =0; i<_players.size(); i++){
                _players.get(i).showWinner(maxID);
            }
        }
    }
    /*
    * display the winner of the game
    */
    void showWinner(int id){
        //edit end game gui to just show winning lane i think
        EndGameGUI.instance.showWinner(id);
    }
    /*
    * show pic lanes for me and send them to everyone else
    */
    void endGame() throws IOException{
        //should have complete pic lanes at this point
        System.out.println("game over");
        _inGame = false;
        //EndGameGUI.instance.frame.setVisible(true);
        
        ScreenManager.instance.changeScreen(ScreenManager.END);
        for(int i = 0; i <_picLanes.length; i++){
            EndGameGUI.instance.addLane(_picLanes[i]);
        }
        //send all players the lanes
        for(int i = 0; i <_players.size(); i++){
            _players.get(i).endGame();
        }
        
    }
    /*
    * counts a submission each time a pic or phrase is recieved
    */
    void countSubmission() throws IOException{
        _submissions++;
        System.out.println("Recieved something");
        //if we recieve a pic from everybody, go to next round
        if(_submissions == _playerCount && goToNextRound()){
            System.out.println("next round");
            _submissions = 0;
            nextRound();
            
            
            //need a next round thi
        }
    }
    /*
    * determines whether to end the game or proceed to the next round
    */
    boolean goToNextRound() throws IOException{
        _curRound++;
        if(_curRound == _playerCount){
            endGame();
            return false;
        }
        else{
            return true;
        }
    }
    /*
    * makes the game go to the next round
    */
    void nextRound() throws IOException{
        //first round is drawRound
        _isDrawRound = !_isDrawRound;
        if(_isDrawRound){
            //DrawingPageGUI.instance.frame.setVisible(true);
            DrawingPageGUI.instance.clearImage();
            DrawingPageGUI.instance.setPhrase(_picLanes[(_playerCount - _curRound + 0)% _playerCount].getLastPhrase());
           // WaitingPage.instance.frame.setVisible(false);
            ScreenManager.instance.changeScreen(ScreenManager.DRAW);
        }
        else{
         //   PhrasePageGUI.instance.frame.setVisible(true);
            PhrasePageGUI.instance.clearPhrase();
            
            PhrasePageGUI.instance.setImage(_picLanes[(_playerCount - _curRound + 0)% _playerCount].getLastImage());
          //  WaitingPage.instance.frame.setVisible(false);
            ScreenManager.instance.changeScreen(ScreenManager.PHRASE);
        }
        //tell everyone what to do for next round
        for(int i =0; i <_players.size(); i++){
            _players.get(i).nextRound();
        }
    }
    /*
    * submit a game pic to self
    */
    public void submitGamePic(BufferedImage img) throws IOException{
        System.out.println("Adding image to lane " + (_playerCount - _curRound + 0)% _playerCount);
        _picLanes[(_playerCount - _curRound)%_playerCount].addImage(img);
        countSubmission();
    }
    /*
    * submit a game phrase to self
    */
    public void submitGamePhrase(String p) throws IOException{
        _picLanes[(_playerCount - _curRound)%_playerCount].addPhrase(p);
        countSubmission();
    }
    /*
    * remove a player from the game if they quit in the lobby
    */
    public void removePlayer(int id) throws IOException{
        if(!_inGame){
            //first remove player from my known player lists
            PDLClient.instance.getPlayerList().remove(id);
            PDLClient.instance.getPlayerIcons().remove(id);
            _players.remove(id-1);



            //tell all other players to remove him as well

            for(int i =0; i < _players.size(); i++){
                _players.get(i).removePlayerFromOther(id);
            }

            //update waiting display if !inGame
            if(!_inGame){
                WaitingRoomGUI.instance.updateDisplay();
            }
        }
    }
    /*
    * This thread listens to players throughout the game
    * Sends to added players when we in game    
    */
    public class PlayerThread extends Thread{
        private Socket _toPlayer;
        private DataInputStream _socketReader;
        private DataOutputStream _socketWriter;
        
        private int _id;
        public PlayerThread(Socket s, int playerId){
            try {
                _toPlayer = s;
                _socketReader = new DataInputStream(s.getInputStream());
                _socketWriter = new DataOutputStream(s.getOutputStream());
                _id = playerId;
                start();
            } catch (IOException ex) {
                System.out.println();
            }
        }
        @Override
        public void run(){
            while(true){
                try {
                    String command = _socketReader.readUTF();
                    System.out.println(command);
                    if(command.startsWith("PICTURE")){
                        recieveImage();
                    }
                    else if(command.startsWith("PHRASE")){
                        recievePhrase();
                    }
                    else if(command.startsWith("VOTE")){
                        countVote();
                    }
                   
                } catch (IOException ex) {
                    //player either quit or something went wrong so
                    //we can kick him out anyway probs
                    System.out.println("PLAYER QUIT!");
                    try {
                        removePlayer(_id);
                    } catch (IOException ex1) {
                        System.out.println("couldnt remove player from game");
                    }
                    stop();
                }
            }
        }
        /*
        * add the vote to the proper pic lane
        */
        void countVote(){
            try {
                int i = _socketReader.readInt();
                recieveVote(i);
            } catch (IOException ex) {
                System.out.println("Could not read vote");
            }
        }
        /*
        * send a new player to other clients
        */
        public void sendNewPlayer(String name, BufferedImage icon) throws IOException{
            
            //tell the player we're sending him a new player
            _socketWriter.writeUTF("NEWPLAYER");
            _socketWriter.flush();

            //send name
            _socketWriter.writeUTF(name);
            _socketWriter.flush();

            sendImage(icon, _socketWriter); 
        }   
        /*
        * recieve an image from a player
        */
        void recieveImage() {
            try {
                BufferedImage img = recieveImageTwo(_socketReader);
                _picLanes[(_playerCount - _curRound + _id)% _playerCount].addImage(img);
                countSubmission();
            } catch (IOException ex) {
                Logger.getLogger(PDLMasterClient.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
        /*
        * recieve a phrase from a player
        */
        void recievePhrase(){
            try {
                String phrase = _socketReader.readUTF();
                _picLanes[(_playerCount - _curRound + _id)% _playerCount].addPhrase(phrase);
                countSubmission();
            } catch (IOException ex) {
                System.out.println("couldnt recieve phrase.");
            }
        }
        /*
        * send a phrase to a player
        */
        void sendPhrase(String p) throws IOException{
            _socketWriter.writeUTF(p);
            _socketWriter.flush();
        }
        /*
        * tells a player the game is starting
        */
        public void startGame() throws IOException{
            _curRound = 0;
            _isDrawRound = true;
            _socketWriter.writeUTF("START");
            _socketWriter.flush();
            
            String randomP = _generator.randomizer();
            //send them the phrase
            _socketWriter.writeUTF(randomP);
            _socketWriter.flush();
            
            //everyones first phrase to add is given by the order they were added
            _picLanes[_id].addPhrase(randomP);
            
            //set master client in game thread MAYBE
        }
        /*
        * tells a player to go to the next round
        */
        public void nextRound() throws IOException{ 
            if(_isDrawRound){
                _socketWriter.writeUTF("DRAW");
                _socketWriter.flush();
                //send correct phrase here
                sendPhrase(_picLanes[(_playerCount - _curRound + _id)% _playerCount].getLastPhrase());
            }else{
                _socketWriter.writeUTF("PHRASE");
                _socketWriter.flush();
            
                //SEND IMAGE HERE
                sendImage(_picLanes[(_playerCount - _curRound + _id)% _playerCount].getLastImage(), _socketWriter);
            }            
        }
        /*
        * tells player the game has ended
        */
        public void endGame() throws IOException{
            _socketWriter.writeUTF("END");
            _socketWriter.flush();
            
            //send pic lanes here, client will rebuild them
            for(int i = 0; i <_picLanes.length; i++){
                ArrayList<String> phrases = _picLanes[i].getPhrases();
                ArrayList<BufferedImage> images = _picLanes[i].getImages();

                for(int j = 0; j <phrases.size(); j++){
                    //send the phrase
                    _socketWriter.writeUTF(phrases.get(j));
                    _socketWriter.flush();
                   
                    if(j <= images.size() - 1){
                        _socketWriter.writeUTF("PIC");
                        _socketWriter.flush();
                        //send an image
                        sendImage(images.get(j), _socketWriter);   
                       
                    }
                }
                _socketWriter.writeUTF("ENDLANE");
                _socketWriter.flush();
            }
            _socketWriter.writeUTF("BYE");
            _socketWriter.flush();
        }
        /*
        * tell the player what lane won
        */
        public void showWinner(int id) throws IOException{
            _socketWriter.writeUTF("WINNER");
            _socketWriter.writeInt(id);
            _socketWriter.flush();
        }
        /*
        * tells the player who quit
        */
        public void removePlayerFromOther(int id) throws IOException{
            if(_id > id){
                _id--;
            }
            _socketWriter.writeUTF("REMOVE");
            _socketWriter.writeInt(id);
            _socketWriter.flush();
        }
    }
    /*
    * this thread keeps connection to the server
    * we tell the server when that game has start/ended
    */
    public class ServerThread extends Thread{
        private Socket _toServer;
        private BufferedReader _socketReader;
        private PrintWriter _socketWriter;
        public ServerThread(Socket s) throws IOException{
            _toServer = s;
            _socketReader = new BufferedReader(new InputStreamReader(_toServer.getInputStream()));
            _socketWriter = new PrintWriter(s.getOutputStream());
        }
        
        @Override 
        public void run(){
            while(true){
                try {
                    String command = _socketReader.readLine();
                } catch (IOException ex) {
                    Logger.getLogger(PDLMasterClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        /*
        * Sends our server "Accept" when we can accept more players, and "Deny" when we do not want more players
        */
        String curState = "";
        public void ChangeGameState(String s){
            if(!curState.equals(s)){
                curState = s;
                _socketWriter.println(s);            
                _socketWriter.flush();
            }
        }
    }
    //sends an image
    public void sendImage(BufferedImage img, DataOutputStream s) throws IOException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "PNG", baos);
        byte [] imgArr = baos.toByteArray();
        sendBytes(imgArr, s);
    }
    //sends byte array
    public void sendBytes(byte[] b, DataOutputStream _socketWriter) throws IOException{
        System.out.println("sending: " + b.length);
        _socketWriter.writeInt(b.length);
        _socketWriter.flush();
        _socketWriter.write(b, 0, b.length);
        _socketWriter.flush();
    }
    //recieve Image as bytes
    public BufferedImage recieveImageTwo(DataInputStream _socketReader) throws IOException{
        int length = _socketReader.readInt();
        byte[] data = new byte[length];
        System.out.println("recievving" + length);
        _socketReader.readFully(data);
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        return ImageIO.read(bais);
    }
}
