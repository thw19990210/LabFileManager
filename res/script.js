

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

    if ($.isEmptyObject(data)) {
        $("#downloadList").append("<a>no file found for the searching!</a>");
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
        div.append(checkBox);

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
                get_token();
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
            $("#welcome_user_photo").append("<img src=\"" + data[2] + "\" class=\"img-circle elevation-2\" alt=\"User Image\">");

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
    $.ajax({
        url: "/api/general/get_token",
        type: "GET",
        error: function () {

        },
        success: function (data) {
            $("#welcome_user_photo").empty();
            $("#welcome_user_photo").append("<img src=\"" + data[2] + "\" class=\"img-circle elevation-2\" alt=\"User Image\">");
            $("#welcome_slogan").empty();
            $("#welcome_slogan").append("<a class=\"d-block\">Welcome! " + data[1] + "</a>");
        }
    });
}

function save_work_path(){
    var work_path = document.getElementById("work_path").value;
    $.ajax({
        url: "/api/general/save_work_path?work_path=" + work_path,
        type: "GET",
        error: function () {

        },
        success: function () {

        }
    });
}

function  get_PDP_access(){
    $.ajax({
        url: "/api/general/get_PDP_access",
        type: "GET",
        error: function () {

        },
        success: function (data) {
            var PDP_access = data[0].split(',');
            var permitted = 0;
            for (key in PDP_access) {
                if (true) {
                    permitted = 1;
                }
            }
            if (permitted == 1) {
                display_PDP_table();
                $("#PDP_table").show();
                $("#PDP-controller").show();
                $("#PDP_access_alert").hide();
            }
            else {
                $("#PDP_table").hide();
                $("#PDP-controller").hide();
                $("#PDP_access_alert").show();
            }
        }
    });
}

function display_PDP_table(){

    $.ajax({
        url: "/api/general/display_PDP_table",
        type: "GET",
        error: function () {

        },
        success: function (data) {
            $("#PDP_table_body").empty();
            $("#PDP_table_body_s2").empty();
            var high = [0,0,0];
            var mid  = [0,0,0];
            var low  = [0,0,0];
            var cnt = 0;

            for (key in data) {

                var row = data[key];
                var no = parseInt(key) + 1;



                if ($.trim(row[2]) == 'High') {
                    for (i in row) {
                        if (row[i] == 'yellow') high[0]++;
                        if (row[i] == 'green') high[1]++;
                        if (row[i] == 'red') high[2]++;
                    }
                }
                else if ($.trim(row[2]) == 'Mid') {
                    cnt++;
                    for (i in row) {
                        if (row[i] == 'yellow') mid[0]++;
                        if (row[i] == 'green') mid[1]++;
                        if (row[i] == 'red') mid[2]++;
                    }
                }
                if ($.trim(row[2]) == 'Low') {
                    for (i in row) {
                        if (row[i] == 'yellow') low[0]++;
                        if (row[i] == 'green') low[1]++;
                        if (row[i] == 'red') low[2]++;
                    }
                }



                $("#PDP_table_body").append("<tr>");
                $("#PDP_table_body").append("<td>"+no+"</td>");
                $("#PDP_table_body").append("<td><a href='/upload'>"+row[1]+"</a></td>");
                $("#PDP_table_body").append("<td>"+row[2]+"</td>");
                $("#PDP_table_body").append("<td bgcolor='"+row[4]+"'><div contenteditable='true'>"+row[3]+"</div></td>");
                $("#PDP_table_body").append("<td bgcolor='"+row[6]+"'><div contenteditable='true'>"+row[5]+"</div></td>");
                $("#PDP_table_body").append("<td bgcolor='"+row[8]+"'><div contenteditable='true'>"+row[7]+"</div></td>");
                $("#PDP_table_body").append("<td bgcolor='"+row[10]+"'><div contenteditable='true'>"+row[9]+"</div></td>");
                $("#PDP_table_body").append("<td bgcolor='"+row[12]+"'><div contenteditable='true'>"+row[11]+"</div></td>");
                $("#PDP_table_body").append("<td bgcolor='"+row[14]+"'><div contenteditable='true'>"+row[13]+"</div></td>");
                $("#PDP_table_body").append("<td bgcolor='"+row[16]+"'><div contenteditable='true'>"+row[15]+"</div></td>");
                $("#PDP_table_body").append("<td bgcolor='"+row[18]+"'><div contenteditable='true'>"+row[17]+"</div></td>");
                $("#PDP_table_body").append("</tr>");

            }

            var matrix = [high, mid, low];
            display_pie_chart(matrix);
        }
    });
}

