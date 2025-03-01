package com.example.executorch_neuroswipe_example_1

//import android.content.Context
import android.os.Bundle
//import android.system.ErrnoException
//import android.system.Os
//import android.util.Log
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
import com.example.executorch_neuroswipe_example_1.ui.theme.Executorch_neuroswipe_example_1Theme
//import org.pytorch.executorch.EValue
//import org.pytorch.executorch.Module
//import org.pytorch.executorch.Tensor
//import java.io.File
//import java.io.FileOutputStream
//import java.io.IOException
//import com.example.executorch_neuroswipe_example_1.swipeTypingDecoders.NeuralSwipeTypingDecoder
//import com.example.executorch_neuroswipe_example_1.decodingAlgorithms.GreedySearch
//import com.example.executorch_neuroswipe_example_1.tokenizers.RuSubwordTokenizer
//import com.example.executorch_neuroswipe_example_1.ngtFeaturesExtraction.TrajFeatsGetter


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Executorch_neuroswipe_example_1Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
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