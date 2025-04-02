# Neural Swipe Typing Demo (Android)

**Proof-of-concept implementation** demonstrating how to integrate a neural swipe typing model into an Android keyboard. 

> [!Note]
> This is not designed as a production keyboard, but as a reference implementation for developers.


## Why This Exists
Most keyboard apps from large tech companies log your swipe gestures to their servers. While open-source keyboards protect your privacy, they often can't match the swipe-typing accuracy of proprietary offerings. This gap exists because proprietary keyboards have been using neural networks for this task ([1](https://research.google/blog/the-machine-intelligence-behind-gboard/), [2](https://www.grammarly.com/blog/engineering/deep-learning-swipe-typing/), [3](https://yandex.ru/company/news/02-06-23)) since 2015  when [Google demonstrated that neural networks improve swipe typing decoding](https://ieeexplore.ieee.org/document/7178336). Developing these models requires ML expertise that may be lacking in small teams. This project aims at helping mobile developers building privacy-focused keyboards.


## Quick Start
1. Obtain xnnpack_my_nearest_feats.pte
    * Use [executorch_export.ipynb in neural-glide-typing project](https://github.com/proshian/neural-swipe-typing/blob/executorch-investigation/src/executorch_export.ipynb) to create xnnpack_my_nearest_feats.pte.
    * Move `xnnpack_my_nearest_feats.pte` to `app/src/main/assets/xnnpack_my_nearest_feats.pte`
3. Obtain prebuilt executorch.aar as below:
    ```shell
    # The link is taken from official documentation: 
    # https://pytorch.org/executorch/0.5/android-prebuilt-library.html#using-prebuilt-libraries
    mkdir -p app/libs
    curl https://ossci-android.s3.amazonaws.com/executorch/release/v0.5.0-rc3/executorch.aar -o app/libs/executorch.aar
    ```
4. Build this android app with Android Studio


## Upcoming Improvements
- [ ] Most importantely - pretrained models for English and other languages
- [ ] Instruction on how to train and integrate models for new languages and new keyboard layouts 
- [ ] UI Design enhancement
- [ ] Add swipe trail visualization
- [ ] Reading tokenizers from json
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