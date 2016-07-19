package com.boxfishedu.workorder.dao.jpa;

import com.boxfishedu.workorder.entity.mysql.CommentCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Created by ansel on 16/7/19.
 */
public interface CommentCardJpaRepositoryCustom {

    public Page<CommentCard> queryCommentCardList(Pageable pageable,Long studentId);
}
