package com.ruozedata.ruozeplatform.domain.cluster;

import javax.persistence.*;

@Entity
@Table(name = "ruozedata_platform_hdfs_summary")
public class HDFSSummary extends BaseEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long total;

    private Long dfsUsed;

    private Float percentUsed;

    private Long dfsFree;

    private Long nonDfsUsed;

    private Long totalFiles;

    private Long totalBlocks;

    private Long missingBlocks;

    private Integer liveDataNodeNums;

    private Integer deadDataNodeNums;

    private Long volumeFailuresTotal;

    public HDFSSummary() {
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Long getDfsUsed() {
        return dfsUsed;
    }

    public void setDfsUsed(Long dfsUsed) {
        this.dfsUsed = dfsUsed;
    }

    public Float getPercentUsed() {
        return percentUsed;
    }

    public void setPercentUsed(Float percentUsed) {
        this.percentUsed = percentUsed;
    }

    public Long getDfsFree() {
        return dfsFree;
    }

    public void setDfsFree(Long dfsFree) {
        this.dfsFree = dfsFree;
    }

    public Long getNonDfsUsed() {
        return nonDfsUsed;
    }

    public void setNonDfsUsed(Long nonDfsUsed) {
        this.nonDfsUsed = nonDfsUsed;
    }

    public Long getTotalFiles() {
        return totalFiles;
    }

    public void setTotalFiles(Long totalFiles) {
        this.totalFiles = totalFiles;
    }

    public Long getTotalBlocks() {
        return totalBlocks;
    }

    public void setTotalBlocks(Long totalBlocks) {
        this.totalBlocks = totalBlocks;
    }

    public Long getMissingBlocks() {
        return missingBlocks;
    }

    public void setMissingBlocks(Long missingBlocks) {
        this.missingBlocks = missingBlocks;
    }

    public Integer getLiveDataNodeNums() {
        return liveDataNodeNums;
    }

    public void setLiveDataNodeNums(Integer liveDataNodeNums) {
        this.liveDataNodeNums = liveDataNodeNums;
    }

    public Integer getDeadDataNodeNums() {
        return deadDataNodeNums;
    }

    public void setDeadDataNodeNums(Integer deadDataNodeNums) {
        this.deadDataNodeNums = deadDataNodeNums;
    }

    public Long getVolumeFailuresTotal() {
        return volumeFailuresTotal;
    }

    public void setVolumeFailuresTotal(Long volumeFailuresTotal) {
        this.volumeFailuresTotal = volumeFailuresTotal;
    }

    @Override
    public String toString() {
        return "HDFSSummary{" +
                "id=" + id +
                ", total=" + total +
                ", dfsUsed=" + dfsUsed +
                ", percentUsed=" + percentUsed +
                ", dfsFree=" + dfsFree +
                ", nonDfsUsed=" + nonDfsUsed +
                ", totalFiles=" + totalFiles +
                ", totalBlocks=" + totalBlocks +
                ", missingBlocks=" + missingBlocks +
                ", liveDataNodeNums=" + liveDataNodeNums +
                ", deadDataNodeNums=" + deadDataNodeNums +
                ", volumeFailuresTotal=" + volumeFailuresTotal +
                '}';
    }
}
