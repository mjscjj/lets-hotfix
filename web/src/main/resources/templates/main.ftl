<!doctype html>
<html lang="en">
<head>
    <!-- Required meta tags -->
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">

    <!-- Bootstrap CSS -->
    <link rel="stylesheet"
          href="https://stackpath.bootstrapcdn.com/bootstrap/4.2.1/css/bootstrap.min.css"
          integrity="sha384-GJzZqFGwb1QTTN6wy59ffF1BuGJpLSa9DkKMp0DgiMDm4iYMj70gZWKYbI706tWS"
          crossorigin="anonymous">

    <!-- Latest compiled and minified CSS -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-select/1.13.2/css/bootstrap-select.min.css">

    <title>Reloading</title>
</head>
<body>

<h5>${hostname!""}</h5>
<h5>${hostIp!""}</h5>
<div>

<form method="post" enctype="multipart/form-data" id="reloadForm">
    <div class="form-row">
        <div class="form-group col-md-12">
            <label for="targetClass">To Be Replaced Class</label>
            <input type="text" class="form-control" id="targetClass" name="targetClass">
        </div>
    </div>
    <div class="form-group">
        <label for="classFile">To Replace Class File</label>
        <input type="file" id="file" name="file"/>
    </div>
    <div class="form-group">
        <label for="targetPid">Target Process</label>
        <select id="targetPid" class="selectpicker" name="targetPid" data-live-search="true"
                form="reloadForm">
            <option>Choose...</option>
            <#list processList as process>
                <option value="${process.pid}">${process.pid} ${process.displayName}</option>
            </#list>
        </select>
    </div>
    <button type="submit" class="btn btn-primary" id="reloadButton">Reload</button>
</form>
</div>

<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.3.1/jquery.min.js"
        integrity="sha256-FgpCb/KJQlLNfOu91ta32o/NMZxltwRo8QtmkMRdAu8="
        crossorigin="anonymous"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.3/umd/popper.min.js"
        integrity="sha384-ZMP7rVo3mIykV+2+9J3UJ46jBk0WLaUAdn689aCwoqbBJiSnjAK/l8WvCWPIPm49"
        crossorigin="anonymous"></script>
<script src="https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/js/bootstrap.min.js"
        integrity="sha384-ChfqqxuZUCnJSK3+MXmPNIyE6ZbWh2IMqE241rYiqJxyMiZ6OW/JmZQ5stwEULTy"
        crossorigin="anonymous"></script>
<!-- Latest compiled and minified JavaScript -->
<script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-select/1.13.2/js/bootstrap-select.min.js"></script>
<script>
    $(function () {

        $('.selectpicker').selectpicker();

        $('#reloadForm').on('submit', function (e) {
            e.preventDefault();
            var form = $("#reloadForm")[0];
            var data = new FormData(form);
            data.append('file', $("#file")[0].files[0])
            // if the validator does not prevent form submit
            var url = "/hotfix";

            // POST values in the background the the script URL
            $.ajax({
                type: "POST",
                method: "POST",
                url: url,
                data: data,
                contentType: false,
                processData: false,
                cache: false,
                success: function (data) {
                    console.log('result ' + data)
                }
            });
            return false;
        })
    });
</script>

</body>
</html>