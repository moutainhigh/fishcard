package com.boxfishedu.mall.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * 订单渠道 含有描述
 *  智能套餐  包含 终极梦想(外教) 和 核心素养(中教)
 * @author jiaozijun
 */
public enum OrderChannelDesc {

    STANDARD("STANDARD","标准付费",""),        // 1
                                              // 2 3 4 套餐类型 都是付费既 order_channel 都是 STANDARD 类型
    OVERALL("OVERALL","核心素养",""),          // 2 combo_type :OVERALL
    INTELLIGENT("INTELLIGENT"," 考试指导",""), // 3 combo_type :INTELLIGENT  turtor_type:CN
    CHINESE("CHINESE","终极梦想",""),          // 4 (combo_type :INTELLIGENT  turtor_type:FRN) and  combo_type= CHINESE
    FOREIGN("FOREIGN","跨文化交流",""),



    EXPERIENCE("EXPERIENCE","免费体验",""),
  //  ADJUST(    "ADJUST","后台调整",""),
    EXCHANGE(  "EXCHANGE","金币兑换",""),
    UNKNOWN("UNKNOWN","未知",""),
    ALLOWANCE("ALLOWANCE","折扣订单","");

    OrderChannelDesc(){}

    private static Map<String, OrderChannelDesc> varMap = new HashMap<>();
    OrderChannelDesc(String code,String desc,String remark){
        this.code = code;
        this.desc = desc;
        this.remark =remark;
    }

    public static Map<String, OrderChannelDesc>  getVarMap(){
        return varMap;
    }

    private String code;
    private String desc;
    private String remark;

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public String getRemark() {  return remark;  }

    static {
        for (OrderChannelDesc v : OrderChannelDesc.values()) {
            varMap.put(v.getCode(), v);
        }
    }

    public static OrderChannelDesc get(String code) {
        if (varMap.containsKey(code)) {
            return varMap.get(code);
        }
        return UNKNOWN;
    }
}
