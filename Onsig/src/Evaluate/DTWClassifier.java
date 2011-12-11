package Evaluate;

import Training.TrainingSignature;
import com.timeseries.TimeSeries;
import com.dtw.TimeWarpInfo;
import java.util.ArrayList;

/**
 * Alaírás osztályozó. DTW algoritmust
 * használ. Lehetőség van a tanítóhalmaztól
 * vett legnagyobb, legkisebb és átlagos távolság
 * alapján dönteni. Beállítható küszöbérték.
 * @author sborde
 */
public class DTWClassifier {
    /* Tanítóhalmaz. */
    private TrainingSignature genuine;

    /** A döntés típusa: minimális érték szerinti. */
    public static int MIN = 0;
    /** A döntés típusa: maximális érték szerinti. */
    public static int MAX = 1;
    /** A döntés típusa: átlagos távolság alapján. */
    public static int AVG = 2;

    /* Küszöbérték. */
    private double threshold;

    private double penUpTime;
    private double penDownTime;

    /**
     * Konstruktor előállítja az osztályozót.
     * @param g tanítóhalmaz
     * @param th küszöb együttható
     */
    public DTWClassifier(TrainingSignature g, double th) {
        this.genuine = g;
        this.threshold = th;
    }

    /**
     * Megállapítja egy aláírásról, hogy a tanítóhalmaz alapján
     * hamis vagy eredeti.
     * @param toTest a tesztelendő aláírás idősora
     * @param window ablakméret
     * @param decisionType döntés típusa: min, max, avg
     * @return 1 ha elfogadja, 0 ha nem
     */
    public int isValidSignature(TimeSeries toTest, int window, int decisionType){
            double elements = 0;    //hány elemmel hasonlítottuk össze

            /* A tanítóhalmaztól vett */
            double minDistance = 0; //legkisebb
            double maxDistance = 0; //legnagyobb
            double averageDistance = 0; //átlagos távolság

            ArrayList<signature.Signature> genuineSigs = genuine.getTrainingSet();
            /* Vesszük a tanítóhalmaz összes elemétől vett távolságot */
            for ( int i = 0 ; i < genuineSigs.size() ; i++) {
                    final TimeWarpInfo info = com.dtw.FastDTW.getWarpInfoBetween(genuineSigs.get(i).getWholeSignature(), toTest, window);
                    if ( i == 0 )  //első elempár esetén inicializálás
                        minDistance = maxDistance = info.getDistance();
                    
                    if ( minDistance > info.getDistance() ) {   //min és max érték kiválasztása
                        minDistance = info.getDistance();
                    } else if ( maxDistance < info.getDistance() ) {
                        maxDistance = info.getDistance();
                    }

                    elements++;
                    averageDistance += info.getDistance();
            }

            averageDistance /= elements;    //átlagos távolság számítás

            int valid = 0;
            
            switch(decisionType) {
                case 0: valid = decide(genuine.getMinDistance(), minDistance, threshold); break;
                case 1: valid = decide(genuine.getMaxDistance(), maxDistance, threshold); break;
                case 2: valid = decide(genuine.getAverageDistance(), averageDistance, threshold); break;
                default: valid = 0; break;
            }
            return valid;
    }

    private void calculateAverageTimes(TimeSeries toTest) {
                for ( int j = 0 ; j < toTest.size() ; j++ ){
                if ( (toTest.getMeasurementVector(j)[2]) == 0 ) {
                    this.penUpTime++;
                } else {
                    this.penDownTime++;
                }
            }
    }

    /**
     * Döntést végző függvény.
     * @param train tanítóhalmazon mért távolság D
     * @param test teszt elem távolsága a legközelebbi/távolabbi elemtől
     * @param threshold küszöb együttható
     * @return 1 ha elfogadjuk, 0 ha nem
     */
    private int decide(double train, double test, double threshold) {
        if ( train*threshold > test)
           return 1;
        else
           return 0;
    }
}
