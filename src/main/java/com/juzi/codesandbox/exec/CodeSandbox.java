package com.juzi.codesandbox.exec;


import com.juzi.codesandbox.model.ExecuteCodeRequest;
import com.juzi.codesandbox.model.ExecuteCodeResponse;

/**
 * @author codejuzi
 */
public interface CodeSandbox {

    /**
     * 代码沙箱执行接口
     *
     * @param executeCodeRequest 执行代码请求
     * @return 执行代码响应
     */
    ExecuteCodeResponse execute(ExecuteCodeRequest executeCodeRequest);
}
