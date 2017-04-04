/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pdlclient;


import java.awt.image.BufferedImage;
import java.util.ArrayList;
/**
 *
 * @author Josh
 */
public class PictureLane {
    private ArrayList<BufferedImage> _images;
    private ArrayList<String> _phrases;
    
    public PictureLane(){
        _images = new ArrayList<>();
        _phrases = new ArrayList<>();
    }
    public void addPhrase(String s){
        _phrases.add(s);
    }
    public void addImage(BufferedImage img){
        _images.add(img);
    }
    
    
}