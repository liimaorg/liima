var changedWarning = "There are unsaved changes which will be lost if you continue. Would you like to continue?";

jQuery(document).ready(function ($) {
    refreshChanges();
    refreshSubChanges();
    // will be called when we try to leave the page
    window.onbeforeunload = function (e) {
        var hasContentChangedValue = false;
        if (typeof hasContentChanged === "function") {
            // the function is only defined on the template, function and globalFunction page
            hasContentChangedValue = hasContentChanged();
        }

        if (!disabledChangeCheck && (isFormUnsaved() || isSubFormUnsaved() || hasContentChangedValue)) {
            hideLoader();
            return changedWarning;
        }
    };
});

function refreshChanges() {
    $('.changeAware, .changeAware input').each(function () {
        handleChanges(this);
    });
}

function refreshSubChanges() {
    $('.subChangeAware, .subChangeAware input').each(function () {
        handleChanges(this);
    });
}

function handleChanges(element) {
    if ($(element).is(':checkbox')) {
        var initialValue = '';
        if ($(element).is(':checked')) {
            initialValue = 'checked';
        } else {
            initialValue = 'unchecked';
        }
        $(element).data('initialValue', initialValue);
    } else {
        $(element).data('initialValue', $(element).val());
    }
    $(element).data('defaultValue', $(element).attr('title'));
}

function confirmLeave(event) {
    disableChangeCheck();
    var result = (!isFormUnsaved() && !isSubFormUnsaved()) || window.confirm(changedWarning);
    if (!result) {
        disabledChangeCheck = false;
        cancelEvent(event);
        hideLoader();
    }
    return result;
}

function confirmSubChangeLeave(event) {
    var result = !isSubFormUnsaved() || window.confirm(changedWarning);
    if (!result) {
        cancelEvent(event);
        hideLoader();
    }
    return result;
}

function cancelEvent(event) {
    event.cancel = true;
    event.returnValue = false;
    event.cancelBubble = true;
    if (event.stopPropagation) {
        event.stopPropagation();
    }
    if (event.preventDefault) {
        event.preventDefault();
    }
}

function isFormUnsaved() {
    return isFormDirty('.changeAware, .changeAware input');
}

function isSubFormUnsaved() {
    return isFormDirty('.subChangeAware, .subChangeAware input');
}

function isFormDirty(selector) {
    var isDirty = false;

    $(selector).each(function () {
        var inputValue = $(this).val();

        if ($(this).is(':checkbox')) {
            if ($(this).is(':checked')) {
                inputValue = 'checked';
            } else {
                inputValue = 'unchecked';
            }
        }

        // is not equal to the original value
        if ($(this).data('initialValue') != inputValue) {
            // is not null or ''
            // and not equal to the defaultValue (gets set by the richfaces placeholder)
            // and initial value has been defined
            if ((inputValue != null && inputValue.trim() != '')
                && (inputValue.trim() != $(this).data('defaultValue'))
                && ($(this).data('initialValue') !== undefined)) {
                isDirty = true;
            }
        }
    });
    return isDirty;
}

var disabledChangeCheck = false;

function disableChangeCheck() {
    disabledChangeCheck = true;
}
