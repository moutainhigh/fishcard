package com.boxfishedu.workorder.entity.mysql;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by oyjun on 16/2/29.
 * TODO:需要增加实际上课时间,实际结束时间;评价字段应该单独出表
 */
@Component
@Data
@Entity
@Table(name = "comment_card")
public class CommentCard {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "student_id", nullable = true)
    private Long studentId;

    @Column(name = "student_name", nullable = true, length = 20)
    private String studentName;

    @Column(name = "teacher_id", nullable = true)
    private Long teacherId;

    @Column(name = "teacher_name", nullable = true, length = 20)
    private String teacherName;

    @Column(name = "status", nullable = true)
    private Integer status;

    @Column(name = "create_time", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    @Column(name = "update_time", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;

    @Column(name = "assign_teacher_time", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date assignTeacherTime;

    @Column(name = "student_ask_time", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date studentAskTime;

    @Column(name = "teacher_answer_time", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date teacherAnswerTime;

    @Column(name = "question_id", nullable = true, length = 20)
    private Long questionId;

    @Column(name = "question_name", nullable = true, length = 255)
    private String questionName;

    @Column(name = "ask_voice_id", nullable = true, length = 20)
    private Long askVoiceId;

    @Column(name = "ask_voice_path", nullable = true, length = 255)
    private String askVoicePath;

    @Column(name = "answer_video_path", nullable = true, length = 255)
    private String answerVideoPath;

    @Column(name = "evaluation_to_teacher", nullable = true, length = 11)
    private Integer evaluationToTeacher;

    @Column(name = "course_id", nullable = true, length = 255)
    private String courseId;

    @Column(name = "course_name", nullable = true, length = 128)
    private String courseName;

    @JoinColumn(name = "service_id", referencedColumnName = "id")//设置对应数据表的列名和引用的数据表的列名
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference
    private Service service;

    @Column(name = "order_id", nullable = true)
    private Long orderId;

    @Column(name="order_code", length = 128)
    private String orderCode;
}
