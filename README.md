# Neural Swipe Typing Android Demo

**Proof-of-concept implementation** demonstrating how to integrate a neural swipe typing model into an Android keyboard. 

> [!Note]
> This is not designed as a production keyboard, but as a reference implementation for developers.


## Demo


## Why This Exists
Most keyboard apps from large tech companies log your swipe gestures to their servers. While open-source keyboards protect your privacy, they often can't match the swipe-typing accuracy of proprietary offerings. This gap exists because proprietary keyboards have been using neural networks for this task ([1](https://research.google/blog/the-machine-intelligence-behind-gboard/), [2](https://www.grammarly.com/blog/engineering/deep-learning-swipe-typing/), [3](https://yandex.ru/company/news/02-06-23)) since 2015  when [Google demonstrated that neural networks improve swipe typing decoding](https://ieeexplore.ieee.org/document/7178336). Developing these models requires ML expertise that may be lacking in small teams. This project aims at helping mobile developers building privacy-focused keyboards.


## State of the project
Working swipe-typing demo app for Russian only (due to the lack of datasets for other languages).

It is possible generate synthetic data (the easiest way is to use minimal jerk trajectory with noise as described in [this](https://www.tandfonline.com/doi/full/10.1080/07370024.2016.1215922) paper). This is the main current goal


The app was tested on several devices with no crashes. However, if a crash happens it may be due to RAM restrictions of a device. The app currently uses a huge trie (~170Mb) (the vocabulary contains over 0.5 million Russian words). Some devices don't allow apps that consume over 256 Mb of RAM. Probably the vocabulary has to be sighnificatly cut.


## Where is the transformation from swipe into sorted list of words done?

The swipe decoding is done by [NeuralSwipeTypingDecoder](./app/src/main/java/com/example/neuralSwipeKeyboardProject/swipeTypingDecoders/NeuralSwipeTypingDecoder.kt) class. That's the component that you need in your keyboard to decode the swipe typing gestures. See its documentation for the details


## Quick Start
1. Obtain assets
    
    Option 1. Generate artifacts yourself:
    * xnnpack_my_nearest_feats.pte (neural network)
        * Use [executorch_export.ipynb in neural-glide-typing project](https://github.com/proshian/neural-swipe-typing/blob/executorch-investigation/src/executorch_export.ipynb) to create xnnpack_my_nearest_feats.pte.
        * Move `xnnpack_my_nearest_feats.pte` to `app/src/main/assets/xnnpack_my_nearest_feats.pte`
    * trie.ser
        * Execute trie-builder/src/main/java/com/example/trie_builder/Main.kt

    Option 2. Download xnnpack_my_nearest_feats.pte and trie.ser from the assets of the [latest release](https://github.com/proshian/neural-swipe-keyboard-android/releases/) and place them in app/src/main/assets
2. Obtain prebuilt executorch.aar as below:
    ```shell
    # The link is taken from official documentation: 
    # https://pytorch.org/executorch/0.5/android-prebuilt-library.html#using-prebuilt-libraries
    mkdir -p app/libs
    curl https://ossci-android.s3.amazonaws.com/executorch/release/v0.5.0-rc3/executorch.aar -o app/libs/executorch.aar
    ```
3. Build this android app with Android Studio


## Upcoming Improvements
- [ ] Most importantely - create and add models for English and other languages
- [ ] Instruction on how to generate a synthetic dataset, train and integrate models for new languages and new keyboard layouts
- [ ] UI Design enhancement
- [ ] Add swipe trail visualization
- [ ] Reading tokenizers from json via serializable
- [ ] Move tokenizers to a separate module
- [ ] Delete tokenizers duplicate from the trie-builder
- [ ] JSON configs that bound all swipe-typing-related entities states. Example:
  ```json
  {
    "feature_extractor": "traj+nearest_keys",
    "subword_tokenizer": "assets/ru_tokenizer.json",
    "keyboard_keys_tokenizer": "assets/ru_keyboard_tokenizer.json",
    "grid": "assets/grids/default.json",
    "model": "assets/swipe_model.pte"
  }
  ```
- [ ] Support for multiple keyboard layouts



## Contributions welcome!