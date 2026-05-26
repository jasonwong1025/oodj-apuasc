In Terminal run:

java -cp "bin;javax.mail.jar;activation-1.1.1.jar" main.Main

For Windows (Command Prompt): 
1. dir /s /b src\*.java > sources.txt & if not exist bin mkdir bin & javac -d bin -cp ".;javax.mail.jar;activation-1.1.1.jar" @sources.txt & del sources.txt
2. java -cp "bin;javax.mail.jar;activation-1.1.1.jar" main.Main

For Windows (PowerShell): 
$javaFiles = Get-ChildItem -Path src -Recurse -Filter *.java | Select-Object -ExpandProperty FullName; if (!(Test-Path bin)) { New-Item -ItemType Directory bin | Out-Null }; javac -d bin -cp ".;javax.mail.jar;activation-1.1.1.jar" $javaFiles; java -cp "bin;javax.mail.jar;activation-1.1.1.jar" main.Main

For macOS / Linux: 
mkdir -p bin && javac -d bin -cp ".:javax.mail.jar:activation-1.1.1.jar" $(find src -name "*.java") && java -cp "bin:javax.mail.jar:activation-1.1.1.jar" main.Main
