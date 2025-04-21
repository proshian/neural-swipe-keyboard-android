# Neural Swipe Typing for Android Keyboards

An Android library that enables keyboards (IME) to support neural network-powered swipe typing, along with a demo app that showcases its functionality.

The models are trained in a separate [neural-swipe-typing repository](https://github.com/proshian/neural-swipe-typing).

> [!Note]  
> The demo app supports only swipe typing and is not intended for daily use — you cannot type individual symbols or even press the Enter key.  
> It serves as a showcase of the library integration; all nuances unrelated to swipe typing are out of scope for this project.

## Demo  
https://github.com/user-attachments/assets/c8375b80-4ac3-4b2a-9423-933cd321a546

## Download Demo App  
A pre-built APK is available in the [Releases section](https://github.com/proshian/neural-swipe-keyboard-android/releases) of this repository.

## Why This Exists  
Most keyboard apps from large tech companies log your swipe gestures to their servers.  
While open-source keyboards protect your privacy, they often fall short in swipe-typing accuracy compared to proprietary offerings.  
This gap exists because major companies have used neural networks for swipe typing 
([1](https://research.google/blog/the-machine-intelligence-behind-gboard/), 
[2](https://www.grammarly.com/blog/engineering/deep-learning-swipe-typing/), 
[3](https://yandex.ru/company/news/02-06-23)) since 2015, 
when [Google demonstrated that neural networks improve swipe typing decoding](https://ieeexplore.ieee.org/document/7178336). 

Building such models requires ML expertise often unavailable to small teams.  
This project aims to help mobile developers build privacy-focused keyboards.

## State of the Project  

### Library  
The library is available as a package named `neuralSwipeTyping`.  

Currently, only Russian is supported due to the lack of datasets for other languages.  
It’s possible to generate synthetic data — the easiest method is described in  
[this paper](https://www.tandfonline.com/doi/full/10.1080/07370024.2016.1215922).  
Expanding language support is a top development priority.

### App  
A functional swipe-typing demo app is available.

The app is stable on tested devices, though it may crash on some due to RAM limitations.
This is not an issue with the library itself — the current trie (~170 MB) includes over 0.5 million Russian words, which is excessive.
Some devices restrict apps to under 256 MB of RAM, making such a large vocabulary impractical.
The vocabulary will be significantly reduced in future versions.

---

## Getting Started

The core swipe decoding logic is implemented in 
[NeuralSwipeTypingDecoder](./neuralSwipeTyping/src/main/java/io/github/proshian/neuralswipetyping/swipeTypingDecoders/NeuralSwipeTypingDecoder.kt), 
the primary component to integrate into a keyboard. 
See its documentation for details.  
A convenient way to create a swipe decoder is using 
[StandardNeuralSwipeTypingDecoderFactory](./neuralSwipeTyping/src/main/java/io/github/proshian/neuralswipetyping/swipeTypingDecoders/StandardNeuralSwipeTypingDecoderFactory.kt).

### 1. Add Dependency

```gradle
implementation(project(":neuralSwipeTyping"))
```

### 2. Configure Decoder

Create a JSON config file in `assets/swipeTypingDecoderConfigs/`. Example:

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

To use the library, include the following in your `assets/` directory:

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


## Quick Start to Build the Demo App

## Quick Start to build the demo app
1. **Obtain assets**  
   Option 1: Generate artifacts yourself:
   - **`xnnpack_my_nearest_feats.pte` (neural network)**  
     - Use [executorch_export.ipynb](https://github.com/proshian/neural-swipe-typing/blob/executorch-investigation/src/executorch_export.ipynb) 
       in neural-glide-typing project to create `ru_default__xnnpack_my_nearest_feats.pte`  
     - Move it to `app/src/main/assets/models/`

    Option 2. Download the files from the [latest release](https://github.com/proshian/neural-swipe-keyboard-android/releases/) 
    and place them in: 
    - `app/src/main/assets/models/` (for `.pte`)  
    - `app/src/main/assets/logitProcessorResources/` (for `trie.ser`)

2. **Build the Android app** using Android Studio



## Upcoming Improvements  
- [ ] **Add support for English**  
- [ ] Create a pipeline for generating synthetic datasets and training neural networks for any language and layout  



## Contributions Welcome!
