
package se.kth.id2209.hw2.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class SList<E> extends ArrayList implements Serializable {

    public SList() {
        super();
    }
    
    public SList(List<E> list) {
        super(list);
    }

}
