/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pdlclient;

import java.awt.CardLayout;
import java.awt.Color;
import java.io.IOException;
import java.net.URISyntaxException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author Josh
 */
public class ScreenManager {
    static ScreenManager instance;
    public final static String HOME = "HOME", SELECT = "SELECT", WAIT = "WAIT", DRAW = "DRAW", WAITING = "WAITING", PHRASE = "PHRASE", END = "END";
    JFrame frame;
    JPanel screens;
    CardLayout cards;
    public static ScreenManager getInstance() throws IOException, URISyntaxException{
        if(instance == null){
            instance = new ScreenManager();
        }
        return instance;
    }
   
    private ScreenManager() throws IOException, URISyntaxException{
        frame = new JFrame("Picture Down The Lane");
        Color c  = new Color(99, 194, 255);
        frame.getContentPane().setBackground(c);
        frame.setSize(1024, 900);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        cards = new CardLayout();
        screens = new JPanel(cards);

        screens.add(HomeGUI.getInstance().getPanel(), HOME);
        
        
        screens.add(SelectRoomGUI.getInstance().getPanel(), SELECT);
        
       
        screens.add(WaitingRoomGUI.getInstance().getPanel(), WAIT);
        
        screens.add(DrawingPageGUI.getInstance().getPanel(), DRAW);
        screens.add(WaitingPage.getInstance().getPanel(), WAITING);
        screens.add(PhrasePageGUI.getInstance().getPanel(), PHRASE);
        screens.add(EndGameGUI.getInstance().getPanel(), END);
        frame.add(screens);
        frame.setVisible(true);
    }
    
    public void changeScreen(String screen){
        if(screen.equals(HOME)){
            cards.show(screens, HOME);
        }
        else if(screen.equals(SELECT)){
            cards.show(screens, SELECT);
        }
        else if(screen.equals(WAIT)){
            cards.show(screens, WAIT);
        }
        else if(screen.equals(DRAW)){
            cards.show(screens, DRAW);
        }
        else if(screen.equals(WAITING)){
            cards.show(screens, WAITING);
        }
        else if(screen.equals(PHRASE)){
            cards.show(screens, PHRASE);
        }else if(screen.equals(END)){
            System.out.println("here");
            
            cards.show(screens, END);
            frame.setSize(1024, 900);
            frame.setVisible(true);
        }
    }
}
