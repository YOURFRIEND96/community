package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface DiscussPostMapper {

    // userid用于个人主页
    List<DiscussPost> selectDiscussPosts(@Param("userId") int userId, @Param("offset")int offset, @Param("limit")int limit);

    int selectDiscussPostRows(@Param("userId") int userId);

}
