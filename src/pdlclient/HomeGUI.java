/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pdlclient;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 * @author Josh
 */
public class HomeGUI {
    public static HomeGUI instance;
    JFrame frame;
    DrawPanel draw;
    JTextField name;
    
    public static HomeGUI getInstance() throws IOException, URISyntaxException{
        if(instance == null){
            instance = new HomeGUI();
        }
        return instance;
    }
    //constructs the home page interface
    private HomeGUI() throws IOException, URISyntaxException{
        
        frame = new JFrame("Picture Down The Lane");
        Color c  = new Color(99, 194, 255);
        frame.getContentPane().setBackground(c);
        frame.setSize(1024, 900);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints cons = new GridBagConstraints();
        
        
        //TITLE IMAGE
        URI imageurl = getClass().getResource("/images/homepage.png").toURI();
        File f = new File(imageurl);
        BufferedImage title = ImageIO.read(f);
     
        JLabel titleLable = new JLabel(new ImageIcon(title));
        cons.fill = GridBagConstraints.NONE;
        cons.gridx = 0;
        cons.gridy =0;
        cons.weightx = 1;
        cons.ipady = 20;
        panel.setBackground(c);
        panel.add(titleLable, cons);
        
        //the drawing box
        int size = 256;
        draw = new DrawPanel(size);
        cons.fill = GridBagConstraints.NONE;
        cons.ipadx = size;
        cons.ipady = size;
        cons.gridx = 0;
        cons.gridy = 1;      
       // cons.weightx = 5;
        panel.add(draw, cons);
        //add color picker
        JPanel colors = draw.getColorPanel(32);
        cons.fill = GridBagConstraints.NONE;
        cons.ipadx = 0;
        cons.ipady = 0;
        cons.gridx = 0;
        cons.gridy = 2;
        panel.add(colors, cons);
        //create a font
        Font font1 = new Font("Comic Sans MS", Font.BOLD, 28);
        
        //NickName text field
        name = new JTextField("Nickname", 10);
        name.setFont(font1);
        name.setBorder(BorderFactory.createEmptyBorder());
        cons.fill = GridBagConstraints.NONE;
        cons.gridy = 3;
        cons.gridx = 0;
        cons.ipady = 20;
        cons.ipadx = 0;
        panel.add(name, cons);
        
        //NICKNAME BACKGROUND
        URI nURL = getClass().getResource("/images/nnbg.png").toURI();
        File nPic = new File(nURL);
        BufferedImage n = ImageIO.read(nPic);
        JLabel nnbg = new JLabel(new ImageIcon(n));
        cons.fill = GridBagConstraints.NONE;
        cons.gridy =3;
        cons.gridx = 0;        
        panel.add(nnbg, cons);
        
        //QUICK PLAY BUTTON
        URI qpURL = getClass().getResource("/images/quickplay.png").toURI();
        File qpPic = new File(qpURL);
        BufferedImage qp = ImageIO.read(qpPic);
        JButton play = new JButton(new ImageIcon(qp));
        //remove borer
        play.setBorder(BorderFactory.createEmptyBorder());
        play.setContentAreaFilled(false);
        cons.fill = GridBagConstraints.NONE;
        cons.gridy = 4;
        cons.gridx = 0;
        cons.ipady = 20;
        panel.add(play, cons);
        
        //CUSTOM PLAY BUTTON
        URI cpURL = getClass().getResource("/images/customplay.png").toURI();
        File cpPic = new File(cpURL);
        BufferedImage cp = ImageIO.read(cpPic);
        JButton play2 = new JButton(new ImageIcon(cp));
         //remove borer
        play2.setBorder(BorderFactory.createEmptyBorder());
        play2.setContentAreaFilled(false);
        //cons.fill = GridBagConstraints.HORIZONTAL;
        cons.gridy = 5;
        cons.gridx = 0;
        cons.ipady = 20;
        panel.add(play2, cons);
        frame.add(panel);

        frame.setVisible(true);
       // frame.pack();
    }
    
    //called when a player selects quick play or custom play
    public void SetClientInfo(){
        PDLClient.instance.playerIcon = draw.getImage();
        PDLClient.instance.playerName = name.getText();
    }
    //Fill in
    public void QuickPlay(){
        
    }
    
    //Fill in
    public void CustomPlay(){
        
    }
}
