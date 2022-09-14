

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
        get_token();
        save_work_path()
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

function key_enter5(e) {
    var evt = window.event || e;
    if (evt.keyCode == 13) {
        get_token();
        save_work_path();
    }
}

function key_enter6(e) {
    var evt = window.event || e;
    if (evt.keyCode == 13) {
        folders_list();
    }
}




function get_token(){
    var work_path = document.getElementById("work_path").value;
    $.ajax({
        url: "/api/general/get_token",
        type: "GET",
        error: function () {

        },
        success: function (data) {
            $("#welcome_slogan").empty();
            $("#welcome_slogan").append("<a class=\"d-block\">Welcome! " + data[1] + "</a>");
            $("#welcome_user_photo").empty();
            $("#welcome_user_photo").append("<img src='https://internal-cdn.amazon.com/badgephotos.amazon.com/?uid=" + data[3] + "' class=\"img-circle elevation-2\" alt=\"User Image\">");

            var access = data[0].split(',');
            var permitted = 0;
            for (key in access) {
                var path = "/" + work_path;
                if (path.startsWith(access[key])) {
                    permitted = 1;
                }
            }
            if (permitted == 1) {
                $("#upload_access_control").show();
                $("#upload_alert").hide();
            }
            else {
                $("#upload_access_control").hide();
                $("#upload_alert").show();
            }
        }
    });
}




function display_name() {

}



function save_work_path_2(){
    var work_path = document.getElementById("path_string").innerHTML;

    if (work_path == "") work_path = "empty_path/";

    work_path = work_path.substring(0, work_path.length - 1);

    $.ajax({
        url: "/api/general/save_work_path?work_path=" + work_path,
        type: "GET",
        error: function () {

        },
        success: function () {

        }
    });
}






function display_options() {
    var to_display = ["project", "sensor", "hardware_version", "software_version", "file_type", "scene",
        "project", "sensor", "hardware_version", "software_version", "file_type", "scene",
        "project", "sensor", "hardware_version", "software_version", "file_type", "scene"];
    // var list_to_display = ["#list1", "#list2", "#list3", "#list4", "#list5"];
    var list_to_display = ["p1", "p2", "p3", "p4", "p5", "p6",
        "p1-l", "p2-l", "p3-l", "p4-l", "p5-l", "p6-l",
        "p1-r", "p2-r", "p3-r", "p4-r", "p5-r", "p6-r"];
    for (var i = 0; i < to_display.length; i++) {
        $.ajax({
            url: "/api/general/display_options?option="+to_display[i]+"&list="+list_to_display[i],
            type: "GET",
            error: function (data) {
                onerror(data);
            },
            success: function (data) {

                for (var key in data) {
                    if (key > 0) {
                        var option = document.createElement('option');
                        var ele = "#" + data[0];
                        option.innerHTML = data[key];
                        $(ele).append(option);
                    }
                }
            }
        });
    }
}

