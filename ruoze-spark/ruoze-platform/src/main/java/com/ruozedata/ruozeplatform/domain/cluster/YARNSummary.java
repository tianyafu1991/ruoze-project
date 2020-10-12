package com.ruozedata.ruozeplatform.domain.cluster;

import javax.persistence.*;

@Entity
@Table(name = "ruozedata_platform_yarn_summary")
public class YARNSummary extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tagQueue;

    private Long allocatedMB;

    private Long allocatedVCores;

    private Long allocatedContainers;

    private Long availableMB;

    private Long availableVCores;

    private Long reservedMB;

    private Long reservedVCores;

    private Long reservedContainers;

    private Long activeUsers;

    private Long activeApplications;

    private Long numActiveNMs;

    private Long numLostNMs;

    public YARNSummary() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTagQueue() {
        return tagQueue;
    }

    public void setTagQueue(String tagQueue) {
        this.tagQueue = tagQueue;
    }

    public Long getAllocatedMB() {
        return allocatedMB;
    }

    public void setAllocatedMB(Long allocatedMB) {
        this.allocatedMB = allocatedMB;
    }

    public Long getAllocatedVCores() {
        return allocatedVCores;
    }

    public void setAllocatedVCores(Long allocatedVCores) {
        this.allocatedVCores = allocatedVCores;
    }

    public Long getAllocatedContainers() {
        return allocatedContainers;
    }

    public void setAllocatedContainers(Long allocatedContainers) {
        this.allocatedContainers = allocatedContainers;
    }

    public Long getAvailableMB() {
        return availableMB;
    }

    public void setAvailableMB(Long availableMB) {
        this.availableMB = availableMB;
    }

    public Long getAvailableVCores() {
        return availableVCores;
    }

    public void setAvailableVCores(Long availableVCores) {
        this.availableVCores = availableVCores;
    }

    public Long getReservedMB() {
        return reservedMB;
    }

    public void setReservedMB(Long reservedMB) {
        this.reservedMB = reservedMB;
    }

    public Long getReservedVCores() {
        return reservedVCores;
    }

    public void setReservedVCores(Long reservedVCores) {
        this.reservedVCores = reservedVCores;
    }

    public Long getReservedContainers() {
        return reservedContainers;
    }

    public void setReservedContainers(Long reservedContainers) {
        this.reservedContainers = reservedContainers;
    }

    public Long getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(Long activeUsers) {
        this.activeUsers = activeUsers;
    }

    public Long getActiveApplications() {
        return activeApplications;
    }

    public void setActiveApplications(Long activeApplications) {
        this.activeApplications = activeApplications;
    }

    public Long getNumActiveNMs() {
        return numActiveNMs;
    }

    public void setNumActiveNMs(Long numActiveNMs) {
        this.numActiveNMs = numActiveNMs;
    }

    public Long getNumLostNMs() {
        return numLostNMs;
    }

    public void setNumLostNMs(Long numLostNMs) {
        this.numLostNMs = numLostNMs;
    }

    @Override
    public String toString() {
        return "YarnSummary{" +
                "id=" + id +
                ", tagQueue='" + tagQueue + '\'' +
                ", allocatedMB=" + allocatedMB +
                ", allocatedVCores=" + allocatedVCores +
                ", allocatedContainers=" + allocatedContainers +
                ", availableMB=" + availableMB +
                ", availableVCores=" + availableVCores +
                ", reservedMB=" + reservedMB +
                ", reservedVCores=" + reservedVCores +
                ", reservedContainers=" + reservedContainers +
                ", activeUsers=" + activeUsers +
                ", activeApplications=" + activeApplications +
                ", numActiveNMs=" + numActiveNMs +
                ", numLostNMs=" + numLostNMs +
                '}';
    }
}
