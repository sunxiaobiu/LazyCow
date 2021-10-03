package tinker.sample.android.crash;

import com.zxy.recovery.callback.RecoveryCallback;

public class MyCrashCallback implements RecoveryCallback {
    @Override
    public void stackTrace(String stackTrace) {
        System.out.println("================stackTrace=================="+stackTrace);
    }

    @Override
    public void cause(String cause) {
        System.out.println("================cause=================="+cause);
    }

    @Override
    public void exception(String throwExceptionType, String throwClassName, String throwMethodName, int throwLineNumber) {
        System.out.println("================throwExceptionType=================="+throwExceptionType);
        System.out.println("================throwClassName=================="+throwClassName);
        System.out.println("================throwMethodName=================="+throwMethodName);
        System.out.println("================throwLineNumber=================="+throwLineNumber);
    }

    @Override
    public void throwable(Throwable throwable) {

    }
}
