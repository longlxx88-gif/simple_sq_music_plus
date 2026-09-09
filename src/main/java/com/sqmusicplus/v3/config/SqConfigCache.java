package com.sqmusicplus.v3.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sqmusicplus.v3.base.entity.SqConfig;
import com.sqmusicplus.v3.base.enums.SetConfigEnum;
import com.sqmusicplus.v3.base.service.SqConfigService;
import com.sqmusicplus.v3.utils.SpringContextUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Classname SqConfigCache
 * @Description 设置缓存
 * @Version 1.0.0
 * @Date 2025/7/15 09:55
 * @Created by SQ
 */

public class SqConfigCache {
    /**
     * 全局设置缓存
     */
    public static HashMap<String, SqConfig> sqConfigMap = new HashMap<>();
    /**
     * 插件配置项
     */
    public static final List<HashMap<String, String>> PlugOptions = new CopyOnWriteArrayList<>();




    public static void setSqConfigMap(HashMap<String, SqConfig> sqConfigMap) {
        SqConfigCache.sqConfigMap = sqConfigMap;
    }

    public static void setSqConfigMap(List<SqConfig> sqConfigs) {
        if( SqConfigCache.sqConfigMap!=null){
            sqConfigMap.clear();
        }
        //制作key值对
        HashMap<String, SqConfig> configHashMap = new HashMap<>();
        for (SqConfig sqConfig : sqConfigs) {
            configHashMap.put(sqConfig.getConfigKey(), sqConfig);
        }
        setSqConfigMap(configHashMap);
    }

    /**
     * 获取配置
     * @param plugKey
     * @return
     */
    public static SqConfig getSqConfig(String plugKey) {
        return sqConfigMap.get(plugKey);
    }
    /**
     * 获取配置
     * @param setConfigEnum
     * @return
     */
    public static SqConfig getSqConfig(SetConfigEnum setConfigEnum) {
        String key = setConfigEnum.getKey();
        return sqConfigMap.get(key);
    }

    /**
     * 获取配置
     * @param setConfigEnum
     * @return
     */
    public static String getSqConfigValue(SetConfigEnum setConfigEnum) {
        try {
            String key = setConfigEnum.getKey();
            return sqConfigMap.get(key).getConfigValue();
        } catch (Exception e) {
            return "";
        }
    }
    /**
     * 删除配置
     */
    public static void removeCacheAndDBbSqConfig(SetConfigEnum setConfigEnum){
        SqConfigService bean = SpringContextUtil.getBean(SqConfigService.class);
        LambdaQueryWrapper<SqConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SqConfig::getConfigKey,setConfigEnum.getKey());
        boolean remove = bean.remove(wrapper);
        if (remove){
            sqConfigMap.remove(setConfigEnum.getKey());
        }

    }
    /**
     * 增加配置到DB
     */
    public static void addConfigToDb(SqConfig sqConfig){
        SqConfigService bean = SpringContextUtil.getBean(SqConfigService.class);
        boolean save = bean.save(sqConfig);
        if (save){
            sqConfigMap.put(sqConfig.getConfigKey(),sqConfig);
        }
    }
    /**
     * 修改配置到DB
     */
    public static void updateConfigToDb(SqConfig sqConfig){
        SqConfigService bean = SpringContextUtil.getBean(SqConfigService.class);
        boolean update = bean.update(Wrappers.<SqConfig>lambdaUpdate().eq(SqConfig::getConfigKey, sqConfig.getConfigKey()).set(SqConfig::getConfigValue, sqConfig.getConfigValue()));
        if (update){
            sqConfigMap.put(sqConfig.getConfigKey(),sqConfig);
        }
    }
    /**
     * 修改配置到DB
     */
    public static void updateConfigToDb(SetConfigEnum setConfigEnum,String  value){
        SqConfigService bean = SpringContextUtil.getBean(SqConfigService.class);
        boolean update = bean.update(Wrappers.<SqConfig>lambdaUpdate().eq(SqConfig::getConfigKey, setConfigEnum.getKey()).set(SqConfig::getConfigValue, value));
        if (update){
            SqConfig sqConfig = bean.selectByKeyAndValue(setConfigEnum.getKey());
            sqConfigMap.put(setConfigEnum.getKey(),sqConfig);
        }
    }
    /**
     * 修改配置到DB
     */
    public static void updateConfigToDb(String configKey,String  value){
        SqConfigService bean = SpringContextUtil.getBean(SqConfigService.class);
        boolean update = bean.update(Wrappers.<SqConfig>lambdaUpdate().eq(SqConfig::getConfigKey, configKey).set(SqConfig::getConfigValue, value));
        if (update){
            SqConfig sqConfig = bean.selectByKeyAndValue(configKey);
            sqConfigMap.put(configKey,sqConfig);
        }
    }

    /**
     * 添加插件选项
     */
    public static synchronized void addPlugOptions(HashMap<String, String> plugOptions) {
        String value = Objects.requireNonNull(plugOptions.get("value"), "音源编号不能为空");
        for (int i = 0; i < PlugOptions.size(); i++) {
            if (value.equals(PlugOptions.get(i).get("value"))) {
                PlugOptions.set(i, new HashMap<>(plugOptions));
                for (int j = PlugOptions.size() - 1; j > i; j--) {
                    if (value.equals(PlugOptions.get(j).get("value"))) PlugOptions.remove(j);
                }
                return;
            }
        }
        PlugOptions.add(new HashMap<>(plugOptions));
    }
    /**
     * 修改选项
     */
    public static void updatePlugOptions(HashMap<String, String> plugOptions) {
        addPlugOptions(plugOptions);
    }
    /**
     * 删除选项
     */
    public static synchronized void removePlugOptions(String value) {
        PlugOptions.removeIf(plugOption -> Objects.equals(value, plugOption.get("value")));
    }

    /**
     * 获取所有配置
     */
    public static List<SqConfig> getAllConfig() {
        Collection<SqConfig> values = sqConfigMap.values();
        return new ArrayList<>(values);
    }




}
