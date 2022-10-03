package com.anton.martynenko.jswrapper.jsexecution;

import com.anton.martynenko.jswrapper.jsexecution.constants.Property;
import com.anton.martynenko.jswrapper.jsexecution.enums.SortBy;
import com.anton.martynenko.jswrapper.jsexecution.enums.Status;
import com.anton.martynenko.jswrapper.jsexecution.problem.JsExecutionCanNotBeCancelledProblem;
import com.anton.martynenko.jswrapper.jsexecution.problem.JsExecutionNotFoundProblem;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static java.lang.String.format;

@SpringBootTest
@AutoConfigureMockMvc
class JsExecutionControllerTest {

    private static final String SOME_CODE = "Some code";
    private static final String NOT_VALID_CODE = "cons ole.log('Some code');";
    private static final String CREATE_JSEXECUTION_REQUEST_BODY = format("{\"scriptBody\": \"%s\"}", SOME_CODE);

    private static final String PLAIN_TEXT_CONTENT_TYPE = MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8";


    @InjectMocks
    private JsExecutionController jsExecutionController;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JsExecutionService jsExecutionService;

    private int id1 = 0;
    private int id2 = 0;
    private int noSuchId = 2;
    private String notExistingProperty = "notExistingProperty";

    private JsExecutionDTO jsExecutionDTO1 = new JsExecutionDTO(id1,
        Status.CREATED,
        "console.log('Some code');",
        ZonedDateTime.now(),
        null,
        "executionLog",
        "errorLog",
        true,
        "Exception info");

    private JsExecutionDTO jsExecutionDTO2 = new JsExecutionDTO(id2,
        Status.CREATED,
        "console.log('Some code');",
        ZonedDateTime.now(),
        null,
        "executionLog",
        "errorLog",
        true,
        "Exception info");

    @BeforeEach
    void prepare() {
        when(jsExecutionService.getOne(id1)).thenReturn(jsExecutionDTO1);
        when(jsExecutionService.getOne(id2)).thenReturn(jsExecutionDTO2);
        when(jsExecutionService.getOne(noSuchId)).thenThrow(new JsExecutionNotFoundProblem(noSuchId));
        when(jsExecutionService.cancelExecution(noSuchId)).thenThrow(new JsExecutionNotFoundProblem(noSuchId));
        doThrow(new JsExecutionNotFoundProblem(noSuchId)).when(jsExecutionService).deleteExecution(noSuchId);
    }

    @Test
    void contextLoads(){
        assertThat(jsExecutionController).isNotNull();
    }

