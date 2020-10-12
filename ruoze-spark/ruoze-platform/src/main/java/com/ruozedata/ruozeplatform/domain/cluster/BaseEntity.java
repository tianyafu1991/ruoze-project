package com.ruozedata.ruozeplatform.domain.cluster;


import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class BaseEntity {

    private Integer createTime;


    private Boolean isDelete = false;

    public BaseEntity() {
    }

    public Integer getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Integer createTime) {
        this.createTime = createTime;
    }

    public Boolean getDelete() {
        return isDelete;
    }

    public void setDelete(Boolean delete) {
        isDelete = delete;
    }

    @Override
    public String toString() {
        return "BaseEntity{" +
                "createTime=" + createTime +
                ", isDelete=" + isDelete +
                '}';
    }
}
