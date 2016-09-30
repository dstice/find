/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */
package com.hp.autonomy.frontend.find.core.savedsearches;

import org.junit.Test;

import java.util.*;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public abstract class AbstractSavedSearchTest<T extends SavedSearch<T>> {
    protected abstract SavedSearch.Builder<T> createBuilder();

    @Test
    public void toQueryTextWithNoConceptClusters() {
        final SavedSearch<T> search = createBuilder()
                .setConceptClusterPhrases(Collections.singleton(new ConceptClusterPhrase("cats", true, -1)))
                .build();

        assertThat(search.toQueryText(), is("(cats)"));
    }

    @Test
    public void toQueryTextWithConceptClusters() {
        final Set<ConceptClusterPhrase> conceptClusterPhrases = new HashSet<>(Arrays.asList(
                new ConceptClusterPhrase("\"fault line\"", true, 0),
                new ConceptClusterPhrase("\"impending doom\"", false, 0),
                new ConceptClusterPhrase("\"california\"", false, 0),
                new ConceptClusterPhrase("\"luke skywalker\"", true, 1),
                new ConceptClusterPhrase("raccoons", true, 2)
        ));

        final SavedSearch<T> search = createBuilder()
                .setConceptClusterPhrases(conceptClusterPhrases)
                .build();

        final String queryText = search.toQueryText();
        final List<String> concepts = new ArrayList<>(Arrays.asList(queryText.split(" AND ")));
        assertThat(concepts, hasSize(3));
        assertThat(concepts, hasItem("(\"luke skywalker\")"));
        assertThat(concepts, hasItem("(raccoons)"));

        concepts.remove("(\"luke skywalker\")");
        concepts.remove("(raccoons)");

        final List<String> clusterConcepts = Arrays.asList(concepts.get(0)
                                                                   .substring(1, concepts.get(0).length() - 1)
                                                                   .replace("\" \"", "\"\n\"")
                                                                   .split("\n"));
        assertThat(clusterConcepts, hasSize(3));
        assertThat(clusterConcepts.get(0), is("\"fault line\""));
        assertThat(clusterConcepts, hasItem("\"california\""));
        assertThat(clusterConcepts, hasItem("\"impending doom\""));
    }
}
