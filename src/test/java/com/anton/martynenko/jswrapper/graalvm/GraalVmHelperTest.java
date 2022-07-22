package com.anton.martynenko.jswrapper.graalvm;

import com.anton.martynenko.jswrapper.jsexecution.problem.JsExecutionCanNotBeExecutedProblem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


@SpringBootTest
class GraalVmHelperTest {

    private static final String VALID_CODE_EXAMPLE2 = "var i = null;";
    private static final String INVALID_CODE_EXAMPLE = "cons ole.log('Some JS code')";

    @Autowired
    private GraalVmHelper graalVmHelper;

    @Test
    void contextLoads() {
        assertThat(graalVmHelper).isNotNull();
    }

    @Test
    void validate() {
        graalVmHelper.validate(VALID_CODE_EXAMPLE2, Language.JS);

        JsExecutionCanNotBeExecutedProblem problem = assertThrows(JsExecutionCanNotBeExecutedProblem.class, () -> graalVmHelper.validate(INVALID_CODE_EXAMPLE, Language.JS));

        assertThat(problem.getTitle()).isEqualTo("Code parsing ends with problem");
        assertThat(problem.getStatus().getStatusCode()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
        assertThat(problem.getType()).isEqualTo(Problem.DEFAULT_TYPE);
    }
}