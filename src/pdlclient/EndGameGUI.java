/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pdlclient;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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
import javax.swing.JTextField;

/**
 *
 * @author Josh
 */
public class EndGameGUI {
    public static EndGameGUI instance;
    JFrame frame;
    private JPanel panel;
    private GridBagConstraints cons;
    BufferedImage bg;
    public static EndGameGUI getInstance() throws IOException, URISyntaxException{
        if(instance == null){
            instance = new EndGameGUI();
        }
        return instance;
    }
    public EndGameGUI() throws URISyntaxException, IOException{
        frame = new JFrame("Waiting...");
        Color c  = new Color(99, 194, 255);
        frame.getContentPane().setBackground(c);
        frame.setSize(1024, 900);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       
        panel = new JPanel(new GridBagLayout());
        cons = new GridBagConstraints();
        panel.setBackground(c);
        frame.add(panel);
        URI nURL = getClass().getResource("/images/nnbg.png").toURI();
        File nPic = new File(nURL);
        bg = ImageIO.read(nPic);
    }
    int curX =0;
    public void addLane(PictureLane p){
        
        ArrayList<String> phrases = p.getPhrases();
        ArrayList<BufferedImage> images = p.getImages();
        cons.gridx = curX;
        cons.fill = GridBagConstraints.NONE;
        int curY = 0;
        for(int i = 0; i <phrases.size(); i++){
            
            cons.gridy = curY;
            //Phrase text field
            JTextField text = new JTextField(phrases.get(i), 20);
            text.setEditable(false);
            //phrase.setFont(font1);
            text.setBorder(BorderFactory.createEmptyBorder());
            text.setBackground(Color.white);
            panel.add(text, cons);


            //PHRASE BACKGROUND
            JLabel nnbg = new JLabel(new ImageIcon(bg.getScaledInstance(256, 32, 0)));
            cons.ipady = 10;
            panel.add(nnbg, cons);
            if(images.size() > i){
                curY++;
                cons.gridy = curY;
                JLabel pic = new JLabel(new ImageIcon(images.get(i)));
                panel.add(pic, cons);
            }
            curY++;
        }
        curX ++;
        frame.setSize(1024, 900);
        frame.setVisible(true);
    }
}
