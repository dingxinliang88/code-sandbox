package com.juzi.codesandbox.exec;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import com.juzi.codesandbox.model.ExecuteCodeRequest;
import com.juzi.codesandbox.model.ExecuteCodeResponse;
import com.juzi.codesandbox.model.ExecuteMessage;
import com.juzi.codesandbox.model.JudgeInfo;
import com.juzi.codesandbox.utils.ProcessUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.juzi.codesandbox.model.CodeSandboxStatusEnum.FAILED;
import static com.juzi.codesandbox.model.CodeSandboxStatusEnum.SUCCEED;


/**
 * @author codejuzi
 */
@Slf4j
public class JavaNativeCodeSandbox implements CodeSandbox {

    private static final String GLOBAL_CODE_DIR_NAME = "tmp_code";

    private static final String GLOBAL_CODE_FILE_NAME = "Main.java";

    private static final String SECURITY_MANAGER_CLASS_PATH = "/Users/codejuzi/Documents/CodeWorkSpace/Projects/JuOj/code-sandbox/src/main/resources/security";

    private static final String SECURITY_CLASS_NAME = "UserCodeSecurityManager";


    @Override
    public ExecuteCodeResponse execute(ExecuteCodeRequest executeCodeRequest) {
        // 1、保存用户代码为文件
        // 获取用户工作文件路径
        String userDir = System.getProperty("user.dir");
        String globalCodePathName = userDir + File.separator + GLOBAL_CODE_DIR_NAME;

        // 判断全局文件路径是否存在
        if (!FileUtil.exist(globalCodePathName)) {
            // 不存在，则创建
            FileUtil.mkdir(globalCodePathName);
        }
        // 存在，则保存用户提交代码，用户代码隔离存放
        String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();
        // 实际存放文件的目录：Main.java
        String userCodePath = userCodeParentPath + File.separator + GLOBAL_CODE_FILE_NAME;
        String code = executeCodeRequest.getCode();
        File userCodeFile = FileUtil.writeBytes(code.getBytes(StandardCharsets.UTF_8), userCodePath);

        // 2、编译代码，得到class文件
        String compileCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsolutePath());
        try {
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);
            ExecuteMessage executeMessage = ProcessUtil.getRunProcessMessage("Compile Code", compileProcess);
            if (executeMessage.getExitValue() != 0) {
                throw new RuntimeException("Compile Error!");
            }
        } catch (IOException | RuntimeException e) {
            clearFile(userCodeFile);
            return handleError(e);
        }

        // 3、执行代码
        // 此处mac下是使用 : 分割不同类，windows下是使用 ; 分割不同类名
        String runCmdPattern = "java -Dfile.encoding=UTF-8 -cp %s:%s -Djava.security.manager=%s Main %s";
        List<String> inputList = executeCodeRequest.getInputList();
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        for (String inputArgs : inputList) {
            String runCmd = String.format(runCmdPattern, userCodeParentPath,
                    SECURITY_MANAGER_CLASS_PATH, SECURITY_CLASS_NAME, inputArgs);
            try {
                Process runProcess = Runtime.getRuntime().exec(runCmd);
//                ExecuteMessage executeMessage = ProcessUtil.getInteractProcessMessage(runProcess, inputArgs); // 交互式
                ExecuteMessage executeMessage = ProcessUtil.getRunProcessMessage("Run Code", runProcess);
                log.info("execute message: {}", executeMessage);
                executeMessageList.add(executeMessage);
            } catch (IOException e) {
                clearFile(userCodeFile);
                return handleError(e);
            }
        }

        // 4、整理输出结果
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        long maxExecTime = 0L;
        List<String> outputList = new ArrayList<>();
        for (ExecuteMessage executeMessage : executeMessageList) {
            String errorMessage = executeMessage.getErrorMessage();
            if (StringUtils.isNotBlank(errorMessage)) {
                // 执行中出错
                executeCodeResponse.setMessage(errorMessage);
                executeCodeResponse.setStatus(FAILED.getValue());
                break;
            }
            outputList.add(executeMessage.getMessage());

            Long execTime = Optional.ofNullable(executeMessage.getTime()).orElse(0L);
            maxExecTime = Math.max(maxExecTime, execTime);
        }
        // 正常执行
        if (outputList.size() == executeMessageList.size()) {
            executeCodeResponse.setStatus(SUCCEED.getValue());
        }
        executeCodeResponse.setOutputList(outputList);

        JudgeInfo judgeInfo = new JudgeInfo();
        // todo Java原生获取内存占用
        judgeInfo.setMemory(0L);
        judgeInfo.setTime(maxExecTime);
        executeCodeResponse.setJudgeInfo(judgeInfo);

        // 5、清理文件
        clearFile(userCodeFile);

        return executeCodeResponse;
    }

    private void clearFile(File file) {
        boolean delFileRes = FileUtil.del(file);
        log.info("Delete File Result: {}", delFileRes);
    }

    private ExecuteCodeResponse handleError(Throwable e) {
        ExecuteCodeResponse response = new ExecuteCodeResponse();
        response.setOutputList(Collections.emptyList());
        response.setMessage(e.getMessage());
        response.setStatus(FAILED.getValue());
        response.setJudgeInfo(new JudgeInfo());
        return response;
    }
}
