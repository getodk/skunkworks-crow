package org.odk.share.dto;

/**
 * Created by laksh on 6/13/2018.
 */

public class TransferInstance {
    public static final String ID = "_id";
    public static final String REVIEWED = "isReviewed";
    public static final String INSTRUCTIONS = "instructions";
    public static final String INSTANCE_ID = "instanceId";
    public static final String TRANSFER_STATUS = "transferStatus";
    public static final String LAST_STATUS_CHANGE_DATE = "lastStatusChangeDate";

    public static final String STATUS_FORM_RECEIVE = "receive";
    public static final String STATUS_FORM_SENT = "sent";

    private Long id;
    private int isReviewed;
    private String instructions;
    private Long instanceId;
    private String transferStatus;
    private Long lastStatusChangeDate;
    private Instance instance;

    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getReviewed() {
        return isReviewed;
    }

    public void setReviewed(Integer reviewed) {
        isReviewed = reviewed;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public String getTransferStatus() {
        return transferStatus;
    }

    public void setTransferStatus(String transferStatus) {
        this.transferStatus = transferStatus;
    }

    public Long getLastStatusChangeDate() {
        return lastStatusChangeDate;
    }

    public void setLastStatusChangeDate(Long lastStatusChangeDate) {
        this.lastStatusChangeDate = lastStatusChangeDate;
    }
}