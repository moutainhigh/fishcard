package com.boxfishedu.card.fixes.entity.jpa;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by LuoLiBing on 16/3/11.
 */
@Table
@Entity
@Data
public class CourseSchedule {

    /**
     * 没有分配老师的ID
     */
    public final static Integer NO_ASSIGN_TEACHER_ID = 0;

    @Id
    @GeneratedValue
    private Long id;

    private Long studentId;

    private Long teacherId;

    @Column(name = "timeslots_id")
    private Integer timeSlotId;

    @Column(name = "course_id", nullable = true, length = 255)
    private String courseId;

    /**
     * 10:老师已选,20:学生已选,30:已推课,40:已上课,50:已过期,60:已作废
     */
    private Integer status = 10;

    @Column(name = "workorder_id")
    private Long workorderId;

    @Temporal(TemporalType.DATE)
    private Date classDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false)
    private Date createTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false)
    private Date updateTime = new Date();
    
    @Column(name = "course_name", nullable = true, length = 255)
    private String courseName;

    @Column(name = "role_id", nullable = true)
    private Integer roleId;

    @Column(name = "course_type")
    private String courseType;

    //不为1的时候只看sku_id
    @Column(name = "sku_id_extra", nullable = true)
    private Integer skuIdExtra;

    @Column(name = "is_freeze", nullable = true)
    private Integer isFreeze;

//    @Column(name="schedule_type")
//    private Integer scheduleType;
}