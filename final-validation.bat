@echo off
chcp 65001 > nul
echo === Android构建准备最终验证 ===
echo.

cd TMessagesProj\src\main\res\values

echo [1/3] 验证XML格式...
powershell -NoProfile -Command "try { [xml]$xml = Get-Content 'strings.xml' -Encoding UTF8; Write-Host '  OK: XML格式正确' -ForegroundColor Green } catch { Write-Host '  ERROR: XML格式错误' -ForegroundColor Red; exit 1 }"
if errorlevel 1 goto :error

echo.
echo [2/3] 检查UTF-8编码问题...
powershell -NoProfile -Command "$content = Get-Content 'strings.xml' -Raw -Encoding UTF8; $count = ([regex]::Matches($content, [char]0xFFFD)).Count; if ($count -eq 0) { Write-Host '  OK: 未发现编码错误' -ForegroundColor Green } else { Write-Host \"  ERROR: 发现 $count 处编码错误\" -ForegroundColor Red; exit 1 }"
if errorlevel 1 goto :error

cd ..\..\..\..

echo.
echo [3/3] 检查TRTC SDK版本...
findstr /C:"LiteAVSDK_TRTC:12.8.0.19279" TMessagesProj\build.gradle > nul
if errorlevel 1 (
    echo   ERROR: TRTC SDK版本不正确
    goto :error
) else (
    echo   OK: TRTC SDK版本正确
)

echo.
echo ========================================
echo SUCCESS: 所有检查通过！
echo Android客户端已准备好构建！
echo ========================================
exit /b 0

:error
echo.
echo ========================================
echo FAILED: 发现错误，需要修复
echo ========================================
exit /b 1

