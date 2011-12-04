package signature;

/*
 * Egy aláírás egy adott idősorát reprezentálja.
 */


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 *
 * @author sborde
 */
public class TimeSeries {

    /**
     * Az aláírás idősora szegmensekre bontva.
     * Egy szegmenshatár ott van, ahol felemeljük a tollat.
     */
    private ArrayList<ArrayList<TimePoint>> segments;

    /**
     * Az input fájl hány oszlopát hagyjuk ki.
     */
    private int offset;

    /**
     * Mely fájlból olvassuk be az adatokat.
     */
    private String filename;

    /**
     * Hány pontot tároljunk egy időponthoz.
     */
    private int pointsNumber;

    /**
     * Egy új idősor létrehoása.
     * @param filename idősort tároló fájlnév
     * @param offset hány oszlopra nem lesz szükség
     * @param pointNum hány adat kell
     * @throws FileNotFoundException
     * @throws IOException
     */
    public TimeSeries(String filename, int offset, int pointNum) throws FileNotFoundException, IOException{
        this.offset = offset;
        this.pointsNumber = pointNum;
        this.filename = filename;
        this.segments = new ArrayList<ArrayList<TimePoint>>();

        int lineNum = -1;   //sorok száma
        
        BufferedReader filein = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
        String line;

        ArrayList<TimePoint> segment = new ArrayList<TimePoint>();  //egy szegmens létrehozása
        segments.add(segment);
        boolean penIsDown = true;
        boolean penDown = true; //a tollról feltesszük, hogy lent van

        while ((line = filein.readLine())!=null) {  //soronként olvasunk
            
            lineNum++;
            int currCol = 0;    //első oszloppal kezdjük

            double[] val = new double[pointsNumber];    //a sorból beolvasott adatok

            StringTokenizer st = new StringTokenizer(line," "); //felbontás szóközök mentén

            while ( st.hasMoreTokens() ){

                String token = st.nextToken();
                
                if ( currCol == 2 ) {   //ha a z koordinátát olvassuk
                    
                    if ( Double.parseDouble(token) == 0.0 ) { //és az 0
                        if ( penDown ) { //és a toll lent volt, akkor most felemeltük
                            penDown = false;    //akkor a toll a levegőben van
                            segment = new ArrayList<TimePoint>();
                            segments.add(segment);
                        }
                    } else {    //ha nem 0-t olvasunk
                        if ( !penDown ) {   //és eddig fent volt a toll
                            penDown = true;    //akkor a toll a levegőben van
                            segment = new ArrayList<TimePoint>();
                            segments.add(segment);
                        }
                    }
                }

                if ( currCol >= this.offset+this.pointsNumber ){    //ha ezeket már nem kell beolvasni, akkor ne menjünk tovább
                    break;
                }

                if ( currCol < this.offset ) {  //ha ezt még nem kell beolvasni, ugorjunk
                    currCol++;
                    continue;
                } else if ( currCol < this.offset+this.pointsNumber ) { //ezt be kell olvasni
                    val[currCol-this.offset] = Double.parseDouble(token);
                    currCol++;
                }
                
            }
            segment.add(new TimePoint(pointNum,val,lineNum,penDown));    //új időpont létrehozása
        }
        System.out.println(segments.size());
        filein.close();
    }

}
