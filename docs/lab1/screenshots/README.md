# 实验1 截图目录说明

把模拟器 / 真机的运行截图按以下文件名放到本目录，实验报告即可自动引用：

| 文件名 | 内容 | 获取方式 |
|---|---|---|
| `01-hello-compose.png` | Compose 应用在本机设备上运行 | 运行后按 `Ctrl+S` 截图，或用 `adb exec-out screencap -p > 01.png` |
| `02-preview-ongoing.png` | 预览面板中 TaskCard「进行中」 | 打开 `TaskCard.kt`，右下 Preview 面板切到 `TaskCard - 进行中`，点右侧截图按钮 |
| `03-preview-completed.png` | 预览面板中 TaskCard「已完成」 | 同上，切到 `TaskCard - 已完成` |
| `04-lab1-screen.png` | Lab1Screen 运行结果（3 条任务，含「未分配」） | 在设备上打开「实验1 · 课程任务」后截图 |

## 真机调试前置条件

真机首次调试需要先在设备上完成以下设置，否则 Android Studio 设备列表里看不到设备：

1. 设置 → 关于手机 → 连续点击「版本号」7 次，开启开发者选项；
2. 设置 → 系统 → 开发者选项 → 打开「USB 调试」；
3. USB 连接电脑后，在手机上确认「允许 USB 调试」弹窗（可勾选「一律允许」）；
4. 电脑端执行 `adb devices` 验证，看到设备序列号即连接成功。

模拟器则无需上述步骤，直接在 Device Manager 中创建并启动 AVD 即可。
