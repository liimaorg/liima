/**
 * Trims long names on the left side when marked with class 'ellipsisLeft'
 * Elements marked with class 'ellipsisRight' are trimmed with css.
 *
 * Returns array with all left or right trimmed elements.
 */
var trim = function(clipped) {
    var clippedLeft = $(".ellipsisLeft").trimLeft();
    var clippedRight = $(".ellipsisRight").markRightTrimmed();

    var clipped = $.merge(clippedLeft, clippedRight);
    return clipped;
}

/**
 * Trims a line of a text with ellipses on left side (like css text-overflow: ellipsis will do on the right)
 * Returns an array with clipped elements.
 *
 * For this to work, the element you pass in needs to have
 * overflow: hidden
 * white-space: nowrap;
 *
 * Based on http://stackoverflow.com/questions/9793473/text-overflow-ellipsis-on-left-side
 *
 */
$.fn.trimLeft = (function(){
    var trimContents = function(row, node){
        node.classList.remove('clipped');
        while (row.scrollWidth > row.offsetWidth) {

            var childNode = node.firstChild;

            if (!childNode)
                return true;

            if (childNode.nodeType == document.TEXT_NODE){
                trimText(row, node, childNode);
            }
            else {
                var empty = trimContents(row, childNode);
                if (empty){
                    node.removeChild(childNode);
                }
            }
        }
    }
    var trimText = function(row, node, textNode){
        var value = textNode.nodeValue;
        while (row.scrollWidth > row.offsetWidth) {
            value = '...' + value.substr(4);
            textNode.nodeValue = value;
            // mark parent elements with class 'clipped'
            // this is used in app.js for showing a tooltip with complete name
            textNode.parentNode.classList.add('clipped');
            if (value == '...'){
                node.removeChild(textNode);
                return;
            }
        }
    }

    return function(row){
        var clipped = [];
        this.each(function(i, row){
            $(row).text($(row).attr("data-title"));
            trimContents(row, row);
            if(row.classList.contains("clipped")){
                clipped.push(row);
            }
        });
        return clipped;
    }
})();

/**
 *  Mark all elements with class 'clipped' that are trimmed with native css ellipsis.
 *  Returns an array with clipped elements.
 */
$.fn.markRightTrimmed = (function(){
    return function(row){
        var clipped = [];
        this.each(function(i, row){
            if(row.scrollWidth > row.offsetWidth){
                row.classList.add('clipped');
                clipped.push(row);
            }
            else{
                row.classList.remove('clipped');
            }
        });
        return clipped;
    }
})();