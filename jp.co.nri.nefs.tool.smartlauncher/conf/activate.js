//var shell = new ActiveXObject("WScript.Shell");
var fso = new ActiveXObject('Scripting.FileSystemObject');

var path = "C:\Users\s2-nakamura\Documents\52.”F’èAE\‰ïĞ‘—¿\52.”F’èAE\AE‘—Ş\–ğˆõ–ÊÚ‘—¿\•”•i.xlsx";
//path = "D:\Apl\ideaIC-2018.3.5.win\NOTICE.txt"

var fileName = fso.GetFileName(path)
var ext = fso.GetExtensionName(path)
var command = "cmd /c " & path

//shell.Run command, 0
shell.AppActivate(fileName)