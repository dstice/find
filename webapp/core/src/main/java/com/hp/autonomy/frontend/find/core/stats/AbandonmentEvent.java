/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.stats;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.hpe.bigdata.frontend.spring.authentication.AuthenticationInformationRetriever;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName(AbandonmentEvent.TYPE)
public class AbandonmentEvent extends ClickEvent {

    static final String TYPE = "abandonment";

    public AbandonmentEvent(
        @JsonProperty("search") final String search,
        @JsonProperty("click-type") final ClickType clickType,
        @JacksonInject final AuthenticationInformationRetriever<?, ?> authenticationInformationRetriever
    ) {
        super(search, clickType, authenticationInformationRetriever);
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
