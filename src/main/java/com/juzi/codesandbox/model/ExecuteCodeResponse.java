package com.juzi.codesandbox.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 执行代码response
 *
 * @author codejuzi
 */
@Data
public class ExecuteCodeResponse implements Serializable {

    /**
     * 执行程序实际输出信息
     */
    private List<String> outputList;

    /**
     * 执行代码信息、内存、耗时等
     */
    private JudgeInfo judgeInfo;

    /**
     * 执行过程信息
     */
    private String message;

    /**
     * 执行代码状态
     *
     * @see CodeSandboxStatusEnum
     */
    private Integer status;
}
