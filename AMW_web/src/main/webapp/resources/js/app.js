
/* Update long names and tooltips*/
var updateElements = function(){
	trim();
	propertyTooltips();
};

function setUpperContext(inputField, propertyValue, isDefinedInCurrentContext) {
	var value = inputField.value;
	if (value == propertyValue) {
		if (isDefinedInCurrentContext == 'false') {
			$(inputField).addClass("upperContext");
			$(inputField).removeClass("currentContext");
		}
	}
}

function validateInput(inputField, isNullable, isOptional, validationRegexp, defaultValue, mik) {
	var value = inputField.value;

    if(isNullable == 'false' && isOptional == 'false' && value == '' && defaultValue == '' && mik == ''){
    	$(inputField).addClass("fieldNoValueValidationError");
    }else{
    	$(inputField).removeClass("fieldNoValueValidationError");
    }

    if((isNullable == 'true' || isOptional == 'true') && value == '' && defaultValue == '' && mik != ''){
    	$(inputField).removeClass("fieldValidationError");
    }
	else{
        var regexMatchValue = value;
        if(value == ''){
            regexMatchValue = defaultValue;
        }

        if (regexMatchValue.match(validationRegexp) != regexMatchValue) {
			var matches = regexMatchValue.match(validationRegexp);
			if (matches && matches.constructor === Array) {
				var len = matches.length;
				for (var i = 0; i < len; i++) {
					if (matches[i] == regexMatchValue) {
						$(inputField).removeClass("fieldValidationError");
						return;
					}
				}
				$(inputField).addClass("fieldValidationError");
			} else {
				$(inputField).addClass("fieldValidationError");
			}
        } else {
        	$(inputField).removeClass("fieldValidationError");
        }
    }
}

function decorateInput(inputField, mik, longValue, longDefaultValue) {
    if (mik != '') {
        $(inputField).addClass("dynaProp");
    }
    if (longValue == 'false' && longDefaultValue == 'false') {
        $(inputField).addClass("oneLine");
    }
    else {
        $(inputField).removeClass("oneLine");
    }
}

function checkReset(actualElement, replacedValue, propertyValue){
    var element = $(actualElement).parent().find('textarea:first');

    if(element.length === 0){
        element = $(actualElement).parent().find('input[type="password"]:first');
    }

    element.toggleClass("upperContext",actualElement.checked);
    element.toggleClass("currentContext",!actualElement.checked);

    if(actualElement.checked){
        element.val(replacedValue);
    }else{
        element.val(propertyValue);
    }
}

$(document).ready(function() {
	updateElements();
	$("#mainform").attr("autocomplete", "off");
});

(function($,sr){
	// debouncing function from John Hann
	// http://unscriptable.com/index.php/2009/03/20/debouncing-javascript-methods/
	var debounce = function (func, threshold, execAsap) {
		var timeout;

		return function debounced () {
			var obj = this, args = arguments;
			function delayed () {
				if (!execAsap)
					func.apply(obj, args);
				timeout = null;
			}

			if (timeout)
				clearTimeout(timeout);
			else if (execAsap)
				func.apply(obj, args);

			timeout = setTimeout(delayed, threshold || 100);
		};
	};
	// smartresize
	jQuery.fn[sr] = function(fn){  return fn ? this.bind('resize', debounce(fn)) : this.trigger(sr); };

})(jQuery,'smartresize');


$(window).smartresize(function(){
	updateElements();
});