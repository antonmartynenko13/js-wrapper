package com.anton.martynenko.jswrapper.jsexecution;

import com.anton.martynenko.jswrapper.jsexecution.enums.SortBy;
import com.anton.martynenko.jswrapper.jsexecution.enums.Status;
import com.anton.martynenko.jswrapper.jsexecution.problem.JsExecutionCanNotBeCancelledProblem;
import com.anton.martynenko.jswrapper.jsexecution.problem.JsExecutionNotFoundProblem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.slf4j.Logger;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;

@SpringBootTest
class JsExecutionServiceTest {

  @InjectMocks
  private JsExecutionService jsExecutionService;

  @Mock
  private ThreadPoolTaskExecutor taskExecutor;

  @Mock
  private JsExecutionFactory jsExecutionFactory;

  @Mock
  private List<JsExecution> storage;

  @Spy
  private Logger logger; //real logger not mock

  private final String VALID_CODE_EXAMPLE = "var i = null;";
  private int id1 = 0;
  private int id2 = 1;
  private int noSuchId = 2;
  private JsExecution jsExecution1 = Mockito.mock(JsExecution.class);
  private JsExecution jsExecution2 = Mockito.mock(JsExecution.class);;
  private JsExecutionDTO jsExecutionDTO1 = Mockito.mock(JsExecutionDTO.class);
  private JsExecutionDTO jsExecutionDTO2 = Mockito.mock(JsExecutionDTO.class);

  @BeforeEach
  void prepare() {

    when(jsExecutionDTO1.getId()).thenReturn(id1);
    when(jsExecutionDTO2.getId()).thenReturn(id2);

    when(jsExecution1.getDto()).thenReturn(jsExecutionDTO1);
    when(jsExecution2.getDto()).thenReturn(jsExecutionDTO2);

    when(storage.size()).thenReturn(2);
    when(storage.get(id1)).thenReturn(jsExecution1);
    when(storage.get(id2)).thenReturn(jsExecution2);
  }

  @Test
  void createAndRun() {
    JsExecutionDTO inputDto = Mockito.mock(JsExecutionDTO.class);
    when(inputDto.getScriptBody()).thenReturn(VALID_CODE_EXAMPLE);

    when(jsExecutionFactory.createNew(VALID_CODE_EXAMPLE)).thenReturn(jsExecution1);

    assertThat(jsExecutionService.createAndRun(inputDto)).isEqualTo(jsExecutionDTO1);
  }

  @Test
  void findAll() {
    // 1. Simple request
    List<JsExecutionDTO> dtoList = new ArrayList<>();
    dtoList.add(jsExecutionDTO1);

    Iterator mockIterator = Mockito.mock(Iterator.class);
    doCallRealMethod().when(storage).forEach(any(Consumer.class));
    when(storage.iterator()).thenReturn(mockIterator);
    when(mockIterator.hasNext()).thenReturn(true, false);
    when(mockIterator.next()).thenReturn(jsExecution1);

    assertThat(jsExecutionService.findAll(null, null)).isEqualTo(dtoList);

    // 2. Request with status filtering
    //not sure I understand why is this needed
    when(mockIterator.hasNext()).thenReturn(true, false);
    when(mockIterator.next()).thenReturn(jsExecution1);

    Status status = Status.SUBMITTED;

    when(jsExecutionDTO1.getStatus()).thenReturn(status);

    assertThat(jsExecutionService.findAll(status, null)).isEqualTo(dtoList);

    //not sure I understand why is this needed
    when(mockIterator.hasNext()).thenReturn(true, false);
    when(mockIterator.next()).thenReturn(jsExecution1);
    assertThat(jsExecutionService.findAll(Status.CREATED, null)).isEmpty();

    // 3. Request with sorting

    //not sure I understand why is this needed
    when(mockIterator.hasNext()).thenReturn(true, true, false);
    when(mockIterator.next()).thenReturn(jsExecution2); //reversed
    when(mockIterator.next()).thenReturn(jsExecution1);

    Collection<JsExecutionDTO> jsExecutionDTOList = jsExecutionService.findAll(null, SortBy.ID);
    assertThat(jsExecutionDTOList.iterator().next()).isEqualTo(jsExecutionDTO1); //must be id 1
  }

  @Test
  void getOne() {
    assertThat(jsExecutionService.getOne(id1)).isEqualTo(jsExecutionDTO1);
    assertThat(jsExecutionService.getOne(id2)).isEqualTo(jsExecutionDTO2);
    assertThrows(JsExecutionNotFoundProblem.class, () -> jsExecutionService.getOne(noSuchId));
  }

  @Test
  void cancelExecution() {
    when(jsExecution1.cancel()).thenReturn(true);

    assertThat(jsExecutionService.cancelExecution(id1)).isEqualTo(jsExecutionDTO1);

    when(jsExecution1.getStatus()).thenReturn(Status.CANCELLED);
    when(jsExecution1.cancel()).thenReturn(false);

    assertThrows(JsExecutionCanNotBeCancelledProblem.class, () -> jsExecutionService.cancelExecution(id1));

    assertThrows(JsExecutionNotFoundProblem.class, () -> jsExecutionService.cancelExecution(noSuchId));
  }

  @Test
  void deleteExecution() {
    assertThrows(JsExecutionNotFoundProblem.class, () -> jsExecutionService.deleteExecution(noSuchId));

    when(storage.set(id1, null)).thenReturn(null);

    assertThrows(JsExecutionNotFoundProblem.class, () -> jsExecutionService.deleteExecution(id1));

    when(storage.set(id2, null)).thenReturn(jsExecution2);

    jsExecutionService.deleteExecution(id2);
  }
}