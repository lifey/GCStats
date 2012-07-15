/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.performizeit.gcstats.pausedetect;

/**
 *
 * @author yadidh
 */
public interface PauseDetectorMXBean {
   public long getLongestPause();
   public String getLongestPauseTimestamp();
   public long getLatestLongPause();
   public String getLatestLongPauseTimestamp();   
   public long getNumLongPause();
   public long getLongPauseMS();
   public void setLongPauseMS(long ms);
   public String getLastStatisticsResetTimestamp();   
   public void resetStatistics();
    
}
