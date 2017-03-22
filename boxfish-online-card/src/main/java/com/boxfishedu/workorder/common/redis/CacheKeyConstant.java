package com.boxfishedu.workorder.common.redis;

/**
 * Created by hucl on 16/6/20.
 */
public class CacheKeyConstant {
    public static final String NOTIFY_TEACHEZr_PREPARE_CLASS_KEY="NOTIFY_TEACHEZr_PREPARE_CLASS_KEY";

    /**   用于存储鱼卡后台 用户信息   key  user_code : value {password: , token: }     token加密之前  user_code + 时间  以-分割 **/
    public static final String FISHCARD_BACK_ORDER_USERINFO="FISHCARD_BACK_ORDER_USERINFO";

    /**  存储抢单信息中  教师对应的鱼卡列表  **/
    public static final String FISHCARD_WORKORDER_GRAB_KEY="FISHCARD_WORKORDER_GRAB_KEY";

    public static final String NOTIFY_TEACHER_PREPARE_CLASS_KEY="NOTIFY_TEACHER_PREPARE_CLASS_KEY";

    public static final String TIME_LIMIT_RULES_CACHE_KEY="TIME_LIMIT_RULES_CACHE_KEY";

    public static final String WORKORDERS_REPEATED_SUBMISSION = "WORKORDERS_REPEATED_SUBMISSION";

    public static final String ASSIGN_REPEATED_SUBMISSION = "ASSIGN_REPEATED_SUBMISSION";

    public static final String WORKORDERS_INSTANT_CLASS = "INSTANT_CLASS";

    public static final String BASE_TIME_SLOTS = "BASE_TIME_SLOTS";

    // 小班课
    public static final String BASE_TIME_SLOTS_SMALL_CLASS = "BASE_TIME_SLOTS_SMALL_CLASS";

    public static final String SCHEDULE_HAS_MORE_HISTORY = "SCHEDULE_HAS_MORE_HISTORY";

    public static final String COMMENT_CARD_AMOUNT = "COMMENT_CARD_AMOUNT";

    // 公开课课堂, 通过LEVEL和日期查询课堂
    public static final String PUBLIC_CLASS_ROOM_WITH_LEVELANDDATE = "PUBLIC_CLASS_ROOM_WITH_LEVELANDDATE";

    // 公开课课堂, 通过id查询
    public static final String PUBLIC_CLASS_ROOM_WITH_ID = "PUBLIC_CLASS_ROOM_WITH_ID";

    public static final String SMALL_CLASS_HEART_BEAT_KEY="groupkey:";

    public static final String TEACHER_OPERATION_KEY="teacheroperation:";

    public static final String STUDENT_PICKED_KEY="group:selected:";

}
