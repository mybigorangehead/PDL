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
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author Josh
 */
public class SelectRoomGUI {
    public static SelectRoomGUI instance = null;
    JTextField code;
    JFrame frame;
    JButton play;
    JButton join;
     public static SelectRoomGUI getInstance() throws IOException, URISyntaxException{
        if(instance == null){
            instance = new SelectRoomGUI();
        }
        return instance;
    }
    private SelectRoomGUI() throws IOException, URISyntaxException{
        frame = new JFrame("Select Room");
         Color c  = new Color(99, 194, 255);
        frame.getContentPane().setBackground(c);
        frame.setSize(1024, 900);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints cons = new GridBagConstraints();
        panel.setBackground(c);
         //QUICK PLAY BUTTON
        URI qpURL = getClass().getResource("/images/createroom.png").toURI();
        File qpPic = new File(qpURL);
        BufferedImage qp = ImageIO.read(qpPic);
        play = new JButton(new ImageIcon(qp));
        //remove borer
        play.setBorder(BorderFactory.createEmptyBorder());
        play.setContentAreaFilled(false);
        cons.fill = GridBagConstraints.NONE;
        cons.gridy = 0;
        cons.gridx = 0;
        cons.ipady = 20;
        ActionListener actionL = new PlayButton();
        play.addActionListener(actionL);
        //cons.weight = 1;
        panel.add(play, cons);
        
        Font font1 = new Font("Comic Sans MS", Font.BOLD, 28);
        
        
        //NickName text field
        code = new JTextField("Enter", 10);
        code.setFont(font1);
        code.setBorder(BorderFactory.createEmptyBorder());
        cons.fill = GridBagConstraints.NONE;
        cons.gridy = 1;
        cons.gridx = 0;
        cons.ipady = 20;
        cons.ipadx = 0;
        panel.add(code, cons);
        
        //NICKNAME BACKGROUND
        URI nURL = getClass().getResource("/images/nnbg.png").toURI();
        File nPic = new File(nURL);
        BufferedImage n = ImageIO.read(nPic);
        JLabel nnbg = new JLabel(new ImageIcon(n));
        cons.fill = GridBagConstraints.NONE;
        cons.gridy =1;
        cons.gridx = 0; 
        cons.ipady = 20;
        panel.add(nnbg, cons);
        
        URI jURL = getClass().getResource("/images/joinroom.png").toURI();
        File jPic = new File(jURL);
        BufferedImage j = ImageIO.read(jPic);
        join = new JButton(new ImageIcon(j));
        //remove borer
        join.setBorder(BorderFactory.createEmptyBorder());
        join.setContentAreaFilled(false);
        join.addActionListener(actionL);
        cons.fill = GridBagConstraints.NONE;
        cons.gridy = 2;
        cons.gridx = 0;
        cons.ipady = 0;
        //cons.weight = 1;
        panel.add(join, cons);
        
        frame.add(panel);

       frame.setVisible(false);
    }
    
    final String CREATE_CODE = "210";
    final String JOIN_CODE = "240";
    public class PlayButton implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == play){
                PDLClient.instance.createRoom();
            }
            else if(e.getSource() == join){
                PDLClient.instance.joinRoom(code.getText());
            }
            
        }        
        
        
    }
}

