package com.original.security.logging;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 敏感数据脱敏工具类。
 * <p>
 * 提供针对安全日志中敏感信息（如密码、Token、手机号等）的脱敏处理。
 * 支持多种脱敏模式：完全隐藏、部分显示、不脱敏。
 * <p>
 * 线程安全：此类是不可变的。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public class SensitiveDataMasker {

    /**
     * 默认脱敏掩码。
     */
    public static final String DEFAULT_MASK = "******";

    /**
     * 脱敏模式。
     */
    public enum MaskingMode {
        /**
         * 完全脱敏（隐藏所有敏感内容）。
         */
        FULL,
        /**
         * 部分脱敏（保留部分信息，如保留 JWT 前 10 字符）。
         */
        PARTIAL,
        /**
         * 不脱敏（原样显示内容）。
         */
        NONE
    }

    private final MaskingMode maskingMode;

    /**
     * 默认构造函数，使用 PARTIAL 模式。
     */
    public SensitiveDataMasker() {
        this(MaskingMode.PARTIAL);
    }

    /**
     * 构造函数。
     *
     * @param maskingMode 脱敏模式
     */
    public SensitiveDataMasker(MaskingMode maskingMode) {
        this.maskingMode = maskingMode != null ? maskingMode : MaskingMode.PARTIAL;
    }

    /**
     * 获取当前脱敏模式。
     *
     * @return 脱敏模式
     */
    public MaskingMode getMaskingMode() {
        return maskingMode;
    }

    /**
     * JWT Token 部分可见长度。
     */
    private static final int JWT_VISIBLE_LENGTH = 10;

    /**
     * 敏感字段名称集合。
     */
    private static final Set<String> SENSITIVE_FIELD_NAMES = new HashSet<>(Arrays.asList(
            "password", "pwd", "passwd", "pass",
            "secret", "secretkey", "secret_key",
            "token", "accesstoken", "access_token", "refreshtoken", "refresh_token",
            "credential", "credentials",
            "apikey", "api_key", "key",
            "privatekey", "private_key",
            "authorization",
            "cookie",
            "sessionid", "session_id", "jsessionid"
    ));

    /**
     * JWT Token 正则模式。
     */
    private static final Pattern JWT_PATTERN = Pattern.compile(
            "^[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+$"
    );

    /**
     * 手机号正则模式。
     */
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^1[3-9]\\d{9}$"
    );

    /**
     * 邮箱正则模式。
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    );

    /**
     * 银行卡号正则模式。
     */
    private static final Pattern CARD_NUMBER_PATTERN = Pattern.compile(
            "^\\d{13,19}$"
    );

    /**
     * 身份证号正则模式。
     */
    private static final Pattern ID_CARD_PATTERN = Pattern.compile(
            "^\\d{17}[\\dXx]$"
    );

    /**
     * 判断字段是否为敏感字段。
     *
     * @param fieldName 字段名
     * @return 如果字段名匹配敏感词列表则返回 true
     */
    public boolean isSensitiveField(String fieldName) {
        if (fieldName == null) {
            return false;
        }
        String normalizedName = fieldName.toLowerCase().replaceAll("[_-]", "");
        return SENSITIVE_FIELD_NAMES.contains(normalizedName);
    }

    /**
     * 根据字段名进行自动脱敏。
     * <p>
     * 如果字段名属于敏感字段列表，则进行相应脱敏。
     *
     * @param fieldName 字段名
     * @param value     字段值
     * @return 脱敏后的值
     */
    public String mask(String fieldName, String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        if (maskingMode == MaskingMode.NONE) {
            return value;
        }
        if (isSensitiveField(fieldName)) {
            return maskByType(fieldName, value);
        }
        return value;
    }

    /**
     * 根据字段类型选择不同脱敏逻辑。
     *
     * @param fieldName 字段名
     * @param value     字段值
     * @return 脱敏后的值
     */
    private String maskByType(String fieldName, String value) {
        String lowerFieldName = fieldName.toLowerCase();

        // 密码类字段
        if (lowerFieldName.contains("pass") || lowerFieldName.contains("pwd")) {
            return maskPassword(value);
        }

        // Token 类字段
        if (lowerFieldName.contains("token")) {
            return maskJwtToken(value);
        }

        // Secret/Key 类字段
        if (lowerFieldName.contains("secret") || lowerFieldName.contains("key")) {
            return DEFAULT_MASK;
        }

        // Credential 类字段
        if (lowerFieldName.contains("credential")) {
            return DEFAULT_MASK;
        }

        // 默认脱敏
        return DEFAULT_MASK;
    }

    /**
     * 脱敏密码。
     *
     * @param password 原始密码
     * @return 脱敏后的密码
     */
    public String maskPassword(String password) {
        if (password == null || password.isEmpty()) {
            return password;
        }
        return DEFAULT_MASK;
    }

    /**
     * 脱敏 JWT Token。
     * <p>
     * PARTIAL 模式下保留前 10 字符并附加 ...
     *
     * @param token 原始 JWT Token
     * @return 脱敏后的 Token
     */
    public String maskJwtToken(String token) {
        if (token == null || token.isEmpty()) {
            return token;
        }
        if (maskingMode == MaskingMode.FULL) {
            return DEFAULT_MASK;
        }
        // PARTIAL 模式
        if (token.length() <= JWT_VISIBLE_LENGTH) {
            return DEFAULT_MASK;
        }
        String visible = token.substring(0, JWT_VISIBLE_LENGTH);
        return visible + "...";
    }

    /**
     * 脱敏手机号。
     *
     * @param phone 原始手机号
     * @return 脱敏后的手机号
     */
    public String maskPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return phone;
        }
        if (phone.length() < 7) {
            return DEFAULT_MASK;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /**
     * 脱敏邮箱。
     *
     * @param email 原始邮箱
     * @return 脱敏后的邮箱
     */
    public String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return email;
        }
        int atIndex = email.indexOf('@');
        if (atIndex < 2) {
            return DEFAULT_MASK;
        }
        String prefix = email.substring(0, Math.min(2, atIndex));
        String domain = email.substring(atIndex);
        return prefix + "***" + domain;
    }

    /**
     * 脱敏银行卡号。
     *
     * @param cardNumber 原始银行卡号
     * @return 脱敏后的银行卡号
     */
    public String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return cardNumber;
        }
        if (cardNumber.length() < 8) {
            return DEFAULT_MASK;
        }
        return cardNumber.substring(0, 4) + "****" + cardNumber.substring(cardNumber.length() - 4);
    }

    /**
     * 脱敏身份证号。
     *
     * @param idCard 原始身份证号
     * @return 脱敏后的身份证号
     */
    public String maskIdCard(String idCard) {
        if (idCard == null || idCard.isEmpty()) {
            return idCard;
        }
        if (idCard.length() < 8) {
            return DEFAULT_MASK;
        }
        return idCard.substring(0, 4) + "****" + idCard.substring(idCard.length() - 4);
    }

    /**
     * 自动识别内容类型并脱敏。
     *
     * @param data 原始数据
     * @return 脱敏后的数据
     */
    public String autoMask(String data) {
        if (data == null || data.isEmpty()) {
            return data;
        }

        // JWT Token
        if (JWT_PATTERN.matcher(data).matches()) {
            return maskJwtToken(data);
        }

        // 手机号
        if (PHONE_PATTERN.matcher(data).matches()) {
            return maskPhone(data);
        }

        // 邮箱
        if (EMAIL_PATTERN.matcher(data).matches()) {
            return maskEmail(data);
        }

        // 身份证号
        if (ID_CARD_PATTERN.matcher(data).matches()) {
            return maskIdCard(data);
        }

        // 银行卡号
        if (CARD_NUMBER_PATTERN.matcher(data).matches()) {
            return maskCardNumber(data);
        }

        // 无法识别且在敏感模式下，返回默认掩码
        return DEFAULT_MASK;
    }

    /**
     * 自定义脱敏。
     *
     * @param data   原始数据
     * @param prefix 保留前缀长度
     * @param suffix 保留后缀长度
     * @return 脱敏后的数据
     */
    public String maskCustom(String data, int prefix, int suffix) {
        if (data == null || data.isEmpty()) {
            return data;
        }
        if (data.length() <= prefix + suffix) {
            return DEFAULT_MASK;
        }
        return data.substring(0, prefix) + "****" + data.substring(data.length() - suffix);
    }
}
