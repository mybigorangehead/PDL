/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pdlclient;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;

import java.net.URISyntaxException;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

/*
unused for now

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URI;
/**
 * 
 * @josh
 */
public class DrawPanel extends JComponent {
    Image img;
    Graphics2D g2;
    int currentX, currentY, oldX, oldY;
    int size;
   // URI imageDest;
    public DrawPanel(int s)throws URISyntaxException{
     //   imageDest = getClass().getResource("/images/").toURI();
        size = s;
        super.setSize(s, s);
        
        setDoubleBuffered(false);
        addMouseListener(new MouseAdapter(){
            @Override
            public void mousePressed(MouseEvent e){
                
            
                oldX = e.getX();
                oldY = e.getY();
                if(g2!=null){
                    g2.setStroke(new BasicStroke(5));
                    g2.drawLine(oldX, oldY, oldX, oldY);
                    repaint();
                }
            }
        });
        addMouseMotionListener(new MouseMotionAdapter(){
            @Override
            public void mouseDragged(MouseEvent e){
                currentX = e.getX();
                currentY = e.getY();
                if(g2!=null){
                    g2.setStroke(new BasicStroke(5));
                    g2.drawLine(oldX, oldY, currentX, currentY);
                    repaint();
                    oldX = currentX;
                    oldY = currentY;
                }
            }
        });
    }   
    @Override
    protected void paintComponent(Graphics g){
        if(img == null){
            img = createImage(size, size);
            g2 = (Graphics2D) img.getGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                    RenderingHints.VALUE_ANTIALIAS_ON);
            clear();
        }
        
        g.drawImage(img, 0, 0, null);
    }
    
    //methods users can use
    public void clear(){
        g2.setPaint(Color.white);
        //cover thing in white
        g2.fillRect(0, 0, size, size);
        g2.setPaint(Color.black);
        repaint();
    }
    
    //used for setting different colors
    public void red(){
        g2.setPaint(Color.red);
    }
    public void black(){
         g2.setPaint(Color.black);
    }
    public void magenta(){
         g2.setPaint(Color.magenta);
    }
    public void green(){
        g2.setPaint(Color.green);
    }
    public void blue(){
        g2.setPaint(Color.blue);
    }
    public void yellow(){
        g2.setPaint(Color.yellow);
    }
    
    public JPanel getColorPanel(int size){
        JPanel controls = new JPanel();
        clearBtn = new JButton("Clear");
        blackBtn = new JButton();
        blackBtn.setPreferredSize(new Dimension(size, size));
        blackBtn.setBackground(Color.black);
        blueBtn = new JButton();
        blueBtn.setPreferredSize(new Dimension(size, size));
        blueBtn.setBackground(Color.blue);
        greenBtn = new JButton();
        greenBtn.setPreferredSize(new Dimension(size, size));
        greenBtn.setBackground(Color.green);
        redBtn = new JButton();
        redBtn.setPreferredSize(new Dimension(size, size));
        redBtn.setBackground(Color.red);
        magentaBtn = new JButton();
        magentaBtn.setPreferredSize(new Dimension(size, size));
        magentaBtn.setBackground(Color.magenta);
        
        yellowBtn = new JButton();
        yellowBtn.setPreferredSize(new Dimension(size, size));
        yellowBtn.setBackground(Color.yellow);
        clearBtn.addActionListener(actionListener);
        blackBtn.addActionListener(actionListener);
        blueBtn.addActionListener(actionListener);
        greenBtn.addActionListener(actionListener);
        redBtn.addActionListener(actionListener);
        magentaBtn.addActionListener(actionListener);
        yellowBtn.addActionListener(actionListener);
        
        saveBtn = new JButton("Save");
        saveBtn.addActionListener(actionListener);
        
        
        controls.add(clearBtn);
        controls.add(blackBtn);
        controls.add(blueBtn);
        controls.add(greenBtn);
        controls.add(redBtn);
        controls.add(magentaBtn);
        controls.add(yellowBtn);
       // controls.add(saveBtn);
        return controls;
    }
    JButton clearBtn, blackBtn, blueBtn, greenBtn, redBtn, magentaBtn, yellowBtn, saveBtn;
    ActionListener actionListener = new ActionListener(){
        @Override
        public void actionPerformed(ActionEvent e) {
           if(e.getSource() == clearBtn){
               clear();
           }else if(e.getSource() == blackBtn){
               black();
           }
           else if(e.getSource() == blueBtn){
               blue();
           }
            else if(e.getSource() == greenBtn){
               green();
           }
            else if(e.getSource() == redBtn){
               red();
           }
            else if(e.getSource() == magentaBtn){
               magenta();
           }
            else if(e.getSource() == yellowBtn){
                yellow();
            }
            else if(e.getSource() == saveBtn){
               /*try {
                  // SaveImg();
               } catch (IOException ex) {
                   
               }*/
            }
        }
    };
    
    //WORKS!!!!!!
    /*
    public void SaveImg() throws IOException{
        //width and height are given by size
        int w = img.getWidth(this);
        int h = img.getHeight(this);
        BufferedImage i = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
        Graphics g = i.getGraphics();
        g.drawImage(img, 0, 0, null);
        ImageIO.write(i, "png", new File("superawesomecoolimage.png"));
        System.out.println("Image Saved!");
    }*/
    
    public BufferedImage getImage(){
        int w = img.getWidth(this);
        int h = img.getHeight(this);
        BufferedImage i = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
        Graphics g = i.getGraphics();
        g.drawImage(img, 0, 0, null);
        return i;
    }    
}
