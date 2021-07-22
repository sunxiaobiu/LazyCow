package tinker.sample.android.model;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class TestClassFile {

    private Class c;
    private boolean hasBeforeClass = false;
    private boolean hasBefore = false;
    private boolean hasAfter = false;
    private boolean hasAfterClass = false;

    private Method beforeClassMethod;
    private Method beforeMethod;
    private Method afterMethod;
    private Method afterClassMethod;

    private List<Method> testMethodList = new ArrayList<>();

    public List<Method> getTestMethodList() {
        return testMethodList;
    }

    public void setTestMethodList(List<Method> testMethodList) {
        this.testMethodList = testMethodList;
    }

    public Class getC() {
        return c;
    }

    public void setC(Class c) {
        this.c = c;
    }

    public boolean isHasBeforeClass() {
        return hasBeforeClass;
    }

    public void setHasBeforeClass(boolean hasBeforeClass) {
        this.hasBeforeClass = hasBeforeClass;
    }

    public boolean isHasBefore() {
        return hasBefore;
    }

    public void setHasBefore(boolean hasBefore) {
        this.hasBefore = hasBefore;
    }

    public boolean isHasAfter() {
        return hasAfter;
    }

    public void setHasAfter(boolean hasAfter) {
        this.hasAfter = hasAfter;
    }

    public boolean isHasAfterClass() {
        return hasAfterClass;
    }

    public void setHasAfterClass(boolean hasAfterClass) {
        this.hasAfterClass = hasAfterClass;
    }

    public Method getBeforeClassMethod() {
        return beforeClassMethod;
    }

    public void setBeforeClassMethod(Method beforeClassMethod) {
        this.beforeClassMethod = beforeClassMethod;
    }

    public Method getBeforeMethod() {
        return beforeMethod;
    }

    public void setBeforeMethod(Method beforeMethod) {
        this.beforeMethod = beforeMethod;
    }

    public Method getAfterMethod() {
        return afterMethod;
    }

    public void setAfterMethod(Method afterMethod) {
        this.afterMethod = afterMethod;
    }

    public Method getAfterClassMethod() {
        return afterClassMethod;
    }

    public void setAfterClassMethod(Method afterClassMethod) {
        this.afterClassMethod = afterClassMethod;
    }
}
