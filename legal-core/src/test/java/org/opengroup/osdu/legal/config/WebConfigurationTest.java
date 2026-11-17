package org.opengroup.osdu.legal.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

class WebConfigurationTest {

    @ParameterizedTest
    @ValueSource(strings = {"/legaltags", "/legaltags/"})
    void shouldPreservePostBodyAndQueryWithEitherTrailingSlash(String path) throws Exception {
        MockMvcBuilders.standaloneSetup(new ProbeController())
            .addFilters(new WebConfiguration().trailingSlashFilter().getFilter())
            .build()
            .perform(post(path).queryParam("mode", "validate")
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"tag\"}"))
            .andExpect(status().isOk())
            .andExpect(content().string("validate:{\"name\":\"tag\"}"));
    }

    @RestController
    static class ProbeController {
        @PostMapping("/legaltags")
        public String echo(@RequestBody String body, @RequestParam("mode") String mode) {
            return mode + ":" + body;
        }
    }
}
