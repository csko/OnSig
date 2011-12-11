package Training;

import com.dtw.TimeWarpInfo;
import java.util.ArrayList;

/**
 * Egy tanítóhalmazt reprezentáló osztály.
 * @author 
 */
public class TrainingSignature {
    private double averageDistance; //a tanítóhalmaz elemei közötti átlagos távolság
    private double minDistance;     //a tanítóhalmaz elemei közötti legkisebb távolság
    private double maxDistance;     //a tanítóhalmaz elemei közötti legnagyobb távolság
    private double [][]distanceMatrix;  //a tanítóhalmaz elemei közötti távolság-mátrix (alsó háromszög mátrix a szimmetria miatt)
    private ArrayList<signature.Signature> trainingSet;   //tanítóhalmaz elemei
    private double avgpenUpTime;  //átlagos idő, amíg a toll fel volt emelve
    private double avgpenDownTime;  //átlagos idő, amíg a tollal írtunk: az előző és ez adja az össz írás időt

    /**
     * A konstruktor a kapott aláírások idősoraiból kiszámítja a tanítóhalmaz
     * távolságmátrixát.
     * @param t - idősorok
     * @param window - ablakméret
     */
    public TrainingSignature(ArrayList<signature.Signature> t, int window){
            distanceMatrix = new double[t.size()][t.size()];
            trainingSet = t;
            double elements = 0;
            calculateAverageTimes();
            
            for ( int i = 0 ; i < t.size() ; i++) {
                for ( int j = 0 ; j < i ; j++ ) {
                    final TimeWarpInfo info = com.dtw.FastDTW.getWarpInfoBetween(t.get(i).getWholeSignature(), t.get(j).getWholeSignature(), window);
                    distanceMatrix[i][j] = info.getDistance();
                    if ( i == 1 && j == 0 ) {
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
            }

            averageDistance /= elements;

    }

    @Override
    public String toString() {
        String retString = "Tanító halmaz mérete: " + trainingSet.size() + "\n";
        retString += "Legközelebbi elemek távolsága: " + minDistance + "\n";
        retString += "Legtávolabbi elemek távolsága: " + maxDistance + "\n";
        retString += "Átlagos távolság távolsága: " + averageDistance + "\n";
        return retString;
    }
    
    public double getAverageDistance() {
        return averageDistance;
    }

    public void setAverageDistance(double averageDistance) {
        this.averageDistance = averageDistance;
    }

    public double getMaxDistance() {
        return maxDistance;
    }

    public void setMaxDistance(double maxDistance) {
        this.maxDistance = maxDistance;
    }

    public double getMinDistance() {
        return minDistance;
    }

    public void setMinDistance(double minDistance) {
        this.minDistance = minDistance;
    }

    public ArrayList<signature.Signature> getTrainingSet() {
        return trainingSet;
    }

    public void printMatrix() {
        for ( int i = 0 ; i < distanceMatrix.length ; i++ ){
            for ( int j = 0 ; j < i ; j++ ) {
                System.out.print(distanceMatrix[i][j] + " ");
            }
            System.out.println();
        }
    }

    /**
     * Kiszámítja az ebben a tanítóhalmazban található minták átlagos
     * penUp és penDown idejét.
     */
    private void calculateAverageTimes() {
        for ( int i = 0 ; i < this.trainingSet.size() ; i++ ){
            for ( int j = 0 ; j < this.trainingSet.get(i).getWholeSignature().size() ; j++ ){
                if ( (this.trainingSet.get(i).getWholeSignature().getMeasurementVector(j)[2]) == 0 ) {
                    this.avgpenUpTime++;
                } else {
                    this.avgpenDownTime++;
                }
            }
        }
        this.avgpenDownTime /= this.trainingSet.size();
        this.avgpenUpTime /= this.trainingSet.size();
        
    }


}
