/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pdlclient;

import java.io.File;
import java.util.Scanner;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;


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
        Scanner in = new Scanner(new File(fileName));

        // read from the file and add to arraylist phrases
        while (in.hasNextLine()){
            phrases.add(in.nextLine());
        }
  
        // close the BufferedReader when we finish reading
        in.close();
    }
    
    public String randomizer(){
        // random generate integer from 0 to 99
        Random rand = new Random();
        randNum = rand.nextInt(phrases.size());
        return phrases.get(randNum);
    }
}
