package com.fjut.moleculerhelper.entity;

import java.util.List;

public class MoleculerServiceArgsEntity {
    // 服务名
    private String name;
    // 版本
    private String version;
    // 方法
    private List<String> actions;
    // 文件路径
    private String filePath;

    public MoleculerServiceArgsEntity(String name, String version, List<String> actions, String filePath) {
        this.name = name;
        this.version = version;
        this.actions = actions;
        this.filePath = filePath;
    }

    public MoleculerServiceArgsEntity() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<String> getActions() {
        return actions;
    }

    public void setActions(List<String> actions) {
        this.actions = actions;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public String toString() {
        return "MoleculerServiceArgsEntity{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", actions=" + actions +
                ", filePath='" + filePath + '\'' +
                '}';
    }
}
