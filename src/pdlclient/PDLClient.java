/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pdlclient;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

/**
 * Using singleton because there will only be one client
 * 
 * @author Josh
 */
public class PDLClient {
    public static PDLClient instance; 
    
    //player name
    private String _playerName;
    
    //the room were in
    private String _currentRoom;
    
    //a master client object gets created if we are a master client
    private PDLMasterClient _master = null;
    private boolean _isMaster = false;
    
    //our socket to connect to the server
    private Socket _toServer;
    
   //thread to hold connection to a game when we are not the master client
    private GameThread _gameThread;
    
    //player icons are 128 x 128 pics
    private BufferedImage _playerIcon;    
    
    //arraylists to know what other players that are in my game
    private ArrayList<String> _playerNames = new ArrayList<>();
    private ArrayList<BufferedImage> _playerIcons = new ArrayList<>();
    
    private PDLClient(){
        
    }
    /**
     * Singleton getInstance
     * @return instance
     */
    public static PDLClient getInstance(){
        if(instance == null){
            instance = new PDLClient();
        }
        return instance;
    }
    /**
    * The main creates the only client object and the only screen manager object(operates the gui)
    */
    public static void main(String[] args) throws IOException, URISyntaxException {
        //instantiate static instances
        PDLClient player = PDLClient.getInstance();
        ScreenManager s = ScreenManager.getInstance();
      
    }
    
    /*
    * Tells our end game screen what lane to show, winner is sent from master client
    */
    public void showWinner(int winner){
        EndGameGUI.instance.showWinner(winner);
    }
    
    /*
    * stores the name enter on the home page of the gui
    */
    public void setPlayerName(String name){
        _playerName = name;
    }
    
    /*
    * stores the icon drawn on the home page oh the gui
    */
    public void setPlayerIcon(BufferedImage i){
        _playerIcon = i;
    }
    
    /*
    * returns my player name
    */
    public String getPlayerName(){
        return _playerName;
    }
    
    /*
    * returns my player icon
    */
    public BufferedImage getPlayerIcon(){
        return _playerIcon;
    }
    
    /*
    * Makes this client a master client
    */
    public void setMaster(String roomCode) throws IOException{
        _isMaster = true;
        _master = new PDLMasterClient(roomCode, _toServer);
    }
    
    /*
    * stores the code of the current room we're in
    */
    public void setCurrentRoom(String code){
        _currentRoom  = code;
    }
    
    /*
    * returns the code of the room we are in
    */
    public String getCurrentRoom(){
        return _currentRoom;
    }
    /*
    * connects us to the server
    */
    public void connectToServer(){
        try {
            _toServer = new Socket("127.0.0.1", 3000);
        } catch (IOException ex) {
            System.out.println("Couldn't connect to server");
        }
    }
    /*
    * returns the socket to the server
    */
    public Socket getServerSocket(){
        return _toServer;
    }
    /*
    * closes the socket to the server
    */
    public void disconnectServer(){
        try {
            _toServer.close();
        } catch (IOException ex) {
            System.out.println("Couldn't disconnect from server");
        }
    }
    //port to use if master client
    int M_PORT = 50001;
    
