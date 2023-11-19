package com.juzi.codesandbox.exec;

import cn.hutool.core.util.StrUtil;
import com.juzi.codesandbox.model.ExecuteMessage;
import com.juzi.codesandbox.utils.ProcessUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.juzi.codesandbox.constants.CodeSandboxConstants.TIME_OUT;

/**
 * @author codejuzi
 */
@Slf4j
@Component
public class JavaNativeAcmCodeSandbox extends CodeSandboxTemplate {

    // todo 上线换成实际的path "/root/ju-oj/security"
    @Value("${oj.sandbox.security-manager-path:/Users/codejuzi/Documents/CodeWorkSpace/Projects/JuOj/code-sandbox/src/main/resources/security}")
    private String SECURITY_MANAGER_CLASS_PATH;

    @Value("${oj.sandbox.security-manager-class-name:UserCodeSecurityManager}")
    private String SECURITY_CLASS_NAME;

    @Override
    protected List<ExecuteMessage> runCode(File userCodeFile, List<String> inputList) throws IOException {
        // 3、执行代码
        // 此处OS X / Linux下是使用 `:` 分割不同类，windows下是使用 `;` 分割不同类名
        String runCmdPattern = "java -Xmx156m -Dfile.encoding=UTF-8 -cp %s" + File.pathSeparator + "%s -Djava.security.manager=%s Main";
        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        StopWatch stopWatch = new StopWatch();
        for (String inputArgs : inputList) {
            String runCmd = String.format(runCmdPattern, userCodeParentPath,
                    SECURITY_MANAGER_CLASS_PATH, SECURITY_CLASS_NAME);

            stopWatch.start();
            Process runProcess = Runtime.getRuntime().exec(runCmd);
            // 超时控制
            Thread timeOutThread = new Thread(() -> {
                try {
                    Thread.sleep(TIME_OUT);
                    log.warn("run code is time out, interrupt");
                    runProcess.destroy();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }, "time_out_thread");
            timeOutThread.start();
            ExecuteMessage executeMessage = ProcessUtil.getInteractProcessMessage(runProcess, inputArgs); // 交互式
            stopWatch.stop();
            if (!timeOutThread.isAlive()) {
                executeMessage.setTime(stopWatch.getLastTaskTimeMillis());
                executeMessage.setErrorMessage("Time out");
            }
            log.info("execute message: {}", executeMessage);
            executeMessageList.add(executeMessage);
            if (StrUtil.isNotBlank(executeMessage.getErrorMessage())) {
                // 已经有用例失败了
                break;
            }
        }
        return executeMessageList;
    }
}
