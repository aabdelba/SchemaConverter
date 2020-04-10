$(function(){

    $('#form1').submit(function(){
    $('#formLoadingToggle').html(

'    <div class="row empty-100-row"></div>                                      '+



'    <p class="text-center">Running the conversion...</p>                       '+

'    <div class="row empty-10-row"></div>                                       '+

'    <div class="center-button">                                                '+
'        <div class="spinner-border text-primary" role="status">                '+
'            <span class="sr-only">Loading...</span>                            '+
'        </div>                                                                 '+
'    </div>                                                                     '+

'    <div class="row empty-25-row"></div>                                       '+

'    <div class="center-button">                                                '+

'        <form action="/form" method="post" id="cancelForm">                    '+
'            <button class="btn btn-danger" type="submit" form="cancelForm"     '+
'                                           value="Submit">Cancel</button>      '+
'        </form>                                                                '+

'    </div>                                                                     '+

'    <div class="row empty-100-row"></div>                                      '






        );
    });
});