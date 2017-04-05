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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

/**
 * Using singleton
 * @author Josh
 */
public class PDLClient {
    public static PDLClient instance; 
    private String _playerName;
    private String _currentRoom;
    
    private PDLMasterClient _master = null;
    private boolean _isMaster = false;
    
    private Socket _toServer;
    
   // private Socket _toMaster;
    private GameThread _gameThread;
    //player icons are 512 x 512 pics
    private BufferedImage _playerIcon;
    
    
    //need to know who else is in my game yo
    private ArrayList<String> _playerNames = new ArrayList<>();
    private ArrayList<BufferedImage> _playerIcons = new ArrayList<>();
    //socket to server
    //socket to master client, may want to create masterclient class soon....
    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    PDLClient(){
     
    }
    public static PDLClient getInstance(){
        if(instance == null){
            instance = new PDLClient();
        }
        return instance;
    }
    
    public static void main(String[] args) throws IOException, URISyntaxException {
        //instantiate static instance
        PDLClient player = PDLClient.getInstance();
        
        //construct gui
        HomeGUI gui = HomeGUI.getInstance();
        SelectRoomGUI sGui = SelectRoomGUI.getInstance();
        WaitingRoomGUI wait =  WaitingRoomGUI.getInstance();
        DrawingPageGUI draw = DrawingPageGUI.getInstance();
        WaitingPage waiting = WaitingPage.getInstance();
        PhrasePageGUI phrase = PhrasePageGUI.getInstance();
        EndGameGUI end = EndGameGUI.getInstance();
    }
    
    public void setPlayerName(String name){
        _playerName = name;
    }
    public void setPlayerIcon(BufferedImage i){
        _playerIcon = i;
    }
    public String getPlayerName(){
        return _playerName;
    }
    public BufferedImage getPlayerIcon(){
        return _playerIcon;
    }
    public void setMaster(String roomCode) throws IOException{
        _isMaster = true;
        _master = new PDLMasterClient(roomCode, _toServer);
    }
    public void setCurrentRoom(String code){
        _currentRoom  = code;
    }
    public String getCurrentRoom(){
        return _currentRoom;
    }
    public void connectToServer(){
        try {
            _toServer = new Socket("127.0.0.1", 3000);
        } catch (IOException ex) {
            System.out.println("Couldn't connect to server");
        }
    }
    public Socket getServerSocket(){
        return _toServer;
    }
    public void disconnectServer(){
        try {
            _toServer.close();
        } catch (IOException ex) {
            System.out.println("Couldn't disconnect from server");
        }
    }
    int M_PORT = 3500;
    public void connectToMasterClient(String ipAdd){
        try {        
            Socket m = new Socket(ipAdd, M_PORT);
            _gameThread = new GameThread(m);
            _gameThread.start();
            System.out.println("connected to master client");
            
        } catch (IOException ex) {
            System.out.println("couldn't connect to master client");
        }
    }
    String CREATE_CODE = "210";
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
            socketWriter.println(CREATE_CODE);
            socketWriter.flush();
            System.out.println("here");
            String myRoomCode = socketReader.readLine();
            System.out.println(myRoomCode);
            SelectRoomGUI.instance.frame.setVisible(false);

            WaitingRoomGUI.instance.frame.setVisible(true);

            //set room code
            setMaster(myRoomCode); 

            //update my display to have my name and icon
            WaitingRoomGUI.instance.updateDisplay();

