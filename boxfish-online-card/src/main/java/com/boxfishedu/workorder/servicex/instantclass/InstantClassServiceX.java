package com.boxfishedu.workorder.servicex.instantclass;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.joran.conditional.ElseAction;
import com.alibaba.fastjson.JSON;
import com.boxfishedu.mall.enums.TutorType;
import com.boxfishedu.workorder.common.bean.instanclass.InstantClassRequestStatus;
import com.boxfishedu.workorder.common.bean.instanclass.TeacherInstantClassStatus;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.dao.mongo.InstantClassTimeRulesMorphiaRepository;
import com.boxfishedu.workorder.entity.mongo.InstantClassTimeRules;
import com.boxfishedu.workorder.entity.mongo.TimeLimitRules;
import com.boxfishedu.workorder.service.instantclass.InstantClassService;
import com.boxfishedu.workorder.servicex.instantclass.container.ThreadLocalUtil;
import com.boxfishedu.workorder.servicex.instantclass.instantvalidator.InstantClassValidators;
import com.boxfishedu.workorder.web.param.InstantRequestParam;
import com.boxfishedu.workorder.web.param.TeacherInstantRequestParam;
import com.boxfishedu.workorder.web.result.InstantClassResult;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.spi.LoggerFactory;
import org.apache.poi.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by hucl on 16/11/3.
 */
@Component
public class InstantClassServiceX {
    private ValueOperations<String, Long> opsForValue;

    @Autowired
    private InstantClassValidators instantClassValidators;

    @Autowired
    private InstantClassService instantClassService;

    @Autowired
    private InstantClassTimeRulesMorphiaRepository instantClassTimeRulesMorphiaRepository;

    @Autowired
    private
    @Qualifier("stringLongRedisTemplate")
    RedisTemplate<String, Long> stringLongRedisTemplate;

    @Autowired
    private
    @Qualifier("teachingServiceRedisTemplate")
    StringRedisTemplate redisTemplate;

    private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());

    private String generateKey(Long studentId) {
        return "InstantClass:user:" + studentId;
    }

    private String timeRangeKey() {
        return "range:" + DateUtil.date2SimpleString(new Date());
    }

    @PostConstruct
    public void initOpsForValue() {
        opsForValue = stringLongRedisTemplate.opsForValue();
    }

    public JsonResultModel instantClass(InstantRequestParam instantRequestParam) {
        putParameterIntoThreadLocal(instantRequestParam);

        //对用户当前行为进行校验
        int validateResult = instantClassValidators.preValidate();
        if (validateResult > InstantClassRequestStatus.UNKNOWN.getCode()) {
            InstantClassRequestStatus instantStatus = InstantClassRequestStatus.getEnumByCode(validateResult);
            switch (instantStatus) {
                case NOT_IN_RANGE:
                    return JsonResultModel.newJsonResultModel(InstantClassResult
                            .newInstantClassResult(instantStatus, "实时上课会在[ " + this.timeRange()+" ]内开启,请到时间再重试~"));
                case HAVE_CLASS_IN_HALF_HOURS:
                    return JsonResultModel.newJsonResultModel(InstantClassResult
                            .newInstantClassResult(instantStatus, "您预约了[ " + DateUtil.dateTrimYear(ThreadLocalUtil.classDateIn30Minutes.get()) +" ]的课程,马上就开始了,此时不能实时上课~"));
                case MATCHED_LESS_THAN_30MINUTES:
                    return JsonResultModel.newJsonResultModel(InstantClassResult
                            .newInstantClassResult(ThreadLocalUtil.instantCardMatched30Minutes.get(),InstantClassRequestStatus.MATCHED));
                default:
                    return JsonResultModel.newJsonResultModel(InstantClassResult
                            .newInstantClassResult(instantStatus));
            }
        }
        return JsonResultModel.newJsonResultModel(instantClassService.getMatchResult());
    }


    private void putParameterIntoThreadLocal(InstantRequestParam instantRequestParam) {
        if (StringUtils.isEmpty(instantRequestParam.getTutorType())) {
            instantRequestParam.setTutorType(TutorType.FRN.toString());
        }
        ThreadLocalUtil.instantRequestParamThreadLocal.set(instantRequestParam);
    }

    public String timeRange() {
        String timeDesc;
        try {
            timeDesc = redisTemplate.opsForValue().get(timeRangeKey());
            if (StringUtils.isNotEmpty(timeDesc)) {
                return timeDesc;
            }
        } catch (Exception ex) {
            logger.error("从redis获取当天可上课时间片失败，从mongo获取");
        }
        Optional<List<InstantClassTimeRules>> instantClassTimeRulesList = instantClassTimeRulesMorphiaRepository.getByDay(DateUtil.date2SimpleString(new Date()));
        if (!instantClassTimeRulesList.isPresent()) {
            return StringUtils.EMPTY;
        }
        timeDesc = this.timeDesc(this.getSortedTimeRulesList(instantClassTimeRulesList.get()));
        redisTemplate.opsForValue().setIfAbsent(this.timeRangeKey(), timeDesc);
        return timeDesc;
    }

    public List<InstantClassTimeRules> getSortedTimeRulesList(List<InstantClassTimeRules> rawTimeRules) {
        return rawTimeRules.stream().sorted(Comparator.comparing(instantClassTimeRules ->
                DateUtil.String2Date(String.join(" ", DateUtil.date2SimpleString(new Date()), instantClassTimeRules.getBegin())).getTime()
        )).collect(Collectors.toList());
    }

    public String timeDesc(List<InstantClassTimeRules> rawTimeRules) {
        List<String> timeStringRules = rawTimeRules.stream()
                .map(timeLimitRule -> String.join("-", timeLimitRule.getBegin().substring(0, 5), timeLimitRule.getEnd().substring(0, 5)))
                .collect(Collectors.toList());
        return StringUtils.join(timeStringRules, " & ");
    }
}
