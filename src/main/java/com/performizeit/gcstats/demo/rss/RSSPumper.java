/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.performizeit.gcstats.demo.rss;

import java.util.ArrayList;

/**
 *
 * @author yadidh
 */
public class RSSPumper {
    public static void main(String[] args) throws InterruptedException {
        int memSizeMB = Integer.parseInt(args[0]);
        int pageSizeKB = 1;
        ArrayList<byte[]> bufs = new ArrayList<>();
        for (int i=0;i<memSizeMB;i++) {
            bufs.add(new byte[1024*1024-8-8-8]);
        }
        System.out.println("Done allocating memory");
        byte tmp=0;
        while (true) {
            long start = System.currentTimeMillis();
            for (byte[] buf : bufs) {
                for (int i=0;i<buf.length;i+=(pageSizeKB*1024)) {
                    buf[i] += tmp;
                    tmp+=1+buf[i];
                }
                            Thread.sleep(2);
            }
           // System.out.println("Finished touching all pages " + (System.currentTimeMillis() -start));
        }
       
        
    }
    
}
