package com.anton.martynenko.jswrapper.jsexecution;

import com.anton.martynenko.jswrapper.jsexecution.enums.Property;
import com.anton.martynenko.jswrapper.jsexecution.enums.SortBy;
import com.anton.martynenko.jswrapper.jsexecution.enums.Status;
import com.anton.martynenko.jswrapper.jsexecution.problem.JsExecutionNotFoundProblem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class JsExecutionControllerTest {

    private Integer id;
    private Integer noSuchId = 1000;
    private JsExecution jsExecution;

    private static final String SOME_CODE = "Some code";
    private static final String NOT_VALID_CODE = "cons ole.log('Some code');";

//    private static final String JSON_CONTENT_TYPE = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8";
 //   private static final String JSON_PROBLEM_CONTENT_TYPE = MediaType.APPLICATION_PROBLEM_JSON_VALUE + ";charset=UTF-8";
    private static final String PLAIN_TEXT_CONTENT_TYPE = MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8";

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private JsExecutionController jsExecutionController;

    @Autowired
    private JsExecutionFactory jsExecutionFactory;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JsExecutionService jsExecutionService;

    @BeforeEach
    void prepare() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        id = 1;
        jsExecution = jsExecutionFactory.createNew(SOME_CODE);
//        jsExecution.setId(id);
        Method setIdMethod = jsExecution.getClass().getDeclaredMethod("setId", Integer.class);
        setIdMethod.setAccessible(true);
        setIdMethod.invoke(jsExecution, id);
    }

    @Test
    void contextLoads(){
        assertThat(jsExecutionController).isNotNull();
    }

    @Test
    void createJsExecution() throws Exception {

        when(jsExecutionService.createJsExecution(SOME_CODE)).thenReturn(jsExecution);

        String content = this.mockMvc.perform(post("/executions")
                .param("scriptBody", SOME_CODE))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(redirectedUrl("/executions/" + id))
                .andReturn().getResponse().getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(content);

        jsonViewShouldBeCorrectlyFilled(jsonNode);

        this.mockMvc.perform(post("/executions"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_UTF8_VALUE))
                .andExpect(jsonPath("title").value(org.zalando.problem.Status.BAD_REQUEST.getReasonPhrase()) )
                .andExpect(jsonPath("status").value(org.zalando.problem.Status.BAD_REQUEST.getStatusCode()) )
                .andExpect(jsonPath("detail").value("Required request parameter 'scriptBody' for method parameter type String is not present"));

    }

    @Test
    void listJsExecutions() throws Exception {

        JsExecution jsExecution2 = jsExecutionFactory.createNew(SOME_CODE);
     //   jsExecution2.setId(2);
        Method setIdMethod = jsExecution2.getClass().getDeclaredMethod("setId", Integer.class);
        setIdMethod.setAccessible(true);
        setIdMethod.invoke(jsExecution2, 2);

        Collection<JsExecution> jsExecutions = Arrays.asList(jsExecution, jsExecution2);

        when(jsExecutionService.getJsExecutions(null, null)).thenReturn(Collections.EMPTY_LIST);
        when(jsExecutionService.getJsExecutions(Status.CREATED, null)).thenReturn(jsExecutions);
        when(jsExecutionService.getJsExecutions(null, SortBy.ID)).thenReturn(jsExecutions);
        when(jsExecutionService.getJsExecutions(Status.CREATED, SortBy.ID)).thenReturn(jsExecutions);

        this.mockMvc.perform(get("/executions"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(content().string("[]"))
                .andReturn().getResponse().getContentAsString();

        String content = this.mockMvc.perform(get("/executions")
                .param("status", Status.SUCCESS.name()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andReturn().getResponse().getContentAsString();

        JsonNode array = objectMapper.readTree(content);
        array.elements().forEachRemaining(this::jsonViewShouldBeCorrectlyFilled);


        content = this.mockMvc.perform(get("/executions")
                .param("sortBy", SortBy.ID.name()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andReturn().getResponse().getContentAsString();

        array = objectMapper.readTree(content);
        array.elements().forEachRemaining(this::jsonViewShouldBeCorrectlyFilled);

        content = this.mockMvc.perform(get("/executions")
                .param("sortBy", SortBy.ID.name())
                .param("status", Status.SUCCESS.name()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andReturn().getResponse().getContentAsString();

        array = objectMapper.readTree(content);
        array.elements().forEachRemaining(this::jsonViewShouldBeCorrectlyFilled);
    }

    @Test
    void getJsExecution() throws Exception {

        when(jsExecutionService.getJsExecution(id))
                .thenReturn(jsExecution);

        String content = this.mockMvc.perform(get("/executions/" + id))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andReturn().getResponse().getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(content);

        jsonViewShouldBeCorrectlyFilled(jsonNode);

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
    void createJsExecutionStopRequest() throws Exception {

        when(jsExecutionService.getJsExecution(id)).thenReturn(jsExecution);
        when(jsExecutionService.stopJsExecution(jsExecution)).thenReturn(jsExecution);

        String content = this.mockMvc.perform(post("/executions/{id}/stoprequest", id))
                .andDo(print())
                .andExpect(status().isAccepted())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andReturn().getResponse().getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(content);

        jsonViewShouldBeCorrectlyFilled(jsonNode);

        when(jsExecutionService.getJsExecution(noSuchId)).thenThrow(new JsExecutionNotFoundProblem(noSuchId));

        this.mockMvc.perform(post("/executions/{noSuchId}/stoprequest", noSuchId))
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