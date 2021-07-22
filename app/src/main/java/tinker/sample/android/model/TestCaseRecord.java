package tinker.sample.android.model;

import org.json.JSONException;
import org.json.JSONObject;

public class TestCaseRecord {
    private String testCaseName;

    private String deviceId;

    private boolean isSuccess;

    private String result;

    public String getTestCaseName() {
        return testCaseName;
    }

    public void setTestCaseName(String testCaseName) {
        this.testCaseName = testCaseName;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("testCaseName", this.testCaseName);
        json.put("deviceId", this.deviceId);
        json.put("isSuccess", this.isSuccess);
        json.put("result", this.result);
        return json;
    }


}
