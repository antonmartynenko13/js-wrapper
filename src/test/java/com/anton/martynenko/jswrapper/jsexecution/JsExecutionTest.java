package com.anton.martynenko.jswrapper.jsexecution;

import com.anton.martynenko.jswrapper.jsexecution.enums.Status;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.util.ReflectionTestUtils;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class JsExecutionTest {

    private static final String JS_CONSOLE_OUTPUT = "Js console output";
    private static final String VALID_CODE_EXAMPLE = String.format("console.log('%s')", JS_CONSOLE_OUTPUT);
    private static final String VALID_CODE_EXAMPLE2 = "var i = null;";

    private static final String FUNCTION_CODE_EXAMPLE = String.format("var f = () => {console.log('%s'); return 2 * 2;}; f();", JS_CONSOLE_OUTPUT);

    private static final String SLOW_JS_CODE = "console.time('mySlowFunction');\n" +
            "let result = 0;\n" +
            "\tfor (var i = Math.pow(1000, 7); i >= 0; i--) {\n" +
            "\tresult += Math.atan(i) * Math.tan(i);\n" +
            "};\n" +
            "console.timeEnd('mySlowFunction')";

    @Autowired
    private JsExecutionFactory jsExecutionFactory;

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Test
    void shouldCreateWithFilledProperties()  {
        JsExecution jsExecution = jsExecutionFactory.createNew(VALID_CODE_EXAMPLE);
        assertThat(jsExecution.getStatus()).isEqualTo(Status.CREATED);
        assertThat(jsExecution.getScheduledTime()).isNotNull();
        assertThat(jsExecution.getExecutionTime()).isNull();
        assertThat(jsExecution.getResultValue()).isNull();
        assertThat(jsExecution.getScriptBody()).isEqualTo(VALID_CODE_EXAMPLE);

        JsExecution jsExecution2 = jsExecutionFactory.createNew(VALID_CODE_EXAMPLE);
        assertThat(jsExecution).isNotEqualTo(jsExecution2);
    }

    @Test
    void shouldExecuteNormallyAndFillProperties() throws ExecutionException, InterruptedException {

        JsExecution jsExecution = jsExecutionFactory.createNew(FUNCTION_CODE_EXAMPLE);

        Future <JsExecution> result = jsExecution.submitExecution(threadPoolTaskExecutor);

        jsExecution = result.get();

        assertThat(jsExecution.getScriptBody()).isEqualTo(FUNCTION_CODE_EXAMPLE);
        assertThat(jsExecution.getResultValue()).isEqualTo("4");
        assertThat(jsExecution.getStatus()).isEqualTo(Status.SUCCESS);
        assertThat(jsExecution.collectExecutionLog()).isEqualTo(JS_CONSOLE_OUTPUT + "\n");
        assertThat(jsExecution.collectErrorLog()).startsWith("[To redirect Truffle log output to a file use one of the following options:");
        assertThat(jsExecution.collectExceptionInfo()).isEmpty();

        jsExecution = jsExecutionFactory.createNew(VALID_CODE_EXAMPLE2);
        result = jsExecution.submitExecution(threadPoolTaskExecutor);

        jsExecution = result.get();

        assertThat(jsExecution.getScriptBody()).isEqualTo(VALID_CODE_EXAMPLE2);
        assertThat(jsExecution.getResultValue()).isEqualTo("undefined");
        assertThat(jsExecution.getStatus()).isEqualTo(Status.SUCCESS);
        assertThat(jsExecution.collectExecutionLog()).isEmpty();
        assertThat(jsExecution.collectErrorLog()).startsWith("[To redirect Truffle log output to a file use one of the following options:");
        assertThat(jsExecution.collectExceptionInfo()).isEmpty();
    }

    @Test
    void shouldSuccessfullyStopAndFillProperties() throws InterruptedException {
        JsExecution jsExecution = jsExecutionFactory.createNew(SLOW_JS_CODE);

        Future <JsExecution> result = jsExecution.submitExecution(threadPoolTaskExecutor);

        TimeUnit.MILLISECONDS.sleep(500);

        assertThat(jsExecution.getStatus()).isEqualTo(Status.RUNNING);

        jsExecution.stop();

        TimeUnit.MILLISECONDS.sleep(1000);

        Assertions.assertTrue(jsExecution.getStatus().equals(Status.CANCELLED) || jsExecution.getStatus().equals(Status.UNSUCCESS));

        assertThat(jsExecution.collectExceptionInfo()).startsWith("Thread was interrupted.");

        assertThat(result.isCancelled()).isTrue();

        jsExecution = jsExecutionFactory.createNew(SLOW_JS_CODE);

        jsExecution.submitExecution(threadPoolTaskExecutor);

        jsExecution.stop();

        assertThat(jsExecution.getStatus()).isEqualTo(Status.CANCELLED);
    }

    @Test
    void equalsAndHashcodeShouldWorkCorrectly(){
        JsExecution jsExecution1 = jsExecutionFactory.createNew(FUNCTION_CODE_EXAMPLE);
        JsExecution jsExecution2 = jsExecutionFactory.createNew(FUNCTION_CODE_EXAMPLE);
        assertThat(jsExecution1.hashCode()).isNotEqualTo(jsExecution2.hashCode());
        assertThat(jsExecution1).isNotEqualTo(jsExecution2);
    }

    @Test
    void shouldSuccesfullyRunAndFinishConcurrently() throws InterruptedException {
        int numberOfThreads = 1000;
        List<JsExecution> jsExecutions = Collections.nCopies(numberOfThreads, new JsExecution(VALID_CODE_EXAMPLE));

        System.out.println(jsExecutions.size());

        ExecutorService service = Executors.newFixedThreadPool(12);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        jsExecutions.forEach(jsExecution -> {
            service.submit(() -> {
                jsExecution.call();
                latch.countDown();
            });
        });

        latch.await();

        jsExecutions.forEach(jsExecution -> {
            assertThat(jsExecution.getStatus().equals(Status.SUCCESS));
        });
    }

}