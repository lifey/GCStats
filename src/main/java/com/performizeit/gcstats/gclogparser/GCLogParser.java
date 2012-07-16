/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.performizeit.gcstats.gclogparser;

import com.performizeit.jmxsupport.OSUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 *
 * @author yadidh
 */
public class GCLogParser {

    public static int skipwhiteSpaces(String s, int start) {

        while (s.length() > start && (s.charAt(start) == ' ' || s.charAt(start) == '\n' || s.charAt(start) == '\r')) {
            start++;
        }
        return start;

    }

    public static int skipInBrackets(String s, int start) {
        int pos = start;
        pos = skipwhiteSpaces(s, pos);
//        System.out.println(pos);
        int nesting = 0;

        if (pos >= s.length() || s.charAt(pos) != '[') {
            return start;
        }
        nesting = 0;
        while (pos < s.length()) {

            if (nesting == 1 && s.charAt(pos) == ']') {
                break;
            }
            //System.out.println(pos + " " + s.charAt(pos) + " " + nesting);
            if (pos >= s.length()) {
                return pos;
            }
            if (s.charAt(pos) == '[') {
                nesting++;
            }
            if (s.charAt(pos) == ']') {
                nesting--;
            }
            pos++;
        }
        return pos + 1;

    }

    public static String locateNextGCEvent(String gclog) {
        Pattern beginToken = Pattern.compile("^([0-9\\.]*):");
        Matcher beginM = beginToken.matcher(gclog);
        int pos = 0;
        if (beginM.find()) {

//            System.out.print(beginM.group(1) + ":");

            int start = beginM.regionStart() + beginM.group(1).length() + 1;
            //           System.out.println(start);
            Matcher contm = null;
            pos = start;
            int posstart;
            do {
                posstart = pos;
                try {
                    pos = skipInBrackets(gclog, pos);
                } catch (Throwable t) {
                    System.out.println("!!!!!!!!!!!!!" + gclog + " " + pos);
                    throw t;
                }
                if (pos >= gclog.length()) {
                    break;
                }
                if (gclog.substring(pos).isEmpty()) {
                    break;
                }
                pos = skipwhiteSpaces(gclog, pos);
                if (gclog.substring(pos).isEmpty()) {
                    break;
                }
                contm = beginToken.matcher(gclog.substring(pos));
                // System.out.println(""+pos);
                if (posstart == pos) {
                    // we have anomaly with those lines I do not know how to solve it generically.
                    if (gclog.substring(pos).startsWith("CMS: abort preclean due to time")) {
                        pos = gclog.substring(pos).indexOf("\\[");
                    }
                }
            } while (contm != null && !contm.find() && posstart != pos);
            //         System.out.println(start + " " + pos);
            //        System.out.println();
        }
        if (pos == 0) {
            return null;
        }
        if (pos > gclog.length()) {
            return null;
        }
        return gclog.substring(0, pos);

    }

    private static ArrayList<String> getGCEventsAsString(String log) throws IOException {

        ArrayList<String> gcEvents = new ArrayList<>();
        while (true) {
            //System.out.println("bls");
            String k = locateNextGCEvent(log);
            if (k == null) {
                break;
            }
            //System.out.println("{" + k + "}");
            gcEvents.add(k);
            log = log.substring(k.length(), log.length());
        }
        return gcEvents;
    }

    private static void shortOutput(ArrayList<GCevent> gcEvents) {
        for (GCevent gcEvent : gcEvents) {
            System.out.println(
                    String.format("%.3f", gcEvent.TS) + ","
                    + String.format("%.3f", gcEvent.user) + ","
                    + String.format("%.3f", gcEvent.sys) + ","
                    + String.format("%.3f", gcEvent.real) + ","
                    + gcEvent.cause);

        }

    }

  

    private static HashMap<String, Aggr> getAggrByCause(ArrayList<GCevent> gcEvents,boolean isConc,float thresh) {
        HashMap<String, Aggr> causes = new HashMap<>();

        for (GCevent gc : gcEvents) {
            if (gc.isConcurrent()!= isConc) continue;
            Aggr causeAggr = causes.get(gc.cause);
            if (causeAggr == null) {
                causeAggr = new Aggr(thresh);
                causes.put(gc.cause, causeAggr);
            } 
            causeAggr.add(gc.real,gc.user,gc.sys);

        }
        return causes;
    }

 




    private static Aggr getAggr(ArrayList<GCevent> gcEvents, boolean isConc,float threshhold) {
        Aggr a = new Aggr(threshhold);
        for (GCevent gc : gcEvents) {
            if (gc.isConcurrent() != isConc) {
                continue;
            }
            a.add(gc.real,gc.user,gc.sys);
        }
        return a;
    }

  
    
