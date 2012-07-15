/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.performizeit.gcstats.gclogparser;

/**
 *
 * @author yadidh
 */
public class Aggr {
        float max;
        float sumReal;
        long num;
        long numLong;
        float longThresh;
        float cpuUsage;
        public Aggr(float thresh) {
            longThresh = thresh;
            max = 0f;
            sumReal= 0f;
            num=0;
            numLong = 0;
            cpuUsage = 0;
        }
            public void add(float real,float user,float sys) {
            num++;
            if (real > longThresh) numLong++;
            sumReal += real;
            if (max < real) max = real;
            cpuUsage += user+sys;
            
        }
        public String toString() {
            return String.format("%9.3f %9.3f %6d %6d %9.3f",max,sumReal,num,numLong ,cpuUsage);
            
        }
    }