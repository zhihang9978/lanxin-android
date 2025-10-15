# 列出所有UTF-8替换字符位置
$FilePath = "TMessagesProj\src\main\res\values\strings.xml"

Write-Host "=== 查找所有UTF-8替换字符 ===" -ForegroundColor Cyan

$lines = Get-Content $FilePath -Encoding UTF8
$errors = @()

for ($i = 0; $i -lt $lines.Count; $i++) {
    $line = $lines[$i]
    $lineNum = $i + 1
    
    if ($line -match [char]0xFFFD) {
        $errors += @{
            Line = $lineNum
            Content = $line.Trim()
        }
    }
}

Write-Host "发现 $($errors.Count) 处编码错误:`n" -ForegroundColor Yellow

# 显示前50个错误
$displayCount = [Math]::Min(50, $errors.Count)
for ($i = 0; $i -lt $displayCount; $i++) {
    $e = $errors[$i]
    Write-Host "行 $($e.Line): $($e.Content)"
}

if ($errors.Count -gt 50) {
    Write-Host "`n... 还有 $($errors.Count - 50) 处错误未显示"
}

