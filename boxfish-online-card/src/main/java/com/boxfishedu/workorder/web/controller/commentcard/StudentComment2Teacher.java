package com.boxfishedu.workorder.web.controller.commentcard;

import com.boxfishedu.workorder.servicex.CommonServeServiceX;
import com.boxfishedu.workorder.servicex.commentcard.StudentComment2TeacherServiceX;
import com.boxfishedu.workorder.web.param.Student2TeacherCommentParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by hucl on 16/7/23.
 */
@CrossOrigin
@RestController
@RequestMapping("/comment/foreign")

public class StudentComment2Teacher {

    @Autowired
    private StudentComment2TeacherServiceX studentComment2TeacherServiceX;

    @Autowired
    private CommonServeServiceX commonServeServiceX;

    @RequestMapping(value = "/student_to_teacher", method = RequestMethod.POST)
    public JsonResultModel studentCommentForTeacher(@RequestBody Student2TeacherCommentParam student2TeacherCommentParam,Long userId) {
        commonServeServiceX.checkToken(student2TeacherCommentParam.getStudentId(),userId);
        JsonResultModel jsonResultModel = new JsonResultModel();
        jsonResultModel.setData("ERROR");
        jsonResultModel.setReturnCode(500);
        jsonResultModel.setReturnMsg("不允许好评和差评同时为空!");
        if(student2TeacherCommentParam.getForGoodReviews() == null && student2TeacherCommentParam.getForBadReviews() == null){
            return jsonResultModel;
        }else if (student2TeacherCommentParam.getForGoodReviews() != null && student2TeacherCommentParam.getForBadReviews() == null){
            int len = student2TeacherCommentParam.getForGoodReviews().size();
            int sum = 0;
            for (String stringTemp:student2TeacherCommentParam.getForGoodReviews()){
                if(stringTemp == null || stringTemp.equals("")){
                    sum++;
                }
            }
            if (len == sum){
                return jsonResultModel;
            }
        }else if (student2TeacherCommentParam.getForGoodReviews() == null && student2TeacherCommentParam.getForBadReviews() != null){
            int len = student2TeacherCommentParam.getForBadReviews().size();
            int sum = 0;
            for (String stringTemp:student2TeacherCommentParam.getForBadReviews()){
                if(stringTemp == null || stringTemp.equals("")){
                    sum++;
                }
            }
            if (len == sum){
                return jsonResultModel;
            }
        }
        studentComment2TeacherServiceX.studentComment2Teacher(student2TeacherCommentParam);
        return JsonResultModel.newJsonResultModel(null);
    }
}
