package com.boxfishedu.workorder.common.redis;

/**
 * Created by hucl on 16/6/20.
 */
public class CacheKeyConstant {
    public static final String NOTIFY_TEACHEZr_PREPARE_CLASS_KEY="NOTIFY_TEACHEZr_PREPARE_CLASS_KEY";

    /**   用于存储鱼卡后台 用户信息   key  user_code : value {password: , token: }     token加密之前  user_code + 时间  以-分割 **/
    public static final String FISHCARD_BACK_ORDER_USERINFO="FISHCARD_BACK_ORDER_USERINFO";
    public static final String NOTIFY_TEACHER_PREPARE_CLASS_KEY="NOTIFY_TEACHER_PREPARE_CLASS_KEY";
}
