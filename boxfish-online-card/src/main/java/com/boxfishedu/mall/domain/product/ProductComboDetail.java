package com.boxfishedu.mall.domain.product;

import com.boxfishedu.mall.common.BaseEntity;
import com.boxfishedu.mall.enums.TutorType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data(staticConstructor = "getInstance")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductComboDetail extends BaseEntity {

    private static final long serialVersionUID = 261627605398655200L;

    public final static Integer DEFAULT_COMBO_CYCLE = 1;

    private Long comboId;

    private Long skuId;

    private ProductSku productSku;

    private Integer skuAmount;

    /**
     * 教师类型 中外教  混合类型
     */
    private TutorType tutorType;

    private Integer productCode;

    /**
     * 套餐分组,例如1001_INTELLIGENT_FRN_64
     */
    private String comboCode;
}