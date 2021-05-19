package ru.hse.dascherbakov_1.facialExpressionRecognition

import android.graphics.Typeface
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.lang.String.format
import kotlin.math.min
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    data class Result(val data: FloatArray, val name: String, val title: String)
    private var index = 0
    private var posts: List<Post>? = null
    private lateinit var imageView: ImageView
    private lateinit var textView: TextView
    private lateinit var viewModel: FacialExpressionViewModel
    private lateinit var button: Button
    private val data = HashMap<String, Result>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imageView = findViewById(R.id.imageView)
        textView = findViewById(R.id.textView)
        button = findViewById(R.id.button)
        viewModel = ViewModelProvider(this).get(
            FacialExpressionViewModel::class.java
        )
        imageView.setOnClickListener { nextPost() }
        button.setOnClickListener { popup() }
        nextPost()
    }

    private fun nextPost() {
        imageView.setImageBitmap(null)
        if (posts?.size ?: 0 > 0 && viewModel.output.value != null) {
            data[posts?.get(index)?.name!!] = Result(
                data = viewModel.output.value!!,
                name = viewModel.label.value!!,
                title = posts?.get(index)?.title!!
            )
        }
        ++index;
        if (posts?.size ?: 0 <= index) {
            val after = if (posts?.size ?: 0 > 0) posts!!.last().name else null;
            this.launch { fetchPosts(after) }
        } else {
            textView.text = posts?.get(index)?.title
            Picasso.get().load(posts?.get(index)?.url).into(imageView)
        }
    }

    private suspend fun fetchPosts(after: String? = null) = coroutineScope {
        index = 0
        posts = redditApi.getPosts(after).body()?.data?.children?.map { it.data }?.filter { it.post_hint == "image" }
        nextPost()
    }

    private fun popup() {
        val scrollView = ScrollView(this)
        val textView = TextView(this)
        textView.typeface = Typeface.MONOSPACE
        val sb = StringBuilder()
        data.forEach {
            sb.append(it.key)
            sb.append(" ")
            sb.append(it.value.name)
            sb.append(" ")
            sb.append(it.value.title.substring(0, min(15, it.value.title.length)))
            sb.append(" ")
            sb.append("\nAng Dis Fea Hap Sad Sur Neu")
            val params = (it.value.data.map { (it * 100).roundToInt() }).toTypedArray<Int>()
            sb.append(format("\n%3d %3d %3d %3d %3d %3d %3d", *params))
            sb.append("\n")
        }
        textView.text = sb.toString()
        scrollView.addView(textView)
        val alert = AlertDialog.Builder(this)
        alert.setTitle("Results")
        alert.setView(scrollView)
        alert.create().show()
    }
}