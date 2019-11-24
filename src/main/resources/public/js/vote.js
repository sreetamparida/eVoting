$(document).ready(function () {
    $("body").on("click", ".voter", function () {
            // alert($(this).attr("uuid"));
            var data = {'candidate_uuid': $(this).attr("uuid")}
            $.ajax({
                type: "POST",
                url: "/vote",
                data: data,
            }).done(function(data) {
                alert("You have voted !" + data)
            });
        }
    )
})