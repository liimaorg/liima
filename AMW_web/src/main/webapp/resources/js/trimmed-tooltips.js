/**
 * Invoke tooltips for trimmed elements.
 * http://jquerytools.org/documentation
 */
var trimmedTooltips = function(clipped) {
    // hide all tooltips
    $('.tooltip').hide();

    // mark existing tooltips hidden
    $('.titleTooltip').hideExistingTooltips();

    // preserve content of title element in data-title (title will be removed later...)
    $('.ellipsisRight,.ellipsisLeft').transferTitleToDataTitle();

    $.each(clipped, function(index, value){

        if(typeof $(value).data("tooltip") == 'undefined') {
            // ensure that a title is set, the jquerytools needs it to create the tooltip
            if ($(value).attr('data-title')) {
                $(value).transferDataTitleToTitle();
            }
            if (value.title) {
                // create jquerytools tooltip
                var tooltip = $(value).tooltip({
                    tipClass: 'tooltip titleTooltip',
                    position: 'bottom center'
                });

                // hack to hide new tooltip
                tooltip.data("tooltip").show();
                tooltip.data("tooltip").hide();

                // suppress native tooltip by removing the title element
                $(value).removeTitle();
            }
        }
        // unhide tooltip for clipped element
        $(value).showExistingTooltips();
    });
};

/**
 * Writes content from attribute title into data-title.
 * Removes attribute title.
 */
$.fn.transferTitleToDataTitle =  (function(){
    return function(row){
        this.each(function(i, row){
            var dataTitle = $(row).attr("data-title");
            var title = $(row).attr("title");
            if(title && !dataTitle){
                $(row).attr("data-title", title);
                dataTitle = $(row).attr("data-title");
            }
            $(row).removeAttr("title");
        });
        return this;
    }
})();

/**
 * Writes content from attribute data-title into title.
 * Removes attribute title from elements.
 */
$.fn.transferDataTitleToTitle =  (function(){
    return function(row){
        this.each(function(i, row){
            $(row).attr("title", $(row).attr("data-title"));
        });
        return this;
    }
})();

/**
 * Removes attribute title from elements
 */
$.fn.removeTitle =  (function(){
    return function(row){
        this.each(function(i, row){
            $(row).removeAttr("title");
        });
        return this;
    }
})();

/**
 * Adds class 'hidden' to elements
 */
$.fn.hideExistingTooltips = (function(){
    return function(row){
        this.each(function(i, row){
            row.classList.add("hidden");
        });
        return this;
    }
})();

/**
 * Removes class 'hidden' from elements if tooltip.getTip() is defined.
 */
$.fn.showExistingTooltips = (function(){
    return function(row){
        this.each(function(i, row){
            var tooltip = $(row).data("tooltip");
            if(typeof(tooltip)!='undefined' && typeof(tooltip.getTip()) != 'undefined'){
                tooltip.getTip().get(0).classList.remove("hidden");
            }
        });
        return this;
    }
})();