function save_PDP_table() {
    var serial_num=document.getElementById("serial_num").value;
    var PDP_type =document.getElementById("PDP_type").value;
    var PDP_content =document.getElementById("PDP_content").value;
    var PDP_color =document.getElementById("PDP_color").value;

    var updatedStr = "serial_num="+serial_num+"&PDP_type="+PDP_type+"&PDP_content="+PDP_content+"&PDP_color="+PDP_color;
    $.ajax({
        url: "/api/general/save_PDP_table?"+updatedStr,
        type:"GET",
        error: function () {
            alert("some error happened!")
        },
        success: function () {
            alert("successfully changed!")
            display_PDP_table();
            document.getElementById("serial_num").value='';
            document.getElementById("PDP_type").value='';
            document.getElementById("PDP_content").value='';
            document.getElementById("PDP_color").value='';
        }
    });
}

function display_pie_chart(matrix){
    // 基于准备好的dom，初始化echarts实例
    var myChart_high = echarts.init(document.getElementById('chart_high'));
    var myChart_mid  = echarts.init(document.getElementById('chart_mid'));
    var myChart_low  = echarts.init(document.getElementById('chart_low'));

    let high = matrix[0][0] + matrix[0][1] + matrix[0][2];
    let mid  = matrix[1][0] + matrix[1][1] + matrix[1][2];
    let low  = matrix[2][0] + matrix[2][1] + matrix[2][2];

    // 指定图表的配置项和数据
    var option1 = {
        title: {
            text: 'High',
            x: 'center'
        },
        series: [
            {
                type: 'pie',
                data: [
                    {
                        value: matrix[0][0],
                        name: matrix[0][0]+'/'+high+"  "
                    },
                    {
                        value: matrix[0][1],
                        name: matrix[0][1]+'/'+high+" "
                    },
                    {
                        value: matrix[0][2],
                        name: matrix[0][2]+'/'+high
                    }
                ],
                radius: '50%',
                color: [
                    '#eedd78',
                    '#91ca8c',
                    '#dd6b66',
                    '#73a373',
                ]
            }
        ]
    };
    var option2 = {
        title: {
            text: 'Mid',
            x: 'center'
        },
        series: [
            {
                type: 'pie',
                data: [
                    {
                        value: matrix[1][0],
                        name: matrix[1][0]+'/'+mid+"  "
                    },
                    {
                        value: matrix[1][1],
                        name: matrix[1][1]+'/'+mid+" "
                    },
                    {
                        value: matrix[1][2],
                        name: matrix[1][2]+'/'+mid
                    }
                ],
                radius: '50%',
                color: [
                    '#eedd78',
                    '#91ca8c',
                    '#dd6b66',
                    '#73a373',
                ]
            }
        ]
    };
    var option3 = {
        title: {
            text: 'Low',
            x: 'center'
        },
        series: [
            {
                type: 'pie',
                data: [
                    {
                        value: matrix[2][0],
                        name: matrix[2][0]+'/'+low+"  "
                    },
                    {
                        value: matrix[2][1],
                        name: matrix[2][1]+'/'+low+" "
                    },
                    {
                        value: matrix[2][2],
                        name: matrix[2][2]+'/'+low
                    }
                ],
                radius: '50%',
                color: [
                    '#eedd78',
                    '#91ca8c',
                    '#dd6b66',
                    '#73a373'
                ]
            }
        ]
    };

    // 使用刚指定的配置项和数据显示图表。
    myChart_high.setOption(option1);
    myChart_mid.setOption(option2);
    myChart_low.setOption(option3);
    $("#pie_chart").show();
}

$(document).ready(function (){
    get_token();
    $("#PDP_table").hide();
    $("#PDP-controller").hide();
    $("#pie_chart").hide();
});


// reload();
// function show_user () {
//     $.
// }
// show_user();