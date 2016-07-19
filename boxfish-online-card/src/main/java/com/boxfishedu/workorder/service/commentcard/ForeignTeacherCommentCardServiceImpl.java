package com.boxfishedu.workorder.service.commentcard;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.workorder.dao.jpa.CommentCardJpaRepository;
import com.boxfishedu.workorder.entity.mysql.CommentCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Created by ansel on 16/7/18.
 */
@Service
public class ForeignTeacherCommentCardServiceImpl implements ForeignTeacherCommentCardService{
    @Autowired
    CommentCardJpaRepository commentCardJpaRepository;

    private Logger logger = LoggerFactory.getLogger(ForeignTeacherCommentCardServiceImpl.class);

    @Override
    public JsonResultModel foreignTeacherCommentCardAdd(CommentCard commentCardForm) {
        Date dateNow = new Date();
        commentCardForm.setCreateTime(dateNow);
        commentCardJpaRepository.save(commentCardForm);
        logger.info("调用外教点评接口更新学生问题,其中commentCardForm="+commentCardForm);
        return new JsonResultModel();
    }

    @Override
    public JsonResultModel foreignTeacherCommentCardUpdate(CommentCard commentCardForm) {
        Date dateNow = new Date();
        commentCardForm.setUpdateTime(dateNow);
        commentCardJpaRepository.save(commentCardForm);
        logger.info("调用外教点评接口更新外教点评,其中commentCardForm="+commentCardForm);
        return new JsonResultModel();
    }

    @Override
    public Page<CommentCard> foreignTeacherCommentQuery(Pageable pageable, Long studentId) {
        logger.info("调用学生查询外教点评列表接口,其中studentId="+studentId+"pageable="+pageable);
        return commentCardJpaRepository.queryCommentCardList(pageable,studentId);
    }

    @Override
    public CommentCard foreignTeacherCommentDetailQuery(Long id) {
        logger.info("调用学生查询某条外教点评具体信息接口,其中id="+id);
        return commentCardJpaRepository.findById(id);
    }
}