    /*
    *connects us to a master client
    */
    public void connectToMasterClient(String ipAdd){
        try {        
            System.out.println(ipAdd);
            Socket m = new Socket(ipAdd, M_PORT);
            System.out.println(ipAdd);
            _gameThread = new GameThread(m);
            _gameThread.start();
            System.out.println("connected to master client");
            
        } catch (IOException ex) {
            System.out.println("couldn't connect to master client");
        }
    }
    //code to send server to create a game
    String CREATE_CODE = "CREATE";
    /*
    * tells the server we want to create a game
    */
    public void createRoom(){
        BufferedReader socketReader;
        PrintWriter socketWriter; 
        try {
            System.out.println("trying to create room");
            connectToServer();
           // PDLClient.instance.toServer = new Socket("127.0.0.1", 3000);
            socketReader = new BufferedReader(new InputStreamReader(getServerSocket().getInputStream()));
            socketWriter = new PrintWriter(getServerSocket().getOutputStream());
            //send code for creating room
            socketWriter.println(CREATE_CODE + " 0");
            socketWriter.flush();
           
            String myRoomCode = socketReader.readLine();
            System.out.println(myRoomCode);
            ScreenManager.instance.changeScreen(ScreenManager.WAIT);
            /*SelectRoomGUI.instance.frame.setVisible(false);

            WaitingRoomGUI.instance.frame.setVisible(true);*/

            //set room code
            setMaster(myRoomCode); 

            //update my display to have my name and icon
            WaitingRoomGUI.instance.updateDisplay();

            //do not tell client to close socket to server, since he is master
        } catch (IOException ex) {
            System.out.println("Couldn't create room");
        }
    }
    //code to join a game
    String JOIN_CODE = "JOIN";
    /*
    * tells the server we want to join a game
    */
    public void joinRoom(String joinCode){
        BufferedReader socketReader;
        PrintWriter socketWriter; 
        try {

            System.out.println("trying to join room");
            connectToServer();
           // PDLClient.instance.toServer = new Socket("127.0.0.1", 3000);
            socketReader = new BufferedReader(new InputStreamReader(_toServer.getInputStream()));
            socketWriter = new PrintWriter(_toServer.getOutputStream());
            //send code for creating room
            socketWriter.println(JOIN_CODE + " " + joinCode);
            socketWriter.flush();
            System.out.println("Joining: " + joinCode);
            String response = socketReader.readLine();
            System.out.println(response);
            if(!response.equals("FAILURE")){                
                //can close connection to server here
                disconnectServer();
                socketReader.close();
                socketWriter.close();
                connectToMasterClient(response);
                ScreenManager.instance.changeScreen(ScreenManager.WAIT);
                //update my display to have my name and icon
                WaitingRoomGUI.instance.updateDisplay();
            }else{
                String errorMessage = socketReader.readLine();
                JOptionPane.showMessageDialog(null, errorMessage, "InfoBox: FAILURE", JOptionPane.ERROR_MESSAGE);
            }

            //do not tell client to close socket to server, since he is master
        } catch (IOException ex) {
            System.out.println("Couldn't create room");
        }
    }
    //code to join quick
    String QUICK = "QUICK";
    /*
    * tells the server we want to quick join a game, creates a game if no games to join
    */
    public void joinQuick(){
        BufferedReader socketReader;
        PrintWriter socketWriter; 
        try {

            System.out.println("trying to join room");
            connectToServer();
           // PDLClient.instance.toServer = new Socket("127.0.0.1", 3000);
            socketReader = new BufferedReader(new InputStreamReader(_toServer.getInputStream()));
            socketWriter = new PrintWriter(_toServer.getOutputStream());
           
            socketWriter.println(QUICK);
            socketWriter.flush();
           

            String response = socketReader.readLine();
            if(response.equals("NEW")){
                String myRoomCode = socketReader.readLine();
                System.out.println(myRoomCode);
                ScreenManager.instance.changeScreen(ScreenManager.WAIT);
                //set room code
                setMaster(myRoomCode); 
                //update my display to have my name and icon
                WaitingRoomGUI.instance.updateDisplay();
            }
            else {
                //String info = socketReader.readLine();
                disconnectServer();
                socketReader.close();
                socketWriter.close();
                connectToMasterClient(response);
                ScreenManager.instance.changeScreen(ScreenManager.WAIT);
            }
        } catch (IOException ex) {
            System.out.println("Couldn't create room");
        }
    }
    /*
    * returns the arraylist of player names in our game
    */
    public ArrayList<String> getPlayerList(){
        return _playerNames;
    }
    
    /*
    * returns the arraylist of player icons in our game
    */
    public ArrayList<BufferedImage> getPlayerIcons(){
        return _playerIcons;
    }
    /*
    This class listens from the master client throughout the game
    */
    public class GameThread extends Thread{
        private Socket _toMaster;
        private DataInputStream _socketReader;
        private DataOutputStream _socketWriter;
        public  GameThread(Socket s){
            try {
                _toMaster = s;
                _socketReader = new DataInputStream(s.getInputStream());
                _socketWriter = new DataOutputStream(s.getOutputStream());
            } catch (IOException ex) {
                System.out.println("error");
            }
        }
        
