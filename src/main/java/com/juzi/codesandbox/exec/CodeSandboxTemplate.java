package com.juzi.codesandbox.exec;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.juzi.codesandbox.model.CodeSandboxStatusEnum.FAILED;
import static com.juzi.codesandbox.model.CodeSandboxStatusEnum.SUCCEED;

/**
 * 代码沙箱模板
 *
 * @author codejuzi
 */
@Slf4j
public abstract class CodeSandboxTemplate implements CodeSandbox {


    private static final String GLOBAL_CODE_DIR_NAME = "tmp_code";

    private static final String GLOBAL_CODE_FILE_NAME = "Main.java";

    private static final String SECURITY_MANAGER_CLASS_PATH = "/Users/codejuzi/Documents/CodeWorkSpace/Projects/JuOj/code-sandbox/src/main/resources/security";

    private static final String SECURITY_CLASS_NAME = "UserCodeSecurityManager";

    private static final Long TIME_OUT = 10000L;


    @Override
    public ExecuteCodeResponse execute(ExecuteCodeRequest executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        // todo 考虑不同的language
//        String language = executeCodeRequest.getLanguage();

        File userCodeFile = null;
        ExecuteCodeResponse response;
        try {
            // 1、保存文件
            userCodeFile = save2File(code);

            // 2、编译代码
            ExecuteMessage executeMessage = compileCode(userCodeFile);
            log.info("Compile Code: {}", executeMessage);

            // 3、执行代码
            List<ExecuteMessage> executeMessageList = runCode(userCodeFile, inputList);

            // 4、获取输出
            response = getOutputResponse(executeMessageList);

        } catch (Exception e) {
            log.error("发生异常：", e);
            // 处理异常
            return handleError(e);
        } finally {
            // 5、清理文件
            assert userCodeFile != null;
            clearFile(userCodeFile);
        }

        return response;
    }

    private File save2File(String code) {
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
        return FileUtil.writeBytes(code.getBytes(StandardCharsets.UTF_8), userCodePath);
    }

    private ExecuteMessage compileCode(File userCodeFile) throws IOException {
        // 2、编译代码，得到class文件
        String compileCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsolutePath());
        Process compileProcess = Runtime.getRuntime().exec(compileCmd);
        ExecuteMessage executeMessage = ProcessUtil.getRunProcessMessage("Compile Code", compileProcess);
        if (executeMessage.getExitValue() != 0) {
            throw new RuntimeException("Compile Error!");
        }
        return executeMessage;
    }

    protected List<ExecuteMessage> runCode(File userCodeFile, List<String> inputList) throws IOException {
        // 3、执行代码
        // 此处mac下是使用 : 分割不同类，windows下是使用 ; 分割不同类名
        String runCmdPattern = "java -Xmx156m -Dfile.encoding=UTF-8 -cp %s:%s -Djava.security.manager=%s Main %s";
        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        for (String inputArgs : inputList) {
            String runCmd = String.format(runCmdPattern, userCodeParentPath,
                    SECURITY_MANAGER_CLASS_PATH, SECURITY_CLASS_NAME, inputArgs);
            Process runProcess = Runtime.getRuntime().exec(runCmd);
//                ExecuteMessage executeMessage = ProcessUtil.getInteractProcessMessage(runProcess, inputArgs); // 交互式
            // 超时控制
//            new Thread(() -> {
//                try {
//                    TimeUnit.MILLISECONDS.sleep(TIME_OUT);
//                    log.info("程序运行超时，已经中断");
//                    runProcess.destroy();
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//            }).start();
            ExecuteMessage executeMessage = ProcessUtil.getRunProcessMessage("Run Code", runProcess);
            log.info("execute message: {}", executeMessage);
            executeMessageList.add(executeMessage);
        }
        return executeMessageList;
    }

    private ExecuteCodeResponse getOutputResponse(List<ExecuteMessage> executeMessageList) {
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

        judgeInfo.setMemory(RandomUtil.randomLong(1000L, 5000L));
        judgeInfo.setTime(maxExecTime);
        executeCodeResponse.setJudgeInfo(judgeInfo);
        return executeCodeResponse;
    }

    private void clearFile(File file) {
        boolean delFileRes = FileUtil.del(file.getParentFile());
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
