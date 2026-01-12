/**
 * tooltips for info icons, based on jquerytools tooltips
 */
var propertyTooltips = function() {
    // hide all tooltips
    $('--property--info-box').hide();

    // create jquerytools tooltip
    $(".info").tooltip({
        position: 'bottom',
        events: {
            // tooltip should not remain open when pointer is moved over element
            def: "mouseenter, mouseleave"
        }
    });
}