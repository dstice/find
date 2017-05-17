/*
 * Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.users;

import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class IdolUserControllerIT extends AbstractFindIT {
    @Test
    public void searchUsers() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = get(IdolUserController.BASE_PATH +
                '/' + IdolUserController.SEARCH_PATH + '/')
                .param(IdolUserController.PARAMETER_SEARCH_TEXT, "*")
                .param(IdolUserController.PARAMETER_START_USER, "0")
                .param(IdolUserController.PARAMETER_MAX_USERS, "5")
                .contentType(MediaType.APPLICATION_JSON)
                .with(authentication(biAuth()));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
    }
}
