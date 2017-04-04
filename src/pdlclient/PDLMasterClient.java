/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pdlclient;

import java.awt.List;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
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
    //private InGameThread gameManager;
    //may need a third thread for the server
    
    
    private ServerSocket _master;
    private String _roomCode;
    private int _listenPort = 3500;
    
    private boolean _inGame = false;
    
    //IN GAME TRACKERS
    private int _submissions = 0;
    private int _curRound = 0;
    private int _playerCount;
    private boolean _isDrawRound = true;
    private PictureLane [] _picLanes;
    
    public PDLMasterClient(String room) throws IOException{
        _roomCode = room;
        PDLClient.instance.setCurrentRoom(_roomCode);
        PDLClient.instance.addPlayer(PDLClient.instance.getPlayerName(), PDLClient.instance.getPlayerIcon());
        _players = new ArrayList<>();
        _master = new ServerSocket(_listenPort);
        _lobby = new LobbyWait();
        _lobby.start();
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
            //read player image
            byte[] sizeArr = new byte[4];
            s.getInputStream().read(sizeArr);
            int size = ByteBuffer.wrap(sizeArr).asIntBuffer().get();
            
            byte[] imgArr = new byte[size];
            s.getInputStream().read(imgArr);
            
            BufferedImage playerIcon = ImageIO.read(new ByteArrayInputStream(imgArr));
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
                
                //read image into byte stream
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                ImageIO.write(icons.get(i), "PNG", byteOut);
                //get image size
                byte [] imgSize = ByteBuffer.allocate(4).putInt(byteOut.size()).array();
                //write size and image to client
                s.getOutputStream().write(imgSize);
                s.getOutputStream().write(byteOut.toByteArray());
                s.getOutputStream().flush();
            }
            socketWriter.println(BYE);
            socketWriter.flush();
            
            //send everyone else only the new player using their threads
            for(int i =0; i<_players.size(); i++){
                _players.get(i).sendNewPlayer(playerName, playerIcon);
            }
            
            //start thread for the new player
            PlayerThread newPlayer = new PlayerThread(s, _players.size()+1);
            _players.add(newPlayer);
           
            
            
        }catch(Exception e){
            System.out.println("error");
        }
    }
    public void startGame(){
        //do a countdown here probs
        _picLanes = new PictureLane[_players.size()+1];
        _playerCount = _players.size()+1;
        for(int i = 0; i<_playerCount; i++){
            _picLanes[i] = new PictureLane(); 
        }
        //0 is the master clients ID, he does the 0 lane
        _picLanes[0].addPhrase("'Phrase to draw'");
        //tell everyone the game is starting
        for(int i =0; i<_players.size(); i++){
            _players.get(i).startGame();
        }
        //tell server game has started
        _inGame = true;
        //+1 for mastr
        
        
        WaitingRoomGUI.instance.frame.setVisible(false);
        DrawingPageGUI.instance.frame.setVisible(true);
        DrawingPageGUI.instance.setPhrase("'Phrase to draw'");       
    }
    void endGame(){
        //should have complete pic lanes at this point
    }
    void countSubmission(){
        _submissions++;
        System.out.println("Recieved something");
        //if we recieve a pic from everybody, go to next round
        if(_submissions == _playerCount && goToNextRound()){
            System.out.println("next round");
            _submissions = 0;
            nextRound();
            //tell everyone what to do for next round
            for(int i =0; i <_players.size(); i++){
                _players.get(i).nextRound();
            }
            
            //need a next round thi
        }
    }
    boolean goToNextRound(){
        _curRound++;
        if(_curRound == _playerCount){
            endGame();
            return false;
        }
        else{
            return true;
        }
    }
    void nextRound(){
        //first round is drawRound
        _isDrawRound = !_isDrawRound;
        if(_isDrawRound){
            DrawingPageGUI.instance.frame.setVisible(true);
            WaitingPage.instance.frame.setVisible(false);
        }
        else{
            
        }
    }
    public void submitGamePic(BufferedImage img){
        _picLanes[_curRound%_playerCount].addImage(img);
        countSubmission();
    }
    public void sumbitGamePhrase(String p){
        _picLanes[_curRound%_playerCount].addPhrase(p);
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
                } catch (IOException ex) {
                    System.out.println("failed to read from player");
                }
            }
        }
        
        public void sendNewPlayer(String name, BufferedImage icon){
            
            //tell the player we're sending him a new player
            _socketWriter.println("NEWPLAYER");
            _socketWriter.flush();

            //send name
            _socketWriter.println(name);
            _socketWriter.flush();

            sendImage(icon); 
        }
        void sendImage(BufferedImage icon){
            try {
                //read image into byte stream
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                ImageIO.write(icon, "PNG", byteOut);
                //get image size
                byte [] imgSize = ByteBuffer.allocate(4).putInt(byteOut.size()).array();
                //write size and image to client
                _toPlayer.getOutputStream().write(imgSize);
                _toPlayer.getOutputStream().write(byteOut.toByteArray());
                _toPlayer.getOutputStream().flush();
            } catch (IOException ex) {
                System.out.println("could not send new player");
            }
            
        }
        void recieveImage(){
            try {
                byte[] sizeArr = new byte[4];
                _toPlayer.getInputStream().read(sizeArr);
                int size = ByteBuffer.wrap(sizeArr).asIntBuffer().get();
                
                byte[] imgArr = new byte[size];
                _toPlayer.getInputStream().read(imgArr);
                
                BufferedImage playerIcon = ImageIO.read(new ByteArrayInputStream(imgArr));
                //add image here
                _picLanes[(_curRound + _id)% _playerCount].addImage(playerIcon);
                countSubmission();
            } catch (IOException ex) {
                System.out.println("Couldn't recieve image.");
            }
        }
        
        void recievePhrase(){
            
        }
        public void startGame(){
            _curRound = 0;
            _isDrawRound = true;
            _socketWriter.println("START");
            _socketWriter.flush();
            
            
            //send them the phrase
            _socketWriter.println("'Phrase to Draw'");
            _socketWriter.flush();
            
            //everyones first phrase to add is given by the order they were added
            _picLanes[_id].addPhrase(("'Phrase to Draw"));
            
            //set master client in game thread MAYBE
        }
        public void nextRound(){
            
            if(_isDrawRound){
                _socketWriter.println("DRAW");
                _socketWriter.flush();
                
                
                //send correct phrase here
            }else{
                _socketWriter.println("PHRASE");
                _socketWriter.flush();
            
                //SEND IMAGE HERE
            }       
            _curRound++;
            
        }
    }
}
