# Neural Swipe Typing Android Demo

**Proof-of-concept implementation** demonstrating how to integrate a neural swipe typing model into an Android keyboard. 
This project is a reference implementation for developers who want to build a privacy-focused keyboard app with swipe typing capabilities.

The models are trained in a separate [neural-swipe-typing repository](https://github.com/proshian/neural-swipe-typing).

> [!Note]
> This is not designed as a production keyboard but as a reference implementation for developers.


## Demo


## Why This Exists
Most keyboard apps from large tech companies log your swipe gestures to their servers. While open-source keyboards protect your privacy, they often can't match the swipe-typing accuracy of proprietary offerings. This gap exists because proprietary keyboards have been using neural networks for this task ([1](https://research.google/blog/the-machine-intelligence-behind-gboard/), [2](https://www.grammarly.com/blog/engineering/deep-learning-swipe-typing/), [3](https://yandex.ru/company/news/02-06-23)) since 2015,  when [Google demonstrated that neural networks improve swipe typing decoding](https://ieeexplore.ieee.org/document/7178336). Developing these models requires ML expertise that may be lacking in small teams. This project aims to help mobile developers build privacy-focused keyboards.

## State of the Project  
A functional swipe-typing demo app is currently available.

> [!Note]
> The keyboard supports only swipe typing and is not suitable for daily use—you cannot type individual symbols or even press the enter button. This is intentional: the project is merely to demonstrate an example of how to integrate a neural swipe typing model into an Android keyboard. All other nuances of keyboard development are out of scope for this project. 

Only Russian is supported at the moment due to the lack of datasets for other languages. It is possible to generate synthetic data (the easiest way is to follow [this paper](https://www.tandfonline.com/doi/full/10.1080/07370024.2016.1215922)). Expanding language support is the top development priority.  

The app is stable on tested devices. However, crashes may occur due to RAM limitations on some devices. The app currently uses a large trie (~170 MB) (the vocabulary contains over 0.5 million Russian words). Some devices restrict apps from consuming more than 256 MB of RAM. The vocabulary probably needs to be significantly reduced.  

## Where Does Swipe-to-Words Transformation Happen?  
The core swipe decoding functionality is implemented in the [NeuralSwipeTypingDecoder](./app/src/main/java/com/example/neuralSwipeKeyboardProject/swipeTypingDecoders/NeuralSwipeTypingDecoder.kt) class. This is the essential component you would integrate into a keyboard implementation. See its documentation for details.  

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
- [ ] **Add support for more languages, including English**  
- [ ] Instructions on generating synthetic datasets, training, and integrating models for new languages and keyboard layouts  
- [ ] UI design enhancements  
- [ ] Swipe trail visualization  
- [ ] Reading tokenizers from JSON via serializable  
- [ ] Moving tokenizers to a separate module  
- [ ] Removing duplicate tokenizers from the trie-builder  
- [ ] JSON configs to define swipe-typing-related entities. Example:  
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

## Contributions Welcome!
