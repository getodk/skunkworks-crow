package org.odk.share.dto;

/**
 * Created by laksh on 8/2/2018.
 */

public class InstanceMap {
    public static final String INSTANCE_UUID = "instance_uuid";
    public static final String ID = "_id";
    public static final String INSTANCE_ID = "instanceId";

    long id;
    String uuid;
    long transferId;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public long getTransferId() {
        return transferId;
    }

    public void setTransferId(long transferId) {
        this.transferId = transferId;
    }
}
