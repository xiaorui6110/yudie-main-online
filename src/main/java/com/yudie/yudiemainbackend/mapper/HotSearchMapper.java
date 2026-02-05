package com.yudie.yudiemainbackend.mapper;

import com.yudie.yudiemainbackend.model.entity.HotSearch;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yudie.yudiemainbackend.model.entity.Post;
import com.yudie.yudiemainbackend.model.vo.PictureVO;
import com.yudie.yudiemainbackend.model.vo.SpaceVO;
import com.yudie.yudiemainbackend.model.vo.UserVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
* @author xiaorui
* @description 针对表【hot_search(热门搜索记录表)】的数据库操作Mapper
* @createDate 2025-05-30 09:53:07
* @Entity com.yudie.yudiemainbackend.model.entity.HotSearch
*/
public interface HotSearchMapper extends BaseMapper<HotSearch> {

    /**
     * 批量插入或更新热门搜索
     */
    void batchInsertOrUpdate(@Param("list") List<HotSearch> hotSearchList);

    /**
     * 获取指定时间之后的热门搜索
     */
    List<HotSearch> getHotSearchAfter(@Param("type") String type,
                                      @Param("startTime") Date startTime,
                                      @Param("limit") Integer limit);

    /**
     * 搜索图片 id
     */
    @Select("SELECT id FROM picture WHERE reviewStatus = 1 AND isDelete = 0 AND spaceId IS NULL AND (name LIKE concat('%',#{keyword},'%') OR introduction LIKE concat('%',#{keyword},'%')) ORDER BY createTime")
    List<Long> searchPicture(@Param("keyword") String keyword);

    /**
     * 搜索用户 id
     */
    @Select("SELECT id FROM user WHERE isDelete = 0 AND userName LIKE concat('%',#{keyword},'%') ORDER BY createTime")
    List<Long> searchUser(@Param("keyword") String keyword);

    /**
     * 搜索空间 id
     */
    @Select("SELECT id FROM space WHERE spaceType = 1 AND isDelete = 0 AND spaceName LIKE concat('%',#{keyword},'%') ORDER BY createTime")
    List<Long> searchSpace(@Param("keyword") String keyword);

    /**
     * 搜索帖子 id
     */
    @Select("SELECT id FROM post WHERE status = 1 AND isDelete = 0 AND (title LIKE concat('%',#{keyword},'%') OR content LIKE concat('%',#{keyword},'%')) ORDER BY createTime")
    List<Long> searchPost(@Param("keyword") String keyword);


}




