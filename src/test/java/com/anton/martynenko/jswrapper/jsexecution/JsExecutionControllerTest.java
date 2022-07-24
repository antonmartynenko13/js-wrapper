package com.anton.martynenko.jswrapper.jsexecution;

import com.anton.martynenko.jswrapper.jsexecution.enums.Property;
import com.anton.martynenko.jswrapper.jsexecution.enums.SortBy;
import com.anton.martynenko.jswrapper.jsexecution.enums.Status;
import com.anton.martynenko.jswrapper.jsexecution.problem.JsExecutionNotFoundProblem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static java.lang.String.format;

@SpringBootTest
@AutoConfigureMockMvc
class JsExecutionControllerTest {

    private Integer id;
    private Integer noSuchId = 1000;
    private JsExecution jsExecution;

    private static final String SOME_CODE = "Some code";
    private static final String NOT_VALID_CODE = "cons ole.log('Some code');";
    private static final String CREATE_JSEXECUTION_REQUEST_BODY = format("{\"scriptBody\": \"%s\"}", SOME_CODE);

//    private static final String JSON_CONTENT_TYPE = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8";
 //   private static final String JSON_PROBLEM_CONTENT_TYPE = MediaType.APPLICATION_PROBLEM_JSON_VALUE + ";charset=UTF-8";
    private static final String PLAIN_TEXT_CONTENT_TYPE = MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8";

    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private JsExecutionController jsExecutionController;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JsExecutionService jsExecutionService;

    @BeforeEach
    void prepare() throws IllegalAccessException {
        id = 1;
        jsExecution = new JsExecution(SOME_CODE);

        FieldUtils.writeField(jsExecution, "id", id, true);
    }

    @Test
    void contextLoads(){
        assertThat(jsExecutionController).isNotNull();
    }

    @Test
    void createJsExecution() throws Exception {

        when(jsExecutionService.saveAndExecute(any(JsExecution.class))).thenReturn(jsExecution);

        this.mockMvc.perform(post("/executions")
                .contentType(APPLICATION_JSON_UTF8)
                .content(CREATE_JSEXECUTION_REQUEST_BODY))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(redirectedUrl("http://localhost/executions/" + id))
                .andReturn().getResponse().getContentAsString();

        this.mockMvc.perform(post("/executions"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_UTF8_VALUE))
                .andExpect(jsonPath("title").value(org.zalando.problem.Status.BAD_REQUEST.getReasonPhrase()) )
                .andExpect(jsonPath("status").value(org.zalando.problem.Status.BAD_REQUEST.getStatusCode()) );
    }

    @Test
    void listJsExecutions() throws Exception {

        JsExecution jsExecution2 = new JsExecution(SOME_CODE);
        FieldUtils.writeField(jsExecution2, "id", 2, true);

        Collection<JsExecution> jsExecutions = Arrays.asList(jsExecution, jsExecution2);

        when(jsExecutionService.getJsExecutions(null, null)).thenReturn(Collections.EMPTY_LIST);
        when(jsExecutionService.getJsExecutions(Status.CREATED, null)).thenReturn(jsExecutions);
        when(jsExecutionService.getJsExecutions(null, SortBy.ID)).thenReturn(jsExecutions);
        when(jsExecutionService.getJsExecutions(Status.CREATED, SortBy.ID)).thenReturn(jsExecutions);

        this.mockMvc.perform(get("/executions"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.links").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.links").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content").isEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content").isArray());

