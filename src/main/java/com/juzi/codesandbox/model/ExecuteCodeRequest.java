package com.juzi.codesandbox.model;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 执行代码请求
 *
 * @author codejuzi
 */
@Data
@Builder
public class ExecuteCodeRequest implements Serializable {

    private static final long serialVersionUID = -1729942560382446421L;

    /**
     * 题目输入
     */
    private List<String> inputList;

    /**
     * 代码信息
     */
    private String code;

    /**
     * 编程语言
     */
    private String language;

}
