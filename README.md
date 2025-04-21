# Neural Swipe Typing for Android Keyboards

An android library to provide keyboards (IME)  with neural network-powered swipe typing and a demo app that uses the library.

The models are trained in a separate [neural-swipe-typing repository](https://github.com/proshian/neural-swipe-typing).

> [!Note]
> The demo app supports only swipe typing and is not suitable for daily use — you cannot type 
> individual symbols or even press the Enter key. It's a showcase of the library integration, 
> and all nuances unrelated to swipe typing are out of scope for this project.

## Demo
https://github.com/user-attachments/assets/c8375b80-4ac3-4b2a-9423-933cd321a546

## Download Demo App
A pre-built APK is available in the [Releases section](https://github.com/proshian/neural-swipe-keyboard-android/releases) of this repository.

## Why This Exists
Most keyboard apps from large tech companies log your swipe gestures to their servers. 
While open-source keyboards protect your privacy, they often can't match the swipe-typing
accuracy of proprietary offerings. This gap exists because proprietary keyboards have been using 
neural networks for this task 
([1](https://research.google/blog/the-machine-intelligence-behind-gboard/), 
[2](https://www.grammarly.com/blog/engineering/deep-learning-swipe-typing/), 
[3](https://yandex.ru/company/news/02-06-23)) since 2015, 
when [Google demonstrated that neural networks improve swipe typing decoding](https://ieeexplore.ieee.org/document/7178336). 
Developing these models requires ML expertise that may be lacking in small teams. 
This project aims to help mobile developers build privacy-focused keyboards.

## State of the Project  

### Library
The library is available as a package named neuralSwipeTyping. 

Only Russian is supported at the moment due to the lack of datasets for other languages.
It is possible to generate synthetic data
(the easiest way is to follow [this paper](https://www.tandfonline.com/doi/full/10.1080/07370024.2016.1215922)).
Expanding language support is the top development priority.

### App
A functional swipe-typing demo app is currently available.

The app is stable on tested devices. 
However, crashes may occur due to RAM limitations on some devices. 
The app currently uses a large trie (~170 MB) (the vocabulary contains over 0.5 million Russian words). 
Some devices restrict apps from consuming more than 256 MB of RAM. 
The vocabulary probably needs to be significantly reduced.  



## Getting Started

The core swipe decoding functionality is implemented in the [NeuralSwipeTypingDecoder](./neuralSwipeTyping/src/main/java/io/github/proshian/neuralswipetyping/swipeTypingDecoders/NeuralSwipeTypingDecoder.kt) class. 
This is the essential component you would integrate into a keyboard implementation. See its documentation for details.
A convinient way to create a neural swipe typing decoder is to use the [StandardNeuralSwipeTypingDecoderFactory](./neuralSwipeTyping/src/main/java/io/github/proshian/neuralswipetyping/swipeTypingDecoders/StandardNeuralSwipeTypingDecoderFactory.kt) class.



### 1. Add Dependency

```gradle
implementation(project(":neuralSwipeTyping"))
```

### 2. Configure Decoder

Create a JSON configuration file in `assets/swipeTypingDecoderConfigs/`. Example:

```json
{
  "modelPath": "models/ru_default.pte",
  "wordTokenizerPath": "tokenizers/word/ru.json",
  "decodingAlgorithmConfig": {
    "type": "beam_search",
    "params": {
      "max_steps": 35,
      "beam_size": 5
    }
  },
  "featureExtractorConfig": {
    "type": "traj+nearest_keys",
    "params": {
      "include_time": false,
      "include_velocities": true,
      "include_acceleration": true,
      "width": 1080,
      "height": 667,
      "keyboard_grid_path": "keyboardLayouts/ru_default.json",
      "keyboard_tokenizer_path": "tokenizers/keyboard/ru.json"
    }
  },
  "logitsProcessorConfig": {
    "type": "prebuilt_trie",
    "params": {
      "trie_path": "logitProcessorResources/trie.ser"
    }
  }
}
```

### 3. Initialize Decoder

```kotlin
fun loadConfig(configPath: String): NeuralSwipeTypingDecoderConfig {
    val json = assets.open(configPath).use { it.reader().readText() }
    return Json.decodeFromString(json)
}
val config = loadConfig("swipeTypingDecoderConfigs/ru_default.json")
val decoder = StandardNeuralSwipeTypingDecoderFactory().create(context, config)
```

### 4. Decode Swipes

```kotlin
val candidates = decoder.decodeSwipe(xCoords, yCoords, timestamps)
```




## Asset Requirements

To use the library, you'll need these assets in your `assets/` folder:

```
assets/
├── models/                   
├── tokenizers/
│   ├── word/                 
│   └── keyboard/             
├── keyboardLayouts/          
├── logitProcessorResources/  
└── swipeTypingDecoderConfigs 
```




## Quick Start to build the demo app
1. Obtain assets
    Option 1. Generate artifacts yourself:
    * xnnpack_my_nearest_feats.pte (neural network)
        * Use [executorch_export.ipynb in neural-glide-typing project](https://github.com/proshian/neural-swipe-typing/blob/executorch-investigation/src/executorch_export.ipynb) to create ru_default__xnnpack_my_nearest_feats.pte.
        * Move `ru_default__xnnpack_my_nearest_feats.pte` to `app/src/main/assets/models/ru_default__xnnpack_my_nearest_feats.pte`
    * trie.ser
        * Execute trie-builder/src/main/java/com/example/trie_builder/Main.kt

    Option 2. Download ru_default__xnnpack_my_nearest_feats.pte and trie.ser from the assets of the [latest release](https://github.com/proshian/neural-swipe-keyboard-android/releases/)
              and place them in app/src/main/assets/models and app/src/main/assets/logitProcessorResources  
              respectively.

2. Build this android app with Android Studio



## Upcoming Improvements  
- [ ] **Add support for English**
- [ ] Create a pipeline for creating a synthetic dataset and training neural networks for any new languages and keyboard layouts



## Contributions Welcome!