        @Override
        public void run(){
            try {
                while(true){
                    String command = _socketReader.readUTF();
                    //master client wants me to officially join, ie send my name and icon, and send me all the names and icons
                    if(command.equals("JOIN")){
                        setUpLobbyDisplay();                        
                    }
                    else if(command.equals("NEWPLAYER")){
                        recieveNewPlayer();
                    }
                    else if(command.equals("START")){
                        setUpDrawPage();
                    }
                    else if(command.equals("DRAW")){
                        setUpDrawPage();
                    }
                    else if(command.equals("PHRASE")){
                        setUpPhrasePage();
                    }
                    else if(command.equals("END")){
                        endGame();
                    }
                    else if(command.equals("WINNER")){
                        showWin();
                    }
                    else if(command.equals("REMOVE")){
                        removePlayer();
                    }
                }                
            } catch (IOException ex) {
                System.out.println("error");
            }
        
        }
        /*
        * displays the winner gui
        */
        void showWin(){
            try {
                int winner = _socketReader.readInt();
                showWinner(winner);
            } catch (IOException ex) {
                System.out.println("could not recieve winner");
            }
        }
        /*
        * sets up and displays the game lobby
        */
        void setUpLobbyDisplay(){
            try {
                //send my name
                _socketWriter.writeUTF(getPlayerName());
                _socketWriter.flush();
                sendImage(getPlayerIcon());
                
                String name;
                //read all other players names and images
                while(!(name = _socketReader.readUTF()).equals("BYE")){
                    
                    System.out.println(name);          
                    BufferedImage playerIcon = recieveImage();
                    addPlayer(name, playerIcon);
                                        
                }
                System.out.println("updating display");
                WaitingRoomGUI.instance.updateDisplay();
            } catch (IOException ex) {
                System.out.println("Error sending name or icon.");
            }
        }
        /*
        * recieves a player name and icon, adds to correct arraylists
        */
        public void recieveNewPlayer(){
            try {
                String name = _socketReader.readUTF();
                BufferedImage playerIcon = recieveImage();
                addPlayer(name, playerIcon);
                WaitingRoomGUI.instance.updateDisplay();                
            } catch (IOException ex) {
                System.out.println("Couldn't recieve new player");
            }
        }
        /*
        * recieves a phrase to draw, displays drawing page
        */
        void setUpDrawPage(){
        
            try {
                String toDraw = _socketReader.readUTF();
                
             
                ScreenManager.instance.changeScreen(ScreenManager.DRAW);
                DrawingPageGUI.instance.clearImage();
                DrawingPageGUI.instance.setPhrase(toDraw);
               // WaitingPage.instance.frame.setVisible(false);
            } catch (IOException ex) {
                System.out.println("Couldnt recieve phrase");
            }
        }
        /*
        * recieves a picture to phrase, displays phrase page
        */
        public void setUpPhrasePage(){
            try {
               
                
                BufferedImage playerIcon = recieveImage();
                //display proper gui
                //WaitingPage.instance.frame.setVisible(false);
                //PhrasePageGUI.instance.frame.setVisible(true);
                ScreenManager.instance.changeScreen(ScreenManager.PHRASE);
                PhrasePageGUI.instance.clearPhrase();
                PhrasePageGUI.instance.setImage(playerIcon);
                
               
            } catch (IOException ex) {
                System.out.println("Couldn't recieve image.");
            }
        }
        /*
        * sends a phrase to the master client
        */
        public void sendGamePhrase(String p) throws IOException{
            _socketWriter.writeUTF("PHRASE");
            _socketWriter.flush();
            _socketWriter.writeUTF(p);
            _socketWriter.flush();
        }
        /*
        *sends a vote to the master client
        */
        public void sendVote(int id) throws IOException{
            _socketWriter.writeUTF("VOTE");
            _socketWriter.writeInt(id);
            _socketWriter.flush();
        }
        /*
        *sends an image to the master client
        */
        public void sendGameImage(BufferedImage img) throws IOException{
            _socketWriter.writeUTF("PICTURE");
            _socketWriter.flush();
            sendImage(img);
        }
         /*
        * receives and displays all the picture lanes
        */
        public void endGame(){
            try {
                String line;
                //remove all from previous games just in case
                EndGameGUI.instance.getPanel().removeAll();
                while(!((line = _socketReader.readUTF()).equals("BYE"))){
                    //read and add phrase
                    PictureLane toAdd = new PictureLane();
                    while(!(line.equals("ENDLANE"))){
                        //add the phrase
                        toAdd.addPhrase(line);
                        line = _socketReader.readUTF();
                        //read image
                        if(!line.equals("ENDLANE")){
                            BufferedImage img = recieveImage();
                            toAdd.addImage(img);
                            line = _socketReader.readUTF();
                            
                        }
                    }
                    EndGameGUI.instance.addLane(toAdd);
                    ScreenManager.instance.changeScreen(ScreenManager.END);
                }
            } catch (IOException ex) {
                System.out.println("Couldn't recieve picture lane");
            }
        }
        //sends an image
        public void sendImage(BufferedImage img) throws IOException{
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "PNG", baos);
            byte [] imgArr = baos.toByteArray();
            sendBytes(imgArr);
        }
        /*
        * sends a byte array
        * used for images
        */
        public void sendBytes(byte[] b) throws IOException{
            System.out.println("sending: " + b.length);
            _socketWriter.writeInt(b.length);
            _socketWriter.flush();
            _socketWriter.write(b, 0, b.length);
            _socketWriter.flush();
        }
        /*
        * recieves an image as a byte array
        */
        public BufferedImage recieveImage() throws IOException{
            int length = _socketReader.readInt();
            byte[] data = new byte[length];
            System.out.println("recievving" + length);
            _socketReader.readFully(data);
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            return ImageIO.read(bais);
        }
        /*
        * removes a player who quit in the lobby
        */
        void removePlayer() throws IOException{
            int id = _socketReader.readInt();
            _playerNames.remove(id);
            _playerIcons.remove(id);
            WaitingRoomGUI.instance.updateDisplay();
        }
    }
    /*
    * returns true if we are a master lcient
    */
    public boolean isMaster(){
        return _isMaster;
    }
    /*
    * store a player name and icon
    */
    public void addPlayer(String playerName, BufferedImage playerImage){
        _playerNames.add(playerName);
        _playerIcons.add(playerImage);
    }
    /*
    * returns the socket to the master client
    */
    public PDLMasterClient getMasterClient(){
        return _master;
    }
    /*
    * sends game image from the thread
    */
    public void sendGameImage(BufferedImage img) throws IOException{
        _gameThread.sendGameImage(img);
    }
    /*
    * sends game phrase from the thread
    */
    public void sendGamePhrase(String p) throws IOException{
        _gameThread.sendGamePhrase(p);
    }
    /*
    * sends game vote from our thread
    */
    public void sendVote(int id) throws IOException{
        _gameThread.sendVote(id);
    }
    
}
