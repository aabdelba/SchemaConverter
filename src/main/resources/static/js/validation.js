
function validateForm() {
  var oldJsonNotEmpty = validateNotEmpty("oldJsonText","oldJsonFiles","Old JSON");
  var oldSchemaNotEmpty = validateNotEmpty("oldSchemaText","oldSchemaFile","Old schema");
  var newSchemaNotEmpty = validateNotEmpty("newSchemaText","newSchemaFile","New schema");
  if(oldJsonNotEmpty && oldSchemaNotEmpty && newSchemaNotEmpty) {
    $('#formToggle').toggleClass("hidden");
    $('#loadingToggle').toggleClass("hidden");
    return true;
  } else {
    return false;
  }
}

function validateNotEmpty(text,file,message) {
  var oldJsonText = document.forms["form1"][text.valueOf()].value;
  var oldJsonFilesLength = document.forms["form1"][file.valueOf()].files.length;
  if (oldJsonText == "" && oldJsonFilesLength == 0) {
    alert(message.valueOf()+" must be provided");
    return false;
  } else {
    return true
  }
}


