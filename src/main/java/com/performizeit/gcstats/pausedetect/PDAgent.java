/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.performizeit.gcstats.pausedetect;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;

import java.lang.management.RuntimeMXBean;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

/**
 *
 * @author yadidh
 */
public class PDAgent extends Thread implements PauseDetectorMXBean {

    private static PDAgent singleton = new PDAgent();
    private static boolean isStarted = false;
    AtomicLong sleepMS = new AtomicLong(50);
    AtomicLong longPauseMS = new AtomicLong(200);
    AtomicLong longestPause = new AtomicLong();
    AtomicLong longestPauseTS = new AtomicLong();
    AtomicLong latestLongPause = new AtomicLong();
    AtomicLong latestLongPauseTS = new AtomicLong();
    AtomicLong numLongPauses = new AtomicLong();
    AtomicLong lastStatisticsResetTimestamp = new AtomicLong(0);
    String logFile;

    public static synchronized boolean start(long longPauseMS, String logFile, long sleepMs) {
        if (isStarted) {
            return false;
        }
        isStarted = true;
        singleton.logFile = logFile;
        try {
            singleton.registerMeself();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        singleton.setDaemon(true);
        singleton.setName("JVM Pause Detector");
        singleton.longPauseMS.set(longPauseMS);
        singleton.sleepMS.set(sleepMs);
        singleton.resetStatistics();

        singleton.start();
        return true;

    }

    private void registerMeself() throws MalformedObjectNameException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName helloName = new ObjectName("java.lang.pausedetector:type=PauseDetector");
        mbs.registerMBean(this, helloName);

    }

    private void writePauseToFile(long ts, long duration, long sleepTime) {
        if (logFile == null) {
            return;
        }
        BufferedWriter fos = null;
        try {

            fos = new BufferedWriter(new FileWriter(logFile, true));
        } catch (Exception ex) {
            System.out.println("unable to open pause detector file for writing" + logFile);
            ex.printStackTrace();
            logFile = null;
            return;
        }

        try {
            RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean();
            float startUptime = (((float)rb.getUptime())-duration)/1000;
            
            fos.write(String.format("%.3f", startUptime)+": [pause detected, {" + (duration - sleepTime) + "," + duration + "}, at "+longToTime(ts)  + "] "+
                    "[Times: user=0.00 sys=0.00, real="+ String.format("%.3f", ((float)duration - sleepTime/2)/1000)+ " secs]\n");
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException ex) {
            }
        }

    }

    @Override
    public void run() {
        while (!this.isInterrupted()) {
            long stTime = System.currentTimeMillis();
            long msS = sleepMS.get();
            try {
                Thread.sleep(msS);
            } catch (InterruptedException ex) {
                continue;
            }
            long duration = System.currentTimeMillis() - stTime;
            if (duration > msS + longPauseMS.get()) {
                writePauseToFile(stTime, duration, msS);

                numLongPauses.incrementAndGet();
                latestLongPause.set(duration);
                latestLongPauseTS.set(stTime);
                if (duration > longestPause.get()) {
                    longestPause.set(duration);
                    longestPauseTS.set(stTime);
                }
            }
        }
    }

    @Override
    public long getLongestPause() {
        return longestPause.get();
    }

    @Override
    public String getLongestPauseTimestamp() {
        return longToTime(longestPauseTS.get());
    }

    @Override
    public long getLatestLongPause() {
        return latestLongPause.get();
    }

    @Override
    public String getLatestLongPauseTimestamp() {
        return longToTime(latestLongPauseTS.get());
    }

    @Override
    public void resetStatistics() {
        longestPause.set(0);
        longestPauseTS.set(0);
        latestLongPause.set(0);
        latestLongPauseTS.set(0);
        numLongPauses.set(0);
        lastStatisticsResetTimestamp.set(System.currentTimeMillis());
    }

    @Override
    public long getNumLongPause() {
        return numLongPauses.get();
    }

    @Override
    public long getLongPauseMS() {
        return longPauseMS.get();
    }

    @Override
    public void setLongPauseMS(long ms) {
        longPauseMS.set(ms);
    }

    @Override
    public String getLastStatisticsResetTimestamp() {
        return longToTime(lastStatisticsResetTimestamp.get());
    }

    private static long getPDMaxPause(String args) {
        String pp = getParam("maxPause", args);
        if (pp == null) {
            return 300; // 300ms is the default
        }
        return Long.parseLong(pp);
    }

    private static long getSleepMs(String args) {
        String pp = getParam("sleepMS", args);
        if (pp == null) {
            long pz=  getPDMaxPause( args);
            long slp = pz/10; // set sleep time to pause /10 
            if (slp>50) slp =50;
            return slp; // 300ms is the default
        }
        return Long.parseLong(pp);
    }

    private static String getPDLogFile(String args) {
        return getParam("logFile", args);

    }

    private static String getParam(String name, String args) {
        Pattern pat = Pattern.compile(name + "=([^,]*)");
        Matcher mat = pat.matcher(args);

        if (mat.find()) {
            System.out.println("Param:" + name + "=" + mat.group(1));
            return mat.group(1);

        }
        return null;
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        System.out.println("Loading agent agentargs=" + agentArgs);


        if (start(getPDMaxPause(agentArgs), getPDLogFile(agentArgs), getSleepMs(agentArgs))) {
            System.out.println("Agent loaded successfuly");
        } else {
            System.out.println("Agent already loaded");
        }
    }

    public static void premain(String agentArgs, Instrumentation inst) {
        agentmain(agentArgs, inst);

    }

    String longToTime(long t) {
        return new Date(t).toString();
    }
}
