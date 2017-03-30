/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pdlclient;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URISyntaxException;
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
        _master = new PDLMasterClient(roomCode);
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
            PDLClient.instance.connectToServer();
           // PDLClient.instance.toServer = new Socket("127.0.0.1", 3000);
            socketReader = new BufferedReader(new InputStreamReader(PDLClient.instance.getServerSocket().getInputStream()));
            socketWriter = new PrintWriter(PDLClient.instance.getServerSocket().getOutputStream());
            //send code for creating room
            socketWriter.println(CREATE_CODE);
            socketWriter.flush();
            System.out.println("here");
            String myRoomCode = socketReader.readLine();
            System.out.println(myRoomCode);

            socketReader.close();
            socketWriter.close();
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
                    //master client wants my player name and icon
                    if(command.equals("JOIN")){
                        setUpLobbyDisplay();
                        
                    }
                }                
            } catch (IOException ex) {
                System.out.println("error");
            }
        
        }
        void setUpLobbyDisplay(){
            try {
                _socketWriter.println(PDLClient.instance.getPlayerName());
                _socketWriter.flush();
                ImageIO.write(PDLClient.instance.getPlayerIcon(), "PNG", _toMaster.getOutputStream());
                String name;
                /*while(!(name = _socketReader.readLine()).equals("BYE")){
                    System.out.println(name);
                    BufferedImage playerIcon = ImageIO.read(ImageIO.createImageInputStream(_toMaster.getInputStream()));
                    PDLClient.instance.addPlayer(name, playerIcon);
                }
                */
                ArrayList<String> players = new ArrayList<>();
                while(!(name = _socketReader.readLine()).equals("BYE")){
                    System.out.println(name);
                    players.add(name);
                }
                
                ArrayList<BufferedImage> icons = new ArrayList<>();
                for(int i =0; i <players.size(); i++){
                    BufferedImage playerIcon = ImageIO.read(ImageIO.createImageInputStream(_toMaster.getInputStream()));
                    icons.add(playerIcon);
                    //send some nonesense
                    _socketWriter.println("yay");
                    _socketWriter.flush();
                  //   _imageReader.flush();
                     
                }
               // _imageReader.close();
                
                for(int i =0; i <players.size(); i++){
                    PDLClient.instance.addPlayer(players.get(i), icons.get(i));
                }
                WaitingRoomGUI.instance.updateDisplay();
            } catch (IOException ex) {
                System.out.println("Error sending name or icon.");
            }
        }
        
    }
    
    public void addPlayer(String playerName, BufferedImage playerImage){
        _playerNames.add(playerName);
        _playerIcons.add(playerImage);
    }
}
