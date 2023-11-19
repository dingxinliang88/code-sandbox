package com.juzi.codesandbox.model;

import lombok.Data;

/**
 * 进程执行信息
 *
 * @author codejuzi
 */
@Data
public class ExecuteMessage {

    /**
     * 退出值
     */
    private Integer exitValue;

    /**
     * 执行信息
     */
    private String message;

    /**
     * 执行过程中错误信息
     */
    private String errorMessage;

    /**
     * 耗时
     */
    private Long time;

    /**
     * 内存情况
     */
    private Long memory;
}
