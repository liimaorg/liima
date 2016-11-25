var globalTags = [];

jQuery(document).ready(function($) {
    $('.tokenfield').tokenfield({
        autocomplete: {
            source: globalTags,
            delay: 100,
        },
        showAutocompleteOnFocus: true,
        delimiter: [',',' ',],
        createTokensOnBlur: true
    });
    $('.tokenfield').on('tokenfield:createtoken', function (event) {
        var existingTokens = $(this).tokenfield('getTokens');
        $.each(existingTokens, function(index, token) {
            if (token.value === event.attrs.value)
                event.preventDefault();
        });

        removeFromList(globalTags, event.attrs.value);
    });
});

function removeFromList(list, value){
    var idx = list.indexOf(value);
    if (idx != -1) {
        list.splice(idx, 1);
    }
    //$('#tokenfield').tokenfield("option", { source: list });
}