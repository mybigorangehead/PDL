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
import javax.swing.JTextField;

/**
 *
 * @author Josh
 */
public class EndGameGUI {
    public static EndGameGUI instance;
  //  JFrame frame;
    private JPanel panel;
    private GridBagConstraints cons;
    BufferedImage bg;
    ArrayList<PictureLane> displayedLanes;
    
    public static EndGameGUI getInstance() throws IOException, URISyntaxException{
        if(instance == null){
            instance = new EndGameGUI();
        }
        return instance;
    }
   
    public EndGameGUI() throws URISyntaxException, IOException{
        displayedLanes = new ArrayList<>();
        //frame = new JFrame("Waiting...");
        Color c  = new Color(99, 194, 255);
        /*frame.getContentPane().setBackground(c);
        frame.setSize(1024, 900);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);*/
       
        panel = new JPanel(new GridBagLayout());
        cons = new GridBagConstraints();
        panel.setBackground(c);
        //frame.add(panel);
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
       
        
        cons.gridy = curY+1;
        JButton vote = new JButton("Vote!");
        vote.addActionListener(new VoteButton(curX));
        panel.add(vote, cons);
        curX ++;
        displayedLanes.add(p);
        //frame.setSize(1024, 900);
        //frame.setVisible(true);
        ScreenManager.instance.changeScreen(ScreenManager.END);
    }
    public JPanel getPanel(){
        return panel;
    }
    public void showWinner(int id){
        panel.removeAll();
        PictureLane p = displayedLanes.get(id);
        ArrayList<String> phrases = p.getPhrases();
        ArrayList<BufferedImage> images = p.getImages();
        cons.gridx = curX;
        cons.fill = GridBagConstraints.NONE;
        int curY = 0;
        Color c  = new Color(99, 194, 255);
        Font font1 = new Font("Comic Sans MS", Font.BOLD, 20);
        JTextField title = new JTextField("Join Code");
        title.setFont(font1);
        title.setEditable(false);
        title.setBackground(c);
        curY++;
        panel.add(title, cons);
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
        ScreenManager.instance.changeScreen(ScreenManager.END);
    }
     public class VoteButton implements ActionListener{
        int id;
        public VoteButton(int i){
            id = i;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            if(PDLClient.instance.isMaster()){ 
                ScreenManager.instance.changeScreen(ScreenManager.WAITING);
                PDLClient.instance.getMasterClient().recieveVote(id);
             //   frame.setVisible(false);
             //   WaitingPage.instance.frame.setVisible(true);
               
            }
            else{
                ScreenManager.instance.changeScreen(ScreenManager.WAITING);
                PDLClient.instance.sendVote(id);
               // frame.setVisible(false);
               // WaitingPage.instance.frame.setVisible(true);
               
            }
        }
        
    }
}
