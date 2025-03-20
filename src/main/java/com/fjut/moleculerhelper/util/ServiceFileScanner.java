package com.fjut.moleculerhelper.util;

import com.fjut.moleculerhelper.entity.MoleculerServiceArgsEntity;
import com.fjut.moleculerhelper.entity.MoleculerServiceMethodCallEntity;
import com.fjut.moleculerhelper.status.MoleculerGlobalData;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServiceFileScanner {
    // 服务名匹配
    private static final Pattern NAME_PATTERN = Pattern.compile("(?:name|serviceName):\\s*['\"]([^'\"]+)['\"]");
    // 版本匹配
    private static final Pattern VERSION_PATTERN = Pattern.compile("version:\\s*['\"]([^'\"]+)['\"]");
    // actions匹配
    private static final Pattern ACTIONS_PATTERN = Pattern.compile("actions:\\s*\\{");
    // 方法调用匹配
    public static final Pattern METHOD_CALL_PATTERN =
            Pattern.compile("(?:ctx\\.call|this\\.broker\\.call)\\('([^']+)'");

    // 遍历项目下的services文件夹，找到所有的service.js和service.ts文件，扫描这些文件，存储对应服务名、方法。
    public static Map<String, String> scanServiceFiles(Project project) {
        // 清空数据
        MoleculerGlobalData.nameToMoleculerEntity.clear();
        MoleculerGlobalData.pathToMoleculerEntity.clear();
        MoleculerGlobalData.nameToMoleculerServiceMethodCallEntity.clear();

        return ReadAction.compute(() -> {
            // 收集满足条件的文件
            Map<String, String> serviceMap = new HashMap<>();
            VirtualFile servicesDir = project.getBaseDir().findChild("services");
            if (servicesDir == null || !servicesDir.isDirectory()) {
                return serviceMap;
            }
            List<VirtualFile> serviceFiles = new ArrayList<>();
            collectServiceFiles(servicesDir, serviceFiles);


            // 遍历service文件，解析服务名，方法等信息
            for (VirtualFile file : serviceFiles) {
                try {
                    String content = new String(file.contentsToByteArray())
                            // 移除多行注释 /* ... */
                            .replaceAll("/\\*[\\s\\S]*?\\*/", "")
                            // 移除单行注释 // ...
                            .replaceAll("//.*?\\r?\\n", "\n");
                    String filePath = file.getPath();
                    MoleculerServiceArgsEntity moleculerServiceArgsEntity = ServiceFileScanner.parseMoleculerServiceArgs(filePath, content);
                    MoleculerGlobalData.nameToMoleculerEntity.put(moleculerServiceArgsEntity.getName(), moleculerServiceArgsEntity);
                    MoleculerGlobalData.pathToMoleculerEntity.put(filePath, moleculerServiceArgsEntity);

                    // 解析方法调用
                    parseMoleculerServiceMethodCall(content, filePath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return serviceMap;
        });
    }

    // 递归services文件夹查找以service.js和service.ts结尾的文件
    private static void collectServiceFiles(@NotNull VirtualFile dir, @NotNull List<VirtualFile> result) {
        VirtualFile[] children = ReadAction.compute(dir::getChildren);
        for (VirtualFile child : children) {
            boolean isDirectory = ReadAction.compute(child::isDirectory);
            if (isDirectory) {
                collectServiceFiles(child, result);
            } else {
                String fileName = ReadAction.compute(child::getPath);
                if (fileName != null && (fileName.endsWith("service.js") || fileName.endsWith("service.ts"))) {
                    result.add(child);
                }
            }
        }
    }


    // 解析Service文件，提取出所有方法调用
    private static void parseMoleculerServiceMethodCall(String content, String filePath) {
        Matcher matcher = METHOD_CALL_PATTERN.matcher(content);

        List<String> actions = MoleculerGlobalData.pathToMoleculerEntity.get(filePath).getActions();
        // 遍历字符，直到找到所有匹配的方法调用
        while (matcher.find()) {
            // 获取服务名
            String methodName = matcher.group(1);

            // 获取方法调用实体列表
            List<MoleculerServiceMethodCallEntity> moleculerServiceMethodCallEntities =
                    MoleculerGlobalData.nameToMoleculerServiceMethodCallEntity
                            .computeIfAbsent(methodName, k -> new ArrayList<>());

            // 从content中查询最靠近服务名的action，构建MoleculerServiceMethodCallEntity
            int distance = 0;
            MoleculerServiceMethodCallEntity moleculerServiceMethodCallEntity = new MoleculerServiceMethodCallEntity(filePath);
            for (String action : actions) {
                if (content.substring(0, matcher.start(1)).lastIndexOf(action)>distance){
                   moleculerServiceMethodCallEntity.setMethodName(action);
                }
            }
            moleculerServiceMethodCallEntities.add(moleculerServiceMethodCallEntity);
        }
    }


    // 解析service文件，提取服务名、版本、方法等信息
    private static MoleculerServiceArgsEntity parseMoleculerServiceArgs(String filePath, String content) {
            // 解析服务名
            Matcher nameMatcher = NAME_PATTERN.matcher(content);
            String name = nameMatcher.find() ? nameMatcher.group(1) : "";

            // 解析版本
            Matcher versionMatcher = VERSION_PATTERN.matcher(content);
            String version = versionMatcher.find() ? versionMatcher.group(1) : "";

            // 如果版本不为空，将版本号与服务名拼接
            if (!version.isEmpty()){
                name=version+ '.' +name;
            }

            // 解析方法
            List<String> actions = parseMoleculerServiceActions(content);
            return new MoleculerServiceArgsEntity(name, version, actions,filePath);
    }

    // 从actions中提取出所有方法名
    private static List<String> parseMoleculerServiceActions(String content) {
        Matcher matcher = ACTIONS_PATTERN.matcher(content);
        List<String> actionList = new ArrayList<>();  // 用于存储解析出的动作键名

        // 如果找到匹配的actions键
        if (matcher.find()) {
            int startIndex = matcher.end();         // 获取匹配结束位置（即 'actions: {' 之后的起始位置）
            int currentLevel = 1;                  // 当前大括号层级的计数器（已处在第一层）
            StringBuilder keyBuilder = new StringBuilder();  // 用于构建当前键名
            boolean isParsingKey = false;          // 标识是否正在解析键名

            // 遍历字符，直到最外层的大括号结束层数（当currentLevel归零时结束）
            for (int i = startIndex; i < content.length() && currentLevel > 0; i++) {
                char c = content.charAt(i);  // 获取当前字符

                // 层级计数器：遇到 '{' 增加层级，遇到 '}' 减少当前层级
                if (c == '{') currentLevel++;
                if (c == '}') currentLevel--;

                // 只在最外层大括号内（currentLevel == 1）进行解析
                if (currentLevel == 1) {
                    if (Character.isJavaIdentifierPart(c)) {
                        // 当遇到合法标识符字符（字母、数字、下划线等）时，开始/继续收集键名
                        isParsingKey = true;
                        keyBuilder.append(c);
                    } else if (isParsingKey) {
                        // 当我们已进入解析key状态，但目前字符无法作为标识符字符时：
                        if (c == ':') {
                            // 发现冒号，表示可能到达键的结尾
                            int j = i + 1;  // 开始验证冒号后的内容

                            // 寻找冒号后第一个非空字符的索引
                            while (j < content.length() && Character.isWhitespace(content.charAt(j))) {
                                j++;
                            }

                            // 验证下一个字符是否是 '{' 表示嵌套对象
                            if (j < content.length() && content.charAt(j) == '{') {
                                // 确认这是一个有效的键与嵌套对象结构（例如 key: { ... }）
                                actionList.add(keyBuilder.toString());  // 保存完整的键名
                                keyBuilder.setLength(0);   // 重置键名构建器
                                isParsingKey = false;      // 结束当前键解析
                                currentLevel++;            // 进入嵌套的新层级
                                i = j;                     // 跳过已处理的'{'位置
                            }
                        } else if (!Character.isWhitespace(c)) {
                            // 遇到非空格字符且不在键名范围内（例如逗号），视为键名已结束或无效
                            keyBuilder.setLength(0);  // 清除当前构造的键名
                            isParsingKey = false;     // 重置状态
                        }
                    }
                } else {
                    // 如果不在当前层级（currentLevel != 1），重置解析状态和键构建器
                    keyBuilder.setLength(0);
                    isParsingKey = false;
                }
            }
        }
        return actionList;
    }
}
