package com.ruozedata.ruozeplatformweb.domain;

import com.ruozedata.ruozeplatformweb.id.MemberSexPK;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Entity
@Table(name = "sex_stat")
@IdClass(MemberSexPK.class)
public class MemberSex {

    @Id
    private String sex;

    @Id
    private Long cnts;

    public MemberSex() {
    }

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

    @Override
    public String toString() {
        return "MemberSex{" +
                "sex='" + sex + '\'' +
                ", cnts=" + cnts +
                '}';
    }
}
