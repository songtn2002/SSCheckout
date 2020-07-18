package com.example.sscheckout;

public class TabInfo {

    private String name;
    private double tab;
    private String range;

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
