function key_enter_lr(e) {
    var evt = window.event || e;
    if (evt.keyCode == 13) {
        search_lr();
    }
}


function display_l (data) {
    $("#downloadList1").fadeIn(500);

    if ($.isEmptyObject(data)) {
        $("#downloadList1").append("<a>no file found for the searching!</a>");
    }

    for (var key in data) {
        var div = document.createElement('div');
        div.classList.add("CenteredItems");
        div.style.display = "inline-flex";
        div.style.flexDirection = "row";

        var clipboard = document.createElement('div');
        // clipboard.data-clipboard-text = data[key].name;
        var fileIcon = document.createElement('img');
        fileIcon.src = data[key].isDirectory ? "folder.svg" : "file.svg";
        var copy = document.createElement('a');

        //image preview function
        var img;
        var img_name = data[key].path;
        if (data[key].name.endsWith(".mp4") ||
            data[key].name.endsWith(".wmv") ||
            data[key].name.endsWith(".m4v") ||
            data[key].name.endsWith(".avi") ||
            data[key].name.endsWith(".rm") ||
            data[key].name.endsWith(".mov") ) {
            img = document.createElement('video');
        }
        else if (data[key].name.endsWith(".raw")) {
            img = document.createElement('img');
            img_name = img_name.slice(0, -4) + ".jpg";
        }
        else {
            img = document.createElement('img');
        }
        img.style.width = "150px";
        img.style.height = "114px";
        img.src = "/storage/" + img_name;
        img.alt = "  no image";

        if(!(data[key].name.endsWith(".mp4") ||
            data[key].name.endsWith(".wmv") ||
            data[key].name.endsWith(".m4v") ||
            data[key].name.endsWith(".avi") ||
            data[key].name.endsWith(".rm")  ||
            data[key].name.endsWith(".mov") ||
            data[key].name.endsWith(".jpg") ||
            data[key].name.endsWith(".png") ||
            data[key].name.endsWith(".raw") ||
            data[key].name.endsWith(".jpeg"))
        ) {
            img.style.opacity = "0";
        }

        //end

        fileIcon.style.width = "28px";
        fileIcon.style.paddingLeft = "5px";
        fileIcon.style.paddingRight = "5px";
        fileIcon.style.marginRight = "7px";
        fileIcon.style.marginLeft = "15px";
        fileIcon.data = data[key].name;
        fileIcon.style.cursor = "pointer";
        fileIcon.onclick = function () {
            copy_name(this.data);
        };

        div.append(img);

        // div.append(fileIcon);

        var fileLink = document.createElement('a');
        fileLink.appendChild(document.createTextNode(data[key].path));
        fileLink.href = "/api/general/files/download?file_path=" + data[key].path;
        // fileLink.classList.add("HeaderHeight");
        fileLink.style.marginTop = "46px"
        fileLink.classList.add("FileEntryName");
        fileLink.classList.add("BCLinkBlue");
        div.append(fileLink);

        // if (!data[key].isDirectory) {
        //     var fileSize = document.createElement('span');
        //     // fileSize.classList.add("HeaderHeight");
        //     fileSize.classList.add("FileEntrySize");
        //     fileSize.classList.add("BCCLightGray");
        //     fileSize.style.marginTop = "46px";
        //     fileSize.appendChild(document.createTextNode("" + humanFileSize(data[key].length)));
        //     div.append(fileSize);
        // }

        var fileDelete = document.createElement('a');
        fileDelete.innerHTML = "&nbsp";
        fileDelete.classList.add("HeaderHeight");
        fileDelete.classList.add("FileEntryDelete");
        var deleteIcon = document.createElement('img');
        deleteIcon.src = "delete.svg";
        deleteIcon.style.width = "15px";
        deleteIcon.style.height = "15px";
        deleteIcon.style.marginTop = "50px"
        fileDelete.style.textDecoration = "none";
        fileDelete.appendChild(deleteIcon);
        fileDelete.data = "/api/general/files/delete/" + data[key].name;
        // fileDelete.onclick = function () {
        //     sendGet(this.data);
        // };
        // div.append(fileDelete);

        div.style.marginBottom = "15px";
        div.style.marginLeft = "1px";

        $("#downloadList1").append(div);

    }
}

