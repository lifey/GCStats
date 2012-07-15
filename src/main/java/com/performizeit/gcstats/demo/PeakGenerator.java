/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.performizeit.gcstats.demo;

import java.util.ArrayList;

/**
 *
 * @author yadidh
 */
public class PeakGenerator implements Runnable {

    int memInMB;
    long holdForMs;
    long restMs;
    int cnt;

    public static void generatePeak(int memInMB, long holdForMs, long restMs, int cnt) {
        PeakGenerator pg = new PeakGenerator(memInMB, holdForMs, restMs, cnt);
        Thread pgT = new Thread(pg);
        pgT.setName("PeakGenerator" + "_" + memInMB);
        pgT.setDaemon(true);
        pgT.start();
    }

    private PeakGenerator(int memInMB, long holdForMs, long restMs, int cnt) {
        this.memInMB = memInMB;
        this.holdForMs = holdForMs;
        this.restMs = restMs;
        this.cnt = cnt;
    }

    @Override
    public void run() {
        for (int i = 0; i < cnt; i++) {
            try {
                Thread.sleep(restMs);
                generatePeak(); 
         
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    private void generatePeak() throws InterruptedException {
        ArrayList<byte[]> bufs = new ArrayList<>();
        for (int i=0;i<memInMB;i++) {
            bufs.add(new byte[1024*1024-8-8-8]);
        }
        Thread.sleep(holdForMs);
        
    }
}
