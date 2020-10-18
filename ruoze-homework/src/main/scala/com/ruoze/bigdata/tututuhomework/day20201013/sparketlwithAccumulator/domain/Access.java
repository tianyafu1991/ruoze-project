package com.ruoze.bigdata.tututuhomework.day20201013.sparketlwithAccumulator.domain;

public class Access {


    /**
     * ip
     */
    private String ip;
    /**
     * 代理IP
     */
    private String proxyIp;
    /**
     * 响应时间
     */
    private Long responseTime;

    /**
     * referer
     */
    private String referer;
    /**
     * 请求类型
     */
    private String method;
    /**
     * 请求url
     */
    private String url;
    /**
     * http状态码
     */
    private String httpCode;

    /**
     * 请求大小
     */
    private Long requestSize;
    /**
     * 响应大小
     */
    private Long responseSize;

    /**
     * 命中缓存的状态
     */
    private String cache;
    /**
     * UA头
     */
    private String uaHead;
    /**
     * 文件类型
     */
    private String type;
    /**
     * 年
     */
    private String year;
    /**
     * 月
     */
    private String month;
    /**
     * 日
     */
    private String day;
    /**
     * 省
     */
    private String province;
    /**
     * 市
     */
    private String city;
    /**
     * 运营商
     */
    private String isp;

    private String http;
    /**
     * 域名
     */
    private String domain;
    /**
     * 访问路径
     */
    private String path;
    /**
     * 参数
     */
    private String params;

    public Access() {
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getProxyIp() {
        return proxyIp;
    }

    public void setProxyIp(String proxyIp) {
        this.proxyIp = proxyIp;
    }

    public Long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Long responseTime) {
        this.responseTime = responseTime;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHttpCode() {
        return httpCode;
    }

    public void setHttpCode(String httpCode) {
        this.httpCode = httpCode;
    }

    public Long getRequestSize() {
        return requestSize;
    }

    public void setRequestSize(Long requestSize) {
        this.requestSize = requestSize;
    }

    public Long getResponseSize() {
        return responseSize;
    }

    public void setResponseSize(Long responseSize) {
        this.responseSize = responseSize;
    }

    public String getCache() {
        return cache;
    }

    public void setCache(String cache) {
        this.cache = cache;
    }

    public String getUaHead() {
        return uaHead;
    }

    public void setUaHead(String uaHead) {
        this.uaHead = uaHead;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getIsp() {
        return isp;
    }

    public void setIsp(String isp) {
        this.isp = isp;
    }

    public String getHttp() {
        return http;
    }

    public void setHttp(String http) {
        this.http = http;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    @Override
    public String toString() {
        return ip + "\t" +
                proxyIp + "\t" +
                responseTime +"\t" +
                referer + "\t" +
                method + "\t" +
                url + "\t" +
                httpCode + "\t" +
                requestSize + "\t" +
                responseSize + "\t" +
                cache + "\t" +
                uaHead + "\t" +
                type + "\t" +
                province + "\t" +
                city + "\t" +
                isp + "\t" +
                http + "\t" +
                domain + "\t" +
                path + "\t" +
                params + "\t" +
                year + "\t" +
                month + "\t" +
                day;
    }
}
