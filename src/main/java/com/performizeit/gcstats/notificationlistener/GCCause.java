/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.performizeit.gcstats.notificationlistener;

/**
 *
 * @author yadidh
 */
public class GCCause {

    String _GCCause[] = {
        "System.gc()",
        "FullGCAlot",
        "ScavengeAlot",
        "Allocation Profiler",
        "JvmtiEnv ForceGarbageCollection",
        "GCLocker Initiated GC",
        "Heap Inspection Initiated GC",
        "Heap Dump Initiated GC",
        "No GC",
        "Allocation Failure",
        "Tenured Generation Full",
        "Permanent Generation Full",
        "CMS Generation Full",
        "CMS Initial Mark",
        "CMS Final Remark",
        "Old Generation Expanded On Last Scavenge",
        "Old Generation Too Full To Scavenge",
        "Ergonomics",
        "G1 Evacuation Pause",
        "G1 Humongous Allocation",
        "Last ditch collection",
        "ILLEGAL VALUE - last gc cause - ILLEGAL VALUE"};
}
