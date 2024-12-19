# PianoWizard

[中文](README.md) | English

## PianoWizard: An Automatic Piano-playing Software on Android

<img src="https://github.com/pansong291/Pictures/raw/master/github/pansong291/piano/wizard/Screenshot_2024-09-28-10-00-03-255_com.netease.sky.jpg" width="800" alt="光遇" />

<img src="https://github.com/pansong291/Pictures/raw/master/github/pansong291/piano/wizard/Screenshot_2024-09-29-13-07-27-211_com.tencent.letsgo.jpg" width="800" alt="元梦之星" />

<img src="https://github.com/pansong291/Pictures/raw/master/github/pansong291/piano/wizard/Screenshot_2024-09-29-19-36-02-492_com.netease.nshm.jpg" width="800" alt="逆水寒" />

<img src="https://github.com/pansong291/Pictures/raw/master/github/pansong291/piano/wizard/Screenshot_2024-09-29-19-45-47-736_com.netease.nshm.jpg" width="800" alt="逆水寒" />

<img src="https://github.com/pansong291/Pictures/raw/master/github/pansong291/piano/wizard/Screenshot_2024-09-29-19-56-36-501_com.netease.harrypotter.jpg" width="800" alt="哈利波特：魔法觉醒" />

<img src="https://github.com/pansong291/Pictures/raw/master/github/pansong291/piano/wizard/Screenshot_2024-10-27-01-51-03-416_com.gamestar.perfectpiano.jpg" width="800" alt="完美钢琴" />

<img src="https://github.com/pansong291/Pictures/raw/master/github/pansong291/piano/wizard/Screenshot_2024-10-25-09-57-10-740_pansong291.piano.wizard.jpg" width="400" alt="浏览乐谱" />

<img src="https://github.com/pansong291/Pictures/raw/master/github/pansong291/piano/wizard/Screenshot_2024-10-25-09-54-53-927_pansong291.piano.wizard.jpg" width="400" alt="演奏设置" />

### Video Demonstrations

* Layout Tutorial: https://youtu.be/tU5OIt_g4E0
* Perfect Piano 88 Keys - Numb: https://youtu.be/gurdiKIJNsI
* Heartbeat Town 37 Keys - Stars in the Galaxy: https://youtu.be/ETLv10iMvRE
* Yuanmeng Zhixing 21 Keys - Missing You Through Time and
  Space: https://www.bilibili.com/video/BV1PRxieAEUf

### Download

https://url56.ctfile.com/d/62103256-63818110-e3e527?p=2624

Password: `2624`

* The idiot version comes with sheet music and can be decompressed with one click without the need
  for manual decompression.
* The ordinary version requires you to decompress the sheet music by yourself.

### Functional Features

1. Fully customizable key layouts, which theoretically apply to all games.
    * For example, in "Sky: Children of the Light", there are only 15 keys, with the lowest note
      being C and going up to C two octaves higher.
    * The piano in "Yuanmeng Zhixing" has 21 keys, which are the natural notes of three octaves.
    * The piano in "Harry Potter: Magic Awakened" has 36 keys and contains semitones, which is the
      equal temperament of three octaves.
    * Something special about "Justice Online" is that apart from the keyboards mentioned above,
      some of its instruments are not even based on the C key, such as the flute and the xiao. There
      may be a few more or fewer low notes. And this can be solved by setting the key offset, so
      PianoWizard is also applicable to "Justice Online".

2. Transposition can be set. Some sheet music cannot be played completely on certain keyboards, such
   as the 15-key and 21-key ones. These keyboards don't have semitones. PianoWizard will try to find
   a suitable transposition to play the song on the current keyboard.

3. It supports playing chords. Chords can be understood as pressing multiple keys simultaneously.

4. It supports converting sheet music from SkyStudio.

5. It supports adjusting the playback speed and three different click modes: tap, hold, and
   continuous click.

6. It supports converting Midi files.

7. It supports setting the pre-play and post-play durations and hiding the control window.

### Others

Currently, PianoWizard only has the basic automatic playing function. More functions will be
developed in the future. The functions that have been planned so far are:

1. Support for manually creating sheet music (numbered musical notation).
2. Support for a simple online sheet music search service.

### Problems Encountered

1. The guzheng in "Justice Online" is really laggy and often disrupts the rhythm of the music. That
   is, sometimes there is a delay, causing it to combine with the next syllable.
2. In "Yuanmeng Zhixing", the same key cannot be pressed continuously in a short period of time. You
   can try it yourself. If you quickly press a key three times, it will only sound twice.
3. So far, there haven't been any problems encountered in "Sky: Children of the Light", "Heartbeat
   Town", and "Harry Potter: Magic Awakened".
4. "Genshin Impact", "Identity V", and other games haven't been tested yet.

### Sheet music grammar

```
// Sheet music: Brother John

// This is a single-line comment

/*
 This is a block comment
*/

// The content of comments will be ignored and is used to provide explanations or remarks for the sheet music content.
// The sheet music allows adding whitespace characters at any position to make it look more comfortable.

[ 1=C, 4/4, 120 ]         // Key signature, time signature, beats per minute.
                          // The key signature can be C, D, E, F, G, A, B.
                          // It can be used in combination with accidentals, such as C#, Db.

1, 2, 3, 1,               // 1 represents "do", 2 represents "re", and so on.
1, 2, 3, 1,               // Each beat is separated by a comma.
3, 4, 5*2,                // 5*2 means the duration of the beat of the note 5 is multiplied by 2.
3, 4, 5*2,
5/2, 6/2, 5/2, 4/2, 3, 1, // 5/2 means the duration of the beat of the note 5 is divided by 2.
5/2, 6/2, 5/2, 4/2, 3, 1,
1, 5-1, 1*2,              // 5-1 means the beat of the note 5 needs to be lowered by one octave.
                          // Similarly, 5+1 means it needs to be raised by one octave.
1, 5-, 1*2,               // When raising or lowering by one octave, the number 1 can be omitted.
                          // Here, 5- is the same as 5-1.

0*8,                      // 0 represents a rest or a pause.

// Chords use the & symbol to combine notes, for example:
1 & 2- & 3+2 *2,
// It means there are three notes in this beat, which are "do", "re" lowered by one octave, and "mi" raised by two octaves respectively.
// The final *2 means the duration of this beat needs to be multiplied by 2.

// When the duration multiplier is not an integer, multiply it by a fraction, for example, to represent a duration of one third:
7 * 1/3, 7 / 3,

// To represent a duration of two thirds:
7 * 2/3,

// Use the # symbol to raise by a half step:
1#,

// To raise by two half steps:
1#2,

// Use the b symbol to lower by a half step:
2b,

// To lower by two half steps:
2b2,

// To raise by one octave and lower by a half step:
3+b, 3b+,

```
