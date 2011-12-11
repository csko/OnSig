package signature;

import com.timeseries.PAA;
import com.timeseries.TimeSeries;
import com.timeseries.TimeSeriesPoint;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Beolvassa egy aláírás adatpontjait fájlból,
 * de csak meghatározott oszlopokat vesz figyelembe.
 * @author sborde
 */
public class Signature {

    /**
     * Egy aláírás szegmensenként tárolva. A felemelt tollú
     * és író szakaszokat nem különböztetjük meg, úgyis váltakoznak.
     */
    private ArrayList<com.timeseries.TimeSeries> segments;

    /**
     * A teljes aláírás nem szegmentálva. A könnyebb kezelhetőség miatt.
     */
    private com.timeseries.TimeSeries wholeSignature;


    /**
     * Egy adott szegmens írási szakasz-e.
     */
    private ArrayList<Boolean> segmentPenDown;

    /**
     * Összes írással töltött idő.
     */
    private int totalPenDownTime;

    /**
     * Összes felemelt tollú idő.
     */
    private int totalPenUpTime;

    public int getTotalPenDownTime() {
        return totalPenDownTime;
    }

    public int getTotalPenUpTime() {
        return totalPenUpTime;
    }

    public int getTotalTime() {
        return getTotalPenDownTime() + getTotalPenUpTime();
    }

    public com.timeseries.TimeSeries getSegment(int i) {
        return segments.get(i);
    }

    /**
     * Egy szegmens újramintavételezését végzi. A köztes pontok
     * számításához a két végpontot használja fel, és a távolságukkal
     * arányosan számolja őket.
     * @param index szegmens sorszáma
     * @param newLength új szegmens hossza
     */
    public void resampleSegment(int index, int newLength) {

        double sampleRate = this.segments.get(index).numOfPts() / newLength;    //mintavételezési ráta
        double lambda, egyMinusLambda;  //lambda és 1-lambda
        
        
        com.timeseries.TimeSeries segmentToResample = this.segments.get(index); //az újramintavételezni kívánt szegmens
        int dimension = segmentToResample.numOfDimensions();    //egy pontban hány érték van
        com.timeseries.TimeSeries segmentResampled = new com.timeseries.TimeSeries(dimension);  //ez lesz az újramintavételezett szegmens

        int i = 0;  //ez halad az új tömbön
        double j = 0.0; //a régi sorozat ezen elemét kell illeszteni az i. új helyre. Ha ez nem rácspont, akkor lin. interpoláció kell
       
        while ( i < newLength ) {
            lambda = j - Math.floor(j);             //lambda az előző ponttól vett távolság
            egyMinusLambda = Math.ceil(j) - j;      //1-lambda a következő ponttól való távolság

            double []resampledPointCoords = new double[dimension];  //az új adatok az adott időpontban időpontban
            
            double []oldSegmentx0 = segmentToResample.getMeasurementVector((int)Math.floor(j));  //bal végpont
            double []oldSegmentx1 = segmentToResample.getMeasurementVector((int)Math.ceil(j));   //jobb végpont (rácspont esetén a kettő megegyezik

            /* Az időpillanat minden koordinátáját egyesével újramintavételezem */
            for ( int k = 0 ; k < dimension ; k++ ) {
                resampledPointCoords[k] = oldSegmentx0[k]*lambda + oldSegmentx1[k]*egyMinusLambda;
            }

            com.timeseries.TimeSeriesPoint resampledTimePoint = new TimeSeriesPoint(resampledPointCoords);  //az i. új időpont
            segmentResampled.addLast(i, resampledTimePoint);
            
            i++;
            j += sampleRate;
        }
        this.segments.set(index, segmentResampled); //cserélem a szegmenst
    }

