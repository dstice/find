/*
 * Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'backbone',
    'find/app/configuration',
    'i18n!find/nls/bundle',
    'find/app/page/search/document/document-detail-tabs',
    'find/app/model/document-model'
], function(_, Backbone, configuration, i18n, documentDetailTabs, DocumentModel) {
    'use strict';

    function filterTabs(model) {
        return _.filter(documentDetailTabs, function(tab) {
            return tab.shown(model);
        });
    }

    function byTitleKey(tabs, titleKey) {
        return _.findWhere(tabs, {title: i18n[titleKey]});
    }

    describe('Document detail tabs', function() {
        describe('with the location tab enabled', function() {
            beforeEach(function() {
                configuration.and.returnValue({
                    map: {
                        enabled: true
                    },
                    hasBiRole: true
                });
            });

            it('displays every tab for a complete model with map enabled', function() {
                const model = new DocumentModel({
                    authors: ['Humbert', 'Gereon'],
                    media: true,
                    sourceType: 'news',
                    thumbnail: 'VGhlIGJhc2UgNjQgZW5jb2RlZCB0aHVtYm5haWw=',
                    transcript: 'test transcript',
                    locations: [
                        {
                            displayName: 'test',
                            latitude: 12.5,
                            longitude: 42.2
                        }
                    ],
                    url: true
                });

                expect(filterTabs(model)).toHaveLength(documentDetailTabs.length);
            });

            it('displays default tabs for an empty model', function() {
                const model = new DocumentModel({
                    authors: [],
                    latitude: undefined,
                    longitude: undefined
                });

                expect(filterTabs(model)).toHaveLength(3);
            });

            it('displays the author tab when an authors attribute is present', function() {
                const model = new DocumentModel({
                    authors: ['Humbert', 'Gereon']
                });

                const tabs = filterTabs(model);
                expect(tabs).toHaveLength(4);
                expect(byTitleKey(tabs, 'search.document.detail.tabs.authors')).toBeDefined();
            });

            it('displays similar sources tab when a source attribute is present', function() {
                const model = new DocumentModel({
                    sourceType: 'News'
                });

                const tabs = filterTabs(model);
                expect(tabs).toHaveLength(4);
                expect(byTitleKey(tabs, 'search.document.detail.tabs.similarSources')).toBeDefined();
            });

            it('displays the transcript tab when the model has transcript, media and url attributes', function() {
                const model = new DocumentModel({
                    media: true,
                    url: true,
                    transcript: 'test transcript'
                });

                const tabs = filterTabs(model);
                expect(tabs).toHaveLength(4);
                expect(byTitleKey(tabs, 'search.document.detail.tabs.transcript')).toBeDefined();
            });

            it('displays the location tab if the locations property is preset', function() {
                const model = new DocumentModel({
                    locations: [
                        {
                            displayName: 'test',
                            latitude: 12.5,
                            longitude: 42.2
                        }
                    ]
                });

                const tabs = filterTabs(model);
                expect(tabs).toHaveLength(4);
                expect(byTitleKey(tabs, 'search.document.detail.tabs.location')).toBeDefined();
            });

            it('does not display the location tab if the locations property is absent', function() {
                const model = new DocumentModel({locations: undefined});

                const tabs = filterTabs(model);
                expect(tabs).toHaveLength(3);
                expect(byTitleKey(tabs, 'search.document.detail.tabs.location')).toBeUndefined();
            });
        });

        describe('with the location tab disabled', function() {
            beforeEach(function() {
                configuration.and.returnValue({
                    map: {
                        enabled: false
                    }
                });
            });

            it('does not display the location tab even if longitude and latitude attributes are present', function() {
                const model = new DocumentModel({
                    longitude: 123.4,
                    latitude: 73
                });

                const tabs = filterTabs(model);
                expect(tabs).toHaveLength(2);
                expect(byTitleKey(tabs, 'search.document.detail.tabs.location')).toBeUndefined();
            });
        });
    });
});
