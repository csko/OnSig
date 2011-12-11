/*
 * FastDtwTest.java   Jul 14, 2004
 *
 * Copyright (c) 2004 Stan Salvador
 * stansalvador@hotmail.com
 */

package com;

import com.timeseries.TimeSeries;
import com.dtw.TimeWarpInfo;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import Training.TrainingSignature;
import java.io.File;
import java.util.ArrayList;
import java.util.regex.Pattern;
import Evaluate.DTWClassifier;
import java.io.FileOutputStream;
import java.io.PrintWriter;


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
      public static void main(String[] args) throws FileNotFoundException
      {
          PrintWriter file = new PrintWriter(new FileOutputStream("output-min.txt",true));
          double step = 0.0001;
          double start = 0.259;
          double end = 0.258;
          double t = start;
          boolean isEnd = false;
        while(true) {
          //step = ((start - end)/2);
          //double t = start - ((start - end)/2);
          t -= step;
          boolean DEBUG = false;
          isEnd = true;
          
          double FRR = 0.0; //frr értéke
          double FAR = 0.0; //far értéke
          int goodtestSize = 0;  //a valódi tesztelt aláírások száma
          int forgtestSize = 0;  //a hamis tesztelt aláírások száma
          double determinedForgery = 0;    //hamisnak nyilvánított aláírások száma
          double determinedGenuine = 0;    //a háromból kettő módszer szerint helyesnek vélt aláírások száma

          //a max, min és átlag eltérés szerint jól osztályozott aláírások száma
          double maxgood = 0;
          double mingood = 0;
          double avggood = 0;

          //hányat talált hamisnak az egyes módszerek szerint
          double maxbad = 0;   
          double minbad= 0;   
          double avgbad = 0;

          double threshold = t;
          //double threshold = 0.15;
          
          /*for ( int j = 0 ; j < 16 ; j++ ) {    //16 aláírónk van
              try {
                  ArrayList<TimeSeries> tg = new ArrayList<TimeSeries>(12); //valós aláírások megfigyelési sora
                  //String filenamepref = "0";
                  //filenamepref += (j<10)?"0"+(j+1):(j+1);
                  //filenamepref += "_";
                  for ( int i = 0 ; i < 12  ; i++ ) {   //a helyes aláírások fele lesz a training halmaz
                      //String filename = filenamepref;
                      if ( i < 10 ) {
                          filename += "0";
                          System.out.println(i);
                      }

                      filename += (i+1);
                      System.out.println(filename);
                      final TimeSeries tsI = new TimeSeries("../data-deriv/genuine/00"+(j+1)+"_"+(i+1)+".HWR", false, false, ' ');  //adott fájlból megfigyelést csinál
                      tg.add(i, tsI);  //elteszem a megfigyelést
                  }
                  
                  TrainingSignature tsGenuine = new TrainingSignature(tg, Integer.parseInt(args[0]));   //létrehozom a valódi aláírások training halmazát
                  DTWClassifier cl = new DTWClassifier(tsGenuine, threshold);    //dtw traininghalmaz szerinti osztályozó
                
                  File dir = new File("../data-deriv/forgery/");
                  String []files = dir.list();

                  ArrayList<String> currentForgeries = new ArrayList<String>(); //hamisítványokat tartalmazó fájlok
                  for ( int i = 0 ; i < files.length ; i++ ) {
                      String filename = filenamepref;
                      filename += (i<10)?"0"+(i+1):(i+1);
                      if ( Pattern.matches("[0-9]*_00"+(i+1)+"_[0-9]*.HWR", files[i]) ) //adott emberhez tartozó hamisítványok leválogatása
                        currentForgeries.add(files[i]);

                  }
                  int counter = currentForgeries.size();
                  TimeSeries []tf = new TimeSeries[counter];    //az összes hamis aláírás teszt lesz
                  for ( int i = 0 ; i < counter ; i++ ){
                        final TimeSeries tsI = new TimeSeries("../data-deriv/forgery/"+currentForgeries.get(i), false, false, ' ');
                        tf[i] = tsI;
                  }

                  
                  for ( int i = 12 ; i < 24 ; i++ ) {   //nézzük a valós aláírások teszthalmazát
                    String filename = filenamepref;
                    filename += (i<10)?"0"+(i+1):(i+1);
                    goodtestSize++; //jó aláírások darabszáma
                    final TimeSeries tsI = new TimeSeries("../data-deriv/genuine/00"+(j+1)+"_"+(i+1)+".HWR", false, false, ' ');    //kinyerjük a jó aláírás megfigyelését

                    //helyesnek ítéljük-e a max, min és átlag eltérés szerint
                    int min = cl.isValidSignature(tsI, Integer.parseInt(args[0]), DTWClassifier.MIN);
                    int max = cl.isValidSignature(tsI, Integer.parseInt(args[0]), DTWClassifier.MAX);
                    int avg = cl.isValidSignature(tsI, Integer.parseInt(args[0]), DTWClassifier.AVG);

                    mingood += min;
                    maxgood += max;
                    avggood += avg;
                    
                    if ( min + max + avg >= 2 )
                        determinedGenuine++;

                  }

                  //menjünk végig a hamis aláírásokon is
                  for ( int i  = 0 ; i < tf.length ; i++ ) {
                    forgtestSize++;
                    int min = cl.isValidSignature(tf[i], Integer.parseInt(args[0]), DTWClassifier.MIN);
                    int max = cl.isValidSignature(tf[i], Integer.parseInt(args[0]), DTWClassifier.MAX);
                    int avg = cl.isValidSignature(tf[i], Integer.parseInt(args[0]), DTWClassifier.AVG);

                    minbad += min;
                    maxbad += max;
                    avgbad += avg;

                    if ( min + max + avg >= 2 )
                        determinedForgery++;
                  }

              } catch (java.lang.InternalError e) {
                  //System.out.println("Nincs ilyen file.");
                  continue;
              }
          }

          if ( (1.0-(determinedGenuine/goodtestSize)) == (determinedForgery/forgtestSize)) {
              file.println("A 2/3-os döntés alapján az EER: " + (determinedForgery/forgtestSize));
              break;
          } else if ( Math.abs((1.0-(mingood/goodtestSize)) - (minbad/forgtestSize)) < 0.01 ) {
              double frr = (1.0-(mingood/goodtestSize));
              double far = (minbad/forgtestSize);
              System.out.println("A különbség: " + Math.abs((1.0-(mingood/goodtestSize)) - (minbad/forgtestSize)));
              file.println("Threshold: " + threshold);
              file.println("FRR: " + frr + "FAR: " + far);
              file.println("A min táv. döntés alapján az EER: " + (minbad/forgtestSize));
              break;
          } else if ( (1.0-(maxgood/goodtestSize)) == (maxbad/forgtestSize) ) {
              file.println("A max táv. döntés alapján az EER: " + (maxbad/forgtestSize));
              break;
          } else if ( (1.0-(avggood/goodtestSize)) == (avgbad/forgtestSize) ){
              file.println("A atl. tav. döntés alapján az EER: " + (avgbad/forgtestSize));
              break;
          }

          System.out.println("Threshold " + threshold + " kész.");

          double frr = (1.0-(mingood/goodtestSize));
          double far = (minbad/forgtestSize);
          System.out.println(frr + " " + far);
          if ( (1.0-(mingood/goodtestSize)) > (minbad/forgtestSize) ) {
              //start = t + step;
              end = t;
              //step /= 10;
              //start -= step;
              System.out.println("Már átugrottuk. Nézzük tovább " + start + " és " + end + " között " + step + " lépésközzel.");
              isEnd = false;
          } else if ( (1.0-(mingood/goodtestSize)) < (mingood/forgtestSize) ) {
              start = t;
              isEnd = false;
          }
          

          file.println("Threshold: " + threshold);
          file.println("FRR:" + (1.0-(determinedGenuine/goodtestSize)));
          file.println("FRR min: " + (1.0-(mingood/goodtestSize)));
          file.println("FRR max: " + (1.0-(maxgood/goodtestSize)));
          file.println("FRR avg: " + (1.0-(avggood/goodtestSize)));

          file.println("FAR: " + (determinedForgery/forgtestSize));
          file.println("FAR min: " + (minbad/forgtestSize));
          file.println("FAR max: " + (maxbad/forgtestSize));
          file.println("FAR avg: " + (avgbad/forgtestSize));
          file.println();

          file.flush();

          
             if ( step == 0.0 )
                break;

             if ( isEnd ) {
                 System.out.println("Nem léptettük a thresholdot, kilépünk.");
                 break;
             }*/
          }
          //file.close();
      }  // end main()


}  // end class FastDtwTest
