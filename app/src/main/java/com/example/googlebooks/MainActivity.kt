package com.example.googlebooks

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.googlebooks.databinding.ActivityMainBinding
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import cz.msebera.android.httpclient.Header
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnSearch.setOnClickListener {
            searchBook()
        }
    }

    private fun searchBook() {
        val query = binding.bookDetails.text.toString()
        val client = AsyncHttpClient()
        val url = "https://www.googleapis.com/books/v1/volumes?q=${query}"

        client.get(url, object : AsyncHttpResponseHandler() {
            override fun onSuccess(
                statusCode: Int,
                headers: Array<out Header>,
                responseBody: ByteArray?
            ) {
                val result = responseBody?.let { String(it) }
                if (result != null) {
                    Log.d(TAG, result)
                }
                binding.progressBar.visibility = View.INVISIBLE

                try {
                    val jsonObject = result?.let { JSONObject(it) }
                    val itemArray = jsonObject?.getJSONArray("items")

                    var i = 0
                    var bookTitle = ""
                    var bookAuthor = ""

                    if (itemArray != null) {
                        while (i < itemArray.length()) {
                            val book = itemArray.getJSONObject(i)
                            val volumeInfo = book.getJSONObject("volumeInfo")
                            try {
                                bookTitle = volumeInfo.getString("title")
                                bookAuthor = volumeInfo.getString("authors")
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            i++
                        }
                    }
                    binding.apply {
                        tvTitle.text = bookTitle
                        tvAuthor.text = bookAuthor
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
                }


            }

            override fun onFailure(
                statusCode: Int,
                headers: Array<out Header>?,
                responseBody: ByteArray?,
                error: Throwable?
            ) {
                val errorMessage = when (statusCode) {
                    401 -> "StatusCode: Bad Request"
                    403 -> "StatusCode: Forbidden"
                    404 -> "StatusCode: Not Found"
                    else -> " StatusCode: ${error?.message}"
                }
                Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_SHORT).show()
            }

        })

    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}