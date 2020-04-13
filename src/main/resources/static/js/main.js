$(function(){

    $('#form1').submit(function(){
        $('#formToggle').toggleClass("hidden");
        $('#loadingToggle').toggleClass("hidden");
    });

});

function goBack() {
    window.history.back();
}


function refresh() {
//        $("#loadingToggle").toggleClass("hidden");
//        $('#formToggle').toggleClass("hidden");
        location.reload();
//        window.history.back();
}
