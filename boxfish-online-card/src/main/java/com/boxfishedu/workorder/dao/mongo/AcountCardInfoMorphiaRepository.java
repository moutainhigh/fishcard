package com.boxfishedu.workorder.dao.mongo;

import com.boxfishedu.workorder.common.bean.AccountCourseBean;
import com.boxfishedu.workorder.common.bean.AccountCourseEnum;
import com.boxfishedu.workorder.common.bean.ComboTypeEnum;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.entity.mongo.AccountCardInfo;
import com.boxfishedu.workorder.entity.mongo.ContinousAbsenceRecord;
import com.sun.media.jfxmedia.logging.Logger;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateResults;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Created by hucl on 16/9/24.
 */
@Component
public class AcountCardInfoMorphiaRepository {
    @Autowired
    protected Datastore datastore;

    private org.slf4j.Logger logger= LoggerFactory.getLogger(this.getClass());

    public AccountCardInfo queryByStudentId(Long studentId){
        Query<AccountCardInfo> query = datastore.createQuery(AccountCardInfo.class);
        query.and(query.criteria("studentId").equal(studentId));
        return query.get();
    }

    public void save(AccountCardInfo accountCardInfo) {
        datastore.save(accountCardInfo);
    }

    public AccountCardInfo initCardInfo(Long studentId, AccountCourseBean accountCourseBean,AccountCourseEnum accountCourseEnum){
        AccountCardInfo accountCardInfo =new AccountCardInfo();
        accountCardInfo.setStudentId(studentId);
        accountCardInfo.setCreateTime(new Date());
        accountCardInfo.setUpdateTime(null);
        switch (accountCourseEnum){
            case CHINESE:
                accountCardInfo.setChinese(accountCourseBean);
                break;
            case FOREIGN:
                accountCardInfo.setForeign(accountCourseBean);
                break;
            case CRITIQUE:
                accountCardInfo.setComment(accountCourseBean);
                break;
            default:
                break;
        }
        this.save(accountCardInfo);
        return accountCardInfo;
    }

    public AccountCardInfo initChnAndFrnCardInfo(Long studentId, AccountCourseBean chineseCourseBean,AccountCourseBean foreignCourseBean){
        AccountCardInfo accountCardInfo =new AccountCardInfo();
        accountCardInfo.setStudentId(studentId);
        accountCardInfo.setChinese(chineseCourseBean);
        accountCardInfo.setForeign(foreignCourseBean);
        accountCardInfo.setCreateTime(new Date());
        accountCardInfo.setUpdateTime(null);
        this.save(accountCardInfo);
        return accountCardInfo;
    }

    public void saveOrUpdateChAndFrn(Long studentId, AccountCourseBean chineseCourseBean, AccountCourseBean foreignAccountBean) {
        Query<AccountCardInfo> updateQuery = datastore.createQuery(AccountCardInfo.class);
        UpdateOperations<AccountCardInfo> updateOperations = datastore.createUpdateOperations(AccountCardInfo.class);

        updateQuery.and(updateQuery.criteria("studentId").equal(studentId));

        updateOperations.set("chinese", chineseCourseBean);
        updateOperations.set("foreign", foreignAccountBean);
        updateOperations.set("updateTime",new Date());

        UpdateResults updateResults = datastore.updateFirst(updateQuery, updateOperations);
        if (updateResults.getUpdatedCount() < 1) {
            AccountCardInfo accountCardInfo = queryByStudentId(studentId);
            if (null == accountCardInfo) {
                logger.info("@AcountCardInfoMorphiaRepository#update#null#开始生成数据,用户[{}]", studentId);
                this.initChnAndFrnCardInfo(studentId, chineseCourseBean, foreignAccountBean);
            } else {
                logger.error("@updateCourseAbsenceNum更新课程信息失败,用户[{}]", studentId);
                throw new BusinessException("更新课程信息失败");
            }
        }
    }

    public void updateCommentLeftAmount(Long studentId,Integer leftAmount){
        Query<AccountCardInfo> updateQuery = datastore.createQuery(AccountCardInfo.class);
        UpdateOperations<AccountCardInfo> updateOperations = datastore.createUpdateOperations(AccountCardInfo.class);

        updateQuery.and(updateQuery.criteria("studentId").equal(studentId));
        AccountCardInfo accountCardInfo= queryByStudentId(studentId);
        accountCardInfo.getForeign().setLeftAmount(leftAmount);
        updateOperations.set("comment", accountCardInfo.getForeign());

        UpdateResults updateResults = datastore.updateFirst(updateQuery, updateOperations);
        if(updateResults.getUpdatedCount()<1){
            logger.error("updateCommentLeftAmount#coment#更新次数失败用户[{}]",studentId);
        }
    }

    public void saveOrUpdate(Long studentId, AccountCourseBean accountCourseBean, AccountCourseEnum accountCourseEnum){
        logger.debug("@AcountCardInfoMorphiaRepository#saveOrUpdate#用户[{}],类型[{}]",studentId,accountCourseEnum.toString());
        Query<AccountCardInfo> updateQuery = datastore.createQuery(AccountCardInfo.class);
        UpdateOperations<AccountCardInfo> updateOperations = datastore.createUpdateOperations(AccountCardInfo.class);

        updateQuery.and(updateQuery.criteria("studentId").equal(studentId));

        switch (accountCourseEnum){
            case CHINESE:
                updateOperations.set("chinese",accountCourseBean);
                break;
            case FOREIGN:
                updateOperations.set("foreign",accountCourseBean);
                break;
            case CRITIQUE:
                updateOperations.set("comment",accountCourseBean);
                break;
            default:
                break;
        }
        updateOperations.set("updateTime",new Date());
        UpdateResults updateResults = datastore.updateFirst(updateQuery, updateOperations);
        if (updateResults.getUpdatedCount() < 1) {
            AccountCardInfo accountCardInfo=queryByStudentId(studentId);
            if(null==accountCardInfo){
                logger.info("@AcountCardInfoMorphiaRepository#update#null#开始生成数据,用户[{}],类型:[{}].参数[{}]",studentId,accountCourseEnum.toString(),accountCourseBean);
                this.initCardInfo(studentId,accountCourseBean,accountCourseEnum);
            }
            else{
                logger.error("@updateCourseAbsenceNum更新课程信息失败,用户[{}],类型:[{}].参数[{}]",studentId,accountCourseEnum.toString(),accountCourseBean);
                throw new BusinessException("更新课程信息失败");
            }
        }
    }


}
