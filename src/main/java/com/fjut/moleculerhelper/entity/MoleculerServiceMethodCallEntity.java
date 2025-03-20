package com.fjut.moleculerhelper.entity;

import java.util.List;

public class MoleculerServiceMethodCallEntity {
    // 方法名
    private String methodName;

    // 方法所在文件路径
    private String filePath;

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getFilePath() {
        return filePath;
    }

    public MoleculerServiceMethodCallEntity(String methodName, String filePath) {
        this.methodName = methodName;
        this.filePath = filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public MoleculerServiceMethodCallEntity(String filePath) {
        this.filePath = filePath;
    }

    public MoleculerServiceMethodCallEntity() {
    }

    @Override
    public String toString() {
        return "MoleculerServiceMethodCallEntity{" +
                "methodName='" + methodName + '\'' +
                ", filePath='" + filePath + '\'' +
                '}';
    }
}
