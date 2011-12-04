package signature;

/**
 * Az aláírás egy időpillanatában megfigyelt értékeket
 * tároló osztály.
 * @author sborde
 */
public class TimePoint {

    /**
     * az értékeket tároló tömb
     */
    private double[] values;

    /**
     * Hány értéket tárolunk egy időpontban.
     */
    private int valuesNum;

    /**
     * Az idősor hanyadik pontja vagyunk.
     */
    private int t;

    /**
     * Jelzi, hogy most épp lent van-e a toll.
     */
    private boolean penDown;

    /**
     * @param values értékek száma
     * @param offset az értékek
     * @param t hanyadik időpillanat vagyunk
     */
    public TimePoint(int values, double[] points, int t, boolean pD){
        this.valuesNum = values;
        this.values = points;
        this.t = t;
        this.penDown = pD;
    }

    public double getCoordValue(int index){
        return this.values[index];
    }

    public boolean isPenDown(){
        return penDown;
    }

}
