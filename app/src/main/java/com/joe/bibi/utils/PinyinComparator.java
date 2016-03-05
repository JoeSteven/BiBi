package com.joe.bibi.utils;

import com.joe.bibi.domain.Contacts;

import java.util.Comparator;

/**
 * Created by Joe on 2016/3/4.
 */
public class PinyinComparator implements Comparator<Contacts> {

    public int compare(Contacts o1, Contacts o2) {
        if (o1.getSortLetters().equals("@")
                || o2.getSortLetters().equals("#")) {
            return -1;
        } else if (o1.getSortLetters().equals("#")
                || o2.getSortLetters().equals("@")) {
            return 1;
        } else {
            return o1.getSortLetters().compareTo(o2.getSortLetters());
        }
    }

}
