Dim shell, shellEnv, fso
set shell = WScript.CreateObject("WScript.Shell")
set fso = CreateObject("Scripting.FileSystemObject")

path = WScript.Arguments(0)

fileName = fso.GetFileName(path)
ext = fso.GetExtensionName(path)
command = "cmd /c " & path
'msgbox command

If ext = "xlsx" or ext = "xls" Then
	excelSpecial()
Else
	shell.Run command, 0
End If

private Sub excelSpecial()
	On Error Resume Next
	'Excelが起動していない場合は普通に起動
	Set xl = GetObject(, "Excel.Application")
	If Err Then
		shell.Run command, 0
	Else
		'Excelが起動している場合、当該Bookが存在していればActivateする
		shouldActivate = False
		For Each obj In xl.Workbooks
			If obj.Name = fileName Then
				shouldActivate = True
				Exit For
			End If
		Next
		If shouldActivate Then
			shell.AppActivate(fileName)
		Else
			shell.Run command, 0
		End If
	End If
End Sub
	
		

'shell.AppActivate("部品.xlsx")
'xl.WorkBooks("構成.xlsx").Activate
'第二引数の0は実行時のWindowサイズを非表示にする
'shell.Run "cmd /c C:\pleiades\workspace\jp.co.nri.nefs.tool.launcher\conf\構成.xlsx", 0
'shell.Run "cmd /c D:\Apl\ideaIC-2018.3.5.win\NOTICE.txt", 0