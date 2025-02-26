# Android inference for neural-glide-typing models

1. Use executorch_export.ipynb in neural-glide-typing project to create xnnpack_my_nearest_feats.pte 
2. Move xnnpack_my_nearest_feats.pte to app/src/main/assets/xnnpack_my_nearest_feats.pte
3. Obtain prebuilt executorch.aar as below:
    ```shell
    # The ink is taken from official documentation: 
    # https://pytorch.org/executorch/0.5/android-prebuilt-library.html#using-prebuilt-libraries
    mkdir -p app/libs
    curl https://ossci-android.s3.amazonaws.com/executorch/release/v0.5.0-rc3/executorch.aar -o app/libs/executorch.aar
    ```
4. Build this android app :)
