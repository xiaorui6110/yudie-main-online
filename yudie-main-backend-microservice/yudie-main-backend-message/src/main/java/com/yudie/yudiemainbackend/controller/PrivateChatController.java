package com.yudie.yudiemainbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yudie.yudiemainbackend.common.BaseResponse;
import com.yudie.yudiemainbackend.common.ResultUtils;
import com.yudie.yudiemainbackend.exception.BusinessException;
import com.yudie.yudiemainbackend.exception.ErrorCode;
import com.yudie.yudiemainbackend.exception.ThrowUtils;
import com.yudie.yudiemainbackend.innerservice.user.InnerUserService;
import com.yudie.yudiemainbackend.model.dto.privatechat.PrivateChatQueryRequest;
import com.yudie.yudiemainbackend.model.entity.PrivateChat;
import com.yudie.yudiemainbackend.model.entity.User;
import com.yudie.yudiemainbackend.service.PrivateChatService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @description: 私聊实现类接口
 * @author: xiaorui
 * @date: 2025-05-31 15:28
 **/
@Slf4j
@RestController
@RequestMapping("/private_chat")
public class PrivateChatController {

    @Resource
    private InnerUserService userService;

    @Resource
    private PrivateChatService privateChatService;

    /**
     * 分页获取私聊列表
     * @param privateChatQueryRequest 私聊列表查询请求
     * @param request 请求
     * @return 分页私聊列表
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<PrivateChat>> listPrivateChatByPage(@RequestBody PrivateChatQueryRequest privateChatQueryRequest,
                                                                 HttpServletRequest request) {
        if (privateChatQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long current = privateChatQueryRequest.getCurrent();
        long size = privateChatQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<PrivateChat> privateChatPage = privateChatService.page(new Page<>(current, size),
                privateChatService.getQueryWrapper(privateChatQueryRequest, loginUser), request);
        return ResultUtils.success(privateChatPage);
    }

    /**
     * 清空未读消息数
     * @param targetUserId 目标用户ID
     * @param isSender 是否是发送者
     * @param request 请求
     * @return 是否成功
     */
    @PostMapping("/clear_unread/{targetUserId}/{isSender}")
    public BaseResponse<Boolean> clearUnreadCount(@PathVariable Long targetUserId, boolean isSender,
                                                  HttpServletRequest request) {
        ThrowUtils.throwIf(targetUserId == null || targetUserId <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        privateChatService.clearUnreadCount(loginUser.getId(), targetUserId, isSender);
        return ResultUtils.success(true);
    }

    /**
     * 更新聊天类型（好友/私信）
     * @param targetUserId 目标用户ID
     * @param isFriend 是否是好友
     * @param request 请求
     * @return 是否成功
     */
    @PostMapping("/update_type/{targetUserId}")
    public BaseResponse<Boolean> updateChatType(@PathVariable Long targetUserId,
                                                @RequestParam Boolean isFriend,
                                                HttpServletRequest request) {
        ThrowUtils.throwIf(targetUserId == null || targetUserId <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        privateChatService.updateChatType(loginUser.getId(), targetUserId, isFriend);
        return ResultUtils.success(true);
    }

    /**
     * 创建或更新私聊
     * @param targetUserId 目标用户ID
     * @param lastMessage 内容
     * @param request 请求
     * @return 私聊
     */
    @PostMapping("/create_update")
    public BaseResponse<PrivateChat> createOrUpdatePrivateChat(@RequestParam Long targetUserId,
                                                               @RequestParam(required = false) String lastMessage,
                                                               HttpServletRequest request) {
        ThrowUtils.throwIf(targetUserId == null || targetUserId <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);

        // 不能和自己私聊
        ThrowUtils.throwIf(targetUserId.equals(loginUser.getId()), ErrorCode.PARAMS_ERROR, "不能和自己私聊");

        // 检查目标用户是否存在
        User targetUser = userService.getById(targetUserId);
        ThrowUtils.throwIf(targetUser == null, ErrorCode.NOT_FOUND_ERROR, "目标用户不存在");

        PrivateChat privateChat = privateChatService.createOrUpdatePrivateChat(loginUser.getId(), targetUserId, lastMessage);
        return ResultUtils.success(privateChat);
    }

    /**
     * 删除私聊
     * @param privateChatId 私聊ID
     * @param request 请求
     * @return 是否成功
     */
    @PostMapping("/delete/{privateChatId}")
    public BaseResponse<Boolean> deletePrivateChat(@PathVariable Long privateChatId,
                                                   HttpServletRequest request) {
        ThrowUtils.throwIf(privateChatId == null || privateChatId <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        boolean result = privateChatService.deletePrivateChat(privateChatId, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 更新聊天名称
     * @param privateChatId 私聊ID
     * @param chatName 名称
     * @param request 请求
     * @return 是否成功
     */
    @PostMapping("/update_name/{privateChatId}")
    public BaseResponse<Boolean> updateChatName(@PathVariable Long privateChatId,
                                                @RequestParam String chatName,
                                                HttpServletRequest request) {
        ThrowUtils.throwIf(privateChatId == null || privateChatId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(chatName.length() > 50, ErrorCode.PARAMS_ERROR, "聊天名称过长");
        User loginUser = userService.getLoginUser(request);
        privateChatService.updateChatName(privateChatId, chatName, loginUser);
        return ResultUtils.success(true);
    }

}
