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
package com.performizeit.gcstats.obs;

import javax.management.openmbean.CompositeDataSupport;


public class GCInfo {

    String gcAction;
    String gcCause;
    String gcName;
//info
    int GcThreadCount;
    long duration;
    long startTime;
    long endTime;
    long id;
    Object memoryBeforeGC;
    Object memoryAfterGC;

    public GCInfo(CompositeDataSupport data) {

        gcAction = (String) data.get("gcAction");

        gcCause = (String) data.get("gcCause");

        gcName = (String) data.get("gcName");
        CompositeDataSupport gcInfo = (CompositeDataSupport) data.get("gcInfo");
        duration = (Long)gcInfo.get("duration");
        GcThreadCount = (Integer)gcInfo.get("GcThreadCount");
        startTime = (Long)gcInfo.get("startTime");
        endTime = (Long)gcInfo.get("endTime");
        id = (Long)gcInfo.get("id");

 
    }
}
