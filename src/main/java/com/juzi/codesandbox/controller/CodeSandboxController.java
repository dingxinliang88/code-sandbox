package com.juzi.codesandbox.controller;

import com.juzi.codesandbox.exec.JavaNativeCodeSandbox;
import com.juzi.codesandbox.model.ExecuteCodeRequest;
import com.juzi.codesandbox.model.ExecuteCodeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.juzi.codesandbox.auth.AuthRequest.AUTH_REQUEST_HEADER;
import static com.juzi.codesandbox.auth.AuthRequest.AUTH_REQUEST_SECRET;

/**
 * @author codejuzi
 */
@Slf4j
@RestController
@RequestMapping("/")
public class CodeSandboxController {

    @Resource
    private JavaNativeCodeSandbox codeSandbox;

    /**
     * 执行代码接口
     *
     * @param executeCodeRequest 请求
     * @return response
     */
    @PostMapping("/exec_code")
    public ExecuteCodeResponse execCode(@RequestBody ExecuteCodeRequest executeCodeRequest, HttpServletRequest request, HttpServletResponse response) {
        String authHeader = request.getHeader(AUTH_REQUEST_HEADER);

        if (!AUTH_REQUEST_SECRET.equals(authHeader)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        if (executeCodeRequest == null) {
            return null;
        }
        log.info("executeCodeRequest = {}", executeCodeRequest);
        return codeSandbox.execute(executeCodeRequest);
    }
}
