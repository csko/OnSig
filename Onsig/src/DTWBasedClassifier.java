
import Evaluate.DTWClassifier;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import com.timeseries.TimeSeries;
import Training.TrainingSignature;
import java.io.File;
import java.util.ArrayList;
import java.util.regex.Pattern;
import signature.Signature;

/**
 * Improved DTW algoritmust használó aláírás
 * validáló.
 * @author sborde
 */
public class DTWBasedClassifier {

    /**
     * Az aláírók tényleges száma. Azért kell megszámolni,
     * mert vannak hiányzó elemek.
     */
    private static int numberOfSigners = 0;

    /**
     * Az összes aláíróra végrehajtott tanítás
     * után az FRR értéke.
     */
    private static double globalFRR = 0.0;
    /**
     * Az összes aláíróra végrehajtott tanítás
     * után az FAR értéke.
     */
    private static double globalFAR = 0.0;

    /**
     * Kigyűjti az adott ID-jű íróhoz tartozó valid és hamis
     * aláírásokat (a valid aláírásokat szétosztja tanító és
     * teszt halmazra).
     * @param signerId aláíró azonosítója
     * @param forgeryFiles hamisítványok
     * @param genuineFiles valós aláírások
     */
    private static void getFilesById(String signerId, ArrayList<Signature> forgeryFiles, ArrayList<Signature> genuineFiles) {
        int []cols = {0,1,2,3,4,5,6,7,8,9,10,11};
        File forgerydir = new File("../data-deriv/forgery/");   //hamisítványok fájljai
        File genuinedir = new File("../data-deriv/genuine/");   //eredeti fájlok
        String []forgedfiles = forgerydir.list();   //összes hamisított aláírás
        String []genuinefiles = genuinedir.list();  //összes valid aláírás
        
        //System.out.println(signerId + " hamisitott alairasai: ");
        for ( int i = 0 ; i < forgedfiles.length ; i++ ) {
            if ( Pattern.matches("[0-9]*_"+signerId+"_[0-9]*.HWR", forgedfiles[i]) ) {
                //System.out.println(forgedfiles[i]);
                forgeryFiles.add(new Signature("../data-deriv/forgery/"+forgedfiles[i], 12, cols));
            }
        }

        //System.out.println(signerId + " eredeti alairasai: ");
        for ( int i = 0 ; i < genuinefiles.length ; i++ ) {
            if ( Pattern.matches(signerId+"_[1-9]^*[0-9]*.HWR", genuinefiles[i]) ) {
                //System.out.println(genuinefiles[i]);
                genuineFiles.add(new Signature("../data-deriv/genuine/"+genuinefiles[i], 12, cols));
            }
        }
    }

    private static double globalThreshold;

    /**
     * Lekéri egy aláíróhoz tartozó helyes és
     * hamis aláírásokat, majd kiszámítja az FRR és
     * FAR értékeket.
     * @param signerId az aláíró azonosítója
     * @return EER-hez tartozó küszöbérték
     */
    private static double findFARAndFRR(String signerId) {
        //System.out.println(signerId);
        ArrayList<Signature> validSignatures = new ArrayList<Signature>();
        ArrayList<Signature> forgSignatures = new ArrayList<Signature>();
        getFilesById(signerId, forgSignatures, validSignatures);

        double testSetRate = 0.5;   //a teszthalmaz aránya az összes valid aláírás közül
        double testSetSize = Math.floor(validSignatures.size()*testSetRate);   //hány darab aláírásból áll a teszthalmaz (a többi tanító)
        double threshold = 1.0;

        double avgfrr = 0.0;    //átlagos far és frr érték az adott aláíróhoz
        double avgfar = 0.0;


        /* Szétválogatom a valid aláírásokat tanító és teszt halmazokra. */
        for ( int i = 0 ; i < (validSignatures.size()/testSetSize) ; i++ ) { //keresztvalidációval tesztelünk
            ArrayList<Signature> trainSignatures = new ArrayList<Signature>((int)(validSignatures.size()-testSetSize));   //tanító halmaz összeállítása
            ArrayList<Signature> testSignatures = new ArrayList<Signature>(); //a teszt valid aláírások halmaza, hamisak közül mind teszt lesz

            for ( int j = 0 ; j < validSignatures.size() ; j++ ) {
                if ( (j >= (i*testSetSize)) && (j<((i+1)*testSetSize))) {   //ha épp ez a teszthalmaz
                    testSignatures.add(validSignatures.get(j));
                } else {    //egyébként tanító
                    trainSignatures.add(validSignatures.get(j));
                }
            }
            
            /* Adott teszthalmaz választással megkeresem az EER-t. */
            double far = 0.0;   //feltesszük, hogy minden hibásat elutasít
            double frr = testSignatures.size();   //alapból feltesszük, hogy minden helyesen helyesnek fogad el
            threshold = globalThreshold;   //küszöbérték

            TrainingSignature trainingSet = new TrainingSignature(trainSignatures, 0);
            DTWClassifier classifier = new DTWClassifier(trainingSet, threshold);

            for ( int j = 0 ; j < testSignatures.size() ; j++ )
                frr -= classifier.isValidSignature(testSignatures.get(j).getWholeSignature(), 0, DTWClassifier.MIN);    //ahányszor 0-t ad (tehát visszautasít egy helyes aláírást), annyi marad a hiba értéke
            
            frr /= testSignatures.size();
            globalFRR += frr;

            for ( int j = 0 ; j < forgSignatures.size() ; j++ )
                far += classifier.isValidSignature(forgSignatures.get(j).getWholeSignature(), 0, DTWClassifier.MIN);    //ahányszor 1-t ad (tehát elfogad egy hamis aláírást), annyi lesz a hiba érték

            far /= forgSignatures.size();
            globalFAR += far;

            avgfar += far;
            avgfrr += frr;

            //System.out.println("FRR: " + frr + " FAR: " + far);
            numberOfSigners++;
        }

        avgfar /= ((validSignatures.size()/testSetSize));
        avgfrr /= ((validSignatures.size()/testSetSize));
        System.out.println(signerId + " kész");
        return threshold;
    }

