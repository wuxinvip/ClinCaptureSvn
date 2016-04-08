function leftnavExpand(strLeftNavRowElementName){
	var objLeftNavRowElement;
	var objExCl;
	objLeftNavRowElement = MM_findObj(strLeftNavRowElementName);
	if (objLeftNavRowElement != null) {
		if (objLeftNavRowElement.style) {
			objLeftNavRowElement = objLeftNavRowElement.style;
		}
		objLeftNavRowElement.display = (objLeftNavRowElement.display == "none" ) ? "" : "none";
	}
	objExCl = MM_findObj("excl_"+strLeftNavRowElementName);
	if (objExCl != null) {
		var newThemeColor = "${newThemeColor}";
		newThemeColor = newThemeColor == "green" || newThemeColor == "violet" || newThemeColor == "darkBlue" ? ("/" + newThemeColor) : "";
		if(objLeftNavRowElement.display == "none"){
			objExCl.src = "images" + newThemeColor + "/bt_Expand.gif";
		}else{
			objExCl.src = "images" + newThemeColor + "/bt_Collapse.gif";
		}
	}
}

function leftnavExpandExt(strLeftNavRowElementName){
	$("div[id="+strLeftNavRowElementName+"]").each(function() {
		leftnavExpand(strLeftNavRowElementName+'_3');
		leftnavExpand(strLeftNavRowElementName+'_4');
	});
}

function showMoreFields(index, name) {
	switch (name) {
		case 'RFC':
			var rowId = 'dnRFCDescriptionRow';
			break;
		case 'updateDescriptions':
			var rowId = 'dnUpdateDescriptionRow';
			break;
		case 'closeDescriptions':
			var rowId = 'dnCloseDescriptionRow';
			break;
	}
	for (var j=index+1; (j<26)&&(j<(index+3)); j++){
		$("tr#"+rowId+"_a"+j).show();
		$("tr#"+rowId+"_b"+j).show();
		$("tr#"+rowId+"_c"+j).show();
	}
}

$(document).ready(function() {
	var sections = new Array('3', '4', '5', '6', '6_1', '6_2', '6_3', '7');
	for (var j=0; j < sections.length; j++){
		if ($("div#section" + sections[j] + " span.alert").text() != '') {
			leftnavExpand("section" + sections[j]);
		}
	}
});

function allFieldsForGeneratedIdAreValid() {
	var result = true;
	var separator = $("input[name=autoGeneratedSeparator]").val();
	var separatorValidationPattern = $("input[name=autoGeneratedSeparator]").attr("validationPattern");
	var prefix = $("input[name=autoGeneratedPrefix]").val();
	var prefixValidationPattern = $("input[name=autoGeneratedPrefix]").attr("validationPattern");
	var prefixRegexp = new RegExp(prefixValidationPattern, "gm");
	var separatorRegexp = new RegExp(separatorValidationPattern, "gm");
	var submitButton = $("input[type=submit]");
	var prefixErrorMessage = $("#autoGeneratedPrefixErrorMessage").text();
	var separatorErrorMessage = $("#autoGeneratedSeparatorErrorMessage").text();
	var prefixMessage = $("#spanAlert-autoGeneratedPrefix");
	prefixMessage.text(prefixErrorMessage);
	var separatorMessage = $("#spanAlert-autoGeneratedSeparator");
	separatorMessage.text(separatorErrorMessage);
	if (prefixRegexp.test(prefix)) {
		prefixMessage.show();
		result = false;
	} else {
		prefixMessage.hide();
	}
	if (separatorRegexp.test(separator)) {
		separatorMessage.show();
		result = false
	} else {
		separatorMessage.hide();
	}
	if (!result) {
		submitButton.attr("disabled",true);
	} else {
		submitButton.removeAttr("disabled");
	}
	return result;
}

function buildDynamicLabel(labelId) {
	if (allFieldsForGeneratedIdAreValid()) {
		var dynamicLabel = $("#" + labelId);
		dynamicLabel.text("");
		var selector = dynamicLabel.attr("objectsSelector");
		$(selector).each(function () {
			var text = dynamicLabel.text();
			if ($(this).attr("processorMode") == 'xGenerator') {
				var xText = "";
				for (var i = 0; i < parseInt($(this).val()); i++) {
					xText += "X";
				}
				text += xText;
			} else {
				text += $(this).val();
			}
			dynamicLabel.text(text);
		});
	}
}

function additionalStudyParameterHandler(index, value, clear) {
	var selectInput = $("#studyConfigurationParameter-" + index);
	var textInput = $("#additionalStudyConfigurationParameter-" + index);
	if (selectInput.val() == value) {
		textInput.removeClass("hidden");
		if (clear) {
			textInput.val("");
		}
	} else {
		textInput.addClass("hidden").val(selectInput.val().replace(/-=custom=-/g, ""));
	}
}

function hideStudyParameterRowsForValue(obj, rowClassName, value) {
	var classNames = rowClassName.split(" ");
	for (var i = 0; i < classNames.length; i++) {
		var trObject = $("tr." + classNames[i]);
		if (obj.val() == value) {
			trObject.hide();
		} else {
			trObject.show();
			if (trObject.hasClass("simulateRadioOnClick")) {
				trObject.find("input[type=radio]:checked").click();
				break;
			}
		}
	}
}

