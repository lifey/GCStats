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

import com.performizeit.gcstats.obs.GCInfo;
import java.util.Set;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeDataSupport;
import static com.performizeit.jmxsupport.JMXConnection.*;

public class GCStats {

    private final MBeanServerConnection server;
    private final int interval;
        Set<ObjectName> gcNames;

    public GCStats(MBeanServerConnection server, int interval) throws MalformedObjectNameException, IOException {
        this.server = server;
        this.interval = interval;
                ObjectName filter = new ObjectName("*:*");
        gcNames = server.queryNames(GC, filter);

        stats = new HashMap<ObjectName, LinkedList<GCInfo>>();
        for (ObjectName gcName : gcNames) {
            LinkedList<GCInfo> l = new LinkedList<GCInfo>();
            stats.put(gcName, l);
        }
    }

    protected void removeFromInterval(int interval, LinkedList<GCInfo> l, long now) {
        for (; l.size() > 0 && l.getFirst().startTime < now - interval; l.removeFirst());

    }
    HashMap<ObjectName, LinkedList<GCInfo>> stats;

    public void singleSample(long curServerUpTime) throws AttributeNotFoundException, InstanceNotFoundException, IOException, ReflectionException, MBeanException {
        for (ObjectName gcName : gcNames) {
            CompositeDataSupport lastGcInfo = (CompositeDataSupport) server.getAttribute(gcName, "LastGcInfo");
            GCInfo gInfo = new GCInfo(lastGcInfo);
            removeFromInterval(interval, stats.get(gcName), curServerUpTime);
            if (gInfo.startTime != 0 && gInfo.startTime > curServerUpTime - interval) {
                if (stats.get(gcName).size() == 0 || !gInfo.equals(stats.get(gcName).getLast())) {
                    stats.get(gcName).addLast(gInfo);
                }
            }
        }

    }
}
