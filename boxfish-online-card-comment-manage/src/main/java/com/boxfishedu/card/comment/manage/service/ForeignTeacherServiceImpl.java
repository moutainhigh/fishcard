package com.boxfishedu.card.comment.manage.service;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.card.comment.manage.entity.dto.*;
import com.boxfishedu.card.comment.manage.entity.form.TeacherForm;
import com.boxfishedu.card.comment.manage.entity.jpa.CommentCardJpaRepository;
import com.boxfishedu.card.comment.manage.entity.mysql.CommentCard;
import com.boxfishedu.card.comment.manage.exception.BoxfishAsserts;
import com.boxfishedu.card.comment.manage.exception.BusinessException;
import com.boxfishedu.card.comment.manage.service.sdk.CommentCardManageSDK;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdto.DTOBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by ansel on 16/9/2.
 */
@Service
public class ForeignTeacherServiceImpl implements ForeignTeacherService{

    @Autowired
    CommentCardManageSDK commentCardManageSDK;

    @Autowired
    EntityManager entityManager;

    @Autowired
    CommentCardJpaRepository commentCardJpaRepository;

    @Autowired
    private DTOBinder dtoBinder;

    private final static Logger logger = LoggerFactory.getLogger(ForeignTeacherServiceImpl.class);


    /**
     * 冻结老师
     * @param teacherId
     */
    @Override
//    @Transactional
    public void freezeTeacherId(Long teacherId) {
        logger.info("@ForeignTeacherServiceImpl: freezing teacher's id in 'freezeTeacherId'......");
        // 验证老师能否被冻结与解冻
        validateFreezeOrUnFreeze(teacherId);
        // 将该老师未完成的点评,转移给内部账号
        List<CommentCard> commentCardList = commentCardJpaRepository.findNoAnswerCommentCardByTeacherId(teacherId);
        for(CommentCard commentCard : commentCardList) {
            // 先设置一个空的老师,生成一个新的点评
            CommentCard newCommentCard = commentCard.changeTeacher(TeacherInfo.UNKNOW);
            commentCardJpaRepository.save(newCommentCard);
            commentCardJpaRepository.save(commentCard);
            // 为新的点评请求内部账号
            InnerTeacher innerTeacher = commentCardManageSDK.getInnerTeacherId(newCommentCard);
            newCommentCard.setInnerTeacher(innerTeacher);
            commentCardJpaRepository.save(newCommentCard);
        }
        // 调用中外教管理管理冻结老师账号
        commentCardManageSDK.freezeTeacherId(teacherId);
    }

    /**
     * 解冻老师
     * @param teacherId
     */
    @Override
    @Transactional
    public void unfreezeTeacherId(Long teacherId) {
        logger.info("@ForeignTeacherServiceImpl: unfreezing teacher's id in 'unfreezeTeacherId'......");
        // 验证老师能否被冻结与解冻
        validateFreezeOrUnFreeze(teacherId);
        commentCardManageSDK.unfreezeTeacherId(teacherId);
    }

    @Override
    public JsonResultModel getTeacherOperations(Long teacherId){
        logger.info("@ForeignTeacherServiceImpl: getting teacher's operations in 'getTeacherOperations'......");
        return commentCardManageSDK.getTeacherOperations(teacherId);
    }


    /**
     * 查询老师对应点评统计
     *
     * select c.teacher_id,c.teacher_name,count(teacher_id) 收到点评总数,
     * sum(case when c.status=400 or c.status=600 then 1 else 0 end)已完成 c1,
     * sum(case when c.status=300 then 1 else 0 end)未回答 c2,
     * sum(case when c.status=500 then 1 else 0 end)超时未回答 c3
     * from comment_card c where c.teacher_id is not null
     * group by c.teacher_id
     * @param pageable
     * @param teacherForm
     * @return
     */
    @Override
    public Page<CommentTeacherInfoDto> commentTeacherPage(Pageable pageable, TeacherForm teacherForm) {
        // 获取查询sql
        StringBuilder querySb = commentTeacherSql();
        StringBuilder countSb = commentTeacherCountSql();
        Map<String, Object> parameters = new HashMap<>();
        // 设置查询条件
        setQueryOptions(querySb, countSb, teacherForm, parameters);
        // 分组条件
        querySb.append(" group by c.teacher_id");
        countSb.append(" group by c.teacher_id) t");
        if(Objects.nonNull(pageable.getSort())) {
            for (Sort.Order order : pageable.getSort()) {
                querySb.append(String.format(" order by %s %s", order.getProperty(), order.getDirection().name()));
            }
        }

        // 分页
        querySb.append(" limit ")
                .append(pageable.getPageSize() * pageable.getPageNumber())
                .append(",")
                .append(pageable.getPageSize());

        // 查询参数设置
        Query nativeQuery = entityManager.createNativeQuery(querySb.toString(), "commentTeacherInfo");
        setParameter(nativeQuery, parameters);

        Query countQuery = entityManager.createNativeQuery(countSb.toString());
        setParameter(countQuery, parameters);
        BigInteger size = (BigInteger) countQuery.getSingleResult();
        List<CommentTeacherInfoDto> resultList = dtoBinder.bindFromBusinessObjectList(CommentTeacherInfoDto.class, nativeQuery.getResultList());
        return new PageImpl<>(resultList, pageable, size.intValue());
    }