    @Test
    void createNew() throws Exception {
        when(jsExecutionService.createAndRun(any(JsExecutionDTO.class))).thenReturn(jsExecutionDTO1);

        this.mockMvc.perform(post("/executions")
            .contentType(APPLICATION_JSON_UTF8)
            .content(CREATE_JSEXECUTION_REQUEST_BODY))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(redirectedUrl("http://localhost/executions/0"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNotEmpty());
    }

    @Test
    void getOne() throws Exception {
        this.mockMvc.perform(get("/executions/" + id1))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(id1));

        this.mockMvc.perform(get("/executions/" + noSuchId))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_UTF8_VALUE))
            .andExpect(jsonPath("title").value(org.zalando.problem.Status.NOT_FOUND.getReasonPhrase()) )
            .andExpect(jsonPath("status").value(org.zalando.problem.Status.NOT_FOUND.getStatusCode()) )
            .andExpect(jsonPath("detail").value(String.format("JsExecution id '%d' not found", noSuchId)) );
    }

    @Test
    void listAll() throws Exception {
        when(jsExecutionService.findAll(Optional.empty(), Optional.empty())).thenReturn(Arrays.asList(jsExecutionDTO1, jsExecutionDTO2));

        this.mockMvc.perform(get("/executions"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(MockMvcResultMatchers.jsonPath("$.links").isNotEmpty())
            .andExpect(MockMvcResultMatchers.jsonPath("$.content").isNotEmpty());

        when(jsExecutionService.findAll(Optional.of(Status.CREATED), Optional.empty()))
            .thenReturn(Arrays.asList(jsExecutionDTO1));

        this.mockMvc.perform(get("/executions?status=CREATED"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(MockMvcResultMatchers.jsonPath("$.links").isNotEmpty())
            .andExpect(MockMvcResultMatchers.jsonPath("$.content").isNotEmpty());

        when(jsExecutionService.findAll(Optional.empty(), Optional.of(SortBy.ID)))
            .thenReturn(Arrays.asList(jsExecutionDTO2));

        this.mockMvc.perform(get("/executions?sortBy=ID"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(MockMvcResultMatchers.jsonPath("$.links").isNotEmpty())
            .andExpect(MockMvcResultMatchers.jsonPath("$.content").isNotEmpty());
    }

    @Test
    void cancelJsExecution() throws Exception {
        this.mockMvc.perform(delete("/executions/{id}/cancel", noSuchId))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_UTF8_VALUE))
            .andExpect(jsonPath("title").value(org.zalando.problem.Status.NOT_FOUND.getReasonPhrase()) )
            .andExpect(jsonPath("status").value(org.zalando.problem.Status.NOT_FOUND.getStatusCode()) )
            .andExpect(jsonPath("detail").value(String.format("JsExecution id '%d' not found", noSuchId)) );

        when(jsExecutionService.cancelExecution(id1)).thenReturn(jsExecutionDTO1);

        this.mockMvc.perform(delete("/executions/{id}/cancel", id1))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(id1));

        when(jsExecutionService.cancelExecution(id2)).thenThrow(new JsExecutionCanNotBeCancelledProblem(
            String.format("JsExecution id %d and status %s can't be canceled",
                jsExecutionDTO2.getId(),
                jsExecutionDTO2.getStatus().name())
        ));

        this.mockMvc.perform(delete("/executions/{id}/cancel", id2))
            .andDo(print())
            .andExpect(status().isMethodNotAllowed())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_UTF8_VALUE))
            .andExpect(jsonPath("title").value(org.zalando.problem.Status.METHOD_NOT_ALLOWED.getReasonPhrase()) )
            .andExpect(jsonPath("status").value(org.zalando.problem.Status.METHOD_NOT_ALLOWED.getStatusCode()) )
            .andExpect(jsonPath("detail").value(String.format("JsExecution id %d and status %s can't be canceled",
                                                            jsExecutionDTO2.getId(),
                                                            jsExecutionDTO2.getStatus().name())) );
    }

    @Test
    void deleteJsExecution() throws Exception {
        this.mockMvc.perform(delete("/executions/{id}", noSuchId))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_UTF8_VALUE))
            .andExpect(jsonPath("title").value(org.zalando.problem.Status.NOT_FOUND.getReasonPhrase()) )
            .andExpect(jsonPath("status").value(org.zalando.problem.Status.NOT_FOUND.getStatusCode()) )
            .andExpect(jsonPath("detail").value(String.format("JsExecution id '%d' not found", noSuchId)) );

        this.mockMvc.perform(delete("/executions/{executionId}",id1))
            .andDo(print())
            .andExpect(status().isNoContent())
            .andExpect(content().string(""));
    }

    @Test
    void getDetails() throws Exception {
        this.mockMvc.perform(get("/executions/{executionId}/{property}", noSuchId, Property.SCRIPT_BODY))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_UTF8_VALUE))
            .andExpect(jsonPath("title").value(org.zalando.problem.Status.NOT_FOUND.getReasonPhrase()) )
            .andExpect(jsonPath("status").value(org.zalando.problem.Status.NOT_FOUND.getStatusCode()) )
            .andExpect(jsonPath("detail").value(String.format("JsExecution id '%d' not found", noSuchId)) );

        this.mockMvc.perform(get("/executions/{executionId}/{property}", id1, notExistingProperty))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_UTF8_VALUE))
            .andExpect(jsonPath("title").value(org.zalando.problem.Status.BAD_REQUEST.getReasonPhrase()) )
            .andExpect(jsonPath("status").value(org.zalando.problem.Status.BAD_REQUEST.getStatusCode()) )
            .andExpect(jsonPath("detail").value(
                String.format("JsExecution contains no '%s' details property", notExistingProperty)));

        this.mockMvc.perform(get("/executions/{executionId}/{property}", id1, Property.SCRIPT_BODY))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(PLAIN_TEXT_CONTENT_TYPE))
            .andExpect(content().string(jsExecutionDTO1.getScriptBody()));

        this.mockMvc.perform(get("/executions/{executionId}/{property}", id1, Property.ERROR_LOG))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(PLAIN_TEXT_CONTENT_TYPE))
            .andExpect(content().string(jsExecutionDTO1.getErrorLog()));

        this.mockMvc.perform(get("/executions/{executionId}/{property}", id1, Property.EXECUTION_LOG))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(PLAIN_TEXT_CONTENT_TYPE))
            .andExpect(content().string(jsExecutionDTO1.getExecutionLog()));

        this.mockMvc.perform(get("/executions/{executionId}/{property}", id1, Property.EXCEPTION_INFO))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(PLAIN_TEXT_CONTENT_TYPE))
            .andExpect(content().string(jsExecutionDTO1.getExceptionInfo()));
    }

}