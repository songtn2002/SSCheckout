package com.example.sscheckout;

import java.util.List;

public class TabInfo {

    private String name;
    private double tab;
    private String range;

    public static int contains(List<TabInfo> tabList, String name){
        int result = -1;
        for (int i = 0; i<tabList.size(); i++){
            if (tabList.get(i).getName().equalsIgnoreCase(name)){
                result = i;
                return result;
            }
        }
        return result;
    }

    public TabInfo(String name, double tab, String range) {
        this.name = name;
        this.tab = tab;
        this.range = range;
    }

    public String getName() {
        return name;
    }

    public double getTab() {
        return tab;
    }

    public String getRange() {
        return range;
    }

    public void setTab(double tab) {
        this.tab = tab;
    }

    @Override
    public String toString() {
        return "TabInfo{" +
                "name='" + name + '\'' +
                ", tab=" + tab +
                ", range='" + range + '\'' +
                '}';
    }
}
