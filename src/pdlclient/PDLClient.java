/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pdlclient;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.Socket;
import java.net.URISyntaxException;

/**
 * Using singleton
 * @author Josh
 */
public class PDLClient {
    public static PDLClient instance;
    String _playerName;
    PDLMasterClient _master = null;
    Socket toServer;
    
    //player icons are 512 x 512 pics
    BufferedImage _playerIcon;
    
    
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
        
        //create and display home gui
        HomeGUI gui = HomeGUI.getInstance();
        SelectRoomGUI sGui = SelectRoomGUI.getInstance();
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
        _master = new PDLMasterClient(roomCode);
    }
    
}
