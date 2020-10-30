package com.ruozedata.ruozeplatformweb.id;

import java.io.Serializable;

public class MemberSexPK implements Serializable {

    private String sex;


    private Long cnts;


    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public Long getCnts() {
        return cnts;
    }

    public void setCnts(Long cnts) {
        this.cnts = cnts;
    }
}
