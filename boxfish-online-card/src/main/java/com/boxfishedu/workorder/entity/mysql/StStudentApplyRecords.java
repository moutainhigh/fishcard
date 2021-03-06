package com.boxfishedu.workorder.entity.mysql;

import lombok.Data;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import java.util.Date;

/**
 * 指定老师上课申请表
 * Created by jiaozijun on 16/12/13.
 */

@Table
@Entity
@Data
public class StStudentApplyRecords {
    public enum ApplyStatus{
        pending,
        agree
    }
    public enum ReadStatus{
        no,
        yes
    }
    //是否有效
    public enum VALID{
        no,
        yes

    }
    public enum MatchStatus{
        un_matched,
        matched,
        wait2apply
    }
    @Id
    @GeneratedValue
    private Long id;
    private Long  studentId;
//    @Transient
//    private String studentImg ; //学生头像url
    private Date applyTime  ; //申请时间
    private Long   workOrderId ;//鱼卡id
    private Long  courseScheleId;// 课程id
    private ApplyStatus applyStatus;// '申请状态 0 待接受  1 已接受
    private Date  createTime ;
    private Date  updateTime;
    private Long  teacherId;     //  指定教师ID
    private ReadStatus isRead;
    private Integer skuId;
    private VALID valid; // 用于逻辑删除数据
    private MatchStatus matchStatus;
    @Transient
    private Integer courseNum;
    @Transient
    private Integer timeSlotId;
    @Transient
    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;
    @Transient
    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;
    @Transient
    private String studentImg;




}
