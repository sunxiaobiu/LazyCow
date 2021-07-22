package tinker.sample.android.util;

import android.content.Context;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestResult;

public class MyTestCase extends Assert implements Test {
    protected Context mContext;
    @Override
    public int countTestCases() {
        return 0;
    }

    @Override
    public void run(TestResult testResult) {

    }

    public Context getContext() {
        return this.mContext;
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

}
