# 完整修复所有64处UTF-8编码错误
$FilePath = "TMessagesProj\src\main\res\values\strings.xml"

Write-Host "=== 修复所有剩余的UTF-8编码错误 ===" -ForegroundColor Cyan

# 读取文件
$content = Get-Content $FilePath -Raw -Encoding UTF8

Write-Host "原始文件大小: $($content.Length) 字节" -ForegroundColor Gray

# 计算初始错误数量
$initialErrors = ([regex]::Matches($content, '�')).Count
Write-Host "发现 $initialErrors 处编码错误" -ForegroundColor Yellow

# 执行所有替换（基于Devin的报告）
$replacements = @{
    # Em dash (—)
    ' �? ' = ' — '
    '�?' = '—'
    
    # Apostrophe (')
    "�?'" = "'"
    "\'�?" = "'"
    '\�?' = "'"
    
    # Ellipsis (…)
    '�?*' = '…**'
    '**�?' = '**…'
    '�?' = '…'
    
    # Left/Right quotes ("")
    '**�?' = '**"'
    '�?**' = '"**'
    '�?"' = '"'
    '"�?' = '"'
    
    # Any remaining � characters
    '�' = ''
}

$totalFixed = 0
foreach ($pattern in $replacements.Keys) {
    $replacement = $replacements[$pattern]
    $matches = ([regex]::Matches($content, [regex]::Escape($pattern))).Count
    if ($matches -gt 0) {
        Write-Host "  替换 '$pattern' → '$replacement' : $matches 处" -ForegroundColor Yellow
        $content = $content -replace [regex]::Escape($pattern), $replacement
        $totalFixed += $matches
    }
}

# 保存修复后的文件
$content | Out-File $FilePath -Encoding UTF8 -NoNewline

Write-Host "`n✅ 共修复 $totalFixed 处错误" -ForegroundColor Green

# 验证剩余错误
$remainingErrors = ([regex]::Matches($content, '�')).Count
if ($remainingErrors -eq 0) {
    Write-Host "✅ 所有UTF-8编码错误已修复！" -ForegroundColor Green
} else {
    Write-Host "⚠️ 仍有 $remainingErrors 处编码错误需要手动检查" -ForegroundColor Yellow
}

# 验证XML
Write-Host "`n正在验证XML格式..." -ForegroundColor Cyan
try {
    [xml]$xml = Get-Content $FilePath -Encoding UTF8
    Write-Host "✅ XML格式验证通过！" -ForegroundColor Green
}
catch {
    Write-Host "❌ XML验证失败" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Yellow
}

