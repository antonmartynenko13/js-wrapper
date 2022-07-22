package com.anton.martynenko.jswrapper.jsexecution;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JsExecutionFactoryTest {

    private static final String VALID_CODE_EXAMPLE = "var i = null;";

    @Autowired
    private JsExecutionFactory jsExecutionFactory;

    @Test
    void contextLoads(){
        assertThat(jsExecutionFactory).isNotNull();
    }

    @Test
    void createNew() {
        assertThat(jsExecutionFactory.createNew(VALID_CODE_EXAMPLE)).isNotNull();
    }
}