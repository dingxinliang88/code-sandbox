package com.juzi.codesandbox.exec;

import com.juzi.codesandbox.model.ExecuteCodeRequest;
import com.juzi.codesandbox.model.ExecuteCodeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author codejuzi
 */
@Slf4j
@Component
public class JavaNativeCodeSandbox extends CodeSandboxTemplate {

    @Override
    public ExecuteCodeResponse execute(ExecuteCodeRequest executeCodeRequest) {
        return super.execute(executeCodeRequest);
    }
}
