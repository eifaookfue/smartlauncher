Dim shell, shellEnv, fso
set shell = WScript.CreateObject("WScript.Shell")
set fso = CreateObject("Scripting.FileSystemObject")

baseDir = fso.getParentFolderName(WScript.ScriptFullName) & "\.."
'msgbox baseDir
javaCmd = "C:\pleiades\java\jdk1.8.0_60\jre\bin\java"
classPath = baseDir & "\target\smartlauncher-1.0.0-distribution.jar"
'classPath = "C:\Users\s2-nakamura\git\smartlauncher\jp.co.nri.nefs.tool.smartlauncher\target\smartlauncher-1.0.0-distribution.jar"
'msgbox classPath
mainClass = "jp.co.nri.nefs.tool.smartlauncher.gui.SmartFrame"
directoryFile = "-directoryFile " & baseDir & "\cfgs\searchdir.csv"
'msgbox directoryFile
aliasFile = "-aliasFile " & baseDir & "\cfgs\alias.csv"
scriptFile = "-scriptFile " & baseDir & "\scripts\activate.vbs"
command = javaCmd & " " & "-cp " & classPath & " " & mainClass & " " & _
         directoryFile & " " & aliasFile & " " & scriptFile
'command = """" & javaCmd & """"
'command = """" & javaCmd & " " & "-cp " & classPath & """"
'command = javaCmd & " -version"
'msgbox command
shell.Run command, 0
