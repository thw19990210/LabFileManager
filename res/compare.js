function key_enter_l(e) {
    var evt = window.event || e;
    if (evt.keyCode == 13) {
        search_l();
    }
}

function key_enter_r(e) {
    var evt = window.event || e;
    if (evt.keyCode == 13) {
        search_r();
    }
}


function search_l() {
    var project   = document.getElementById("p1-l").value; project = (project == "project") ? "" : project;
    var sensor    = document.getElementById("p2-l").value; sensor = (sensor == "sensor") ? "" : sensor;
    // var color_tem = document.getElementById("p3").value;
    // var illumin   = document.getElementById("p4").value;
    // var ISO       = document.getElementById("p5").value;
    // var serial_n  = document.getElementById("p6").value;
    var HW_v      = document.getElementById("p3-l").value; HW_v = (HW_v == "hardware") ? "" : HW_v;
    var SW_v      = document.getElementById("p4-l").value; SW_v = (SW_v == "software") ? "" : SW_v;
    var file_type = document.getElementById("p5-l").value; file_type = (file_type == "file type") ? "" : file_type;
    var scene = document.getElementById("p6-l").value; scene = (scene == "scene") ? "" : scene;

    var color_tem = document.getElementById("p31-l").value + "~" + document.getElementById("p32-l").value;
    var illumin   = document.getElementById("p41-l").value + "~" + document.getElementById("p42-l").value;
    var ISO       = document.getElementById("p51-l").value + "~" + document.getElementById("p52-l").value;
    var ET        = document.getElementById("p61-l").value + "~" + document.getElementById("p62-l").value;

    var str = "project="+project+"&"+"sensor="+sensor+"&"+"color_tem="+color_tem+"&"+"illumin="+illumin+"&"+"ISO="+ISO+"&"+"ET="+ET+"&"+"HW_v="+HW_v+"&"+"SW_v="+SW_v+"&"+"file_type="+file_type+"&"+"scene="+scene;
    $("#downloadList1").empty();
    $("#downloadList1").hide();
    $("#Spinner").show();

    $.ajax({
        url: "/api/general/search?"+str,
        type: "GET",
        // data: {project: project, color_tem: color_tem},
        error: function () {
            alert("Sorry! Some error happened");
        },
        success: function (data) {
            display_l(data);
        }
    });
}

function display_l (data) {
    $("#Spinner").hide();
    $("#downloadList1").fadeIn(500);

    if ($.isEmptyObject(data)) {
        $("#no-file-alert").show();
    }
    else {
        $("#no-file-alert").hide();
    }

    for (var key in data) {
        var div = document.createElement('div');
        div.classList.add("CenteredItems_adjust");
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
        img.style.marginLeft = "15px";
        img_name = img_name.replace(/\s/g,'%20');
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

        div.append(img);

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



        div.append(fileIcon);

        var fileLink = document.createElement('a');
        fileLink.appendChild(document.createTextNode(data[key].path));
        fileLink.href = "/api/general/files/download?file_path=" + data[key].path;
        // fileLink.classList.add("HeaderHeight");
        fileLink.style.marginTop = "46px"
        fileLink.classList.add("FileEntryName");
        fileLink.classList.add("BCLinkBlue");
        fileLink.id = "link"+key;
        div.append(fileLink);

        if (!data[key].isDirectory) {
            var fileSize = document.createElement('span');
            // fileSize.classList.add("HeaderHeight");
            fileSize.classList.add("FileEntrySize");
            fileSize.classList.add("BCCLightGray");
            fileSize.style.marginTop = "46px";
            fileSize.appendChild(document.createTextNode("" + humanFileSize(data[key].length)));
            div.append(fileSize);
        }

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

        var checkBox = document.createElement('input');
        checkBox.type = "checkbox";
        checkBox.style.marginLeft = "10px";
        checkBox.style.marginRight = "10px";
        checkBox.id = "checkbox"+key;
        // div.append(checkBox);

        div.style.marginBottom = "15px";

        $("#downloadList1").append(div);

    }
}

