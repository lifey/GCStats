/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.performizeit.gcstats.gclogparser;

import com.performizeit.jmxsupport.OSUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

/**
 *
 * @author yadidh
 */
public class CMSFixer {

    public static void main(String[] args) throws IOException {
        String log = OSUtil.readStream(System.in);
        System.out.println(fixCMS(log));

    }
    public static String fixCMS(String src) throws IOException {
        StringWriter dst = new StringWriter();
                BufferedReader r = new BufferedReader(new StringReader(src));
        String prevLine = null;
        while (true) {
            String curLine = r.readLine();
            if (curLine == null) {
                dst.append(prevLine);
                break;
            }
            if (curLine.startsWith(":")) {
                String token = "[ParNew";
                int breakPoint = prevLine.indexOf(token);
                if (breakPoint != -1) {
                    breakPoint += token.length();
                    String parNewStart = prevLine.substring(0, breakPoint);
                    String CMSshit = prevLine.substring(breakPoint);
                    prevLine = parNewStart + curLine;
                    curLine = CMSshit;
                }

            }
            if (curLine.startsWith(" CMS: abort preclean due to time")) {
                int breakPos = curLine.indexOf(" [Times:");
                String part1 = curLine.substring(0,breakPos);
                String part2 = curLine.substring(breakPos);
                curLine= "[" + part1 + "]" + part2;
            }
            if (prevLine != null)//initially it is null
             dst.append(prevLine+"\n");
            prevLine = curLine;
        }
        return dst.toString();
        
    }
}
