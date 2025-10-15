using System;
using System.IO;
using System.Text;

class FixUtf8
{
    static void Main()
    {
        string filePath = @"TMessagesProj\src\main\res\values\strings.xml";
        
        Console.WriteLine("=== UTF-8 Encoding Fix ===");
        
        // 读取文件
        string content = File.ReadAllText(filePath, Encoding.UTF8);
        
        // 统计初始错误
        int initialCount = CountReplacementChars(content);
        Console.WriteLine($"Found {initialCount} replacement characters");
        
        // 执行替换
        content = content.Replace("\uFFFD?", "—");          // �? → —
        content = content.Replace("??\uFFFD", "—");         // ??� → —
        content = content.Replace("\uFFFD\uFFFD", "—");     // �� → —
        content = content.Replace("\uFFFD*", "…**");        // �* → …**
        content = content.Replace("**\uFFFD", "**…");       // **� → **…
        content = content.Replace("s\uFFFD", "s'");         // s� → s'
        content = content.Replace("0\uFFFD", "0–9");        // 0� → 0–9
        content = content.Replace("\uFFFDC", "–");          // �C → –
        content = content.Replace(" \uFFFD ", " — ");       // � → —
        content = content.Replace("\uFFFD", "");            // 剩余的� → 删除
        
        // 保存文件
        File.WriteAllText(filePath, content, new UTF8Encoding(false));
        
        // 统计剩余错误
        int remainingCount = CountReplacementChars(content);
        Console.WriteLine($"Fixed: {initialCount - remainingCount}");
        Console.WriteLine($"Remaining: {remainingCount}");
        
        if (remainingCount == 0)
        {
            Console.ForegroundColor = ConsoleColor.Green;
            Console.WriteLine("✓ All UTF-8 errors fixed!");
            Console.ResetColor();
        }
    }
    
    static int CountReplacementChars(string text)
    {
        int count = 0;
        foreach (char c in text)
        {
            if (c == '\uFFFD') count++;
        }
        return count;
    }
}

