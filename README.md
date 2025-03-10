# PianoWizard

中文 | [English](README_en.md)

## 游界琴魔：一款安卓上的自动弹琴软件

<img src="https://github.com/pansong291/Pictures/raw/master/github/pansong291/piano/wizard/Screenshot_2024-09-28-10-00-03-255_com.netease.sky.jpg" width="800" alt="光遇" />

<img src="https://github.com/pansong291/Pictures/raw/master/github/pansong291/piano/wizard/Screenshot_2024-09-29-13-07-27-211_com.tencent.letsgo.jpg" width="800" alt="元梦之星" />

<img src="https://github.com/pansong291/Pictures/raw/master/github/pansong291/piano/wizard/Screenshot_2024-09-29-19-36-02-492_com.netease.nshm.jpg" width="800" alt="逆水寒" />

<img src="https://github.com/pansong291/Pictures/raw/master/github/pansong291/piano/wizard/Screenshot_2024-09-29-19-45-47-736_com.netease.nshm.jpg" width="800" alt="逆水寒" />

<img src="https://github.com/pansong291/Pictures/raw/master/github/pansong291/piano/wizard/Screenshot_2024-09-29-19-56-36-501_com.netease.harrypotter.jpg" width="800" alt="哈利波特：魔法觉醒" />

<img src="https://github.com/pansong291/Pictures/raw/master/github/pansong291/piano/wizard/Screenshot_2024-10-27-01-51-03-416_com.gamestar.perfectpiano.jpg" width="800" alt="完美钢琴" />

<img src="https://github.com/pansong291/Pictures/raw/master/github/pansong291/piano/wizard/Screenshot_2024-10-25-09-57-10-740_pansong291.piano.wizard.jpg" width="400" alt="浏览乐谱" />

<img src="https://github.com/pansong291/Pictures/raw/master/github/pansong291/piano/wizard/Screenshot_2024-10-25-09-54-53-927_pansong291.piano.wizard.jpg" width="400" alt="演奏设置" />

### Bilibili 视频演示

* 布局教程: https://www.bilibili.com/video/BV18jDvY2E5p
* 完美钢琴 88 键 - Numb： https://www.bilibili.com/video/BV15j1MYPEtT
* 心动小镇 37 键 - 星河万里： https://www.bilibili.com/video/BV1otxmeJERy
* 元梦之星 21 键 - 穿越时空的思念： https://www.bilibili.com/video/BV1PRxieAEUf
* 无限暖暖 14 键 - 向云端： https://www.bilibili.com/video/BV1Dg6WYREYi
* 奥比岛 22 键 - 诀别书： https://www.bilibili.com/video/BV1Wn6HYgE1r
* 会玩 21 键 - 暖暖： https://www.bilibili.com/video/BV12frXYBESo

### 下载

https://url56.ctfile.com/d/62103256-63818110-e3e527?p=2624

密码: `2624`

* 懒人版自带乐谱，可一键解压，无需手动解压。
* 普通版需要自己解压乐谱。

### 功能特性

1. 完全自定义按键布局，理论上适用所有游戏。
    * 比如光遇只有 15 键，最低音是 C，一直到升两个八度的 C。
    * 元梦之星的钢琴有 21 键，就是 3 个八度的自然音。
    * 哈利波特里的钢琴是 36 键，它含有半音符，就是 3 个八度的十二平均律。
    * 特殊一点的逆水寒，除了上面提到的键盘外，它有些乐器甚至不是以 C
      调打底，比如笛子和箫，可能多出几个低音，也有少了几个低音的。而这可以通过设置按键偏移来解决，所以游界琴魔也是适用于逆水寒的。

2. 可设置变调，有些乐谱在某些键盘里是无法完整弹奏出来的，如 15 键， 21
   键，这些键盘没有半音，游界琴魔会尝试找出一个合适的变调以在当前键盘上演奏该歌曲。

3. 支持弹奏和弦，和弦可以理解为同时按下多个按键。

4. 支持转换 SkyStudio 的乐谱。

5. 支持倍速调整和三种不同的点击模式：点触、按住和连点。

6. 支持转换 Midi 文件。

7. 支持设置演奏前后摇，隐藏控制窗口。

### 其他

目前游界琴魔只有基础的自动弹奏功能。后续会开发更多功能，目前已规划功能有：

1. 支持手动打谱（简谱）。
2. 支持一个简单的在线乐谱搜索服务。

### 遇到的问题

1. 逆水寒那个古筝真的是太卡了，经常会打乱音乐节奏，就是它有时候有延迟，导致跟下一个音节拼到一起了。
2. 元梦之星同一个按键无法在短时间内连续按下，自己动手也能试出来，快速连按一个键 3 次，它只会响 2 次。
3. 光遇目前还没有遇到任何问题，心动小镇和哈利波特也没问题。
4. 原神，第五人格以及其他游戏还没测试。

### 乐谱语法

```
// 乐谱：两只老虎

// 这是单行注释

/*
 这是块注释
*/

// 注释的内容会被忽略，用于对乐谱内容作说明或备注。
// 乐谱允许在任意位置添加空白字符，可以使其看起来更舒适一些。

[ 1=C, 4/4, 120 ]         // 基准音调，节拍，每分钟拍数。
                          // 基准音调可为 C, D, E, F, G, A, B。
                          // 可配合变音记号使用，如 C#, Db。

1, 2, 3, 1,               // 1 表示 do，2 表示 re，以此类推。
1, 2, 3, 1,               // 每个节拍使用英文逗号隔开。
3, 4, 5*2,                // 5*2 表示 5 这个节拍的时值乘以 2。
3, 4, 5*2,
5/2, 6/2, 5/2, 4/2, 3, 1, // 5/2 表示 5 这个节拍的时值除以 2。
5/2, 6/2, 5/2, 4/2, 3, 1,
1, 5-1, 1*2,              // 5-1 表示 5 这个节拍的音符需要降 1 个八度。
                          // 类似的，5+1 表示需要升 1 个八度。
1, 5-, 1*2,               // 升降 1 倍的八度时，数字 1 可省略。
                          // 此处的 5- 即为 5-1。

0*8,                      // 0 表示休止符或者停顿。

// 和弦使用 & 符号拼接音符，比如：
1 & 2- & 3+2 *2,
// 表示这个节拍有三个音符，分别是 do，降 1 个八度的 re，和升 2 个八度的 mi。
// 最后的 *2 表示这个节拍的时值需要乘以 2。

// 时值倍率不是整数时，则乘以一个分数，比如表示三分之一的时值：
7 * 1/3, 7 / 3,

// 表示三分之二的时值：
7 * 2/3,

// 升半调使用 # 号：
1#,

// 升两个半调：
1#2,

// 降半调使用 b 号：
2b,

// 降两个半调：
2b2,

// 升一个八度降半调：
3+b, 3b+,

```
