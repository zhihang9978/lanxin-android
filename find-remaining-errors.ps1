# 查找所有剩余的UTF-8编码问题
$FilePath = "TMessagesProj\src\main\res\values\strings.xml"

Write-Host "=== 扫描剩余UTF-8编码错误 ===" -ForegroundColor Cyan

$lines = Get-Content $FilePath -Encoding UTF8
$errors = @()

for ($i = 0; $i -lt $lines.Count; $i++) {
    $line = $lines[$i]
    $lineNum = $i + 1
    
    # 检查各种可能的编码问题
    if ($line -match '�') {
        $errors += "行 $lineNum : 包含替换字符 (�)`n  $line"
    }
    
    # 检查可能缺失的字符（空标签内容但原本应该有内容）
    if ($line -match '<string[^>]*>\s*</string>' -and $line -notmatch 'name="(AppName|Empty|Divider)"') {
        # 某些空字符串是有意的，跳过
    }
}

if ($errors.Count -gt 0) {
    Write-Host "`n发现 $($errors.Count) 处潜在错误:" -ForegroundColor Yellow
    $errors | ForEach-Object { Write-Host $_ -ForegroundColor Red }
} else {
    Write-Host "`n✅ 未发现UTF-8编码错误！" -ForegroundColor Green
}

# 统计特殊字符使用情况
Write-Host "`n=== 特殊字符统计 ===" -ForegroundColor Cyan
$content = Get-Content $FilePath -Raw -Encoding UTF8

$specialChars = @{
    'Em dash (—)' = ([regex]::Matches($content, '—')).Count
    'Apostrophe (\u2019)' = ([regex]::Matches($content, ''')).Count  
    'Ellipsis (…)' = ([regex]::Matches($content, '…')).Count
    'Left quote (\u201C)' = ([regex]::Matches($content, '"')).Count
    'Right quote (\u201D)' = ([regex]::Matches($content, '"')).Count
    'Replacement char (�)' = ([regex]::Matches($content, '�')).Count
}

foreach ($char in $specialChars.Keys) {
    $count = $specialChars[$char]
    if ($count -gt 0) {
        Write-Host "  $char : $count 处" -ForegroundColor Gray
    }
}

