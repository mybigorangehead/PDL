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
    
    
    private ArrayList<PlayerThread> _players;
    private LobbyWait _lobby;
    private ServerThread _toServer;
    //private InGameThread gameManager;
    //may need a third thread for the server
    
    
    private ServerSocket _master;
    private String _roomCode;
    private int _listenPort = 50001;
    
    private boolean _inGame = false;
    
    //IN GAME TRACKERS
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
    public void addPlayerToGame(Socket s){
        try{
            BufferedReader socketReader = new BufferedReader(new InputStreamReader(s.getInputStream()));
            PrintWriter socketWriter = new PrintWriter(s.getOutputStream());
            //ask for name and icon, recieve in respective order
            socketWriter.println("JOIN");
            socketWriter.flush();
            
            //read player name
            String playerName = socketReader.readLine();
           // System.out.println(playerName);
            
            BufferedImage playerIcon = PDLClient.instance.recieveImage(s);
            //add player to my players
            PDLClient.instance.addPlayer(playerName, playerIcon);
            WaitingRoomGUI.instance.updateDisplay();
            
            //send the new player everyone
            ArrayList<String> names = PDLClient.instance.getPlayerList();
            ArrayList<BufferedImage> icons = PDLClient.instance.getPlayerIcons();
            
            for(int i =0; i <names.size(); i++){
                //send name
                socketWriter.println(names.get(i));
                socketWriter.flush();
                PDLClient.instance.sendImage(icons.get(i), s);
                String bs = socketReader.readLine();
                System.out.println(bs);
            
            }
            
            //send everyone else only the new player using their threads
            for(int i =0; i<_players.size(); i++){
                _players.get(i).sendNewPlayer(playerName, playerIcon);
            }
            
            //start thread for the new player
            PlayerThread newPlayer = new PlayerThread(s, _players.size()+1);
            _players.add(newPlayer);
           
            socketWriter.println(BYE);
            socketWriter.flush();
            //if our lobby is full, tell the server to not send us any more players
            if(_players.size() == maxPlayers){
                _toServer.ChangeGameState("DENY");
            }
            
        }catch(Exception e){
            System.out.println("error");
        }
    }
    public void startGame(){
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
        //+1 for mastr

        ScreenManager.instance.changeScreen(ScreenManager.DRAW);
        //  WaitingRoomGUI.instance.frame.setVisible(false);
        //  DrawingPageGUI.instance.frame.setVisible(true);
        DrawingPageGUI.instance.setPhrase(randomP);
       
    }
    int [] votes = new int[6];
    int voteCount = 0;
    public void recieveVote(int id){
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
    void showWinner(int id){
        //edit end game gui to just show winning lane i think
        EndGameGUI.instance.showWinner(id);
    }
    void endGame() throws IOException{
        //should have complete pic lanes at this point
        System.out.println("game over");
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
    public void submitGamePic(BufferedImage img) throws IOException{
        System.out.println("Adding image to lane " + (_playerCount - _curRound + 0)% _playerCount);
        _picLanes[(_playerCount - _curRound)%_playerCount].addImage(img);
        countSubmission();
    }
    public void submitGamePhrase(String p) throws IOException{
        _picLanes[(_playerCount - _curRound)%_playerCount].addPhrase(p);
        countSubmission();
    }
    /*
    * This thread listens to players throughout the game
    * Sends to added players when we in game    
    */
    public class PlayerThread extends Thread{
        private Socket _toPlayer;
        private BufferedReader _socketReader;
        private PrintWriter _socketWriter;
        
        private int _id;
        public PlayerThread(Socket s, int playerId){
            try {
                _toPlayer = s;
                _socketReader = new BufferedReader(new InputStreamReader(s.getInputStream()));
                _socketWriter = new PrintWriter(s.getOutputStream());
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
                    String command = _socketReader.readLine();
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
                    System.out.println("failed to read from player");
                    //can probably handle a player quiting here por favor
                }
            }
        }
        void countVote(){
            try {
                int i = Integer.parseInt(_socketReader.readLine());
                recieveVote(i);
            } catch (IOException ex) {
                System.out.println("Could not read vote");
            }
        }
        public void sendNewPlayer(String name, BufferedImage icon) throws IOException{
            
            //tell the player we're sending him a new player
            _socketWriter.println("NEWPLAYER");
            _socketWriter.flush();

            //send name
            _socketWriter.println(name);
            _socketWriter.flush();

            PDLClient.instance.sendImage(icon, _toPlayer); 
        }        
        void recieveImage() {
            try {
                BufferedImage img = PDLClient.instance.recieveImage(_toPlayer);
                _picLanes[(_playerCount - _curRound + _id)% _playerCount].addImage(img);
                countSubmission();
            } catch (IOException ex) {
                Logger.getLogger(PDLMasterClient.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
        void recievePhrase(){
            try {
                String phrase = _socketReader.readLine();
                _picLanes[(_playerCount - _curRound + _id)% _playerCount].addPhrase(phrase);
                countSubmission();
            } catch (IOException ex) {
                System.out.println("couldnt recieve phrase.");
            }
        }
        void sendPhrase(String p){
            _socketWriter.println(p);
            _socketWriter.flush();
        }
        public void startGame(){
            _curRound = 0;
            _isDrawRound = true;
            _socketWriter.println("START");
            _socketWriter.flush();
            
            String randomP = _generator.randomizer();
            //send them the phrase
            _socketWriter.println(randomP);
            _socketWriter.flush();
            
            //everyones first phrase to add is given by the order they were added
            _picLanes[_id].addPhrase(randomP);
            
            //set master client in game thread MAYBE
        }
        public void nextRound() throws IOException{ 
            if(_isDrawRound){
                _socketWriter.println("DRAW");
                _socketWriter.flush();
                //send correct phrase here
                sendPhrase(_picLanes[(_playerCount - _curRound + _id)% _playerCount].getLastPhrase());
            }else{
                _socketWriter.println("PHRASE");
                _socketWriter.flush();
            
                //SEND IMAGE HERE
                PDLClient.instance.sendImage(_picLanes[(_playerCount - _curRound + _id)% _playerCount].getLastImage(), _toPlayer);
            }            
        }
        public void endGame() throws IOException{
            _socketWriter.println("END");
            _socketWriter.flush();
            
            //send pic lanes here, client will rebuild them
            for(int i = 0; i <_picLanes.length; i++){
                ArrayList<String> phrases = _picLanes[i].getPhrases();
                ArrayList<BufferedImage> images = _picLanes[i].getImages();

                for(int j = 0; j <phrases.size(); j++){
                    //send the phrase
                    _socketWriter.println(phrases.get(j));
                    _socketWriter.flush();
                   
                    if(j <= images.size() - 1){
                        _socketWriter.println("PIC");
                        _socketWriter.flush();
                        //send an image
                        PDLClient.instance.sendImage(images.get(j), _toPlayer);   
                        //waits 
                        String s = _socketReader.readLine();
                        System.out.println(s);
                    }
                }
                _socketWriter.println("ENDLANE");
                _socketWriter.flush();
            }
            _socketWriter.println("BYE");
            _socketWriter.flush();
        }
        public void showWinner(int id){
            _socketWriter.println("WINNER");
            _socketWriter.println(id);
            _socketWriter.flush();
        }
    }
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
}
