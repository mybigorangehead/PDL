/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pdlclient;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author yayang
 */
public class phraseGenerator {
    private int randNum;
    private ArrayList<String> phrases = new ArrayList<>();
    
    public phraseGenerator() {
        try {
            // The name of the file to open
            String fileName = "pictionary.txt";
            InputStream in = getClass().getResourceAsStream("pictionary.txt");
            
            String phrase = null;
            // wrap a BufferedReader around FileReader
            Scanner file = new Scanner(in);
            System.out.println("here");
            // read from the file and add to arraylist phrases
            while (file.hasNextLine()){
                phrases.add(file.nextLine());
            }
            
            // close the BufferedReader when we finish reading
            file.close();
        } catch (Exception ex) {
            Logger.getLogger(phraseGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String randomizer(){
        // random generate integer from 0 to 99
        Random rand = new Random();
        randNum = rand.nextInt(phrases.size());
        return phrases.get(randNum);
    }
}
