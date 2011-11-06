/*
 * FastDtwTest.java   Jul 14, 2004
 *
 * Copyright (c) 2004 Stan Salvador
 * stansalvador@hotmail.com
 */

package com;

import com.timeseries.TimeSeries;
import com.dtw.TimeWarpInfo;
import java.text.DecimalFormat;
import Training.TrainingSignature;
import java.io.File;
import java.util.ArrayList;
import java.util.regex.Pattern;
import Evaluate.DTWClassifier;


/**
 * This class contains a main method that executes the FastDTW algorithm on two
 * time series with a specified radius.
 *
 * @author Stan Salvador, stansalvador@hotmail.com
 * @since Jul 14, 2004
 */
public class FastDtwTest
{
   /**
    * This main method executes the FastDTW algorithm on two time series with a
    * specified radius. The time series arguments are file names for files that
    * contain one measurement per line (time measurements are an optional value
    * in the first column). After calculating the warp path, the warp
    * path distance will be printed to standard output, followed by the path
    * in the format "(0,0),(1,0),(2,1)..." were each pair of numbers in
    * parenthesis are indexes of the first and second time series that are
    * linked in the warp path
    *
    * @param args  command line arguments (see method comments)
    */
      public static void main(String[] args)
      {
          TimeSeries []tg = new TimeSeries[16];
          for ( int i = 0 ; i < 16  ; i++ ) {
              final TimeSeries tsI = new TimeSeries("../data-deriv/genuine/001_"+(i+1)+".HWR", false, false, ' ');
              tg[i] = tsI;
          }

          TrainingSignature tsGenuine = new TrainingSignature(tg, Integer.parseInt(args[0]));

          System.out.println(tsGenuine);


          
          File dir = new File("../data-deriv/forgery/");
          String []files = dir.list();

          ArrayList<String> currentForgeries = new ArrayList<String>();
          for ( int i = 0 ; i < files.length ; i++ ) {
              if ( Pattern.matches("[0-9]*_001_[0-9]*.HWR", files[i]) )
                currentForgeries.add(files[i]);
              
          }
          int counter = 2*currentForgeries.size()/3;
          TimeSeries []tf = new TimeSeries[counter];
          for ( int i = 0 ; i < counter ; i++ ){
                final TimeSeries tsI = new TimeSeries("../data-deriv/forgery/"+currentForgeries.get(i), false, false, ' ');
                tf[i] = tsI;
          }
          TrainingSignature tsForgery = new TrainingSignature(tf, Integer.parseInt(args[0]));

          System.out.println(tsForgery);

          System.out.println("Min: Genuine(" + tsGenuine.getMinDistance() +") >? Forgery(" + tsForgery.getMinDistance() + ") " + (tsGenuine.getMinDistance()>tsForgery.getMinDistance()));
          System.out.println("Max: Genuine(" + tsGenuine.getMaxDistance() +") >? Forgery(" + tsForgery.getMaxDistance() + ") " + (tsGenuine.getMaxDistance()>tsForgery.getMaxDistance()));
          System.out.println("Avg: Genuine(" + tsGenuine.getAverageDistance() +") >? Forgery(" + tsForgery.getAverageDistance() + ") " + (tsGenuine.getAverageDistance()>tsForgery.getAverageDistance()));

          double FRR = 0.0;
          double testSize = 0;
          double determinedGenuine = 0;

          double maxgood = 0;
          double mingood = 0;
          double avggood = 0;

          System.out.println();
          for ( int i = 16 ; i < 24 ; i++ ) {
            testSize++;
            final TimeSeries tsI = new TimeSeries("../data-deriv/genuine/001_"+(i+1)+".HWR", false, false, ' ');
            DTWClassifier cl = new DTWClassifier(tsGenuine, tsForgery);
            int min = cl.isValidSignature(tsI, Integer.parseInt(args[0]), DTWClassifier.MIN);
            int max = cl.isValidSignature(tsI, Integer.parseInt(args[0]), DTWClassifier.MAX);
            int avg = cl.isValidSignature(tsI, Integer.parseInt(args[0]), DTWClassifier.AVG);
            mingood += min;
            maxgood += max;
            avggood += avg;
            System.out.println("A 001_"+(i+1)+".HWR aláírás:");
            System.out.println("A legkisebb távokat összevetve: " + min);
            System.out.println("A legnagyobb távokat összevetve: " + max);
            System.out.println("A átlagos távokat összevetve: " + avg);
            if ( min + max + avg >= 2 )
                determinedGenuine++;
          }

          System.out.println("Helyes aláírás jól osztályozva: " + (determinedGenuine/testSize));
          System.out.println("Helyes aláírás jól osztályozva min alapján: " + (mingood/testSize));
          System.out.println("Helyes aláírás jól osztályozva max alapján: " + (maxgood/testSize));
          System.out.println("Helyes aláírás jól osztályozva avg alapján: " + (avggood/testSize));

      }  // end main()


}  // end class FastDtwTest
