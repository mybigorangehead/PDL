/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pdlclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Josh
 */
public class PDLMasterClient extends Thread{
    ServerSocket _master;
    String _roomCode;
    int listenPort = 3500;
    
    boolean inGame = false;
    public PDLMasterClient(String room) throws IOException{
        _roomCode = room;
        PDLClient.instance.setCurrentRoom(_roomCode);
        _master = new ServerSocket(listenPort);
        start();
    }
    
    @Override
    public void run() {
        Socket incomingPlayer = null;
       // BufferedReader socketReader = new BufferedReader(new InputStreamReader(PDLClient.instance.toServer.getInputStream()));
        System.out.println("Waiting for players");
        while(!inGame){
            try {
                incomingPlayer = _master.accept();
            } catch (IOException ex) {
                Logger.getLogger(PDLMasterClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
