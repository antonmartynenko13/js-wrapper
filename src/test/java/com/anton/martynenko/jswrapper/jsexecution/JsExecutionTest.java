package com.anton.martynenko.jswrapper.jsexecution;

import com.anton.martynenko.jswrapper.jsexecution.enums.Status;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.StopWatch;


import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

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
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Test
    void shouldCreateWithFilledProperties()  {
        JsExecution jsExecution = new JsExecution(VALID_CODE_EXAMPLE);
        assertThat(jsExecution.getStatus()).isEqualTo(Status.CREATED);
        assertThat(jsExecution.getScheduledTime()).isNotNull();
        assertThat(jsExecution.getExecutionTime()).isNull();
        assertThat(jsExecution.getScriptBody()).isEqualTo(VALID_CODE_EXAMPLE);
    }

    @Test
    void shouldExecuteNormallyAndFillProperties() throws ExecutionException, InterruptedException, IllegalAccessException {

        JsExecution jsExecution = new JsExecution(FUNCTION_CODE_EXAMPLE);

        jsExecution.submitExecution(threadPoolTaskExecutor);

        Future<JsExecution> executionFuture = (Future) FieldUtils.readField(jsExecution, "executionFuture", true);

        executionFuture.get();

        assertThat(jsExecution.getScriptBody()).isEqualTo(FUNCTION_CODE_EXAMPLE);
        assertThat(jsExecution.getStatus()).isEqualTo(Status.SUCCESSFUL);
        assertThat(jsExecution.collectExecutionLog()).isEqualTo(JS_CONSOLE_OUTPUT + "\n");
        assertThat(jsExecution.collectErrorLog()).startsWith("[To redirect Truffle log output to a file use one of the following options:");

        jsExecution = new JsExecution(VALID_CODE_EXAMPLE2);
        jsExecution.submitExecution(threadPoolTaskExecutor);

        executionFuture = (Future) FieldUtils.readField(jsExecution, "executionFuture", true);

        executionFuture.get();

        assertThat(jsExecution.getScriptBody()).isEqualTo(VALID_CODE_EXAMPLE2);
        assertThat(jsExecution.getStatus()).isEqualTo(Status.SUCCESSFUL);
        assertThat(jsExecution.collectExecutionLog()).isEmpty();
        assertThat(jsExecution.collectErrorLog()).startsWith("[To redirect Truffle log output to a file use one of the following options:");
    }

    @Test
    void shouldSuccessfullyStopAndFillProperties() throws InterruptedException, IllegalAccessException {
        JsExecution jsExecution = new JsExecution(SLOW_JS_CODE);

        jsExecution.submitExecution(threadPoolTaskExecutor);

        Future executionFuture = (Future) FieldUtils.readField(jsExecution, "executionFuture", true);

        TimeUnit.MILLISECONDS.sleep(500);

        assertThat(jsExecution.getStatus()).isEqualTo(Status.RUNNING);

        jsExecution.cancel();

        TimeUnit.MILLISECONDS.sleep(1000);

        Assertions.assertEquals(jsExecution.getStatus(), Status.CANCELLED);

        assertThat(executionFuture.isCancelled()).isTrue();
    }

    @Test
    void equalsAndHashcodeShouldWorkCorrectly(){
        JsExecution jsExecution1 = new JsExecution(FUNCTION_CODE_EXAMPLE);
        JsExecution jsExecution2 = new JsExecution(FUNCTION_CODE_EXAMPLE);
        assertThat(jsExecution1.hashCode()).isNotEqualTo(jsExecution2.hashCode());
        assertThat(jsExecution1).isNotEqualTo(jsExecution2);
    }

    @Test
    void shouldSuccesfullyRunAndFinishConcurrently() throws InterruptedException {
        int numberOfThreads = 1000;
        int poolSize = 12;
        List<JsExecution> jsExecutions = Collections.nCopies(numberOfThreads, new JsExecution(VALID_CODE_EXAMPLE));

        System.out.println(jsExecutions.size());

        ExecutorService service = Executors.newFixedThreadPool(poolSize);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        StopWatch watch = new StopWatch();
        watch.start();
        jsExecutions.forEach(jsExecution -> {
            service.submit(() -> {
                jsExecution.run();
                latch.countDown();
            });
        });

        latch.await();

        watch.stop();
        System.out.printf("Threads number: %d | Pool size: %d | Time Elapsed: %d",
            numberOfThreads, poolSize, watch.getTotalTimeMillis());

        jsExecutions.forEach(jsExecution -> {
            assertThat(jsExecution.getStatus().equals(Status.SUCCESSFUL));
        });
    }

    @Test
    void shouldCreateValidDto() {
        JsExecution jsExecution = new JsExecution(VALID_CODE_EXAMPLE);

        JsExecutionDTO jsExecutionDTO = jsExecution.getDto();

        assertThat(jsExecutionDTO.getId()).isEqualTo(jsExecution.getId());
        assertThat(jsExecutionDTO.getStatus()).isEqualTo(jsExecution.getStatus());
        assertThat(jsExecutionDTO.getScriptBody()).isEqualTo(jsExecution.getScriptBody());
        assertThat(jsExecutionDTO.getExceptionInfo()).isEmpty();
        assertThat(jsExecutionDTO.getErrorLog()).isEmpty();
        assertThat(jsExecutionDTO.getExecutionLog()).isEmpty();
        assertThat(jsExecutionDTO.getScheduledTimeString()).isNotEmpty();
        assertThat(jsExecutionDTO.getExecutionTimeString()).isNull();
    }
}