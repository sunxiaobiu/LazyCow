package tinker.sample.android.model;

import java.util.Date;

public class DispatchStrategy {
    private int id;
    private String deviceId;
    private Date createTime;
    private int startId;
    private int endId;
    private int batchSize;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public int getStartId() {
        return startId;
    }

    public void setStartId(int startId) {
        this.startId = startId;
    }

    public int getEndId() {
        return endId;
    }

    public void setEndId(int endId) {
        this.endId = endId;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("id:").append(this.id).append(";")
                .append("deviceId:").append(this.deviceId).append(";")
                .append("createTime:").append(this.createTime).append(";")
                .append("startId:").append(this.startId).append(";")
                .append("endId:").append(this.endId).append(";")
                .append("batchSize:").append(this.batchSize).append(";");
        return stringBuilder.toString();
    }

}
