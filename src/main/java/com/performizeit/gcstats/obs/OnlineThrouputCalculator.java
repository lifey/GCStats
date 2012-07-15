/*
 *
 * Copyright 2011 Performize-IT LTD.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.performizeit.gcstats.obs;

import java.io.IOException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import static com.performizeit.jmxsupport.JMXConnection.*;

public class OnlineThrouputCalculator {

    private final MBeanServerConnection server;
    private final int interval;
    private final int samplingRate;
    private final int reportRate;
    private final int errorMaxTime;

    PauseStats pStats;
    GCStats gStats;


    public OnlineThrouputCalculator(MBeanServerConnection server, int interval, int samplingRate, int reportRate, int errorMaxTime) throws MalformedObjectNameException, IOException {
        this.server = server;
        this.interval = interval;
        this.samplingRate = samplingRate;
        this.reportRate = reportRate;
        this.errorMaxTime = errorMaxTime;
        pStats =   new PauseStats(errorMaxTime, server, interval, samplingRate);
        gStats = new GCStats(server, interval);


    }

    public void connectToJMX() throws Exception {
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb, Locale.US);

        // Explicit argument indices may be used to re-order output.
        formatter.format("%9s %7s %5s %-20s %5s %5s %5s %5s %5s %6s %5s %7s %5s", "Time", "Int", "MMP","GC", "CNT", "Skip", "SumP", "maxP", "MinIn", "Thrpt", "GCNT", "GTIM", "GMEM");
        System.out.println(formatter.toString());

        long lastReport = -1 * reportRate;
        long startTime = 0; //getUptime(server);

        while (true) {
            long curServerUpTime = pStats.singlePauseWrapper();
            gStats.singleSample(curServerUpTime);

            // Display statistics only if reportRate passed
            if (lastReport < curServerUpTime - reportRate && startTime < curServerUpTime - reportRate) {
                long realInterval = (curServerUpTime - startTime < interval) ? curServerUpTime - startTime : interval;
                if (realInterval == 0) {
                    realInterval = 1;
                }
                printStats(curServerUpTime, gStats.stats, realInterval);
                lastReport = curServerUpTime;
            }
            Thread.sleep(samplingRate);

        }

    }





    private void printStats(long curTime, HashMap<ObjectName, LinkedList<GCInfo>> stats, long interval) throws Exception {
        long globalSumPauseTime = 0;
        long globalMaxPauseTime = 0;
        long globalMinInterval = 100000000000l;
        long globalCnt = 0;
        long globalSkips = 0;
        long globalMaxUsedHeap = 0;
        for (ObjectName o : stats.keySet()) {
            LinkedList<GCInfo> list = stats.get(o);
            long sumPause = 0;
            long maxPause = 0;
            long minInterval = interval * 10;

            long skips = 0;
            if (list.size() > 0) {
                long minId = list.getFirst().id;
                long maxId = list.getLast().id;
                skips = (maxId - minId + 1) - list.size();
                long lastStart = -1;
                for (GCInfo info : list) {
                    sumPause += info.duration;
                    if (lastStart != -1) {
                        long diff = info.startTime - lastStart;
                        if (diff < minInterval) {
                            minInterval = diff;
                        }
                    }
                    lastStart = info.startTime;
                    if (info.duration > maxPause) {
                        maxPause = info.duration;
                    }
               //     if (info.usedHeapAfterGC > globalMaxUsedHeap) {
               //         globalMaxUsedHeap = info.usedHeapAfterGC;
                //    }
                }
            }

            // Explicit argument indices may be used to re-order output.
            StringBuilder sb = new StringBuilder();
            Formatter formatter = new Formatter(sb, Locale.US);
            String GCname = o.getCanonicalName();
            if (GCname.startsWith("java.lang:")) {
                GCname = GCname.substring("java.lang:".length());
            }
            if (GCname.startsWith("name=")) {
                GCname = GCname.substring("name=".length());
            }
            if (GCname.endsWith(",type=GarbageCollector")) {
                GCname = GCname.substring(0, GCname.length() - ",type=GarbageCollector".length());
            }
            long collectionCount = (Long) server.getAttribute(o, "CollectionCount");
            long collectionTime = (Long) server.getAttribute(o, "CollectionTime");

            formatter.format("%9.3f %7d %5s %-20s %5d %5d %5d %5d %5s %6s %5d %7d", ((float) curTime) / 1000, interval, "",GCname, list.size(), skips, sumPause, maxPause, (minInterval < interval ? (minInterval + "") : "N/A"), "", collectionCount, collectionTime);
            System.out.println(formatter.toString());
            //     System.out.println(curTime + " interval=" + interval + "ms " + o.getCanonicalName() + " numCollections=" + list.size() + " skips=" + skips + " sumPause=" + sumPause + "ms maxPause=" + maxPause + "ms" + (minInterval < interval ? (" minDiff=" + minInterval + "ms") : ""));
            globalSumPauseTime += sumPause;
            if (globalMaxPauseTime < maxPause) {
                globalMaxPauseTime = maxPause;
            }
            if (globalMinInterval > minInterval) {
                globalMinInterval = minInterval;
            }
            globalSkips += skips;
            globalCnt += list.size();

        }
        float throughput = (float) (100.0 * (0.0 + interval - globalSumPauseTime) / interval);
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb, Locale.US);
        formatter.flush();
        String sMaxUsedHeap = "";
        if (globalMaxUsedHeap > 10 * 1024 * 1024) {
            sMaxUsedHeap = (globalMaxUsedHeap / 1024 / 1024) + "MB";
        } else if (globalMaxUsedHeap > 10 * 1024) {
            sMaxUsedHeap = (globalMaxUsedHeap / 1024) + "KB";
        } else if (globalMaxUsedHeap > 0) {
            sMaxUsedHeap = globalMaxUsedHeap + "B";
        } else {
            sMaxUsedHeap = "N/A";
        }
        formatter.format("%9.3f %7d %5d %-20s %5d %5d %5d %5d %5s %3.2f %5s %7s %5s", inSecsTimestamp(curTime), interval,pStats.getMaxPause(), "Total", globalCnt, globalSkips, globalSumPauseTime, globalMaxPauseTime, (globalMinInterval < interval ? (globalMinInterval + "") : "N/A"), throughput, "", "", sMaxUsedHeap);
        System.out.println(formatter.toString());
    }


}
