package com.juzi.codesandbox.model;

import lombok.Data;

/**
 * 进程执行信息
 *
 * @author codejuzi
 */
@Data
public class ExecuteMessage {

    private Integer exitValue;

    private String message;

    private String errorMessage;

    private Long time;

    private Long memory;
}