    private static ArrayList<GCevent> getGCEvents(ArrayList<String> gcEventsStrs) {
        ArrayList<GCevent> gcEvents = new ArrayList<>();
        Pattern tsToken = Pattern.compile("^([0-9\\.]*): \\[([\\.\\-a-z A-Z\\(\\)]*)[\\[\\]:,0-9]");
        Pattern endToken = Pattern.compile(".*\\[Times: user=([0-9\\.]*) sys=([0-9\\.]*), real=([0-9\\.]*) secs\\] $");
        for (String event : gcEventsStrs) {

            Matcher tsM = tsToken.matcher(event);
            String ts = "-1";
            String phase = "";
            if (tsM.find()) {
                ts = tsM.group(1);
                phase = tsM.group(2);
            }
            for (String hardcodedPhase : GCevent.concPhases) {
                if (event.contains(hardcodedPhase)) {
                    phase = hardcodedPhase;
                }
            }
            for (String hardcodedPhase : GCevent.stwPhases) {
                if (event.contains(hardcodedPhase)) {
                    phase = hardcodedPhase;
                }
            }


            String user = "-0.0001";
            String real = "-0.0001";
            String sys = "-0.0001";
            Matcher endM = endToken.matcher(event);
            if (endM.find()) {
                user = endM.group(1);
                sys = endM.group(2);
                real = endM.group(3);
            }

            gcEvents.add(new GCevent(ts, phase, user, sys, real, event));
        }
        return gcEvents;

    }
     

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Synopsis:  GCLogParser <summary|long|short|fixCMS> [startTS] [endTS]");
            System.out.println("summary - prints summary of log");
            System.out.println("long - rewrites the log between times startTS and endTS");
            System.out.println("short - rewrites the log between times startTS and endTS in CSV format");
            System.out.println("fixCMS - rewrites the log but only fixes CMS Quirks....");
            System.exit(1);
        }
        float longThresh = 0.5f;// half a second
        String log = OSUtil.readStream(System.in);
        log = CMSFixer.fixCMS(log);
        ArrayList<String> gcEventStrs = getGCEventsAsString(log);
        ArrayList<GCevent> gcEvents = getGCEvents(gcEventStrs);
        if (args.length >= 2) {
            long startTS = Long.parseLong(args[1]);
            long endTS = 1000000000000l;
            if (args.length >= 3) {
                endTS = Long.parseLong(args[2]);
            }
            gcEvents = bound(gcEvents, startTS, endTS);
        }
        switch (args[0]) {
            case "short":
                shortOutput(gcEvents);
                break;
            case "fixCMS":
                System.out.println(log);
                break;
            case "long":
                printLong(gcEvents);
                break;
            case "summary":
                float span = gcEvents.get(gcEvents.size() - 1).TS;
                Aggr pauseAggr = getAggr(gcEvents,false, longThresh);
                Aggr concAggr = getAggr(gcEvents,true, longThresh);
                System.out.println("number Pausing GC events=" + pauseAggr.num);
                System.out.println("number Concurrent GC events=" + concAggr.num);
                System.out.println("number long(longer than 0.5 sec) GC events=" + pauseAggr.numLong);
                System.out.println("span=" + floatz( span));
                System.out.println("sum Pause=" + floatz( pauseAggr.sumReal));
                System.out.println("Availability=" + floatz( 100 * (1 - (pauseAggr.sumReal / span))) + "%");
                System.out.println(String.format("%-38s %9s %9s %6s %6s %9s","category","maxreal","sumreal","#","#long","cpu"));
                System.out.println("STW Time:                              " + pauseAggr.toString());
                HashMap<String, Aggr> causes = getAggrByCause(gcEvents,false,longThresh);
                for (String cause : causes.keySet()) {
                        System.out.println( String.format("  %-35s",cause) + ": "+causes.get(cause).toString());
                }
                System.out.println("ConcTime:                              " + concAggr.toString());
                causes = getAggrByCause(gcEvents,true,longThresh);
                for (String cause : causes.keySet()) {
                        System.out.println( String.format("  %-35s",cause) +  ": "+causes.get(cause).toString());
                }
                break;
        }
    }

    private static ArrayList<GCevent> bound(ArrayList<GCevent> gcEvents, long startTS, long length) {
        ArrayList<GCevent> partial = new ArrayList<>();
        for (GCevent gcEvent : gcEvents) {
            float ts = gcEvent.TS;
            if (ts >= startTS && ts <= startTS + length) {
                gcEvent.TS = ts - startTS;
                gcEvent.origin = String.format("%.3f", ts - startTS) + gcEvent.origin.substring(gcEvent.origin.indexOf(":"));
                partial.add(gcEvent);
            }
        }

        return partial;
    }
    public static String floatz(float f) {
        return String.format("%.3f", f);
    }

    private static void printLong(ArrayList<GCevent> gcEvents) {
        for (GCevent gcEvent : gcEvents) {
            System.out.print(gcEvent.origin);

        }


    }
}
