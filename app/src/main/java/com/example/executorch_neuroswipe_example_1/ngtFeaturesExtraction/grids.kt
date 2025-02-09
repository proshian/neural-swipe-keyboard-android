package com.example.executorch_neuroswipe_example_1.ngtFeaturesExtraction

class KeyboardKeyHitbox(val x: Int, val y: Int, val w: Int, val h: Int)

class KeyboardKey (val label: String, val hitbox: KeyboardKeyHitbox)

class KeyboardGrid(val width: Int, val height: Int, val keys: List<KeyboardKey>)


fun getDefaultGrid(): KeyboardGrid {
    return KeyboardGrid(1080, 667, listOf(
        KeyboardKey("й", KeyboardKeyHitbox(0, 15, 99, 154)),
        KeyboardKey("ц", KeyboardKeyHitbox(98, 15, 99, 154)),
        KeyboardKey("у", KeyboardKeyHitbox(196, 15, 100, 154)),
        KeyboardKey("к", KeyboardKeyHitbox(295, 15, 99, 154)),
        KeyboardKey("е", KeyboardKeyHitbox(393, 15, 99, 154)),
        KeyboardKey("н", KeyboardKeyHitbox(491, 15, 99, 154)),
        KeyboardKey("г", KeyboardKeyHitbox(589, 15, 99, 154)),
        KeyboardKey("ш", KeyboardKeyHitbox(687, 15, 99, 154)),
        KeyboardKey("щ", KeyboardKeyHitbox(785, 15, 100, 154)),
        KeyboardKey("з", KeyboardKeyHitbox(884, 15, 99, 154)),
        KeyboardKey("х", KeyboardKeyHitbox(982, 15, 98, 154)),
        KeyboardKey("ф", KeyboardKeyHitbox(0, 169, 99, 154)),
        KeyboardKey("ы", KeyboardKeyHitbox(98, 169, 99, 154)),
        KeyboardKey("в", KeyboardKeyHitbox(196, 169, 100, 154)),
        KeyboardKey("а", KeyboardKeyHitbox(295, 169, 99, 154)),
        KeyboardKey("п", KeyboardKeyHitbox(393, 169, 99, 154)),
        KeyboardKey("р", KeyboardKeyHitbox(491, 169, 99, 154)),
        KeyboardKey("о", KeyboardKeyHitbox(589, 169, 99, 154)),
        KeyboardKey("л", KeyboardKeyHitbox(687, 169, 99, 154)),
        KeyboardKey("д", KeyboardKeyHitbox(785, 169, 100, 154)),
        KeyboardKey("ж", KeyboardKeyHitbox(884, 169, 99, 154)),
        KeyboardKey("э", KeyboardKeyHitbox(982, 169, 98, 154)),
        KeyboardKey("", KeyboardKeyHitbox(0, 323, 120, 154)),  // shift
        KeyboardKey("я", KeyboardKeyHitbox(119, 323, 94, 154)),
        KeyboardKey("ч", KeyboardKeyHitbox(212, 323, 95, 154)),
        KeyboardKey("с", KeyboardKeyHitbox(306, 323, 94, 154)),
        KeyboardKey("м", KeyboardKeyHitbox(399, 323, 95, 154)),
        KeyboardKey("и", KeyboardKeyHitbox(493, 323, 94, 154)),
        KeyboardKey("т", KeyboardKeyHitbox(586, 323, 95, 154)),
        KeyboardKey("ь", KeyboardKeyHitbox(680, 323, 94, 154)),
        KeyboardKey("б", KeyboardKeyHitbox(773, 323, 95, 154)),
        KeyboardKey("ю", KeyboardKeyHitbox(867, 323, 95, 154)),
        KeyboardKey("", KeyboardKeyHitbox(961, 323, 119, 154)),  // backspace
        KeyboardKey("", KeyboardKeyHitbox(0, 477, 141, 154)),  // toNumberState
        KeyboardKey("", KeyboardKeyHitbox(140, 477, 120, 154)),  // globe
        KeyboardKey("", KeyboardKeyHitbox(259, 477, 98, 154)),  // ,
        KeyboardKey("", KeyboardKeyHitbox(356, 477, 455, 154)),  // space
        KeyboardKey("", KeyboardKeyHitbox(810, 477, 98, 154)),  // .
        KeyboardKey("", KeyboardKeyHitbox(907, 477, 173, 154)),  // enter
    ))
}