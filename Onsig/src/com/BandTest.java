/*
 * BandTest.java   Jul 14, 2004
 *
 * Copyright (c) 2004 Stan Salvador
 * stansalvador@hotmail.com
 */

package com;

import com.timeseries.TimeSeries;
import com.dtw.TimeWarpInfo;
import com.dtw.LinearWindow;

/**
 * @author Stan Salvador, stansalvador@hotmail.com
 * @since Jul 14, 2004
 */

public class BandTest
{

      // PUBLIC FUNCTIONS
      public static void main(String[] args)
      {
         if (args.length != 3)
         {
            System.out.println("USAGE:  java BandTest timeSeries1 timeSeries2 radius");
            System.exit(1);
         }
         else
         {
            final TimeSeries tsI = new TimeSeries(args[0], false, false, ',');
            final TimeSeries tsJ = new TimeSeries(args[1], false, false, ',');
            final TimeWarpInfo info = com.dtw.DTW.getWarpInfoBetween(tsI, tsJ, new LinearWindow(tsI, tsJ, Integer.parseInt(args[2])));

            System.out.println("Warp Distance: " + info.getDistance());
            System.out.println("Warp Path:     " + info.getPath());
         }  // end if

      }  // end main()


}  // end class BandTest
