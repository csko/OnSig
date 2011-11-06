/*
 * FastDTW.java   Jul 14, 2004
 *
 * Copyright (c) 2004 Stan Salvador
 * stansalvador@hotmail.com
 */

package com.dtw;

import com.timeseries.TimeSeries;
import com.timeseries.PAA;


public class FastDTW_1
{
   // CONSTANTS
   final static int DEFAULT_SEARCH_RADIUS = 1;


   public static double getWarpDistBetween(TimeSeries tsI, TimeSeries tsJ)
   {
      return fastDTW(tsI, tsJ, DEFAULT_SEARCH_RADIUS).getDistance();
   }


   public static double getWarpDistBetween(TimeSeries tsI, TimeSeries tsJ, int searchRadius)
   {
      return fastDTW(tsI, tsJ, searchRadius).getDistance();
   }


   public static WarpPath getWarpPathBetween(TimeSeries tsI, TimeSeries tsJ)
   {
      return fastDTW(tsI, tsJ, DEFAULT_SEARCH_RADIUS).getPath();
   }


   public static WarpPath getWarpPathBetween(TimeSeries tsI, TimeSeries tsJ, int searchRadius)
   {
      return fastDTW(tsI, tsJ, searchRadius).getPath();
   }


   public static TimeWarpInfo getWarpInfoBetween(TimeSeries tsI, TimeSeries tsJ, int searchRadius)
   {
      return fastDTW(tsI, tsJ, searchRadius);
   }


   private static TimeWarpInfo fastDTW(TimeSeries tsI, TimeSeries tsJ, int searchRadius)
   {
      if (searchRadius < 0)
         searchRadius = 0;

      final int minTSsize = searchRadius+2;

      if ( (tsI.size() <= minTSsize) || (tsJ.size()<=minTSsize) )
      {
         // Perform full Dynamic Time Warping.
         return DTW.getWarpInfoBetween(tsI, tsJ);
      }
      else
      {
         final double resolutionFactor = 2.0;

         final PAA shrunkI = new PAA(tsI, (int)(tsI.size()/resolutionFactor));
         final PAA shrunkJ = new PAA(tsJ, (int)(tsJ.size()/resolutionFactor));

          // Determine the search window that constrains the area of the cost matrix that will be evaluated based on
          //    the warp path found at the previous resolution (smaller time series).
          final SearchWindow window = new ExpandedResWindow(tsI, tsJ, shrunkI, shrunkJ,
                                                            FastDTW.getWarpPathBetween(shrunkI, shrunkJ, searchRadius),
                                                            searchRadius);

         // Find the optimal warp path through this search window constraint.
         return DTW.getWarpInfoBetween(tsI, tsJ, window);
      }  // end if
   }  // end recFastDTW(...)

}  // end class fastDTW
