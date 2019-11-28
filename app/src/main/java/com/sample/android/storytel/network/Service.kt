package com.sample.android.storytel.network

import com.sample.android.storytel.BuildConfig
import com.sample.android.storytel.domain.Comment
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import timber.log.Timber
import javax.inject.Singleton

/**
 * A retrofit service to fetch items.
 */
interface StorytelService {

    @GET("posts")
    fun getPosts(): Observable<List<NetworkPost>>

    @GET("photos")
    fun getPhotos(): Observable<List<NetworkPhoto>>

    @GET("posts/{id}/comments")
    fun getComments(@Path("id") id: Int) : Observable<List<Comment>>
}

/**
 * Build the Moshi object that Retrofit will be using, making sure to add the Kotlin adapter for
 * full Kotlin compatibility.
 */
private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private fun getLoggerInterceptor(): Interceptor {
    val logger = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger {
        Timber.d(it)
    })
    logger.level = HttpLoggingInterceptor.Level.BASIC
    return logger
}

/**
 * Main entry point for network access.
 */
@Module
class Network {

    // Configure retrofit to parse JSON and use rxJava
    @Singleton
    @Provides
    fun retrofit(): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.STORYTEL_BASE_URL)
        .client(
            OkHttpClient.Builder()
                .addInterceptor(getLoggerInterceptor())
                .build()
        )
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()

    @Singleton
    @Provides
    fun itemApi(retrofit: Retrofit):StorytelService = retrofit.create(StorytelService::class.java)
}