    /**
     * 未收到点评老师
     * @param pageable
     * @param teacherForm
     * @return
     */
    @Override
    public Page<NoCommentTeacherInfoDto> uncommentTeacherPage(Pageable pageable, TeacherForm teacherForm) {
        return commentCardManageSDK.getNoCommentPage(pageable, teacherForm);
    }

    /**
     * 老师点评次数设置日志
     * @param pageable
     * @param teacherId
     * @return
     */
    @Override
    public Page<CommentCountSetLog> commentCountSetLogPage(Pageable pageable, Long teacherId) {
        return commentCardManageSDK.getCommentCountSetLogPage(pageable, teacherId);
    }

    @Override
    public TeacherInfo getTeacherInfoById(Long teacherId) {
        try {
            TeacherInfo teacherInfo = commentCardManageSDK.getTeacherInfoById(teacherId);
            return Objects.isNull(teacherInfo) ? TeacherInfo.UNKNOW : teacherInfo;
        } catch (Exception e) {
            e.printStackTrace();
            return TeacherInfo.UNKNOW;
        }
    }

    @Override
    public Page<TeacherInfo> getCanCommentTeacherPage(Pageable pageable, TeacherForm teacherForm) {
        return commentCardManageSDK.getTeacherInfoPage(pageable, teacherForm);
    }

    @Override
    public Page<FreezeLogDto> getTeacherFreezeLogPage(Pageable pageable, Long teacherId) {
        return commentCardManageSDK.getTeacherFreezeLongPage(pageable, teacherId);
    }

    private StringBuilder commentTeacherCountSql() {
        StringBuilder builder = new StringBuilder(200);
        builder.append("select count(1) from (select c.teacher_id from comment_card c where c.teacher_id is not null ");
        return builder;
    }

    private StringBuilder commentTeacherSql() {
        StringBuilder builder = new StringBuilder(200);
        builder.append("select c.teacher_id teacherId,count(teacher_id) commentCount,")/*收到点评总数*/
                .append("sum(case when c.status=400 or c.status=600 then 1 else 0 end) finishCount,")/*已完成*/
                .append("sum(case when c.status=300 then 1 else 0 end) unfinishCount,")/*未回答*/
                .append("sum(case when c.status=500 then 1 else 0 end) timeoutCount ")/*超时未回答*/
                .append("from comment_card c ")
                .append("where c.teacher_id is not null ");
        return builder;
    }

    private void setQueryOptions(StringBuilder queryBuilder, StringBuilder countQueryBuilder,
                                 TeacherForm teacherForm, Map<String, Object> parameters) {
        // 老师Id不为空
        if(Objects.nonNull(teacherForm.getTeacherId())) {
            queryBuilder.append("and c.teacher_id=:teacherId ");
            countQueryBuilder.append("and c.teacher_id=:teacherId ");
            parameters.put("teacherId", teacherForm.getTeacherId());
        } else if(StringUtils.isNotBlank(teacherForm.getTeacherName())) {
            List<TeacherInfo> teacherInfos = commentCardManageSDK.getTeacherListByName(teacherForm.getTeacherName());
            if(CollectionUtils.isNotEmpty(teacherInfos)) {
                String[] ids = teacherInfos.stream().map(
                        teacher -> teacher.getId().toString()).toArray((size) -> new String[size]);
                String idsStr = String.join(",", ids);
                countQueryBuilder.append("and c.teacher_id in(" + idsStr + ") ");
                queryBuilder.append("and c.teacher_id in(" + idsStr + ") ");
            } else {
                countQueryBuilder.append("and c.teacher_id=-1 ");
                queryBuilder.append("and c.teacher_id=-1 ");
            }
        }
        // 状态不为空
        if(Objects.nonNull(teacherForm.getTeacherStatus())) {

        }
    }

    private void setParameter(Query query, Map<String, Object> parameters) {
        parameters.forEach(query::setParameter);
    }

    private void validateFreezeOrUnFreeze(Long id) {
        TeacherInfo teacherInfo = commentCardManageSDK.getTeacherInfoById(id);
        BoxfishAsserts.notNull(teacherInfo, "对应老师不存在");
        if(Objects.equals(teacherInfo.getTeacherType(), 1)) {
            throw new BusinessException("内部账号不能执行冻结或者解冻!!");
        }
    }

}
