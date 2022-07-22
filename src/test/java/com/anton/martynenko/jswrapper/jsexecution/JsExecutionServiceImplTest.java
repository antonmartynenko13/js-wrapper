package com.anton.martynenko.jswrapper.jsexecution;

import com.anton.martynenko.jswrapper.jsexecution.enums.SortBy;
import com.anton.martynenko.jswrapper.jsexecution.enums.Status;
import com.anton.martynenko.jswrapper.jsexecution.problem.JsExecutionNotFoundProblem;
import com.anton.martynenko.jswrapper.graalvm.GraalVmHelper;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.slf4j.Logger;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.zalando.problem.Problem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest
class JsExecutionServiceImplTest {

    private static final String VALID_CODE_EXAMPLE = "var i = null;";

    @InjectMocks
    private JsExecutionServiceImpl jsExecutionServiceImpl;

    @Mock
    private JsExecutionRepository jsExecutionRepository;

    @Mock
    private GraalVmHelper graalVmHelper;

    @Mock
    private JsExecutionFactory jsExecutionFactory;

    @Mock
    private ThreadPoolTaskExecutor taskExecutor;

    @Spy
    private Logger logger; //real logger not mock

    @Test
    void contextLoads(){
        assertThat(jsExecutionServiceImpl).isNotNull();
        assertThat(jsExecutionRepository).isNotNull();
        assertThat(graalVmHelper).isNotNull();
    }


    @Test
    void createJsExecution() {
        JsExecution jsExecutionMock = Mockito.mock(JsExecution.class);

        when(jsExecutionFactory.createNew(VALID_CODE_EXAMPLE)).thenReturn(jsExecutionMock);
        when(jsExecutionRepository.save(jsExecutionMock)).thenReturn(jsExecutionMock);


        JsExecution jsExecution = jsExecutionServiceImpl.createJsExecution(VALID_CODE_EXAMPLE);

        assertThat(jsExecution).isEqualTo(jsExecutionMock);
    }

    @Test
    void getJsExecutions() {

        when(jsExecutionRepository.findAll(null, null)).thenReturn(new ArrayList<>());
        Collection<JsExecution> jsExecutionCollectionMock = Arrays.asList(jsExecutionFactory.createNew(VALID_CODE_EXAMPLE),
                                                                                jsExecutionFactory.createNew(VALID_CODE_EXAMPLE));
        when(jsExecutionRepository.findAll(Status.CREATED, SortBy.ID)).thenReturn(jsExecutionCollectionMock);

        assertThat(jsExecutionServiceImpl.getJsExecutions(null, null)).isEmpty();
        assertThat(jsExecutionServiceImpl.getJsExecutions(Status.CREATED, SortBy.ID)).hasSize(2);
    }

    @Test
    void getJsExecution() {
        Integer id = 1;
        Integer noSuchId = 1000;

        JsExecution jsExecutionMock = Mockito.mock(JsExecution.class);

        when(jsExecutionRepository.getOne(id)).thenReturn(jsExecutionMock);
        when(jsExecutionRepository.getOne(noSuchId)).thenReturn(null);

        assertThat(jsExecutionServiceImpl.getJsExecution(id)).isEqualTo(jsExecutionMock);

        JsExecutionNotFoundProblem problem = assertThrows(JsExecutionNotFoundProblem.class, () -> jsExecutionServiceImpl.getJsExecution(noSuchId));
        assertThat(problem.getTitle()).isEqualTo(org.zalando.problem.Status.NOT_FOUND.getReasonPhrase());
        assertThat(problem.getType()).isEqualTo(Problem.DEFAULT_TYPE);
        assertThat(problem.getStatus().getStatusCode()).isEqualTo(org.zalando.problem.Status.NOT_FOUND.getStatusCode());
        assertThat(problem.getDetail()).isEqualTo("JsExecution id 1000 not found");
    }

    @Test
    void deleteJsExecution() {

        JsExecution jsExecutionMock = Mockito.mock(JsExecution.class);

        when(jsExecutionMock.getStatus()).thenReturn(Status.RUNNING);

        jsExecutionServiceImpl.deleteJsExecution(jsExecutionMock);

        //if nothing bad happens
        assertTrue(true);
    }

    @Test
    void stopJsExecution() {
        JsExecution jsExecution = Mockito.mock(JsExecution.class);

        assertThat(jsExecutionServiceImpl.stopJsExecution(jsExecution)).isEqualTo(jsExecution);
    }
}