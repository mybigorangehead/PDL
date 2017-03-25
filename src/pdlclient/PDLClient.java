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
    public String playerName;
    public PDLMasterClient master = null;
    Socket toServer;
    
    
    
    //player icons are 512 x 512 pics
    public BufferedImage playerIcon;
    
    
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
        SelectRoomGUI r = new SelectRoomGUI();
    }
    
}
