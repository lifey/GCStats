/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.performizeit.gcstats.notificationlistener;

import com.sun.management.GcInfo;
import static com.performizeit.gcstats.notificationlistener.GCstatsNotificationListener.*;

/**
 *
 * @author yadidh
 */
public class AggrGCInfo {
        long totDuration;
        long startTime;
        long maxPause;
        boolean isConcurrent;
        int numThreads = -1;
        long lastId ;

        public AggrGCInfo() {
            reset();
            //isConcurrent = isCon;
            //numThreads = nThreads;
        }
        
        public synchronized void addGCInfo(GcInfo info) {
            totDuration += fix(info.getDuration());
            if (info.getDuration() > maxPause) maxPause = fix(info.getDuration());
            if (numThreads == -1) numThreads = (Integer)info.get("GcThreadCount");
                long nextId = (Long)info.get("id");
                if (lastId != -1 ) {
                    if (lastId +1 != nextId) {
                        System.out.println("Error ids are not sequential"  +lastId +" "+nextId);
                    }
                }
                lastId = nextId;
            
            
        }
        public synchronized void reset(){
            totDuration = 0;
            maxPause =0;
            lastId =-1;
            startTime = System.currentTimeMillis();
        }
        
        public synchronized long getMaxPause() {
            return maxPause;
        }
        public synchronized  long getSumPause() {
            return totDuration;
        }
        public synchronized float getThroughput() {
            long now = System.currentTimeMillis();
            return 1f-(((float)totDuration)/(now -startTime));
            
        }
        
        public synchronized void printAndReset() {
            System.out.println(((float)System.currentTimeMillis())/1000 +" " + getSumPause() + " " + getMaxPause() + " " + getThroughput());
            reset();
        }
    
}
