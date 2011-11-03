/*
 * Egy aláírást reprezentáló osztály.
 * Tárolja az aláírás globális tulajdonságait,
 * valamint egy konténerben az egyes időpillanatokat.
 */

package signature;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sborde
 */
public class Signature {

    private int totalTime;
    private int penUpTime;

    private double averageSpeed;
    private int segmentsCount;

    private ArrayList<TimeInterval> t;

    public Signature(String inputFileName) {
        penUpTime = 0;
        t = new ArrayList<TimeInterval>();
        try {
            BufferedReader in = new BufferedReader(new FileReader(inputFileName));
            String line;
            while ( (line = in.readLine()) != null ) {
                t.add(new TimeInterval(line));
            }
            in.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Signature.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Signature.class.getName()).log(Level.SEVERE, null, ex);
        }
        totalTime = t.size();
        for ( Iterator<TimeInterval> i = t.iterator() ; i.hasNext() ; ) {
            if (((TimeInterval)i.next()).getZ() == 0) {
                penUpTime++;
            }
                
        }
    }

}
