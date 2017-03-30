/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pdlclient;

import java.awt.List;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author Josh
 */
public class PDLMasterClient{
    
    
    private ArrayList<PlayerThread> players;
    private ServerSocket _master;
    private String _roomCode;
    private int listenPort = 3500;
    private LobbyWait _lobby;
    private boolean inGame = false;
    
    public PDLMasterClient(String room) throws IOException{
        _roomCode = room;
        PDLClient.instance.setCurrentRoom(_roomCode);
        PDLClient.instance.addPlayer(PDLClient.instance.getPlayerName(), PDLClient.instance.getPlayerIcon());
        players = new ArrayList<>();
        _master = new ServerSocket(listenPort);
        _lobby = new LobbyWait();
        
        _lobby.start();
    }
    
   
    
    
    public class LobbyWait extends Thread{
         @Override
        public void run() {
            Socket incomingPlayer = null;
           // BufferedReader socketReader = new BufferedReader(new InputStreamReader(PDLClient.instance.toServer.getInputStream()));
            System.out.println("Waiting for players");
            while(!inGame){
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
            BufferedImage playerIcon = ImageIO.read(ImageIO.createImageInputStream(s.getInputStream()));
            //add player to my players
            PDLClient.instance.addPlayer(playerName, playerIcon);
            WaitingRoomGUI.instance.updateDisplay();
            //send the new player everyone
            ArrayList<String> names = PDLClient.instance.getPlayerList();
            ArrayList<BufferedImage> icons = PDLClient.instance.getPlayerIcons();
            
            for(int i =0; i <names.size(); i++){
                socketWriter.println(names.get(i));
                socketWriter.flush();
                //String wait = socketReader.readLine();
               /* ImageIO.write(icons.get(i), "PNG", s.getOutputStream());
                s.getOutputStream().flush();*/
            }
            socketWriter.println(BYE);
            socketWriter.flush();
            for(int i =0; i <icons.size(); i++){
                ImageIO.write(icons.get(i), "PNG", s.getOutputStream());
                s.getOutputStream().flush();
                //read the nonsense
                socketReader.readLine();
            }
            
            //send everyone else only the new player using their threads
            
        }catch(Exception e){
            System.out.println("error");
        }
    }
    public class PlayerThread extends Thread{
        private Socket _toPlayer;
        private BufferedReader _socketReader;
        private PrintWriter _socketWriter;
        public PlayerThread(Socket s){
            try {
                _toPlayer = s;
                _socketReader = new BufferedReader(new InputStreamReader(s.getInputStream()));
                _socketWriter = new PrintWriter(s.getOutputStream());
            } catch (IOException ex) {
                System.out.println();
            }
        }
        @Override
        public void run(){
            while(true){
                try {
                    String command = _socketReader.readLine();
                } catch (IOException ex) {
                    System.out.println("failed to read from player");
                }
            }
        }
    }
}
