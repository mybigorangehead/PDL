/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pdlclient;
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
import java.net.Socket;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JOptionPane;

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
        ScreenManager s = ScreenManager.getInstance();
      
    }
    public void showWinner(int winner){
        EndGameGUI.instance.showWinner(winner);
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
            _toServer = new Socket("172.25.43.145", 3000);
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
    int M_PORT = 50001;
    public void connectToMasterClient(String ipAdd){
        try {        
            Socket m = new Socket(ipAdd, M_PORT);
           // System.out.println("here");
            _gameThread = new GameThread(m);
            _gameThread.start();
            System.out.println("connected to master client");
            
        } catch (IOException ex) {
            System.out.println("couldn't connect to master client");
        }
    }
    String CREATE_CODE = "CREATE";
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
    String JOIN_CODE = "JOIN";
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
    String QUICK = "QUICK";
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
                    else if(command.equals("END")){
                        endGame();
                    }
                    else if(command.equals("WINNER")){
                        showWin();
                    }
                }                
            } catch (IOException ex) {
                System.out.println("error");
            }
        
        }
        void showWin(){
            try {
                int winner = Integer.parseInt(_socketReader.readLine());
                showWinner(winner);
            } catch (IOException ex) {
                System.out.println("could not recieve winner");
            }
        }
        void setUpLobbyDisplay(){
            try {
                //send my name
                _socketWriter.println(getPlayerName());
                _socketWriter.flush();
                sendImage(getPlayerIcon(), _toMaster);
                
                String name;
                //read all other players names and images
                while(!(name = _socketReader.readLine()).equals("BYE")){
                    
                    System.out.println(name);           

                    BufferedImage playerIcon = recieveImage(_toMaster);
                    addPlayer(name, playerIcon);
                    _socketWriter.println("GOTIT");
                    _socketWriter.println("GOTIT");
                    _socketWriter.flush();
                    
                }
                System.out.println("updating display");
                WaitingRoomGUI.instance.updateDisplay();
            } catch (IOException ex) {
                System.out.println("Error sending name or icon.");
            }
        }
        public void recieveNewPlayer(){
            try {
                String name = _socketReader.readLine();
                

                BufferedImage playerIcon = recieveImage(_toMaster);
                addPlayer(name, playerIcon);
                _socketWriter.println("GOTIT");
                _socketWriter.flush();
                WaitingRoomGUI.instance.updateDisplay();
                
            } catch (IOException ex) {
                System.out.println("Couldn't recieve new player");
            }
        }    
        void setUpDrawPage(){
        
            try {
                String toDraw = _socketReader.readLine();
                
             
                ScreenManager.instance.changeScreen(ScreenManager.DRAW);
                DrawingPageGUI.instance.clearImage();
                DrawingPageGUI.instance.setPhrase(toDraw);
               // WaitingPage.instance.frame.setVisible(false);
            } catch (IOException ex) {
                System.out.println("Couldnt recieve phrase");
            }
        }
        
        public void setUpPhrasePage(){
            try {
               
                
                BufferedImage playerIcon = recieveImage(_toMaster);
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
        public void sendGamePhrase(String p){
            _socketWriter.println("PHRASE");
            _socketWriter.flush();
            _socketWriter.println(p);
            _socketWriter.flush();
        }
        public void sendVote(int id){
            _socketWriter.println("VOTE");
            _socketWriter.println(id);
            _socketWriter.flush();
        }
        public void sendGameImage(BufferedImage img) throws IOException{
            _socketWriter.println("PICTURE");
            _socketWriter.flush();
            sendImage(img, _toMaster);
        }
        public void endGame(){
            try {
                String line;
                while(!((line = _socketReader.readLine()).equals("BYE"))){
                    //read and add phrase
                    PictureLane toAdd = new PictureLane();
                    while(!(line.equals("ENDLANE"))){
                        //add the phrase
                        toAdd.addPhrase(line);
                        line = _socketReader.readLine();
                        //read image
                        if(!line.equals("ENDLANE")){
                            BufferedImage img = recieveImage(_toMaster);
                            toAdd.addImage(img);
                            _socketWriter.println("GOTIT");
                            _socketWriter.flush();
                            line = _socketReader.readLine();
                            
                        }
                    }
                    EndGameGUI.instance.addLane(toAdd);
                    ScreenManager.instance.changeScreen(ScreenManager.END);
                }
            } catch (IOException ex) {
                System.out.println("Couldn't recieve picture lane");
            }
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
    public void sendGameImage(BufferedImage img) throws IOException{
        _gameThread.sendGameImage(img);
    }
    public void sendGamePhrase(String p){
        _gameThread.sendGamePhrase(p);
    }
    public void sendVote(int id){
        _gameThread.sendVote(id);
    }
    public void sendImage(BufferedImage img, Socket s) throws IOException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "PNG", baos);
        byte [] imgArr = baos.toByteArray();
        sendBytes(imgArr, s);
    }
    public void sendBytes(byte[] b, Socket s) throws IOException{
        System.out.println("sending: " + b.length);
        OutputStream out = s.getOutputStream();
        DataOutputStream dos = new DataOutputStream(out);
        dos.flush();
        dos.writeInt(b.length);
        dos.flush();
        dos.write(b, 0, b.length);
        dos.flush();
        
        //dos.close();
        //out.close();
    }
    public BufferedImage recieveImage(Socket s) throws IOException{
        
        InputStream in = s.getInputStream();
        DataInputStream dis = new DataInputStream(in);
        int length = dis.readInt();
        byte[] data = new byte[length];
        System.out.println("recievving" + length);
        dis.readFully(data);
        
        //dis.reset();
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        return ImageIO.read(bais);
    }
}
