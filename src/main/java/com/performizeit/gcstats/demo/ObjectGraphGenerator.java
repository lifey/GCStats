/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.performizeit.gcstats.demo;

import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author yadidh
 */
public class ObjectGraphGenerator {

    static Random r = null;
    static BaseObjectMap bom = BaseObjectMap.getInstance();

    public static int nextRandId() {
        return Math.abs(r.nextInt()) % bom.getSize();
    }

    public static void generateObjects(int size) {

        for (int i = 0; i < size; i++) {

            bom.createNewObject(r.nextLong());
        }
    }

    public static void generateLinks(int numObjs) {


        for (int i = 0; i < numObjs; i++) {
            long idS = nextRandId();
            BaseObject src = bom.getObj(idS);
            long idD = nextRandId();
            BaseObject dst = bom.getObj(idD);
            src.addRef(dst);
        }
    }

    public static long traverseObject(long id, int depth,int add) {


        BaseObject bo = bom.getObj(id);
        long sum = bo.getVal();
        bo.addVal(add);
        if (depth > 0) {
            for (BaseObject child : bo.getList()) {
                sum += traverseObject(child.getId(), depth - 1,add++);

            }
        }
        return sum;

    }

    public static void main(String[] args) throws InterruptedException {
        if (args.length == 0) {
            System.out.println("Synopsis <num Objs> [avg links per obj=10] [seed =1] [peak size MB]");
        }
        int size = Integer.parseInt(args[0]);
        int linksPerObj = 10;
        int seed = 1;
        if (args.length >= 2) {
            linksPerObj = Integer.parseInt(args[1]);
        }
int peakSizeMB=1;
        if (args.length >= 3) {
            seed = Integer.parseInt(args[2]);
        }
                if (args.length >= 4) {
             peakSizeMB = Integer.parseInt(args[3]);
        }
        r = new Random(seed);
        
        generateObjects(size);
        System.out.println("Generated " + size + " objects");
        generateLinks(size * linksPerObj);
        System.out.println("Generated " + size * linksPerObj + " links");
        PeakGenerator.generatePeak(peakSizeMB, 20000, 120000, 10000000);
        while (true) {
            traverseObject(nextRandId(), 5,1);
            ArrayList<Integer> ids = new ArrayList<>();
            for(int i=0;i<1500;i++) {
               ids.add(nextRandId());
                
            }
            ArrayList<Long> vals = bom.getValues(ids);
            long sum=0;
            for (long val :vals) {
                sum+= val;
            }
            System.out.print(sum+" ");
            Thread.sleep(50);
        }
    }
}
