In Terminal run:

java -cp "bin;javax.mail.jar;activation-1.1.1.jar" main.Main

For Windows (Command Prompt): dir /s /B src*.java > sources.txt && javac -d bin -cp ".;javax.mail.jar;activation-1.1.1.jar" @sources.txt && del sources.txt && java -cp "bin;javax.mail.jar;activation-1.1.1.jar" main.Main
For Windows (PowerShell): $javaFiles = Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName }; javac -d bin -cp ".;javax.mail.jar;activation-1.1.1.jar" $javaFiles; java -cp "bin;javax.mail.jar;activation-1.1.1.jar" main.Main
For macOS / Linux: javac -d bin -cp ".:javax.mail.jar:activation-1.1.1.jar" $(find src -name "*.java") && java -cp "bin:javax.mail.jar:activation-1.1.1.jar" main.Main
