define([
    'backbone',
    'jquery',
    'underscore',
    'find/app/util/database-name-resolver',
    'find/app/util/string-blank',
    'text!find/templates/app/page/search/input-view.html',
    'typeahead',
    'bootstrap'
], function (Backbone, $, _, databaseNameResolver, stringBlank, template) {
    // don't send typeahead query unless search string is at least this long
    const QUERY_MIN_CHARS = 2;
    // don't send typeahead query unless user hasn't typed anything for this long
    const QUERY_DEBOUNCE_MS = 400;

    return Backbone.View.extend({
        template: _.template(template),

        events: {
            'submit .find-form': function (event) {
                event.preventDefault();

                if (this.enableTypeAhead) {
                    this.search(this.$input.typeahead('val'));
                    this.$input.typeahead('close');
                } else {
                    this.search(this.$input.val());
                }
            },
            'typeahead:select': function () {
                if (this.enableTypeAhead) {
                    this.search(this.$input.typeahead('val'));
                }
            },
            'focus .find-input': function () {
                this.strategy.inFocus(this.$('.find-input-button'));
            },
            'blur .find-input': function () {
                this.strategy.onBlur(this.$('.find-input-button'));
            }
        },

        initialize: function (options) {
            this.strategy = options.strategy;
            this.strategy.initialize(this);
            this.enableTypeAhead = options.enableTypeAhead;
            // XHR for ongoing typeahead query
            this.queryXhr = null;
        },

        render: function () {
            this.$el.html(this.template({
                placeholder: this.strategy.placeholder,
                inputClass: this.strategy.inputClass
            }));

            this.$input = this.$('.find-input');

            if (this.enableTypeAhead) {
                // Prevent the Twitter right-arrow-key listener, since there's problems with text being overwritten
                // by the suggestion when trying to scroll to the end.
                this.$input.on('keydown', function(evt){
                    if (evt.keyCode === 39) {
                        evt.stopImmediatePropagation();
                    }
                });

                this.$input.typeahead({
                    hint: false,
                    highlight: true,
                    minLength: QUERY_MIN_CHARS
                }, {
                    async: true,
                    limit: 7,
                    source: _.debounce(this.query.bind(this), QUERY_DEBOUNCE_MS)
                });

            }

            this.updateText();
        },

        focus: function () {
            _.defer(function () {
                this.$input.focus();
            }.bind(this));
        },

        unFocus: function () {
            _.defer(function () {
                this.$input.blur();
            }.bind(this));
        },

        query: function (query, sync, async) {
            // Don't look for suggestions if the query is blank
            if (stringBlank(query)) {
                sync([]);
            } else {
                if (this.queryXhr) {
                    this.queryXhr.abort();
                }
                const queryXhr = this.queryXhr = $.get('api/public/typeahead', {
                    text: query
                }, function (results) {
                    if (this.queryXhr === queryXhr) {
                        this.queryXhr = null;
                    }
                    async(results);
                });
            }
        },

        search: function (query) {
            this.strategy.onTextUpdate($.trim(query));
        },

        updateText: function () {
            if (this.$input) {
                const newValue = this.strategy.onExternalUpdate();

                if (this.enableTypeAhead) {
                    this.$input.typeahead('val', newValue);
                } else {
                    this.$input.val(newValue);
                }
            }
        }
    });

});
