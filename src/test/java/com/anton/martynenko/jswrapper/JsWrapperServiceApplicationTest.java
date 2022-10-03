package com.anton.martynenko.jswrapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
class JsWrapperServiceApplicationTest {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ThreadPoolTaskExecutor threadPoolTaskExecutor;

	@Test
	void contextLoads() {
		assertThat(objectMapper).isNotNull();
		assertThat(threadPoolTaskExecutor).isNotNull();
	}

}
