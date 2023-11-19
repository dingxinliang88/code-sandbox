package com.juzi.codesandbox.model;

import lombok.Data;

/**
 * 程序执行信息
 *
 * @author codejuzi
 */
@Data
public class JudgeInfo {
    /**
     * 程序执行信息
     */
    private String message;

    /**
     * 消耗时间(ms)
     */
    private Long time;

    /**
     * 消耗内存(KB)
     */
    private Long memory;
}
