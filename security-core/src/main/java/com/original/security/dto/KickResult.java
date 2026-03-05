package com.original.security.dto;

/**
 * 会话踢出结果 DTO。
 * <p>
 * 返回踢出操作的结果信息，包括踢出的会话数量、目标用户 ID 等。
 * </p>
 *
 * @author Naulu
 * @since 0.1.0
 */
public class KickResult {

    private String userId;
    private int kickedCount;
    private String message;

    /**
     * 创建踢出结果。
     */
    public KickResult() {
    }

    /**
     * 创建踢出结果。
     *
     * @param userId 目标用户 ID
     * @param kickedCount 踢出的会话数量
     * @param message 结果消息
     */
    public KickResult(String userId, int kickedCount, String message) {
        this.userId = userId;
        this.kickedCount = kickedCount;
        this.message = message;
    }

    /**
     * 获取目标用户 ID。
     *
     * @return 用户 ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * 设置目标用户 ID。
     *
     * @param userId 用户 ID
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * 获取踢出的会话数量。
     *
     * @return 会话数量
     */
    public int getKickedCount() {
        return kickedCount;
    }

    /**
     * 设置踢出的会话数量。
     *
     * @param kickedCount 会话数量
     */
    public void setKickedCount(int kickedCount) {
        this.kickedCount = kickedCount;
    }

    /**
     * 获取结果消息。
     *
     * @return 消息
     */
    public String getMessage() {
        return message;
    }

    /**
     * 设置结果消息。
     *
     * @param message 消息
     */
    public void setMessage(String message) {
        this.message = message;
    }
}
