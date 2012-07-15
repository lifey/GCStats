/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.performizeit.gcstats.demo;

import java.util.ArrayList;

/**
 *
 * @author yadidh
 */
public class BaseObject {
    ArrayList<BaseObject> refs = new ArrayList();
    long objectID;
    long val;

    BaseObject(long oId,long val) {
        objectID = oId;
        this.val = val;
    }

    Long getId() {
        return objectID;
    }
    void addRef(BaseObject bo) {

        refs.add(bo);
    }

    Iterable<BaseObject> getList() {
        return refs;
    }

    long getVal() {
        return val;
    }

    void addVal(int add) {
        val += add;
    }
 
}
