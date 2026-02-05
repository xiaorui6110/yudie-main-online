package com.yudie.yudiemainbackend.mapper;

import com.yudie.yudiemainbackend.model.entity.Message;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yudie.yudiemainbackend.model.vo.MessageVO;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author xiaorui
* @description 针对表【message(留言板表)】的数据库操作Mapper
* @createDate 2025-05-31 10:38:11
* @Entity com.yudie.yudiemainbackend.model.entity.Message
*/
public interface MessageMapper extends BaseMapper<Message> {
    /**
     * 查询留言板前500条
     * @return List<MessageVO>
     */
    @Select("select id,content,createTime from message order by createTime desc limit 500")
    List<MessageVO> getTop500();
}




