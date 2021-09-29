package tinker.sample.android.model;

import org.json.JSONObject;

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

    public String toString() {
        String dispatchStrategy = "";
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("deviceId", getDeviceId());
            jsonObject.put("startId", getStartId());
            jsonObject.put("endId", getEndId());
            jsonObject.put("batchSize", getBatchSize());
            dispatchStrategy = jsonObject.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dispatchStrategy;
    }

}
