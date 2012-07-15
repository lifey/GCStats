/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.performizeit.gcstats.gclogparser;

/**
 *
 * @author yadidh
 */
public class GCevent {

    String cause;
    float TS;
    float sys;
    float user;
    float real;
    String origin;

    public GCevent(String TS, String cause, String user, String sys, String real, String origin) {
        this.cause = cause;
        this.TS = Float.parseFloat(TS);
        this.sys = Float.parseFloat(sys);
        this.user = Float.parseFloat(user);
        this.real = Float.parseFloat(real);
        this.origin = origin;
    }

    public  boolean isConcurrent() {
        for (String phase : concPhases) {
            if (cause.equals(phase)) {
                return true;
            }
        }
        return false;
    }
    public static final String[] stwPhases = {
        //CMS
        "ParNew", "concurrent mode failure", // CMS new gent collection + concurrent mode failure
        "CMS-initial-mark",
        "Rescan (parallel)",
        //Parallel
        "PSYoungGen", "ParOldGen",
        //G1
        "GC pause (young)",
        "GC pause (mixed)"
    };
    public static final String[] concPhases = {
        //CMS
        "CMS-concurrent-mark", "CMS-concurrent-mark-start",
        "CMS-concurrent-sweep", "CMS-concurrent-sweep-start",
        "CMS-concurrent-preclean", "CMS-concurrent-preclean-start",
        "CMS-concurrent-reset", "CMS-concurrent-reset-start",
        "CMS-concurrent-abortable-preclean",
        //G1  
        "GC concurrent-cleanup-start", "GC concurrent-cleanup-end",
        "GC concurrent-mark-start", "GC concurrent-mark-end",
        "GC concurrent-root-region-scan-start",
        "GC concurrent-root-region-scan-end"
    };
}
