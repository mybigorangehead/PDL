/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pdlclient;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @author Josh
 */
public class WaitingRoomGUI {
    static WaitingRoomGUI instance;
   // public JFrame frame;
    private JTextField roomCode;
    private int maxPlayers = 6;
    private JTextField [] players = new JTextField[maxPlayers];
    private JLabel [] playerIcons = new JLabel[maxPlayers];
    private JButton start;
    private JPanel panel;
    public static WaitingRoomGUI getInstance() throws IOException, URISyntaxException{
        if(instance == null){
            instance = new WaitingRoomGUI();
        }
        return instance;
    }
    private WaitingRoomGUI() throws URISyntaxException, IOException{
        /*frame = new JFrame("Waiting Room");
        Color c  = new Color(99, 194, 255);
        frame.getContentPane().setBackground(c);
        frame.setSize(1024, 900);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);*/
        Color c  = new Color(99, 194, 255);
        panel = new JPanel(new GridBagLayout());
        GridBagConstraints cons = new GridBagConstraints();
        panel.setBackground(c);
        
        
        //create "Join Code" display
        Font font1 = new Font("Comic Sans MS", Font.BOLD, 20);
        JTextField title = new JTextField("Join Code");
        title.setFont(font1);
        title.setEditable(false);
        title.setBackground(c);
        Color textColor = new Color(231, 120, 120);
        title.setForeground(textColor);
        title.setBorder(BorderFactory.createEmptyBorder());
        cons.fill = GridBagConstraints.NONE;
        //cons.anchor = GridBagConstraints.WEST;
        cons.gridy = 0;
        cons.gridx = 1;
        panel.add(title, cons);
        
        //create placeholder text field for the join code
        Font codeFont = new Font("Comic Sans MS", Font.BOLD, 16);
        roomCode = new JTextField("0000");
        roomCode.setFont(codeFont);
        roomCode.setEditable(false);
        roomCode.setBackground(c);
        roomCode.setBorder(BorderFactory.createEmptyBorder());
        cons.fill = GridBagConstraints.NONE;
        cons.gridy = 1;
        cons.gridx = 1;
        panel.add(roomCode, cons);        
        
        
        //read in empty circle pic
        URI circ = getClass().getResource("/images/emptycircle.png").toURI();
        File circF = new File(circ);
        BufferedImage emptyCirc = ImageIO.read(circF);
    
        //create 6 empty place holders for the player icons
        //NickName text field
        int startY = 2;
        int startX = 0;
        Font nameFont = new Font("Comic Sans MS", Font.BOLD, 14);
        for(int i = 0; i <maxPlayers; i++){
            players[i] = new JTextField("........");
            players[i].setFont(nameFont);
            players[i].setBackground(c);
            players[i].setBorder(BorderFactory.createEmptyBorder());
            players[i].setEditable(false);
            cons.fill = GridBagConstraints.NONE;
            cons.gridy = startY;
            cons.gridx = startX;
            panel.add(players[i], cons);
            //icon label
            playerIcons[i] = new JLabel(new ImageIcon(emptyCirc.getScaledInstance(56, 56, 0)));
            cons.fill = GridBagConstraints.NONE;
            cons.gridx = startX;
            cons.gridy = startY+1;
            panel.setBackground(c);
            panel.add(playerIcons[i], cons);
            if(i%2 == 1){
                startY+=2;
            }
            else{
                startX = (startX == 0)? 2:0;
            }
            
        }
        //if(PDLClient.instance.isMaster()){
            
        URI url = getClass().getResource("/images/startbutton.png").toURI();
        File startF = new File(url);
        BufferedImage startImg = ImageIO.read(startF);
        start = new JButton(new ImageIcon(startImg));
        start.setBorder(BorderFactory.createEmptyBorder());
        start.setContentAreaFilled(false);
        start.addActionListener(new StartButton());
        //CREATE ACTIONLISTENER HERE YO
        //start.addActionListener(actionL);
        cons.fill = GridBagConstraints.NONE;
        cons.gridy = startY + 2;
        cons.gridx = 1;
        cons.ipady = 0;
        
        //cons.weight = 1;
        panel.add(start, cons);
            
        start.setVisible(false);
        start.setEnabled(false);
        
       /* frame.add(panel);
        frame.setVisible(false);*/
    }
    public JPanel getPanel(){
        return panel;
    }
    public void updateDisplay(){
        roomCode.setText(PDLClient.instance.getCurrentRoom());
        //players[0].setText(PDLClient.instance.getPlayerName());
        //playerIcons[0].setIcon(new ImageIcon(PDLClient.instance.getPlayerIcon().getScaledInstance(56, 56, 0)));
        ArrayList<String> names = PDLClient.instance.getPlayerList();
        ArrayList<BufferedImage> icons = PDLClient.instance.getPlayerIcons();
        for(int i = 0; i <names.size(); i++){
            players[i].setText(names.get(i));
            playerIcons[i].setIcon(new ImageIcon(icons.get(i).getScaledInstance(56, 56, 0)));            
        }
        if(PDLClient.instance.isMaster()){
            start.setVisible(true);
            start.setEnabled(true);
        }
    }
    public class StartButton implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            if(PDLClient.instance.getPlayerList().size() >=2){
                PDLClient.instance.getMasterClient().startGame();
            }
        }
    }     
}
