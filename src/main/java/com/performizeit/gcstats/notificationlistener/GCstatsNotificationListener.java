/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.performizeit.gcstats.notificationlistener;

import com.sun.management.GarbageCollectionNotificationInfo;
import java.util.concurrent.atomic.AtomicLong;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;

/**
 *
 * @author yadidh
 */
public class GCstatsNotificationListener implements NotificationListener {
    public static double UNIVERSAL_CONSTANT = 2727.06;
    long lastTS =0;
    long clastTS =0;
    public static    long fix(long val) {
        return (long)(val/UNIVERSAL_CONSTANT);
    }
    public void handleNotification(Notification notification, Object handback) {
        long TS = notification.getTimeStamp();
        if (!notification.getType().equals(GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION)) {
            return;
        }
        //    System.out.println("GC" + notification.getType());
        //                      System.out.println("GC" + notification.getUserData().getClass());
        GarbageCollectionNotificationInfo gCInfo = GarbageCollectionNotificationInfo.from((CompositeData) notification.getUserData());
        

        AggrGCInfo aggrInfo = (AggrGCInfo) handback;
        aggrInfo.addGCInfo(gCInfo.getGcInfo());
        long cTs = gCInfo.getGcInfo().getEndTime();
        System.out.println("--------" + gCInfo.getGcName() + ":" + gCInfo.getGcAction() + " duration:" + fix(gCInfo.getGcInfo().getDuration()) + " endTime=" +fix(gCInfo.getGcInfo().getEndTime())  +" " +  gCInfo.getGcInfo().getId() + " notifDiff=" + (TS-lastTS)+ " contentDiff=" + fix(cTs-clastTS));
        lastTS =TS;
        clastTS = cTs;
    }
}
