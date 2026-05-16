# PaiCLI 移植踩坑记录

> **日期**：2026-05-16  
> **任务**：将 PaiCLI 项目移植到 Zong 项目的 services/agent 模块  
> **参与人员**：AI Agent + 用户

---

## 问题1：读取大文件速度太慢

### 问题描述

在移植 PaiCLI 代码时，AI 需要读取源文件（如 `Agent.java` 1001 行、`PlanExecuteAgent.java` 37KB），但每次只能读取 300-400 行，导致：
- 一个 1000 行的文件需要 3-4 次 `read_file` 调用
- 大量 token 消耗在读取文件上
- 整体进度非常缓慢

### 根本原因

1. **工具限制**：`read_file` 工具默认分段读取（防止超出 token 限制）
2. **逐文件操作**：每个文件单独处理，没有批量机制
3. **依赖链复杂**：`PlanExecuteAgent.java` 依赖 `Planner.java`、`ExecutionPlan.java`、`Task.java` 等，需要逐个读取

### 解决方案

✅ **用户建议**："你直接复制，而不是重新写代码"

**实施步骤**：

1. **用户手动复制**：将 PaiCLI 项目目录直接复制到目标位置（瞬时完成）
   ```powershell
   复制 D:\biancheng\Coding\CLI\paicli-main\src\main\java\com\paicli\
   到   d:\biancheng\Coding\zong\services\agent\src\main\java\com\zong\
   ```

2. **Python 脚本批量替换**：用 Python 脚本批量修改 181 个 Java 文件
   - 替换包名：`com.paicli` → `com.zong`
   - 替换路径：`.paicli` → `.zong`
   - 移除版权声明、作者标记等印记
   - 处理时间：几秒钟完成 181 个文件

### 经验教训

❌ **避免**：让 AI 逐个读取和写入大文件  
✅ **推荐**：
1. 大批量文件操作 → 用户手动复制 + AI 批量修改
2. 文本替换 → 用 Python/PowerShell 脚本批量处理
3. 编码问题 → 用 Python 脚本（比 PowerShell 更可靠）

---

## 问题2：PowerShell 编码问题

### 问题描述

在使用 PowerShell 的 `Get-Content` 和 `Set-Content` 时，遇到编码冲突：
```
Mixing GBK/UTF-8 style encodings in a same-file rewrite can corrupt text.
Preserve the original encoding/BOM, use -AsByteStream,
or use the built-in file editing tools instead of shell text replacement.
```

### 根本原因

- PowerShell 默认编码与文件实际编码不一致（UTF-8 vs GBK）
- `Get-Content` 和 `Set-Content` 使用不同编码读写同一文件

### 解决方案

✅ **改用 Python 脚本**：Python 对 UTF-8 的支持更好，且可以显式指定编码

```python
# 读取时尝试 UTF-8，失败则尝试 GBK
try:
    with open(filepath, 'r', encoding='utf-8') as f:
        lines = f.readlines()
except UnicodeDecodeError:
    with open(filepath, 'r', encoding='gbk') as f:
        lines = f.readlines()

# 写入时统一使用 UTF-8
with open(filepath, 'w', encoding='utf-8') as f:
    f.writelines(new_lines)
```

### 经验教训

❌ **避免**：在 Windows 下用 PowerShell 批量处理文本文件  
✅ **推荐**：
1. 文本批量替换 → 用 Python 脚本
2. 文件操作 → 显式指定编码（UTF-8）
3. Emoji/Unicode 输出 → 注意控制台编码（Windows 默认 GBK）

---

## 问题3：目录重命名冲突

### 问题描述

在重命名目录时，目标目录已存在，导致冲突：
```
Rename-Item : Cannot create a file when that file already exists.
```

### 场景还原

1. 复制 `com/paicli/` 到 `com/zong/` → 产生 `com/zong/paicli/` 目录
2. 需要把 `paicli/` 重命名为 `agent/`
3. 但 `com/zong/` 下已存在 `agent/` 目录（之前创建的）
4. 直接重命名失败（冲突）

