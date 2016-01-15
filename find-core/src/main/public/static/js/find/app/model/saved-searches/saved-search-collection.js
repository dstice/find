define([
    'backbone',
    'find/app/model/saved-searches/saved-search-model'
], function(Backbone, SavedSearchModel) {

    return Backbone.Collection.extend({
        url: '../api/public/saved-search',
        model: SavedSearchModel
    });

});
