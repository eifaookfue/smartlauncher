var seiseki = [80, 72, 94, 68]

document.write("<p>");

for(var i = 0; i < seiseki.length; i++){
  document.write(seiseki[i] + "点は");

  if (seiseki[i] > 80){
    document.write("合格です<br />");
    alert("goukaku")
  }else if (seiseki[i] > 70){
    document.write("追試です<br />");
  }else{
    document.write("不合格です<br />");
  }
}

document.write("</p>");