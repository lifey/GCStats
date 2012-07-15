/*
 *
 * Copyright 2012 Performize-IT LTD.
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
package com.performizeit.gcstats.notificationlistener;


import com.performizeit.jmxsupport.JMXConnection;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import uk.co.flamingpenguin.jewel.cli.CliFactory;

public class Main {

    public static void main(String args[]) throws Exception {
        if (args.length < 1) {
            System.out.println(CliFactory.createCli(GCstatsOptions.class).getHelpMessage());
            System.out.println("Enable remote JMX:\n   -Dcom.sun.management.jmxremote\n   -Dcom.sun.management.jmxremote.port=50001\n   -Dcom.sun.management.jmxremote.authenticate=false\n   -Dcom.sun.management.jmxremote.ssl=false\n");
            System.exit(1);
        }

        GCstatsOptions opts = CliFactory.parseArguments(GCstatsOptions.class, args);
        String passwd = opts.getPassword();
        ArrayList<JMXConnection> servers = new ArrayList<>();
        for (String hostPortUser : opts.getConectionStringList()) {
            JMXConnection server;
            try {
                Integer.parseInt(hostPortUser);
                server = new JMXConnection(hostPortUser);
            } catch (NumberFormatException e) {
                server = new JMXConnection(hostPortUser, passwd);
            }
            servers.add(server);
        }

        JMXConnection server = servers.get(0);
        MBeanServerConnection serverConnection = server.getServerConnection();
        ObjectName filter = new ObjectName("*:*");
        Set<ObjectName> gcNames = serverConnection.queryNames(JMXConnection.GC, filter);

        ConcurrentHashMap<ObjectName, AggrGCInfo> aggrInfo = new ConcurrentHashMap<>();
        for (ObjectName gcName : gcNames) {
            System.out.println(gcName);
            aggrInfo.put(gcName, new AggrGCInfo());
            serverConnection.addNotificationListener(gcName, new GCstatsNotificationListener(), null, aggrInfo.get(gcName));



        }
        while (true) {
            for (ObjectName a : aggrInfo.keySet()) {
                AggrGCInfo l = aggrInfo.get(a);
                System.out.println("****** " +a.toString() + " throughput="  + l.getThroughput()*100+"% maxPause=" + l.getMaxPause());
            }
            Thread.sleep(10000);
        }
    }
}
