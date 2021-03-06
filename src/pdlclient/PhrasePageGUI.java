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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This gui shows the picture another player draw, and allows player to type in 
 * a phrase
 * @author yayang
 */
public class PhrasePageGUI {

    
    public static PhrasePageGUI instance;
    //JFrame frame;
    private JLabel img;
    private JTextField phrase;
    private JButton submit;
    private JPanel panel;
    public static PhrasePageGUI getInstance() throws IOException, URISyntaxException{
        if(instance == null){
            instance = new PhrasePageGUI();
        }
        return instance;
    }
    //constructs the home page interface
    private PhrasePageGUI() throws IOException, URISyntaxException{
        Color c  = new Color(99, 194, 255);       
        panel = new JPanel(new GridBagLayout());
        GridBagConstraints cons = new GridBagConstraints();        
        
        
        //Phrase text field
        phrase = new JTextField("Enter Phrase here..", 20);
        phrase.setEditable(true);
        //phrase.setFont(font1);
        phrase.setBorder(BorderFactory.createEmptyBorder());
        phrase.setBackground(Color.white);
        cons.fill = GridBagConstraints.NONE;
        cons.gridy = 1;
        cons.gridx = 0;
        cons.ipady = 0;
        cons.ipadx = 0;
        panel.add(phrase, cons);
        
        
        //PHRASE BACKGROUND
        URI nURL = getClass().getResource("/images/nnbg.png").toURI();
        File nPic = new File(nURL);
        BufferedImage n = ImageIO.read(nPic);
        JLabel nnbg = new JLabel(new ImageIcon(n.getScaledInstance(256, 32, 0)));
        cons.fill = GridBagConstraints.NONE;
        cons.gridy =1;
        cons.gridx = 0;    
        cons.ipady = 10;
        panel.add(nnbg, cons);

        panel.setBackground(c);
        
        //the drawing box
        int size = 256;
        img = new JLabel(new ImageIcon());
        cons.fill = GridBagConstraints.NONE;

        cons.gridx = 0;
        cons.gridy = 0;      
        // cons.weightx = 5;
        panel.add(img, cons);
       
        //create a font
        Font font1 = new Font("Comic Sans MS", Font.BOLD, 14);
        
        
        //CUSTOM PLAY BUTTON
        URI cpURL = getClass().getResource("/images/submit.png").toURI();
        File cpPic = new File(cpURL);
        BufferedImage cp = ImageIO.read(cpPic);
        submit = new JButton(new ImageIcon(cp));
         //remove borer
        submit.setBorder(BorderFactory.createEmptyBorder());
        submit.setContentAreaFilled(false);

        cons.gridy = 5;
        cons.gridx = 0;
        cons.ipady = 20;
        panel.add(submit, cons);
        submit.addActionListener(new SubmitButton());
    }
    public void setImage(BufferedImage i){
        img.setIcon(new ImageIcon(i));
    }
    public void clearPhrase(){
        phrase.setText("");
    }
    public JPanel getPanel(){
        return panel;
    }
    public class SubmitButton implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            if(PDLClient.instance.isMaster()){
                
                try {
                    // frame.setVisible(false);
                    // WaitingPage.instance.frame.setVisible(true);
                    ScreenManager.instance.changeScreen(ScreenManager.WAITING);
                    PDLClient.instance.getMasterClient().submitGamePhrase(phrase.getText());
                } catch (IOException ex) {
                    Logger.getLogger(PhrasePageGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else{
                
               // frame.setVisible(false);
               // WaitingPage.instance.frame.setVisible(true);
                ScreenManager.instance.changeScreen(ScreenManager.WAITING);
                try {
                    PDLClient.instance.sendGamePhrase(phrase.getText());
                } catch (IOException ex) {
                    Logger.getLogger(PhrasePageGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
    }
}
