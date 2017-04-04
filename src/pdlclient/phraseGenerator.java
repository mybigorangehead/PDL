/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pdlclient;

import java.awt.List;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
    
    public phraseGenerator() throws IOException{
        // The name of the file to open
        String fileName = "pictionary.txt";        
        String phrase = null;
        // wrap a BufferedReader around FileReader
        BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));

        // read from the file and add to arraylist phrases
        while ((phrase = bufferedReader.readLine()) != null){
            phrases.add(phrase);
        }
  
        // close the BufferedReader when we finish reading
        bufferedReader.close();
    }
    
    public String randomizer(){
        // random generate integer from 0 to 99
        Random rand = new Random();
        randNum = rand.nextInt(phrases.size());
        return phrases.get(randNum);
    }
}