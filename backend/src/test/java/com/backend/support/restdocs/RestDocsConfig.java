package com.backend.support.restdocs;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.operation.preprocess.Preprocessors;

@TestConfiguration
public class RestDocsConfig {

    @Bean
    public RestDocumentationResultHandler restDocumentationResultHandler() {
        return MockMvcRestDocumentation.document("{method-name}",
                Preprocessors.preprocessRequest(
                        Preprocessors.modifyHeaders()
                                .remove("Content-Length")
                                .remove("Host"),
                        Preprocessors.prettyPrint()
                ),
                Preprocessors.preprocessResponse(
                        Preprocessors.modifyHeaders()
                                .remove("Content-Length")
                                .remove("X-Content-Type-Options")
                                .remove("X-XSS-Protection")
                                .remove("X-Frame-Options")
                                .remove("Cache-Control")
                                .remove("Pragma")
                                .remove("Vary")
                                .remove("Expires"),
                        Preprocessors.prettyPrint()
                )
        );
    }

}
