package com.yudie.yudiemainbackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 分享记录表
 * @author lenovo
 * @TableName share_record
 */
@TableName(value ="share_record")
@Data
public class ShareRecord implements Serializable {
    /**
     * 分享记录ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 被分享内容的ID
     */
    private Long targetId;

    /**
     * 内容类型：1-图片 2-帖子
     */
    private Integer targetType;

    /**
     * 被分享内容所属用户ID
     */
    private Long targetUserId;

    /**
     * 是否分享（注意 MySQL 中 tinyint(1) 类型会转换为 Boolean 类型）
     */
    private Boolean isShared;

    /**
     * 分享时间
     */
    private Date shareTime;

    /**
     * 是否已读（0-未读，1-已读）
     */
    private Integer isRead;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}