        this.mockMvc.perform(get("/executions")
                .param("status", Status.CREATED.name()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.links").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.links").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content").isArray());


        this.mockMvc.perform(get("/executions")
                .param("sortBy", SortBy.ID.name()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.links").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.links").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content").isArray());

        this.mockMvc.perform(get("/executions")
                .param("sortBy", SortBy.ID.name())
                .param("status", Status.CREATED.name()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.links").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.links").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content").isArray());
    }

    @Test
    void getJsExecution() throws Exception {

        when(jsExecutionService.getJsExecution(id))
                .thenReturn(jsExecution);

        this.mockMvc.perform(get("/executions/" + id))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(jsExecution.getId() + ""))
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultValue").value(jsExecution.getResultValue()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(jsExecution.getStatus() + ""))
            .andExpect(MockMvcResultMatchers.jsonPath("$.scheduledTime").value(jsExecution.getScheduledTime()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.executionTime").value(jsExecution.getExecutionTime()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.links").isNotEmpty())
            .andExpect(MockMvcResultMatchers.jsonPath("$.links").isArray());


        when(jsExecutionService.getJsExecution(noSuchId))
                .thenThrow(new JsExecutionNotFoundProblem(noSuchId));

        this.mockMvc.perform(get("/executions/" + noSuchId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_UTF8_VALUE))
                .andExpect(jsonPath("title").value(org.zalando.problem.Status.NOT_FOUND.getReasonPhrase()) )
                .andExpect(jsonPath("status").value(org.zalando.problem.Status.NOT_FOUND.getStatusCode()) )
                .andExpect(jsonPath("detail").value(String.format("JsExecution id %d not found", noSuchId)) );
    }

    @Test
    void deleteJsExecution() throws Exception {

        when(jsExecutionService.getJsExecution(id)).thenReturn(jsExecution);


        this.mockMvc.perform(delete("/executions/{executionId}",id))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        when(jsExecutionService.getJsExecution(noSuchId))
                .thenThrow(new JsExecutionNotFoundProblem(noSuchId));


        this.mockMvc.perform(delete("/executions/{executionId}", noSuchId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_UTF8_VALUE))
                .andExpect(jsonPath("title").value(org.zalando.problem.Status.NOT_FOUND.getReasonPhrase()) )
                .andExpect(jsonPath("status").value(org.zalando.problem.Status.NOT_FOUND.getStatusCode()) )
                .andExpect(jsonPath("detail").value(String.format("JsExecution id %d not found", noSuchId)) );
    }

    @Test
    void cancelJsExecution() throws Exception {

        when(jsExecutionService.getJsExecution(id)).thenReturn(jsExecution);
        when(jsExecutionService.cancelJsExecution(jsExecution)).thenReturn(jsExecution);

        this.mockMvc.perform(delete("/executions/{id}/cancel", id))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(jsExecution.getId() + ""))
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultValue").value(jsExecution.getResultValue()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(jsExecution.getStatus() + ""))
            .andExpect(MockMvcResultMatchers.jsonPath("$.scheduledTime").value(jsExecution.getScheduledTime()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.executionTime").value(jsExecution.getExecutionTime()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.links").isNotEmpty())
            .andExpect(MockMvcResultMatchers.jsonPath("$.links").isArray());

        FieldUtils.writeField(jsExecution, "status", Status.SUCCESSFUL, true);

        this.mockMvc.perform(delete("/executions/{id}/cancel", id))
                .andDo(print())
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_UTF8_VALUE))
                .andExpect(jsonPath("title").value(org.zalando.problem.Status.METHOD_NOT_ALLOWED.getReasonPhrase()) )
                .andExpect(jsonPath("status").value(org.zalando.problem.Status.METHOD_NOT_ALLOWED.getStatusCode()) )
                .andExpect(jsonPath("detail").value(format("JsExecution with status %s cant be cancelled.", jsExecution.getStatus())) )
        ;

        when(jsExecutionService.getJsExecution(noSuchId)).thenThrow(new JsExecutionNotFoundProblem(noSuchId));

        this.mockMvc.perform(delete("/executions/{noSuchId}/cancel", noSuchId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_UTF8_VALUE))
                .andExpect(jsonPath("title").value(org.zalando.problem.Status.NOT_FOUND.getReasonPhrase()) )
                .andExpect(jsonPath("status").value(org.zalando.problem.Status.NOT_FOUND.getStatusCode()) )
                .andExpect(jsonPath("detail").value(String.format("JsExecution id %d not found", noSuchId)) );
    }

    @Test
    void getExecutionDetails() throws Exception {
        when(jsExecutionService.getJsExecution(id)).thenReturn(jsExecution);

        this.mockMvc.perform(get("/executions/{executionId}/{property}", id, Property.SCRIPTBODY))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(PLAIN_TEXT_CONTENT_TYPE))
                .andExpect(content().string(jsExecution.getScriptBody()));

        this.mockMvc.perform(get("/executions/{executionId}/{property}", id, Property.EXCEPTIONINFO))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(PLAIN_TEXT_CONTENT_TYPE))
                .andExpect(content().string(jsExecution.collectExceptionInfo()));

        this.mockMvc.perform(get("/executions/{executionId}/{property}", id, Property.ERRORLOG))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(PLAIN_TEXT_CONTENT_TYPE))
                .andExpect(content().string(jsExecution.collectErrorLog()));

        this.mockMvc.perform(get("/executions/{executionId}/{property}", id, Property.EXECUTIONLOG.name().toLowerCase()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(PLAIN_TEXT_CONTENT_TYPE))
                .andExpect(content().string(jsExecution.collectExecutionLog()));

    }

    void jsonViewShouldBeCorrectlyFilled(@NotNull JsonNode jsonNode){
        Integer id = jsonNode.get("id").asInt();
        assertThat(id).isNotNull();

        assertThat(jsonNode.get("resultValue").isNull()).isTrue();
        assertThat(jsonNode.get("status").asText()).isEqualTo(Status.CREATED.name());

        ZonedDateTime scheduledTime = ZonedDateTime.parse(jsonNode.get("scheduledTime").asText());
        assertThat(scheduledTime.toLocalDate()).isEqualTo(LocalDate.now());

        assertThat(jsonNode.get("executionTime").isNull()).isTrue();

        Iterator<JsonNode> elements = jsonNode.get("links").elements();

        assertThat(elements.hasNext()).isTrue();

        elements.forEachRemaining(linkNode -> {
            String rel = linkNode.get("rel").asText();
            String href = linkNode.get("href").asText();
            assertThat(href).isEqualTo(String.format("http://localhost/executions/%d/%s", id, rel));
        });

    }
}