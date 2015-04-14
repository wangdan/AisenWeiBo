<script type="text/javascript">

function fillAccount(){
    document.getElementById("loginName").value = '%username%';
    document.getElementById("loginPassword").value = '%password%';
    document.getElementById("loginAction").click();
}

function getAccount(){
    window.loginjs.setAccount(document.getElementById("loginName").value, document.getElementById("loginPassword").value);
}

</script>