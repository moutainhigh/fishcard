package com.boxfishedu.workorder.entity.mysql;

import com.boxfishedu.workorder.common.bean.PublicClassInfoStatusEnum;
import com.boxfishedu.workorder.common.bean.instanclass.ClassTypeEnum;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.web.param.fishcardcenetr.PublicClassBuilderParam;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by hucl on 16/12/28.
 * 小班课
 */
@Data
@Entity
@Table(name = "small_class")
public class SmallClass implements Cloneable, Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "group_name")
    private String groupName;

    @Column(name = "group_id")
    private String groupId;

    @Column(name = "chat_room_id")
    private Long chatRoomId;

    @Column(name = "course_id")
    private String courseId;

    @Column(name = "course_type")
    private String courseType;

    @Column(name = "course_name")
    private String courseName;

    @Column(name = "difficulty_level")
    private String difficultyLevel;

    private String cover;

    @Temporal(TemporalType.DATE)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+08:00")
    private Date classDate;

    @Column(name = "slot_id")
    private Integer slotId;

    @Column(name = "role_id")
    private Integer roleId;

    @Column(name = "teacher_id")
    private Long teacherId;

    @Column(name = "teacher_name")
    private String teacherName;

    @Column(name = "teacher_photo")
    private String teacherPhoto;

    private Long groupLeader;

    private Long groupLeaderCard;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;

    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;

    @Temporal(TemporalType.TIMESTAMP)
    private Date actualStartTime;

    @Temporal(TemporalType.TIMESTAMP)
    private Date actualEndTime;

    private Integer status;

    @JsonIgnore
    @Transient
    private Date reportTime;

    @JsonIgnore
    @Transient
    private Long statusReporter;

    @JsonIgnore
    @Transient
    private String writeBackDesc;

    @JsonIgnore
    @Transient
    private List<Long> allStudentIds;

    @JsonIgnore
    @Transient
    private List<WorkOrder> allCards;

    @JsonIgnore
    @Transient
    private PublicClassInfoStatusEnum classStatusEnum;

    //班级类型
    private String classType;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false)
    private Date updateTime = DateTime.now().toDate();

    @Override
    public SmallClass clone() {
        SmallClass smallClass = null;
        try {
            smallClass = (SmallClass) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new BusinessException("clone error");
        }
        return smallClass;
    }

    public SmallClass() {

    }

    public SmallClass(PublicClassBuilderParam publicClassBuilderParam) {
        this.setTeacherName(publicClassBuilderParam.getTeacherName());
        this.setTeacherId(publicClassBuilderParam.getTeacherId());
        this.setUpdateTime(new Date());
        this.setRoleId(publicClassBuilderParam.getRoleId().intValue());
        this.setClassType(ClassTypeEnum.PUBLIC.name());
        this.setClassDate(DateUtil.simpleString2Date(publicClassBuilderParam.getDate()));
        this.setSlotId(publicClassBuilderParam.getSlotId().intValue());
        this.setCreateTime(new Date());
        this.setDifficultyLevel(publicClassBuilderParam.getDifficulty());
    }

    public ClassTypeEnum getStatusEnum() {
        return ClassTypeEnum.getByName(this.getClassType());
    }
}
