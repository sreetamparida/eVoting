function openContent(content,elmnt){
    var navcont,btnav,cont;
    navcont = document.getElementsByClassName('navcont');
    for (var i =0;i< navcont.length ; i++) {
        navcont[i].style.display="none";
    }
    btnav = document.getElementsByClassName('btnav');
    for (var i = 0; i < btnav.length; i++) {
        btnav[i].style.backgroundColor="";
    }
    document.getElementById(content).style.display="block";
    elmnt.style.backgroundColor="grey";
    cont = document.getElementById("login_form");
    window.onclick = function(event) {
        if (event.target == cont) {
            cont.style.display = "none";
            document.getElementById("log").style.backgroundColor="black";

        }
    }
}