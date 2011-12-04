/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com;

import java.io.FileNotFoundException;
import java.io.IOException;
import signature.TimeSeries;

/**
 *
 * @author sunc
 */
public class ImprovedDTWTest {
    public static void main(String[] args) throws FileNotFoundException, IOException{
        TimeSeries ts = new TimeSeries("../data-deriv/genuine/001_1.HWR", 3, 3);
    }
}
