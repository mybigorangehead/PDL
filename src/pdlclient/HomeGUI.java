/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pdlclient;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * The GUI seen when you first start the client
 * allows users to enter a nickname
 * allows users to draw a player icon
 * allows users to select play mode
 * @author Josh
 */
public class HomeGUI {
    static HomeGUI instance;
    //JFrame frame;
    private DrawPanel draw;
    private JTextField name;
    private JButton quickPlay;
    private JButton customPlay;
    private JPanel myPanel;
    public static HomeGUI getInstance() throws IOException, URISyntaxException{
        if(instance == null){
            instance = new HomeGUI();
        }
        return instance;
    }
    //constructs the home page interface
    private HomeGUI() throws IOException, URISyntaxException{
       
        myPanel = new JPanel(new GridBagLayout());
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
        Color c  = new Color(99, 194, 255);
        myPanel.setBackground(c);
        myPanel.add(titleLable, cons);
        
        //the drawing box
        int size = 128;
        draw = new DrawPanel(size);
        cons.fill = GridBagConstraints.NONE;
        cons.ipadx = size;
        cons.ipady = size;
        cons.gridx = 0;
        cons.gridy = 1;      
       // cons.weightx = 5;
        myPanel.add(draw, cons);
        //add color picker
        JPanel colors = draw.getColorPanel(16);
        cons.fill = GridBagConstraints.NONE;
        cons.ipadx = 0;
        cons.ipady = 0;
        cons.gridx = 0;
        cons.gridy = 2;
        myPanel.add(colors, cons);
        //create a font
        Font font1 = new Font("Comic Sans MS", Font.BOLD, 16);
        
        //NickName text field
        name = new JTextField("Nickname", 10);
        name.setFont(font1);
        name.setBorder(BorderFactory.createEmptyBorder());
        cons.fill = GridBagConstraints.NONE;
        cons.gridy = 3;
        cons.gridx = 0;
        cons.ipady = 0;
        cons.ipadx = 0;
        myPanel.add(name, cons);
        
        //NICKNAME BACKGROUND
        URI nURL = getClass().getResource("/images/nnbg.png").toURI();
        File nPic = new File(nURL);
        BufferedImage n = ImageIO.read(nPic);
        JLabel nnbg = new JLabel(new ImageIcon(n));
        cons.fill = GridBagConstraints.NONE;
        cons.gridy =3;
        cons.gridx = 0;        
        myPanel.add(nnbg, cons);
        
        
        
         ActionListener cAl = new MenuButtons();
        //QUICK PLAY BUTTON
        URI qpURL = getClass().getResource("/images/quickplay.png").toURI();
        File qpPic = new File(qpURL);
        BufferedImage qp = ImageIO.read(qpPic);
        quickPlay = new JButton(new ImageIcon(qp));
        //remove borer
        quickPlay.setBorder(BorderFactory.createEmptyBorder());
        quickPlay.setContentAreaFilled(false);
        quickPlay.addActionListener(cAl);
        cons.fill = GridBagConstraints.NONE;
        cons.gridy = 4;
        cons.gridx = 0;
        cons.ipady = 20;
        myPanel.add(quickPlay, cons);
        
        //CUSTOM PLAY BUTTON
        URI cpURL = getClass().getResource("/images/customplay.png").toURI();
        File cpPic = new File(cpURL);
        BufferedImage cp = ImageIO.read(cpPic);
        customPlay = new JButton(new ImageIcon(cp));
         //remove borer
        customPlay.setBorder(BorderFactory.createEmptyBorder());
        customPlay.setContentAreaFilled(false);
       
        customPlay.addActionListener(cAl);
        //cons.fill = GridBagConstraints.HORIZONTAL;
        cons.gridy = 5;
        cons.gridx = 0;
        cons.ipady = 20;
        myPanel.add(customPlay, cons);
        //frame.add(panel);

        //frame.setVisible(true);
       // frame.pack();
    }
    
    //called when a player selects quick play or custom play
    public void SetClientInfo(){
        PDLClient.instance.setPlayerIcon(draw.getImage());
        PDLClient.instance.setPlayerName(name.getText());
    }
    //Fill in
    public void QuickPlay(){
        SetClientInfo();
        ScreenManager.instance.changeScreen(ScreenManager.SELECT);
        PDLClient.instance.joinQuick();
        //send server quickplay message
    }
    
    //Fill in
    public void CustomPlay(){
        SetClientInfo();
        //frame.setVisible(false);
        //SelectRoomGUI.instance.frame.setVisible(true);
        ScreenManager.instance.changeScreen(ScreenManager.SELECT);
        
    }
    public class MenuButtons implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == quickPlay){
                QuickPlay();
            }
            else if(e.getSource() == customPlay){
                CustomPlay();
            }
        }
        
    }
    public JPanel getPanel(){
        return myPanel;
    }
}