### 解决方案

✅ **先删除目标目录，再重命名**：
```powershell
# 1. 删除之前创建的 agent 目录（残留）
Remove-Item -Recurse -Force "d:\biancheng\Coding\zong\services\agent\src\main\java\com\zong\agent"

# 2. 把 paicli 目录重命名为 agent
Rename-Item "d:\biancheng\Coding\zong\services\agent\src\main\java\com\zong\paicli" "agent" -Force
```

### 经验教训

❌ **避免**：直接重命名到已存在的目标  
✅ **推荐**：
1. 重命名前先检查目标是否存在
2. 如果存在，先删除或合并
3. 批量重命名时，先用测试目录验证脚本

---

## 问题4：批量替换时如何移除"原作者印记"

### 问题描述

PaiCLI 源文件中有大量原作者印记需要移除：
- 版权声明：`* Copyright (c) 2024-2025 ...`
- 作者标记：`* @author ...`
- 项目标记：`* @see ...`

如果逐个文件手动编辑，效率极低。

### 解决方案

✅ **在 Python 脚本中加入智能判断**：

```python
def should_remove_line(line):
    """判断是否应该移除该行（版权声明、作者标记等）"""
    stripped = line.strip().lower()
    # 移除版权声明
    if stripped.startswith('* copyright') or stripped.startswith('* @copyright'):
        return True
    # 移除作者标记
    if stripped.startswith('* @author') or stripped.startswith('* author:'):
        return True
    # 移除公司/项目标记
    if 'paicli' in stripped and ('@see' in stripped or '项目' in stripped):
        return True
    return False

def process_file(filepath):
    """处理单个 Java 文件"""
    # ... 读取文件 ...
    
    new_lines = []
    for line in lines:
        # 判断是否应该移除该行
        if should_remove_line(line):
            continue  # 跳过（移除）
        
        # 替换包名、路径等
        # ...
        
        new_lines.append(line)
    
    # ... 写回文件 ...
```

### 经验教训

❌ **避免**：逐个文件手动编辑  
✅ **推荐**：
1. 批量操作 + 规则判断（版权、作者、项目标记）
2. 先用测试文件验证规则（避免误删）
3. 保留重要注释（只移除"印记"，不移除功能注释）

---

## 总结：移植大批量代码的最佳实践

### ✅ 推荐流程

1. **用户手动复制**：将源项目目录直接复制到目标位置（最快）
2. **AI 批量修改**：
   - 用 Python 脚本批量替换包名、路径
   - 移除版权声明、作者标记等印记
   - 处理编码问题（UTF-8 / GBK）
3. **验证结果**：检查目录结构、包名、import 语句是否正确

### ❌ 避免的做法

1. **让 AI 逐个读取和写入大文件**：太慢，消耗大量 token
2. **在 Windows 下用 PowerShell 批量处理文本**：编码问题多
3. **不先清理目标目录就复制**：容易导致冲突

### 📝 工具选择建议

| 任务 | 推荐工具 | 原因 |
|------|----------|------|
| 复制大批量文件 | 用户手动复制 / `Copy-Item` | 最快 |
| 批量文本替换 | Python 脚本 | 编码处理更好 |
| 重命名目录 | PowerShell `Rename-Item` | 简单直接 |
| 删除目录 | PowerShell `Remove-Item -Recurse -Force` | 强制删除 |

---

## 附录：完整的 Python 批量替换脚本

见 `d:\biancheng\Coding\zong\batch_replace.py`（已删除，可重新生成）

**核心功能**：
1. 批量替换包名：`com.paicli` → `com.zong`
2. 批量替换路径：`.paicli` → `.zong`
3. 移除版权声明、作者标记
4. 自动处理编码（UTF-8 / GBK）

**使用方法**：
```bash
cd d:\biancheng\Coding\zong
python batch_replace.py
```

---

**文档版本**：v1.0  
**最后更新**：2026-05-16  
**负责人**：AI Agent + 用户
