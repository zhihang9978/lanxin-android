# 最终的全面UTF-8编码修复
$FilePath = "TMessagesProj\src\main\res\values\strings.xml"

Write-Host "=== 最终UTF-8编码修复 ===" -ForegroundColor Cyan

# 读取文件
$content = Get-Content $FilePath -Raw -Encoding UTF8

$original = ([regex]::Matches($content, [char]0xFFFD)).Count
Write-Host "发现 $original 处编码错误" -ForegroundColor Yellow

# 执行所有必要的替换
# 替换UTF-8替换字符为合适的字符

# 通用替换模式
$replacements = @{
    # 破折号/连字符
    ([char]0xFFFD + '?') = '—'
    ('??' + [char]0xFFFD) = '—'
    ([char]0xFFFD + [char]0xFFFD) = '—'
    
    # 省略号
    ([char]0xFFFD + '*') = '…**'
    ('**' + [char]0xFFFD) = '**…'
    
    # 撇号
    ([char]0xFFFD + [char]0x27) = [char]0x2019  # '
    ('s' + [char]0xFFFD) = 's'''
    
    # 引号
    ('"' + [char]0xFFFD + '"') = '""'
    ([char]0xFFFD + [char]0xFFFD + '*') = '…**'
    
    # 其他常见模式
    (' ' + [char]0xFFFD + ' ') = ' — '
    ([char]0xFFFD + 'C') = '–'
    ('0' + [char]0xFFFD) = '0–9'
    
    # 清理剩余的替换字符
    [char]0xFFFD = ''
}

$totalFixed = 0
foreach ($pattern in $replacements.Keys) {
    $replacement = $replacements[$pattern]
    $before = $content
    $content = $content.Replace($pattern, $replacement)
    if ($content -ne $before) {
        $count = ([regex]::Matches($before, [regex]::Escape($pattern))).Count
        if ($count -gt 0) {
            Write-Host "  修复 $count 处" -ForegroundColor Yellow
            $totalFixed += $count
        }
    }
}

# 保存
$content | Out-File $FilePath -Encoding UTF8 -NoNewline

$remaining = ([regex]::Matches($content, [char]0xFFFD)).Count
Write-Host "`n修复完成:" -ForegroundColor Cyan
Write-Host "  初始错误: $original" -ForegroundColor Gray
Write-Host "  已修复: $totalFixed" -ForegroundColor Green
Write-Host "  剩余: $remaining" -ForegroundColor $(if ($remaining -eq 0) { 'Green' } else { 'Yellow' })

if ($remaining -eq 0) {
    Write-Host "`n✅ 所有UTF-8编码错误已修复！" -ForegroundColor Green
} else {
    Write-Host "`n⚠️ 仍有 $remaining 处需要手动修复" -ForegroundColor Yellow
}

# 验证XML
try {
    [xml]$xml = Get-Content $FilePath -Encoding UTF8
    Write-Host "✅ XML格式验证通过！" -ForegroundColor Green
}
catch {
    Write-Host "❌ XML验证失败: $($_.Exception.Message)" -ForegroundColor Red
}

