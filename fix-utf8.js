const fs = require('fs');
const path = require('path');

const filePath = path.join(__dirname, 'TMessagesProj', 'src', 'main', 'res', 'values', 'strings.xml');

console.log('=== UTF-8 Encoding Fix ===');

// 读取文件
let content = fs.readFileSync(filePath, 'utf8');

// 统计初始错误
const initialCount = (content.match(/\uFFFD/g) || []).length;
console.log(`Found ${initialCount} replacement characters`);

// 执行所有替换
content = content.replace(/\uFFFD\?/g, '—');          // �? → —
content = content.replace(/\?\?\uFFFD/g, '—');        // ??� → —
content = content.replace(/\uFFFD\uFFFD/g, '—');      // �� → —
content = content.replace(/\uFFFD\*/g, '…**');        // �* → …**
content = content.replace(/\*\*\uFFFD/g, '**…');      // **� → **…
content = content.replace(/s\uFFFD/g, "s'");          // s� → s'
content = content.replace(/0\uFFFD/g, '0–9');         // 0� → 0–9
content = content.replace(/\uFFFDC/g, '–');           // �C → –
content = content.replace(/ \uFFFD /g, ' — ');        // � → —
content = content.replace(/\uFFFD/g, '');             // 剩余的� → 删除

// 保存文件
fs.writeFileSync(filePath, content, 'utf8');

// 统计剩余错误
const remainingCount = (content.match(/\uFFFD/g) || []).length;
console.log(`Fixed: ${initialCount - remainingCount}`);
console.log(`Remaining: ${remainingCount}`);

if (remainingCount === 0) {
    console.log('\x1b[32m✓ All UTF-8 errors fixed!\x1b[0m');
} else {
    console.log(`\x1b[33m⚠ Still has ${remainingCount} errors\x1b[0m`);
}

