package com.anton.martynenko.jswrapper.jsexecution;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
class JsExecutionServiceConcurrencyTests {
  private final String VALID_CODE_EXAMPLE2 = "var i = null;";
  private final int numberOfThreads = 1000;
  private final int poolSize = 12;
  private final List<JsExecutionDTO> jsExecutionDTOList = Collections.nCopies(numberOfThreads, new JsExecutionDTO(VALID_CODE_EXAMPLE2));
  private ExecutorService service;
  private CountDownLatch latch;
  private final StopWatch watch = new StopWatch();
  private final AtomicInteger crashesAmount = new AtomicInteger();

  @Autowired
  private JsExecutionService jsExecutionService;

  @BeforeEach
  void prepare() {
    service = Executors.newFixedThreadPool(poolSize);
    latch = new CountDownLatch(numberOfThreads);
    crashesAmount.set(0);
  }

  @Test
  void shouldInsertWithIndexesAndDeleteCorrectly() throws InterruptedException, IllegalAccessException {

    watch.start();
    jsExecutionDTOList.forEach(jsExecutionDTO -> {
      service.submit(() -> {
        try{
          JsExecutionDTO resultDTO = jsExecutionService.createAndRun(jsExecutionDTO);
          //deleting some of them
          if (resultDTO.getId() % 10 == 0) {
            jsExecutionService.deleteExecution(resultDTO.getId());
          }
        } catch (Exception e){
          System.err.println(e.toString());
          crashesAmount.incrementAndGet();
        }

        latch.countDown();
      });
    });

    latch.await();

    watch.stop();
    System.out.printf("Threads number: %d | Pool size: %d | Time Elapsed: %d %n",
        numberOfThreads, poolSize, watch.getTotalTimeMillis());

    assertThat(crashesAmount.get()).isZero();

    List<JsExecution> storage = (List<JsExecution>) FieldUtils.readField(jsExecutionService, "storage", true);

    System.out.printf("Storage contains %d values%n", storage.size());
    //after inserting let's check all indexes
    int nullAmount = 0;
    for (int i = 0; i < storage.size(); i++) {
      JsExecution jsExecution = storage.get(i);

      if (jsExecution != null) {
        //System.out.printf("id %d and position is %d%n", jsExecution.getId(), i);
        assertThat(jsExecution.getId()).isEqualTo(i);
      } else {
        nullAmount++;
      }
    }
    System.out.printf("Found %d nulls%n", nullAmount);
  }

  @Test
  void shouldNotCrashBecauseOfModificationDuringIteration() throws InterruptedException {

    System.out.println("Size is " + jsExecutionDTOList.size());

    jsExecutionDTOList.forEach(jsExecutionDTO -> {

      service.submit(() -> {
        try {
          jsExecutionService.createAndRun(jsExecutionDTO);
        } catch (Exception e) {
          System.err.println(e);
        }
        latch.countDown();
      });
      service.submit(() -> {
        try {
          jsExecutionService.findAll(null, null);
        } catch (Exception e) {
          System.err.println(e);
          crashesAmount.incrementAndGet();
        }
      });
    });

    latch.await();
    System.out.println(crashesAmount);
    assertThat(crashesAmount.get()).isZero();

  }

  @Test
  void stupidShit() {
    List <String> list = new ArrayList<>();
    List<String> next = new ArrayList<>();
    for (int i = 0; i < 1000; i++) {
      list.add(i + "");
    }
  //  for (String s: list) {
  //    list.add("123");
  //  }

    list.stream().forEach(s -> {
      System.out.println(s);
      next.add(s);
    });

    System.out.println("List size " + list.size());
    assertThat(list).isEqualTo(next);
  }

}
