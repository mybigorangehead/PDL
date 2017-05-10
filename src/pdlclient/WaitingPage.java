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
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * GUI panel that just says "Waiting..." in the center 
 * @author Josh
 */
public class WaitingPage {
    public static WaitingPage instance;
    //JFrame frame;
    private JPanel panel;
    public static WaitingPage getInstance() throws IOException{
        if(instance == null){
            instance = new WaitingPage();
        }
        return instance;
    }
    private WaitingPage() throws IOException{
       // frame = new JFrame("Waiting...");
        Color c  = new Color(99, 194, 255);
       
        panel = new JPanel(new GridBagLayout());
        GridBagConstraints cons = new GridBagConstraints();
        panel.setBackground(c);
        
        
        //create "Join Code" display
        Font font1 = new Font("Comic Sans MS", Font.BOLD, 20);
        JTextField title = new JTextField("Waiting...");
        title.setFont(font1);
        title.setEditable(false);
        title.setBackground(c);
        Color textColor = new Color(231, 120, 120);
        title.setForeground(textColor);
        title.setBorder(BorderFactory.createEmptyBorder());
        cons.fill = GridBagConstraints.NONE;
        cons.gridy = 0;
        cons.gridx = 0;
        panel.add(title, cons);
        
        
        
        //frame.add(panel);
        //frame.setVisible(false);
    }
    public JPanel getPanel(){
        return panel;
    }
}
