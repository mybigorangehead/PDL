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
 *
 * @author yayang
 */
public class DrawingPageGUI {
    /*public static void main(String[] args) throws IOException, URISyntaxException 
    {
        DrawingPageGUI DrawingPage = new DrawingPageGUI();
        DrawingPage = getInstance();
    }*/
    
    public static DrawingPageGUI instance;
    JFrame frame;
    private DrawPanel draw;
    private JTextField phrase;
    private JButton submit;
    public static DrawingPageGUI getInstance() throws IOException, URISyntaxException{
        if(instance == null){
            instance = new DrawingPageGUI();
        }
        return instance;
    }
    //constructs the home page interface
    private DrawingPageGUI() throws IOException, URISyntaxException{
        frame = new JFrame("Drawing Page");
        Color c  = new Color(99, 194, 255);
        frame.getContentPane().setBackground(c);
        frame.setSize(1024, 900);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints cons = new GridBagConstraints();        
        
        
        //Phrase text field
        phrase = new JTextField("Nickphrase", 20);
        phrase.setEditable(false);
        //phrase.setFont(font1);
        phrase.setBorder(BorderFactory.createEmptyBorder());
        phrase.setBackground(Color.white);
        cons.fill = GridBagConstraints.NONE;
        cons.gridy = 0;
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
        cons.gridy =0;
        cons.gridx = 0;    
        cons.ipady = 10;
        panel.add(nnbg, cons);

        panel.setBackground(c);

        
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
        JPanel colors = draw.getColorPanel(16);
        cons.fill = GridBagConstraints.NONE;
        cons.ipadx = 0;
        cons.ipady = 0;
        cons.gridx = 0;
        cons.gridy = 2;
        panel.add(colors, cons);
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
       // ActionListener cAl = new MenuButtons();
        //submit.addActionListener(cAl);
        //cons.fill = GridBagConstraints.HORIZONTAL;
        cons.gridy = 5;
        cons.gridx = 0;
        cons.ipady = 20;
        panel.add(submit, cons);
        submit.addActionListener(new SubmitButton());
        frame.add(panel);

        frame.setVisible(false);
       // frame.pack();
    }
    public void setPhrase(String p){
        phrase.setText(p);
    }

    public class SubmitButton implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            if(PDLClient.instance.isMaster()){
                PDLClient.instance.getMasterClient().submitGamePic(draw.getImage());
                frame.setVisible(false);
                WaitingPage.instance.frame.setVisible(true);
            }
            else{
                PDLClient.instance.sendGameImage(draw.getImage());
                frame.setVisible(false);
                WaitingPage.instance.frame.setVisible(true);
            }
        }
        
    }
}