    /**
     * Létrehozza az aláírást a createTimeSeries metódus
     * meghívásával.
     * @param file beolvasandó fájl neve
     * @param numOfPoints beolvasandó pontok száma
     * @param cols beolvasandó oszlopok
     */
    public Signature(String file, int numOfPoints, int []cols) {
        try {
            createTimeSeries(file, numOfPoints, cols);
        } catch (java.io.FileNotFoundException e) {
            System.out.println("Nincs ilyen file: " + file);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * A konstruktor hívja meg. Egy adott fájlból beolvassa az adatokat, és
     * létrehozza az idősor objektumokat.
     * @param file beolvasandó fájl neve
     * @param numOfPoints beolvasandó adatok száma
     * @param cols a beolvasandó adatok mely oszlopokban vannak, így nem szükséges egymás utáni oszlopokat olvasni
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void createTimeSeries(String file, int numOfPoints, int []cols) throws FileNotFoundException, IOException {
        BufferedReader filein = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        String line;

        int totalTime = 0;
        boolean penDown = true; //a tollról feltesszük, hogy lent van
        int currentSegmentsLength = 0;

        wholeSignature = new com.timeseries.TimeSeries(numOfPoints);
        segments = new ArrayList<com.timeseries.TimeSeries>();
        segmentPenDown = new ArrayList<Boolean>();

        com.timeseries.TimeSeries segment = new com.timeseries.TimeSeries(numOfPoints); //egy aláírásszegmens idősora
        segments.add(segment);  //hozzáadjuk a szegmenseket
        
        while ((line = filein.readLine())!=null) {  //soronként olvasunk

            //lineNum++;
            int currCol = 0;    //első oszloppal kezdjük
            int whichColNeed = 0;   //hanyadik sor kell
            double[] val = new double[numOfPoints];    //a sorból beolvasott adatok

            StringTokenizer st = new StringTokenizer(line," "); //felbontás szóközök mentén
                       
            while ( st.hasMoreTokens() ){

                String token = st.nextToken();  //vesszük a sor következő szavát
                
                if ( currCol == 2 ) {   //ha a z koordinátát olvassuk

                    if ( Double.parseDouble(token) == 0.0 ) { //és az 0
                        if ( penDown ) { //és a toll lent volt, akkor most felemeltük
                            segmentPenDown.add(penDown);
                            penDown = false;    //akkor a toll a levegőben van
                            segment = new com.timeseries.TimeSeries(numOfPoints);
                            segments.add(segment);
                            currentSegmentsLength = 0;
                        }
                    } else {    //ha nem 0-t olvasunk
                        if ( !penDown ) {   //és eddig fent volt a toll
                            segmentPenDown.add(penDown);
                            penDown = true;    //akkor a toll a levegőben van
                            segment = new com.timeseries.TimeSeries(numOfPoints);
                            segments.add(segment);
                            currentSegmentsLength = 0;
                        }
                    }
                }
                                
                if ( currCol > cols[cols.length-1] ){    //ha ezeket már nem kell beolvasni, akkor ne menjünk tovább
                    break;
                } else if ( currCol < cols[0] ) {  //ha ezt még nem kell beolvasni, ugorjunk
                    currCol++;
                    continue;
                } else if ( currCol == cols[whichColNeed] ) { //ezt be kell olvasni
                    val[whichColNeed] = Double.parseDouble(token);
                    currCol++;
                    whichColNeed++;
                }

            }

            com.timeseries.TimeSeriesPoint point = new TimeSeriesPoint(val);    //új időpont létrehozása a beolvasott értékekkel
            segment.addLast(currentSegmentsLength, point);      //időpont hozzáadása a szegmenshez
            wholeSignature.addLast(totalTime, point);           //időpont hozzáadása a teljes aláíráshoz
            currentSegmentsLength++;    //szegmensben lévő időpontok számának növelése
            totalTime++;
        }//end of while
        segmentPenDown.add(penDown);

        for ( int i = 0 ; i < segmentPenDown.size() ; i++ ){
            if ( segmentPenDown.get(i) )
                this.totalPenDownTime += segments.get(i).numOfPts();
            else
                this.totalPenUpTime += segments.get(i).numOfPts();
        }

        /*System.out.println("Number of segments: " + segments.size());
        System.out.println("Legth of segments': ");
        for ( int i = 0 ; i < segments.size() ; i++ ) {
            System.out.println((i+1) + ". szegmens: " + segments.get(i).numOfPts() + " " + segmentPenUp.get(i));
        }*/
        //System.out.println(wholeSignature.numOfPts());
        filein.close();
    }

    /**
     * Visszaadja az aláírást teljes egészében, szegmentálatlanul.
     * @return az aláírás teljes idősora
     */
    public TimeSeries getWholeSignature() {
        return wholeSignature;
    }

    /**
     * Hány szakaszból áll az aláírás.
     * @return szakaszok száma
     */
    public int numberOfSegments() {
        return segments.size();
    }
    

}
