package signature;

/*
 * Egy aláírás egy adott időintervallumában megfigyelt
 * tulajdonságait reprezentálja.
 */


import java.util.StringTokenizer;

/**
 *
 * @author sborde
 */
public class TimeInterval {

    /**
     * Koordináták deriváltjait tárolja.
     * Első dimenzió [0;3] a 0. - 3. derivált
     * Második dimenzió pedig az x, y, z koordináta értéke
     * 
     */
    private int [][]coords;

    /**
     * Hanyadik időpillanatról van szó.
     */
    private int t;

    public int getT() {
        return t;
    }

    public void setT(int t) {
        this.t = t;
    }

    public int getX() {
        return coords[0][0];
    }

    public void setX(int x) {
        this.coords[0][0] = x;
    }

    public int getY() {
        return coords[0][1];
    }

    public void setY(int y) {
        this.coords[0][1] = y;
    }

    public int getZ() {
        return coords[0][2];
    }

    public void setZ(int z) {
        this.coords[0][2] = z;
    }

    public int getDx() {
        return coords[1][0];
    }

    public void setDx(int dx) {
        this.coords[1][0] = dx;
    }

    public int getDy() {
        return coords[1][1];
    }

    public void setDy(int dy) {
        this.coords[1][1] = dy;
    }

    public int getDz() {
        return coords[1][2];
    }

    public void setDz(int dz) {
        this.coords[1][2] = dz;
    }

    public int getDdx() {
        return coords[2][0];
    }

    public void setDdx(int ddx) {
        this.coords[2][0] = ddx;
    }

    public int getDdy() {
        return coords[2][1];
    }

    public void setDdy(int ddy) {
        this.coords[2][1] = ddy;
    }

    public int getDdz() {
        return coords[2][2];
    }

    public void setDdz(int ddz) {
        this.coords[2][2] = ddz;
    }

    public int getDddx() {
        return coords[3][0];
    }

    public void setDddx(int dddx) {
        this.coords[3][0] = dddx;
    }

    public int getDddy() {
        return coords[3][1];
    }

    public void setDddy(int dddy) {
        this.coords[3][1] = dddy;
    }

    public int getDddz() {
        return coords[3][2];
    }

    public void setDddz(int dddz) {
        this.coords[3][2] = dddz;
    }

    public TimeInterval(String datas) {
        this.coords = new int[4][3];
        StringTokenizer st = new StringTokenizer(datas);
        for ( int i = 0 ; i < 4 ; i++ ) {
            for ( int j = 0 ; j < 3 ; j++ ){
                this.coords[i][j] = new Integer(st.nextToken());
            }
        }
    }

}
