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




import java.util.List;
import uk.co.flamingpenguin.jewel.cli.CommandLineInterface;
import uk.co.flamingpenguin.jewel.cli.Option;
import uk.co.flamingpenguin.jewel.cli.Unparsed;


@CommandLineInterface(application = "GCstats")
public interface GCstatsOptions {

    @Option(defaultValue = "5000", shortName = "m", description = "Set amount of time to measure in milliseconds [default:5000]")
    long getTimeToMeasure();

    @Option(defaultValue = "1", shortName = "i", description = "Number of iterations [default:1]")
    int getIterations();

    @Option(shortName = "n")
    boolean isMeasureNewGenGC();

    @Option(shortName = "o")
    boolean isMeasureOldGenGC();

    @Option(defaultToNull = true, shortName = "u", description = "Set user for remote connect [optional]")
    String getUser();

    @Option(defaultToNull = true, shortName = "p", description = "Set password for remote connect [optional]")
    String getPassword();

    @Option(defaultValue = "1000", shortName = "l", description = "Warn long waits longer than ")
    public int getLongPauseThresh();
    
    @Unparsed()
    List<String> getConectionStringList();
}
