/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.performizeit.gcstats.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author yadidh
 */
public class BaseObjectMap {

    private static BaseObjectMap instance = new BaseObjectMap();
    HashMap<Long, BaseObject> objectMap = new HashMap<>();
    AtomicLong id = new AtomicLong(0);

    static BaseObjectMap getInstance() {
        return instance;
    }

    public BaseObject createNewObject(long val) {
        BaseObject bo = new BaseObject(id.getAndIncrement(), val);
        objectMap.put(bo.getId(), bo);
        return bo;
    }

    public int getSize() {
        return objectMap.size();
    }

    public BaseObject getObj(long id) {
        return objectMap.get(id);
    }

    ArrayList<Long> getValues(ArrayList<Integer> ids) {
        ArrayList<Long> vals = new ArrayList<>();
        for (int id : ids) {
            vals.add(this.getObj(id).getVal());
        }
        return vals;
    }
}