function display_r (data) {
    $("#downloadList2").fadeIn(500);

    if ($.isEmptyObject(data)) {
        $("#downloadList2").append("<a>no file found for the searching!</a>");
    }

    for (var key in data) {
        var div = document.createElement('div');
        div.classList.add("CenteredItems");
        div.style.display = "inline-flex";
        div.style.flexDirection = "row";

        var clipboard = document.createElement('div');
        // clipboard.data-clipboard-text = data[key].name;
        var fileIcon = document.createElement('img');
        fileIcon.src = data[key].isDirectory ? "folder.svg" : "file.svg";
        var copy = document.createElement('a');

        //image preview function
        var img;
        var img_name = data[key].path;
        if (data[key].name.endsWith(".mp4") ||
            data[key].name.endsWith(".wmv") ||
            data[key].name.endsWith(".m4v") ||
            data[key].name.endsWith(".avi") ||
            data[key].name.endsWith(".rm") ||
            data[key].name.endsWith(".mov") ) {
            img = document.createElement('video');
        }
        else if (data[key].name.endsWith(".raw")) {
            img = document.createElement('img');
            img_name = img_name.slice(0, -4) + ".jpg";
        }
        else {
            img = document.createElement('img');
        }
        img.style.width = "150px";
        img.style.height = "114px";
        img.src = "/storage/" + img_name;
        img.alt = "  no image";

        if(!(data[key].name.endsWith(".mp4") ||
            data[key].name.endsWith(".wmv") ||
            data[key].name.endsWith(".m4v") ||
            data[key].name.endsWith(".avi") ||
            data[key].name.endsWith(".rm")  ||
            data[key].name.endsWith(".mov") ||
            data[key].name.endsWith(".jpg") ||
            data[key].name.endsWith(".png") ||
            data[key].name.endsWith(".raw") ||
            data[key].name.endsWith(".jpeg"))
        ) {
            img.style.opacity = "0";
        }

        //end

        fileIcon.style.width = "28px";
        fileIcon.style.paddingLeft = "5px";
        fileIcon.style.paddingRight = "5px";
        fileIcon.style.marginRight = "7px";
        fileIcon.style.marginLeft = "15px";
        fileIcon.data = data[key].name;
        fileIcon.style.cursor = "pointer";
        fileIcon.onclick = function () {
            copy_name(this.data);
        };

        div.append(img);

        // div.append(fileIcon);

        var fileLink = document.createElement('a');
        fileLink.appendChild(document.createTextNode(data[key].path));
        fileLink.href = "/api/general/files/download?file_path=" + data[key].path;
        // fileLink.classList.add("HeaderHeight");
        fileLink.style.marginTop = "46px"
        fileLink.classList.add("FileEntryName");
        fileLink.classList.add("BCLinkBlue");
        div.append(fileLink);

        // if (!data[key].isDirectory) {
        //     var fileSize = document.createElement('span');
        //     // fileSize.classList.add("HeaderHeight");
        //     fileSize.classList.add("FileEntrySize");
        //     fileSize.classList.add("BCCLightGray");
        //     fileSize.style.marginTop = "46px";
        //     fileSize.appendChild(document.createTextNode("" + humanFileSize(data[key].length)));
        //     div.append(fileSize);
        // }

        var fileDelete = document.createElement('a');
        fileDelete.innerHTML = "&nbsp";
        fileDelete.classList.add("HeaderHeight");
        fileDelete.classList.add("FileEntryDelete");
        var deleteIcon = document.createElement('img');
        deleteIcon.src = "delete.svg";
        deleteIcon.style.width = "15px";
        deleteIcon.style.height = "15px";
        deleteIcon.style.marginTop = "50px"
        fileDelete.style.textDecoration = "none";
        fileDelete.appendChild(deleteIcon);
        fileDelete.data = "/api/general/files/delete/" + data[key].name;
        // fileDelete.onclick = function () {
        //     sendGet(this.data);
        // };
        // div.append(fileDelete);

        div.style.marginBottom = "15px";
        div.style.marginLeft = "1px";

        $("#downloadList2").append(div);

    }
}


function search_lr() {
    var project   = document.getElementById("p1c").value;
    var sensor    = document.getElementById("p2c").value;
    // var color_tem = document.getElementById("p3").value;
    // var illumin   = document.getElementById("p4").value;
    // var ISO       = document.getElementById("p5").value;
    // var serial_n  = document.getElementById("p6").value;
    var HW_v      = document.getElementById("p7c").value;
    var SW_v      = document.getElementById("p8c").value;
    var file_type = document.getElementById("p9c").value;

    var color_tem = document.getElementById("p31c").value + "~" + document.getElementById("p32c").value;
    var illumin   = document.getElementById("p41c").value + "~" + document.getElementById("p42c").value;
    var ISO       = document.getElementById("p51c").value + "~" + document.getElementById("p52c").value;
    var ET        = document.getElementById("p61c").value + "~" + document.getElementById("p62c").value;

    var str = "project="+project+"&"+"sensor="+sensor+"&"+"color_tem="+color_tem+"&"+"illumin="+illumin+"&"+"ISO="+ISO+"&"+"ET="+ET+"&"+"HW_v="+HW_v+"&"+"SW_v="+SW_v+"&"+"file_type="+file_type;

    $("#Spinner").show();

    $.ajax({
        url: "/api/general/search?"+str,
        type: "GET",
        // data: {project: project, color_tem: color_tem},
        error: function () {
            alert("Sorry! Some error happened");
        },
        success: function (data) {
            if ($("#List1_checkbox").is(":checked")) {
                $("#downloadList1").empty();
                display_l(data);
            }
            if ($("#List2_checkbox").is(":checked")) {
                $("#downloadList2").empty();
                display_r(data);
            }
        }
    });
}
