package com.fjut.moleculerhelper.status;

import com.fjut.moleculerhelper.entity.MoleculerServiceArgsEntity;
import com.fjut.moleculerhelper.entity.MoleculerServiceMethodCallEntity;

import java.util.HashMap;
import java.util.List;

// 存储从moleculer文件中解析出来的数据，如服务名、版本号、actions等
public class MoleculerGlobalData {
    // 存储文件名到MoleculerEntity的映射
    public static HashMap<String, MoleculerServiceArgsEntity> pathToMoleculerEntity = new HashMap<>();

    // 存储服务名到MoleculerEntity的映射
    public static HashMap<String, MoleculerServiceArgsEntity> nameToMoleculerEntity = new HashMap<>();

    // 存储服务名到MoleculerServiceMethodCallEntity的映射
    public static HashMap<String, List<MoleculerServiceMethodCallEntity>> nameToMoleculerServiceMethodCallEntity = new HashMap<>();
}