function search_r() {
    var project   = document.getElementById("p1-r").value; project = (project == "project") ? "" : project;
    var sensor    = document.getElementById("p2-r").value; sensor = (sensor == "sensor") ? "" : sensor;
    // var color_tem = document.getElementById("p3").value;
    // var illumin   = document.getElementById("p4").value;
    // var ISO       = document.getElementById("p5").value;
    // var serial_n  = document.getElementById("p6").value;
    var HW_v      = document.getElementById("p3-r").value; HW_v = (HW_v == "hardware") ? "" : HW_v;
    var SW_v      = document.getElementById("p4-r").value; SW_v = (SW_v == "software") ? "" : SW_v;
    var file_type = document.getElementById("p5-r").value; file_type = (file_type == "file type") ? "" : file_type;
    var scene = document.getElementById("p6-r").value; scene = (scene == "scene") ? "" : scene;

    var color_tem = document.getElementById("p31-r").value + "~" + document.getElementById("p32-r").value;
    var illumin   = document.getElementById("p41-r").value + "~" + document.getElementById("p42-r").value;
    var ISO       = document.getElementById("p51-r").value + "~" + document.getElementById("p52-r").value;
    var ET        = document.getElementById("p61-r").value + "~" + document.getElementById("p62-r").value;

    var str = "project="+project+"&"+"sensor="+sensor+"&"+"color_tem="+color_tem+"&"+"illumin="+illumin+"&"+"ISO="+ISO+"&"+"ET="+ET+"&"+"HW_v="+HW_v+"&"+"SW_v="+SW_v+"&"+"file_type="+file_type+"&"+"scene="+scene;
    $("#downloadList2").empty();
    $("#downloadList2").hide();
    $("#Spinner").show();

    $.ajax({
        url: "/api/general/search?"+str,
        type: "GET",
        // data: {project: project, color_tem: color_tem},
        error: function () {
            alert("Sorry! Some error happened");
        },
        success: function (data) {
            display_r(data);
        }
    });
}

function display_r (data) {
    $("#Spinner").hide();
    $("#downloadList2").fadeIn(500);

    if ($.isEmptyObject(data)) {
        $("#no-file-alert").show();
    }
    else {
        $("#no-file-alert").hide();
    }

    for (var key in data) {
        var div = document.createElement('div');
        div.classList.add("CenteredItems_adjust");
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
        img.style.marginLeft = "15px";
        img_name = img_name.replace(/\s/g,encodeURIComponent(' '));
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

        div.append(img);

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



        div.append(fileIcon);

        var fileLink = document.createElement('a');
        fileLink.appendChild(document.createTextNode(data[key].path));
        fileLink.href = "/api/general/files/download?file_path=" + data[key].path;
        // fileLink.classList.add("HeaderHeight");
        fileLink.style.marginTop = "46px"
        fileLink.classList.add("FileEntryName");
        fileLink.classList.add("BCLinkBlue");
        fileLink.id = "link"+key;
        div.append(fileLink);

        if (!data[key].isDirectory) {
            var fileSize = document.createElement('span');
            // fileSize.classList.add("HeaderHeight");
            fileSize.classList.add("FileEntrySize");
            fileSize.classList.add("BCCLightGray");
            fileSize.style.marginTop = "46px";
            fileSize.appendChild(document.createTextNode("" + humanFileSize(data[key].length)));
            div.append(fileSize);
        }

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

        var checkBox = document.createElement('input');
        checkBox.type = "checkbox";
        checkBox.style.marginLeft = "10px";
        checkBox.style.marginRight = "10px";
        checkBox.id = "checkbox"+key;
        // div.append(checkBox);

        div.style.marginBottom = "15px";

        $("#downloadList2").append(div);

    }
}
