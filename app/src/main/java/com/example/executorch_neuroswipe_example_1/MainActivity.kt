package com.example.executorch_neuroswipe_example_1

import android.content.Context
import android.os.Bundle
import android.system.ErrnoException
import android.system.Os
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.executorch_neuroswipe_example_1.ngtFeaturesExtraction.getNearestKeys
import com.example.executorch_neuroswipe_example_1.ui.theme.Executorch_neuroswipe_example_1Theme
import org.pytorch.executorch.EValue
import org.pytorch.executorch.Module
import org.pytorch.executorch.Tensor
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import com.example.executorch_neuroswipe_example_1.swipeTypingDecoders.NeuralSwipeTypingDecoder
import com.example.executorch_neuroswipe_example_1.decodingAlgorithms.GreedySearch
import com.example.executorch_neuroswipe_example_1.tokenizers.RuSubwordTokenizer
import com.example.executorch_neuroswipe_example_1.ngtFeaturesExtraction.TrajFeatsGetter
import com.example.executorch_neuroswipe_example_1.ngtFeaturesExtraction.getNKL

class MainActivity : ComponentActivity() {

    companion object {
        @Throws(IOException::class)
        fun assetFilePath(context: Context, assetName: String): String {
            val file = File(context.filesDir, assetName)
            if (file.exists() && file.length() > 0) {
                return file.absolutePath
            }

            context.assets.open(assetName).use { `is` ->
                FileOutputStream(file).use { os ->
                    val buffer = ByteArray(4 * 1024)
                    var read: Int
                    while ((`is`.read(buffer).also { read = it }) != -1) {
                        os.write(buffer, 0, read)
                    }
                    os.flush()
                }
                return file.absolutePath
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // The ADSP_LIBRARY_PATH setting was copied from the official executorch tutorial.
        // I think ADSP may be needed for efficient neural network execution on edge
        // but it's just a guess.
        try {
            Os.setenv("ADSP_LIBRARY_PATH", applicationInfo.nativeLibraryDir, true)
        } catch (e: ErrnoException) {
            Log.e("ExecuTorchNeuroswipe", "Cannot set ADSP_LIBRARY_PATH", e)
            finish()
        }

        val modelFileName: String = "xnnpack_my_nearest_feats.pte"

        val encoderDecoderModule = Module.load(assetFilePath(applicationContext, modelFileName))
            ?: throw IllegalStateException("Error loading model from assets")


        val greedySearch = GreedySearch(encoderDecoderModule,
            sosToken = 36, eosToken = 33, maxSteps = 35)

        val subwordTokenizer = RuSubwordTokenizer()



        val xytTransform: (IntArray, IntArray, IntArray) -> Array<EValue> = { x, y, t ->
            val trajFeatsGetter = TrajFeatsGetter()
            val nearestKeyLookup = getNKL()
            val exampleCoordFeats = trajFeatsGetter.getFeats(x, y, t, gridName = "default")

            // Step 2: Get nearest keys and create a Tensor
            val nearestKeysArray = getNearestKeys(x, y, nearestKeyLookup)
            val exampleNearestKeys = Tensor.fromBlob(
                nearestKeysArray,
                longArrayOf(x.size.toLong(), 1) // Shape: [num_points, 1]
            )

            // Step 3: Log the results for debugging
            Log.i("MyTag", "exampleNearestKeys: $exampleNearestKeys")
            Log.i("MyTag", "exampleNearestKeys data: ${exampleNearestKeys.dataAsIntArray.contentToString()}")

            // Step 4: Return an Array<EValue> containing the transformed inputs
            arrayOf(
                EValue.from(exampleCoordFeats), // Trajectory features
                EValue.from(exampleNearestKeys) // Nearest keys tensor
            )
        }


        val neuralSwipeTypingDecoder = NeuralSwipeTypingDecoder(
            encoderDecoderModule, greedySearch, subwordTokenizer, xytTransform
        )


        val x = intArrayOf(872,858,840,798,743,713,653,624,609,599,591,587,584,585,600,644,683,720,753,786,807,827,845,860,864,867,869,869,861,824,758,740,702,665,632,611,600,593,588,587,590,596,603,609,606,600,554,482,408,347,291,242,206,173,146,126,111,104,98,92,86,80,65,57,54,50,50,48,48,48)
        val y = intArrayOf(250,250,250,245,241,241,241,242,242,242,244,244,245,254,268,284,295,302,313,323,335,350,370,389,398,403,409,412,410,386,334,319,293,274,254,242,238,235,232,233,241,248,253,257,262,263,266,260,253,253,253,257,257,257,256,251,245,241,233,224,206,179,128,91,80,58,49,28,5,5)
        val t = intArrayOf(0,94,103,118,135,152,176,191,207,224,240,256,275,289,306,322,341,356,374,390,405,422,439,458,475,488,505,521,538,555,576,587,603,621,637,653,671,685,702,720,736,753,771,785,804,818,835,850,870,883,899,917,934,950,969,983,1000,1017,1034,1051,1068,1084,1102,1119,1133,1151,1167,1181,1201,1211)


        val candidates = neuralSwipeTypingDecoder.decodeSwipe(x, y, t)

        val mostProbableCandidate = candidates[0]


        Log.i("MyTag", "candidates[0]: ${mostProbableCandidate[0]}")





        setContent {
            Executorch_neuroswipe_example_1Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = mostProbableCandidate,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Executorch_neuroswipe_example_1Theme {
        Greeting("Android")
    }
}