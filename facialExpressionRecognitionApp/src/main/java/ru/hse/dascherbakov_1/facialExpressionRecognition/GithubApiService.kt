package ru.hse.dascherbakov_1.facialExpressionRecognition

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class Post(
    val title: String,
    val post_hint: String,
    val url: String,
    val name: String,
)

data class PostResponse(val data: Post)
data class PostsListing(val children: List<PostResponse>)
data class RedditResponse(val data: PostsListing)

interface RedditApi {
    @GET("hot.json")
    suspend fun getPosts(@Query("after") after: String?): Response<RedditResponse>
}

fun retrofit(): Retrofit = Retrofit.Builder()
    .baseUrl("https://reddit.com/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val redditApi: RedditApi = retrofit().create(RedditApi::class.java)