package com.ruoze.bigdata.homework.day20201018;

public class CDHLogJava {
    private String hostname;

    private String servicename;

    private String time;

    private String logtype;

    private String loginfo;

    public CDHLogJava() {
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getServicename() {
        return servicename;
    }

    public void setServicename(String servicename) {
        this.servicename = servicename;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLogtype() {
        return logtype;
    }

    public void setLogtype(String logtype) {
        this.logtype = logtype;
    }

    public String getLoginfo() {
        return loginfo;
    }

    public void setLoginfo(String loginfo) {
        this.loginfo = loginfo;
    }

    @Override
    public String toString() {
        return "CDHLogJava{" +
                "hostname='" + hostname + '\'' +
                ", servicename='" + servicename + '\'' +
                ", time='" + time + '\'' +
                ", logtype='" + logtype + '\'' +
                ", loginfo='" + loginfo + '\'' +
                '}';
    }

}
