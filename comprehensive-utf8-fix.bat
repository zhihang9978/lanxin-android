@echo off
chcp 65001 > nul
echo === 全面修复UTF-8编码问题 ===
echo.

cd TMessagesProj\src\main\res\values

echo 正在备份原文件...
copy strings.xml strings.xml.backup > nul

echo 执行UTF-8字符修复...

REM 使用PowerShell进行批量替换
powershell -NoProfile -ExecutionPolicy Bypass -Command "$content = Get-Content 'strings.xml' -Raw -Encoding UTF8; $original = $content.Length; Write-Host '原始大小:' $original '字节'; $replacements = @{ [char]0xFFFD + '?' = [char]0x2014; [char]0xFFFD + [char]0x0027 = [char]0x2019; '\' + [char]0xFFFD + [char]0x0027 = '\' + [char]0x2019; [char]0xFFFD + '*' = [char]0x2026 + '**'; '**' + [char]0xFFFD = '**' + [char]0x2026; [char]0xFFFD = '' }; $fixed = 0; foreach ($k in $replacements.Keys) { $v = $replacements[$k]; $m = ([regex]::Matches($content, [regex]::Escape($k))).Count; if ($m -gt 0) { Write-Host \"  修复 $m 处\"; $content = $content -replace [regex]::Escape($k), $v; $fixed += $m } }; $content | Out-File 'strings.xml' -Encoding UTF8 -NoNewline; Write-Host \"总共修复: $fixed 处\"; $remaining = ([regex]::Matches($content, [char]0xFFFD)).Count; Write-Host \"剩余错误: $remaining 处\"; if ($remaining -eq 0) { Write-Host '成功：所有编码错误已修复！' -ForegroundColor Green; exit 0 } else { Write-Host '警告：仍有编码错误' -ForegroundColor Yellow; exit 1 }"

echo.
echo 完成！
pause

