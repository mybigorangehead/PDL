/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pdlclient;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Josh
 */
public class PDLMasterClient {
    ServerSocket _master;
    String _roomCode;
    int listenPort = 3500;
    
    boolean inGame = false;
    public PDLMasterClient(String room) throws IOException{
        _roomCode = room;
        _master = new ServerSocket(listenPort);
        WaitForPlayers();
    }
    
    void WaitForPlayers() throws IOException{
        Socket incomingPlayer;
        System.out.println("Waiting for players");
        while(!inGame){
            incomingPlayer = _master.accept();
        }
    }
}
