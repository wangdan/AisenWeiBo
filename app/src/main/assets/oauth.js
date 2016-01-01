<script type="text/javascript">

function fillAccount() {
    document.getElementById("userId").value = '%username%';
    document.getElementById("passwd").value = '%password%';
    document.getElementById("submit").click();
}

function getAccount() {
    window.loginjs.setAccount(document.getElementById("userId").value, document.getElementById("passwd").value);
}

</script>