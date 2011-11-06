/*
 * DTW algoritmus segítségével végez osztályozást.
 */

package Evaluate;

import Training.TrainingSignature;
import com.timeseries.TimeSeries;
import com.dtw.TimeWarpInfo;

/**
 *
 * @author sborde
 */
public class DTWClassifier {
    private TrainingSignature genuine;
    private TrainingSignature forgery;

    public static int MIN = 0;
    public static int MAX = 1;
    public static int AVG = 2;

    public DTWClassifier(TrainingSignature g, TrainingSignature f) {
        this.genuine = g;
        this.forgery = f;
    }

    public int isValidSignature(TimeSeries toTest, int window, int decisionType){
            double elements = 0;
            double minDistance = 0;
            double maxDistance = 0;
            double averageDistance = 0;
            TimeSeries []genuineSigs = genuine.getTrainingSet();
            for ( int i = 0 ; i < genuineSigs.length ; i++) {
                    final TimeWarpInfo info = com.dtw.FastDTW.getWarpInfoBetween(genuineSigs[i], toTest, window);
                    if ( i == 0 ) {
                        minDistance = maxDistance = info.getDistance();
                    }
                    if ( minDistance > info.getDistance() ) {
                        minDistance = info.getDistance();
                    } else if ( maxDistance < info.getDistance() ) {
                        maxDistance = info.getDistance();
                    }
                    elements++;
                    averageDistance += info.getDistance();
            }

            averageDistance /= elements;

            int valid = 0;
            switch(decisionType) {
                case 0: valid = decide(genuine.getMinDistance(), minDistance, 0.0); break;
                case 1: valid = decide(genuine.getMaxDistance(), maxDistance, 0.0); break;
                case 2: valid = decide(genuine.getAverageDistance(), averageDistance, 0.0); break;
                default: valid = 0; break;
            }
            return valid;
    }

    public void printDatas(int num, double min, double max, double avg) {
        String retString = "Tanító halmaz mérete: " + num + "\n";
        retString += "Legközelebbi elemek távolsága: " + min + "\n";
        retString += "Legtávolabbi elemek távolsága: " + max + "\n";
        retString += "Átlagos távolság távolsága: " + avg + "\n";
        System.out.println(retString);
    }

    private int decide(double train, double test, double threshold) {
        if ( train >= (test+threshold))
           return 1;
        else
           return 0;
    }
}
