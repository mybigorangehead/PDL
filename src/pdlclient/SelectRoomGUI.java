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
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author Josh
 */
public class SelectRoomGUI {
    JFrame frame;
    public SelectRoomGUI() throws IOException, URISyntaxException{
        frame = new JFrame("Select Room");
         Color c  = new Color(99, 194, 255);
        frame.getContentPane().setBackground(c);
        frame.setSize(2048, 1600);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints cons = new GridBagConstraints();
        
         //QUICK PLAY BUTTON
        URI qpURL = getClass().getResource("/images/quickplay.png").toURI();
        File qpPic = new File(qpURL);
        BufferedImage qp = ImageIO.read(qpPic);
        JButton play = new JButton(new ImageIcon(qp));
        //remove borer
        play.setBorder(BorderFactory.createEmptyBorder());
        play.setContentAreaFilled(false);
        cons.fill = GridBagConstraints.NONE;
        cons.gridy = 0;
        cons.gridx = 0;
        cons.ipady = 0;
        //cons.weight = 1;
        panel.add(play, cons);
        
        frame.add(panel);

        frame.setVisible(true);
    }
}