            //do not tell client to close socket to server, since he is master
        } catch (IOException ex) {
            System.out.println("Couldn't create room");
        }
    }
    String JOIN_CODE = "240";
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
            if(!response.equals("FAILED")){                
                //can close connection to server here
                disconnectServer();
                socketReader.close();
                socketWriter.close();

                connectToMasterClient(response);
                SelectRoomGUI.instance.frame.setVisible(false);

                WaitingRoomGUI.instance.frame.setVisible(true);


                //update my display to have my name and icon
                WaitingRoomGUI.instance.updateDisplay();
            }

            //do not tell client to close socket to server, since he is master
        } catch (IOException ex) {
            System.out.println("Couldn't create room");
        }
    }
    
    public ArrayList<String> getPlayerList(){
        return _playerNames;
    }
    public ArrayList<BufferedImage> getPlayerIcons(){
        return _playerIcons;
    }
    /*
    This class listens from the master client throughout the game
    */
    public class GameThread extends Thread{
        private Socket _toMaster;
        private BufferedReader _socketReader;
        private PrintWriter _socketWriter;
        public  GameThread(Socket s){
            try {
                _toMaster = s;
                _socketReader = new BufferedReader(new InputStreamReader(s.getInputStream()));
                _socketWriter = new PrintWriter(s.getOutputStream());
            } catch (IOException ex) {
                System.out.println("error");
            }
        }
        
        @Override
        public void run(){
            try {
                while(true){
                    String command = _socketReader.readLine();
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
                }                
            } catch (IOException ex) {
                System.out.println("error");
            }
        
        }
        void setUpLobbyDisplay(){
            try {
                //send my name
                _socketWriter.println(getPlayerName());
                _socketWriter.flush();
                
                //read my icon into byte stream
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                ImageIO.write(getPlayerIcon(), "PNG", byteOut);
                
                //get image size, write both size and image to master client
                byte [] imgSize = ByteBuffer.allocate(4).putInt(byteOut.size()).array();
                _toMaster.getOutputStream().write(imgSize);
                _toMaster.getOutputStream().write(byteOut.toByteArray());
                _toMaster.getOutputStream().flush();
                
                String name;
                while(!(name = _socketReader.readLine()).equals("BYE")){
                    System.out.println(name);
                    
                    byte[] sizeArr = new byte[4];
                    _toMaster.getInputStream().read(sizeArr);
                    int size = ByteBuffer.wrap(sizeArr).asIntBuffer().get();

                    byte[] imgArr = new byte[size];
                    _toMaster.getInputStream().read(imgArr);

                    BufferedImage playerIcon = ImageIO.read(new ByteArrayInputStream(imgArr));
                    addPlayer(name, playerIcon);
                }
                WaitingRoomGUI.instance.updateDisplay();
            } catch (IOException ex) {
                System.out.println("Error sending name or icon.");
            }
        }
        public void recieveNewPlayer(){
            try {
                String name = _socketReader.readLine();
                byte[] sizeArr = new byte[4];
                _toMaster.getInputStream().read(sizeArr);
                int size = ByteBuffer.wrap(sizeArr).asIntBuffer().get();

                byte[] imgArr = new byte[size];
                _toMaster.getInputStream().read(imgArr);

                BufferedImage playerIcon = ImageIO.read(new ByteArrayInputStream(imgArr));
                addPlayer(name, playerIcon);
                WaitingRoomGUI.instance.updateDisplay();
                
            } catch (IOException ex) {
                System.out.println("Couldn't recieve new player");
            }
        }    
        void setUpDrawPage(){
        
            try {
                String toDraw = _socketReader.readLine();
                
                //disable lobby menu
                WaitingRoomGUI.instance.frame.setVisible(false);
                
                //enable drawing page
                DrawingPageGUI.instance.frame.setVisible(true);
                DrawingPageGUI.instance.clearImage();
                DrawingPageGUI.instance.setPhrase(toDraw);
                WaitingPage.instance.frame.setVisible(false);
            } catch (IOException ex) {
                System.out.println("Couldnt recieve phrase");
            }
        }
        public void sendGameImage(BufferedImage img){
            try {
                System.out.println("sending picture");
                _socketWriter.println("PICTURE");
                _socketWriter.flush();
                //read my icon into byte stream
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                ImageIO.write(img, "PNG", byteOut);
                
                //get image size, write both size and image to master client
                byte [] imgSize = ByteBuffer.allocate(4).putInt(byteOut.size()).array();
                _toMaster.getOutputStream().write(imgSize);
                _toMaster.getOutputStream().write(byteOut.toByteArray());
                _toMaster.getOutputStream().flush();
            } catch (IOException ex) {
                System.out.println("Could not send image.");
            }
        }
        public void setUpPhrasePage(){
            try {
                //read image
                byte[] sizeArr = new byte[4];
                _toMaster.getInputStream().read(sizeArr);
                int size = ByteBuffer.wrap(sizeArr).asIntBuffer().get();
                
                byte[] imgArr = new byte[size];
                _toMaster.getInputStream().read(imgArr);
                
                BufferedImage playerIcon = ImageIO.read(new ByteArrayInputStream(imgArr));
                //display proper gui
                WaitingPage.instance.frame.setVisible(false);
                PhrasePageGUI.instance.frame.setVisible(true);
                PhrasePageGUI.instance.clearPhrase();
                PhrasePageGUI.instance.setImage(playerIcon);
                
               
            } catch (IOException ex) {
                System.out.println("Couldn't recieve image.");
            }
        }
        public void sendGamePhrase(String p){
            _socketWriter.println("PHRASE");
            _socketWriter.flush();
            _socketWriter.println(p);
            _socketWriter.flush();
        }
    }
    public boolean isMaster(){
        return _isMaster;
    }
    public void addPlayer(String playerName, BufferedImage playerImage){
        _playerNames.add(playerName);
        _playerIcons.add(playerImage);
    }
    public PDLMasterClient getMasterClient(){
        return _master;
    }
    public void sendGameImage(BufferedImage img){
        _gameThread.sendGameImage(img);
    }
    public void sendGamePhrase(String p){
        _gameThread.sendGamePhrase(p);
    }
   
}
