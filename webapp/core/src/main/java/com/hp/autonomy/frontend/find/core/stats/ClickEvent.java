/*
 * Copyright 2014-2016 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.core.stats;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hpe.bigdata.frontend.spring.authentication.AuthenticationInformationRetriever;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class ClickEvent extends FindEvent {

    @JsonProperty("click-type")
    private final ClickType clickType;

    public ClickEvent(
        final String search,
        final ClickType clickType,
        final AuthenticationInformationRetriever<?, ?> authenticationInformationRetriever
    ) {
        super(search, authenticationInformationRetriever);

        this.clickType = clickType;
    }
}
