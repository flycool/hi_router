package com.kotlin.hi_router.model;

import java.util.List;

/**
 * create by max at 2020/10/23 11:52
 */

public class BottomBar {

    public int selectTab;
    public List<Tab> tabs;

    public static class Tab {
        public int size;
        public boolean enable;
        public int index;
        public String pageUrl;
        public String title;
    }
}
