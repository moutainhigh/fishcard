package com.boxfishedu.workorder.entity.mysql;

import lombok.Data;

import java.util.List;

/**
 * Created by ansel on 2017/4/11.
 */
@Data
public class TeacherAppReleaseForm {
    private Integer code;

    private String message;

    private List data;
}