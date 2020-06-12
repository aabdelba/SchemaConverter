
function goBack() {
    window.history.back();
}


function refresh() {
//        $("#loadingToggle").toggleClass("hidden");
//        $('#formToggle').toggleClass("hidden");
        location.reload();
//        window.history.back();
}


$("#oldJsonText").bind("paste", function(e){
    // access the clipboard using the api
    var pastedData = e.originalEvent.clipboardData.getData('text');
    pastedData = pastedData.replace(/\t/g,'  ');
    setTimeout(function() {$("#oldJsonText").val(pastedData);}, 0);
} );
$("#oldSchemaText").bind("paste", function(e){
    // access the clipboard using the api
    var pastedData = e.originalEvent.clipboardData.getData('text');
    pastedData = pastedData.replace(/\t/g,'  ');
    setTimeout(function() {$("#oldSchemaText").val(pastedData);}, 0);
} );
$("#newSchemaText").bind("paste", function(e){
    // access the clipboard using the api
    var pastedData = e.originalEvent.clipboardData.getData('text');
    pastedData = pastedData.replace(/\t/g,'  ');
    setTimeout(function() {$("#newSchemaText").val(pastedData);}, 0);
} );