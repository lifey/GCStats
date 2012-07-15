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

import com.performizeit.gcstats.obs.PauseMetric;
import java.util.LinkedList;
import javax.management.MBeanServerConnection;
import static com.performizeit.jmxsupport.JMXConnection.*;

public class PauseStats {

    public PauseStats(long errorMaxTime, MBeanServerConnection server, int interval, int samplingRate) {
        this.errorMaxTime = errorMaxTime;
        this.server = server;
        this.interval = interval;
        this.samplingRate = samplingRate;
    }
    
    long errorMaxTime;
        private final MBeanServerConnection server;
    private final int interval;
    private final int samplingRate;
        LinkedList<PauseMetric> pauseStats = new LinkedList<PauseMetric>();
            public  long singlePauseWrapper() {
        long s = System.currentTimeMillis();
        long curServerUpTime = 0;// getUptime(server);
        long e = System.currentTimeMillis();
        long pause = e - s;
        if (pause > errorMaxTime) {
            System.out.println(((float) curServerUpTime) / 1000 + " **** pause  >" + errorMaxTime + " = [" + (pause) + "," + ((pause) + samplingRate) + "]");
        }
        singlePause(curServerUpTime, pause);
        return curServerUpTime;
    }
    public  long getMaxPause() {
        long max =0;
        for (PauseMetric p : pauseStats) {
            if (p.pause > max) {
                max = p.pause;
            }
        }
        return max;
    }
        protected void removeFromPauseInterval(int interval, long now) {
        for (; pauseStats.size() > 0 && pauseStats.getFirst().measureAt < now - interval; pauseStats.removeFirst());

    }

    public  void singlePause(long curServerUpTime, long pause) {
        PauseMetric pm = new PauseMetric(curServerUpTime, pause);
        removeFromPauseInterval(interval, curServerUpTime);
        pauseStats.add(pm);
    }

}
