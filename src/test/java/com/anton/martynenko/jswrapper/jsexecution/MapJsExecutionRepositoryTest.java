package com.anton.martynenko.jswrapper.jsexecution;

import com.anton.martynenko.jswrapper.jsexecution.enums.SortBy;
import com.anton.martynenko.jswrapper.jsexecution.enums.Status;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.StopWatch;

import java.util.Collection;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MapJsExecutionRepositoryTest {

    private static final String VALID_CODE_EXAMPLE = "var i = null;";

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    private JsExecutionRepository jsExecutionMapRepository;

    @Test
    void contextLoads(){
        assertThat(jsExecutionMapRepository).isNotNull();
    }

    @BeforeEach
    void cleanStorage(){
        jsExecutionMapRepository.deleteAll();
    }

    @Test
    void saveAndGetOne() {
        JsExecution jsExecution = new JsExecution(VALID_CODE_EXAMPLE);

        JsExecution jsExecutionEntity = jsExecutionMapRepository.save(jsExecution);
        assertThat(jsExecutionEntity.getId()).isNotNull();
        assertThat(jsExecutionEntity.getScriptBody()).isEqualTo(jsExecution.getScriptBody());
        assertThat(jsExecutionEntity.getStatus()).isEqualTo(jsExecution.getStatus());
        assertThat(jsExecutionEntity.getScheduledTime()).isEqualTo(jsExecution.getScheduledTime());
        assertThat(jsExecutionMapRepository.getOne(jsExecutionEntity.getId())).isEqualTo(jsExecutionEntity);
    }

    @Test
    void saveConcurrent() throws InterruptedException {
        int numberOfThreads = 1000;
        int poolSize = 12;
        ExecutorService service = Executors.newFixedThreadPool(poolSize);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        StopWatch watch = new StopWatch();
        watch.start();

        for (int i = 0; i < numberOfThreads; i++) {
            final int j = i;
            service.submit(() -> {
                jsExecutionMapRepository.save(new JsExecution("Code example " + j));
                latch.countDown();
            });
        }
        latch.await();
        watch.stop();
        System.out.printf("Threads number: %d | Pool size: %d | Time Elapsed: %d",
            numberOfThreads, poolSize, watch.getTotalTimeMillis());

        assertThat(jsExecutionMapRepository.findAll()).hasSize(numberOfThreads);
        AtomicInteger idGenerator = (AtomicInteger) ReflectionTestUtils.getField(jsExecutionMapRepository, "ID_GENERATOR");
        assertThat(idGenerator.get()).isEqualTo(numberOfThreads);
    }

    @Test
    void findAll() throws ExecutionException, InterruptedException, IllegalAccessException {
        JsExecution jsExecution1 = new JsExecution(VALID_CODE_EXAMPLE);
        JsExecution jsExecution2 = new JsExecution(VALID_CODE_EXAMPLE);
        JsExecution jsExecution3 = new JsExecution(VALID_CODE_EXAMPLE);

        jsExecution1 = jsExecutionMapRepository.save(jsExecution1);
        jsExecution2 = jsExecutionMapRepository.save(jsExecution2);
        jsExecution3 = jsExecutionMapRepository.save(jsExecution3);
        Collection<JsExecution> jsExecutions = jsExecutionMapRepository.findAll();
        assertThat(jsExecutions).hasSize(3);

        jsExecutions = jsExecutionMapRepository.findAll(Status.CANCELLED, SortBy.ID);
        assertThat(jsExecutions).isEmpty();

        jsExecution1.submitExecution(threadPoolTaskExecutor);
        Future<JsExecution> executionFuture
            = (Future<JsExecution>) FieldUtils.readField(jsExecution1, "executionFuture", true);

        executionFuture.get();
        jsExecutions = jsExecutionMapRepository.findAll(Status.SUCCESSFUL, SortBy.ID);
        assertThat(jsExecutions).hasSize(1);
        assertThat(jsExecutions.iterator().next()).isEqualTo(jsExecution1);


    }

    @Test
    void deleteAndDeleteAll() {
        JsExecution jsExecution = new JsExecution(VALID_CODE_EXAMPLE);
        jsExecution = jsExecutionMapRepository.save(jsExecution);
        jsExecutionMapRepository.save(new JsExecution(VALID_CODE_EXAMPLE));
        jsExecutionMapRepository.save(new JsExecution(VALID_CODE_EXAMPLE));

        assertThat(jsExecutionMapRepository.findAll()).hasSize(3);

        jsExecutionMapRepository.delete(jsExecution);

        assertThat(jsExecutionMapRepository.findAll()).hasSize(2);

        jsExecutionMapRepository.deleteAll();

        assertThat(jsExecutionMapRepository.findAll()).isEmpty();
    }


}