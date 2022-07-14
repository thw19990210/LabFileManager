
var bar = document.getElementById('js-progressbar');
UIkit.upload('.js-upload', {
    url: '/api/general/files/upload',
    multiple: true,
    name: "file",
    beforeSend: function () {
        console.log('beforeSend', arguments);
    },
    beforeAll: function () {
        console.log('beforeAll', arguments);
    },
    load: function () {
        console.log('load', arguments);
    },
    error: function () {
        console.log('error', arguments);
    },
    complete: function (e) {
        console.log('complete', e);
        console.log('complete', arguments);
    },
    loadStart: function (e) {
        console.log('loadStart', arguments);
        bar.removeAttribute('hidden');
        bar.max = e.total;
        bar.value = e.loaded;
    },
    progress: function (e) {
        console.log('progress', arguments);
        bar.max = e.total;
        bar.value = e.loaded;
    },
    loadEnd: function (e) {
        console.log('loadEnd', arguments);
        bar.max = e.total;
        bar.value = e.loaded;
    },
    completeAll: function () {
        console.log('completeAll', arguments);
        setTimeout(function () {
            bar.setAttribute('hidden', 'hidden');
        }, 1000);

        alert("Successfully Uploaded!");
    }
});

$.ajaxSetup({
    contentType: "application/json; charset=utf-8"
});

function humanFileSize(bytes, si = false, dp = 1) {
    const thresh = si ? 1000 : 1024;
    if (Math.abs(bytes) < thresh) {
        return bytes + '\xa0';
    }
    const units = ['kB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
    let u = -1;
    const r = 10 ** dp;
    do {
        bytes /= thresh;
        ++u;
    } while (Math.round(Math.abs(bytes) * r) / r >= thresh && u < units.length - 1);
    return bytes.toFixed(dp) + '\xa0' + units[u];
}

function sendGet(sendUrl) {
    $.ajax({
        url: sendUrl,
        type: "GET",
        error: function (data, status) {
            reload();
        },
        success: function (data, status) {
            reload();
        }
    });
}

function reload() {
    $("#downloadList").empty();
    $("#downloadList").hide();
    $("#Spinner").show();

    $.ajax({
        url: "/api/general/files/list",
        type: "GET",
        error: function (data, status) {
            onerror(data);
        },
        success: function (data, status) {
            display(data);
        }
    });
}

function search() {
    var project   = document.getElementById("p1").value;
    var sensor    = document.getElementById("p2").value;
    var color_tem = document.getElementById("p3").value;
    var illumin   = document.getElementById("p4").value;
    var ISO       = document.getElementById("p5").value;
    var serial_n  = document.getElementById("p6").value;
    var HW_v      = document.getElementById("p7").value;
    var SW_v      = document.getElementById("p8").value;
    var file_type = document.getElementById("p9").value;
    var str = "project="+project+"&"+"sensor="+sensor+"&"+"color_tem="+color_tem+"&"+"illumin="+illumin+"&"+"ISO="+ISO+"&"+"serial_n="+serial_n+"&"+"HW_v="+HW_v+"&"+"SW_v="+SW_v+"&"+"file_type="+file_type;
    $("#downloadList").empty();
    $("#downloadList").hide();
    $("#Spinner").show();

    $.ajax({
        url: "/api/general/search?"+str,
        type: "GET",
        // data: {project: project, color_tem: color_tem},
        error: function () {
            alert("Sorry! Some error happened");
        },
        success: function (data) {
            display(data);
        }
    });
}

function display (data) {
    $("#Spinner").hide();
    $("#downloadList").fadeIn(500);
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
        if (data[key].name.endsWith(".mp4") ||
            data[key].name.endsWith(".wmv") ||
            data[key].name.endsWith(".m4v") ||
            data[key].name.endsWith(".avi") ||
            data[key].name.endsWith(".rm") ||
            data[key].name.endsWith(".mov") ) {
            img = document.createElement('video');
        }
        else {
            img = document.createElement('img');
        }
        img.style.width = "150px";
        img.style.height = "114px";
        img.src = "/storage/" + data[key].name;
        img.alt = "  no image"

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

        div.append(fileIcon);

        var fileLink = document.createElement('a');
        fileLink.appendChild(document.createTextNode(data[key].name));
        fileLink.href = "/api/general/files/download/" + data[key].name;
        // fileLink.classList.add("HeaderHeight");
        fileLink.style.marginTop = "46px"
        fileLink.classList.add("FileEntryName");
        fileLink.classList.add("BCLinkBlue");
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
        div.append(fileDelete);

        div.style.marginBottom = "15px";

        $("#downloadList").append(div);

    }
}

function copy_name(copyValue) {
    const _input = document.createElement('input');
    _input.value = copyValue;
    document.body.appendChild(_input);
    _input.select();
    document.execCommand('copy');
    document.body.removeChild(_input);
}

function key_enter2(e) {
    var evt = window.event || e;
    if (evt.keyCode == 13) {
        search();
    }
}

function key_enter0(e) {
    var evt = window.event || e;
    if (evt.keyCode == 13) {
        validation();
    }
}

function key_enter(e) {
    var evt = window.event || e;
    if (evt.keyCode == 13) {
        search_pro();
    }
}

function key_enter4(e) {
    var evt = window.event || e;
    if (evt.keyCode == 13) {
        change_pswd();
    }
}

function search_pro() {
    var content = document.getElementById("pro").value;
    $("#downloadList").empty();
    $("#downloadList").hide();
    $("#Spinner").show();

    $.ajax({
        url: "/api/general/search_pro?content="+content,
        type: "GET",
        // data: {project: project, color_tem: color_tem},
        error: function () {
            alert("Sorry! Some error happened");
        },
        success: function (data) {
            display(data);
        }
    });
}

function validation() {
    var user_name=document.getElementById("username_input").value;
    var password =document.getElementById("password_input").value;
    var acct = "username="+user_name+"&password="+password;
    $.ajax({
        url: "/api/general/login?"+acct,
        type: "GET",
        error: function () {
            alert("Sorry! Some error happened");
        },
        success: function (data) {
            if (data == "success!") {
                location.href="/";
            }
            else {
                alert(data);
            }
        }
    });
}

function change_pswd() {
    var user_name=document.getElementById("username_input2").value;
    var password =document.getElementById("password_input1").value;
    var new_password1 =document.getElementById("password_input21").value;
    var new_password2 =document.getElementById("password_input22").value;
    var change;
    if (new_password1 === new_password2) {
        change = "username="+user_name+"&password="+password+"&new_password="+new_password1;
    }
    else {
        alert("New password didn't match!")
    }

    $.ajax({
        url: "/api/general/change?"+change,
        type: "GET",
        error: function () {
            alert("Sorry! Some error happened");
        },
        success: function (data) {
            if (data == "success!") {
                location.href="/login"
                alert(data);
            }
            else {
                alert(data);
            }
        }
    });
}

reload();
