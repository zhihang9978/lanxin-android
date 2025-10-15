# 简单直接的UTF-8编码修复
$FilePath = "TMessagesProj\src\main\res\values\strings.xml"

Write-Host "=== UTF-8编码修复 ===" -ForegroundColor Cyan

# 读取所有行
$lines = Get-Content $FilePath -Encoding UTF8

Write-Host "处理 $($lines.Count) 行..." -ForegroundColor Gray

# 定义替换映射
$fixedLines = @()
$fixedCount = 0

foreach ($line in $lines) {
    $originalLine = $line
    
    # 替换所有UTF-8替换字符序列
    $line = $line -replace '\uFFFD\?', '—'          # �? → —
    $line = $line -replace '\?\?\uFFFD', '—'        # ??� → —
    $line = $line -replace '\uFFFD\uFFFD', '—'      # �� → —
    $line = $line -replace '\uFFFD\*', '…**'        # �* → …**
    $line = $line -replace '\*\*\uFFFD', '**…'      # **� → **…
    $line = $line -replace 's\uFFFD', "s'"          # s� → s'
    $line = $line -replace '0\uFFFD', '0–9'         # 0� → 0–9
    $line = $line -replace '\uFFFDC', '–'           # �C → –
    $line = $line -replace ' \uFFFD ', ' — '        # � → —
    $line = $line -replace '\uFFFD', ''             # 剩余的� → 删除
    
    if ($line -ne $originalLine) {
        $fixedCount++
    }
    
    $fixedLines += $line
}

# 保存修复后的文件
$fixedLines | Out-File $FilePath -Encoding UTF8

Write-Host "✅ 修复了 $fixedCount 行" -ForegroundColor Green

# 验证
$content = Get-Content $FilePath -Raw -Encoding UTF8
$remaining = ([regex]::Matches($content, '\uFFFD')).Count

Write-Host "剩余错误: $remaining" -ForegroundColor $(if ($remaining -eq 0) { 'Green' } else { 'Yellow' })

try {
    [xml]$xml = Get-Content $FilePath -Encoding UTF8
    Write-Host "✅ XML格式验证通过！" -ForegroundColor Green
}
catch {
    Write-Host "❌ XML验证失败" -ForegroundColor Red
}

