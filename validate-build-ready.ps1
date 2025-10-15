# 验证Android构建准备就绪
Write-Host "=== Android构建准备验证 ===" -ForegroundColor Cyan

$checks = @()

# 1. XML格式验证
Write-Host "`n[1/5] 验证XML格式..." -ForegroundColor Yellow
try {
    [xml]$xml = Get-Content 'TMessagesProj\src\main\res\values\strings.xml' -Encoding UTF8
    $stringCount = $xml.resources.string.Count
    Write-Host "  ✅ XML格式正确 ($stringCount 个字符串资源)" -ForegroundColor Green
    $checks += $true
} catch {
    Write-Host "  ❌ XML格式错误: $($_.Exception.Message)" -ForegroundColor Red
    $checks += $false
}

# 2. UTF-8编码检查
Write-Host "`n[2/5] 检查UTF-8编码问题..." -ForegroundColor Yellow
$content = Get-Content 'TMessagesProj\src\main\res\values\strings.xml' -Raw -Encoding UTF8
$replacementChars = ([regex]::Matches($content, '�')).Count
if ($replacementChars -eq 0) {
    Write-Host "  ✅ 未发现UTF-8替换字符" -ForegroundColor Green
    $checks += $true
} else {
    Write-Host "  ❌ 发现 $replacementChars 个UTF-8替换字符" -ForegroundColor Red
    $checks += $false
}

# 3. TRTC SDK版本检查
Write-Host "`n[3/5] 检查TRTC SDK版本..." -ForegroundColor Yellow
$buildGradle = Get-Content 'TMessagesProj\build.gradle' -Raw
if ($buildGradle -match 'LiteAVSDK_TRTC:12\.8\.0\.19279') {
    Write-Host "  ✅ TRTC SDK版本正确 (12.8.0.19279)" -ForegroundColor Green
    $checks += $true
} else {
    Write-Host "  ❌ TRTC SDK版本不正确" -ForegroundColor Red
    $checks += $false
}

# 4. 构建变体检查
Write-Host "`n[4/5] 检查构建变体..." -ForegroundColor Yellow
$settings = Get-Content 'settings.gradle' -Raw
$variants = @(':TMessagesProj', ':TMessagesProj_App', ':TMessagesProj_AppHuawei', 
              ':TMessagesProj_AppHockeyApp', ':TMessagesProj_AppStandalone', ':TMessagesProj_AppTests')
$allPresent = $true
foreach ($variant in $variants) {
    if ($settings -notmatch [regex]::Escape($variant)) {
        Write-Host "  ❌ 缺少变体: $variant" -ForegroundColor Red
        $allPresent = $false
    }
}
if ($allPresent) {
    Write-Host "  ✅ 所有6个构建变体都存在" -ForegroundColor Green
    $checks += $true
} else {
    $checks += $false
}

# 5. Gradle包装器检查
Write-Host "`n[5/5] 检查Gradle包装器..." -ForegroundColor Yellow
if ((Test-Path 'gradlew') -and (Test-Path 'gradlew.bat')) {
    Write-Host "  ✅ Gradle包装器存在" -ForegroundColor Green
    $checks += $true
} else {
    Write-Host "  ❌ Gradle包装器缺失" -ForegroundColor Red
    $checks += $false
}

# 总结
Write-Host "`n" + ("=" * 50) -ForegroundColor Cyan
$passed = ($checks | Where-Object { $_ -eq $true }).Count
$total = $checks.Count

if ($passed -eq $total) {
    Write-Host "✅ 所有检查通过 ($passed/$total)" -ForegroundColor Green
    Write-Host "`n🚀 Android客户端已准备好构建！" -ForegroundColor Green
    exit 0
} else {
    Write-Host "❌ 部分检查失败 ($passed/$total)" -ForegroundColor Red
    Write-Host "`n⚠️ 需要修复上述问题后才能构建" -ForegroundColor Yellow
    exit 1
}