    public static void main(String []args) throws FileNotFoundException, IOException {

        PrintWriter file; //kimeneti fájlstream
        double threshold = globalThreshold;   //a küszöbérték, aminél megkaptuk az EER-t
        String defaultValidSignatureDirectory = "../data-deriv/genuine/";

        double begin = 50.0;
        double end = 1.0;
        
        if ( args.length == 0 ) {   //ha paraméter nélkül futtatjuk, akkor teszteljük
            file = new PrintWriter(new FileOutputStream(new File("output_min_min_neuc.txt")));
            while (true) {
                globalThreshold = begin - ((begin-end)/2);
                for ( int i = 0 ; i < 16 ; i++ ) {
                    if ( i < 9 ) {
                        findFARAndFRR("00"+(i+1));
                    } else {
                        findFARAndFRR("0"+(i+1));
                    }
                }
                globalFRR /= numberOfSigners;
                globalFAR /= numberOfSigners;
                System.out.println("Osszesitett FRR: " + globalFRR + " FAR: " + globalFAR);
                file.println("Threshold: " + globalThreshold);
                file.println("Osszesitett FRR: " + globalFRR + " FAR: " + globalFAR);
                file.flush();


                if ( Math.abs(globalFRR-globalFAR) < 0.01 )
                    break;

                if ( globalFRR < globalFAR ) {
                    begin = globalThreshold;
                } else {
                    end = globalThreshold;
                }
                
            }
            file.close();
            
        } else {    //egyébként pedig első paraméterben kapja a tesztelendő aláírást, utána pedig sorban a tanító halmazt jelölő elemeket
            try {
                file = new PrintWriter(new FileOutputStream("output.txt"));

                int []col = {0,1,2,3,4,5,6,7,8,9,10,11};
                final Signature testSignature = new Signature(defaultValidSignatureDirectory + args[0], 12, col);    //kinyerjük a tesztelendő aláírást

                ArrayList<Signature> trainSignatures = new ArrayList<Signature>(args.length-1);   //tanító aláírások tömbje
                for ( int i = 1 ; i < args.length ; i++ ) { //sorra vesszük a többi aláírást, amiből tanítóhalmazt építünk
                    final Signature trainSignature = new Signature(defaultValidSignatureDirectory + args[i], 12, col);   //i. tanító aláírás
                    trainSignatures.add(i-1, trainSignature);   //beteszem a tanítóelemek közé
                }

                TrainingSignature trainingSet = new TrainingSignature(trainSignatures, 0);    //létrehozom a tanítóhalmazt 0 ablakmérettel (ez az automatikus beállítás)

                DTWClassifier classifier = new DTWClassifier(trainingSet, threshold);   //készítek egy osztályozót

                int decision = classifier.isValidSignature(testSignature.getWholeSignature(), 0, DTWClassifier.MIN);   //megnézem, hogy a MIN távolság szerint (ez bizonyult a legjobbnak a próbálgatásokkor) valósnak ítélem-e

                file.println(decision); //kiírom a fájlba a döntést
                file.flush();

            } catch ( FileNotFoundException e ) {
                System.out.println("Fajl nem talalhato.");
            }
        }
        

    }
}
