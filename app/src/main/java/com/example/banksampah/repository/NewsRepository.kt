package com.example.banksampah.repository

import android.util.Log
import com.example.banksampah.data.NewsItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.text.SimpleDateFormat
import java.util.*

class NewsRepository {

    private val TARGET_URL = "https://www.detik.com/tag/sampah"
    private val TAG = "NewsRepository"

    sealed class NewsResult {
        data class Success(val news: List<NewsItem>) : NewsResult()
        data class Error(val message: String, val type: ErrorType) : NewsResult()
    }

    enum class ErrorType {
        NO_INTERNET,
        TIMEOUT,
        SERVER_ERROR,
        PARSING_ERROR,
        UNKNOWN
    }

    suspend fun fetchLatestNews(): NewsResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Scraping news from: $TARGET_URL")

            val doc: Document = Jsoup.connect(TARGET_URL)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .referrer("https://www.google.com")
                .timeout(15000)
                .followRedirects(true)
                .get()

            Log.d(TAG, "Page fetched successfully: ${doc.title()}")

            val newsItems = mutableListOf<NewsItem>()
            val articles = doc.select("article")
            Log.d(TAG, "Found ${articles.size} articles")

            articles.take(12).forEach { article ->
                try {
                    val linkElement = article.select("a[href]").firstOrNull()
                    val link = linkElement?.attr("abs:href") ?: ""

                    val titleElement = article.select("h3, h2").firstOrNull()
                    val title = titleElement?.text()?.trim()
                        ?: linkElement?.attr("title")?.trim()
                        ?: linkElement?.text()?.trim()
                        ?: ""

                    val imgElement = article.select("img").firstOrNull()
                    var imageUrl = ""

                    if (imgElement != null) {
                        imageUrl = imgElement.attr("data-src")

                        if (imageUrl.isEmpty() || imageUrl.contains("default-43.gif")) {
                            val srcUrl = imgElement.attr("src")
                            if (!srcUrl.contains("default-43.gif")) {
                                imageUrl = srcUrl
                            }
                        }

                        if (imageUrl.isEmpty() || imageUrl.contains("default-43.gif")) {
                            imageUrl = imgElement.attr("data-lazy-src")
                        }

                        if (imageUrl.isEmpty() || imageUrl.contains("default-43.gif")) {
                            val srcset = imgElement.attr("srcset")
                            if (srcset.isNotEmpty()) {
                                imageUrl = srcset.split(",").firstOrNull()?.trim()?.split(" ")?.firstOrNull() ?: ""
                            }
                        }

                        if (imageUrl.contains("default-43.gif")) {
                            imageUrl = ""
                        }

                        if (imageUrl.isNotEmpty() && !imageUrl.startsWith("http")) {
                            imageUrl = "https:" + imageUrl
                        }
                    }

                    if (imageUrl.isEmpty()) {
                        val divWithBg = article.select("div[style*=background-image]").firstOrNull()
                        if (divWithBg != null) {
                            val style = divWithBg.attr("style")
                            val urlMatch = Regex("""url\(['"]?([^'"()]+)['"]?\)""").find(style)
                            if (urlMatch != null) {
                                imageUrl = urlMatch.groupValues[1]
                                if (!imageUrl.startsWith("http")) {
                                    imageUrl = "https:" + imageUrl
                                }
                            }
                        }
                    }

                    val dateText = article.select("div[class*=date], span[class*=date], time").firstOrNull()?.text() ?: ""

                    if (title.isNotEmpty() && link.isNotEmpty() && !link.contains("/tag/")) {
                        newsItems.add(
                            NewsItem(
                                title = title,
                                date = formatDate(dateText),
                                imageUrl = imageUrl.ifEmpty { getPlaceholderImage() },
                                link = link
                            )
                        )
                        Log.d(TAG, "Added news: $title")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing article: ${e.message}")
                }
            }

            if (newsItems.isEmpty()) {
                Log.w(TAG, "No news parsed from page")
                NewsResult.Error(
                    "Tidak dapat memuat berita. Format halaman mungkin telah berubah.",
                    ErrorType.PARSING_ERROR
                )
            } else {
                Log.d(TAG, "Successfully scraped ${newsItems.size} news items")
                NewsResult.Success(newsItems)
            }

        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "No internet connection", e)
            NewsResult.Error(
                "Tidak ada koneksi internet. Periksa koneksi Anda dan coba lagi.",
                ErrorType.NO_INTERNET
            )
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "Connection timeout", e)
            NewsResult.Error(
                "Koneksi timeout. Periksa koneksi internet Anda.",
                ErrorType.TIMEOUT
            )
        } catch (e: org.jsoup.HttpStatusException) {
            Log.e(TAG, "HTTP error: ${e.statusCode}", e)
            NewsResult.Error(
                "Server sedang bermasalah (${e.statusCode}). Coba lagi nanti.",
                ErrorType.SERVER_ERROR
            )
        } catch (e: Exception) {
            Log.e(TAG, "Unknown error: ${e.message}", e)
            NewsResult.Error(
                "Terjadi kesalahan. Silakan coba lagi.",
                ErrorType.UNKNOWN
            )
        }
    }

    private fun getPlaceholderImage(): String {
        return "https://via.placeholder.com/280x130/4CAF50/FFFFFF?text=Berita+Sampah"
    }

    private fun formatDate(dateString: String): String {
        return try {
            when {
                dateString.isEmpty() -> getCurrentDate()
                dateString.contains(",") -> {
                    val parts = dateString.split(",")
                    if (parts.size >= 2) {
                        val datePart = parts[1].trim().split(" ")
                        if (datePart.size >= 3) {
                            val day = datePart[0]
                            val month = monthToNumber(datePart[1])
                            val year = datePart[2]
                            "$day/$month/$year"
                        } else {
                            dateString
                        }
                    } else {
                        dateString
                    }
                }
                dateString.contains("lalu", ignoreCase = true) -> dateString
                else -> dateString
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error formatting date: $dateString", e)
            getCurrentDate()
        }
    }

    private fun monthToNumber(month: String): String {
        return when (month.lowercase()) {
            "jan", "januari" -> "01"
            "feb", "februari" -> "02"
            "mar", "maret" -> "03"
            "apr", "april" -> "04"
            "mei", "may" -> "05"
            "jun", "juni" -> "06"
            "jul", "juli" -> "07"
            "agu", "agustus", "aug" -> "08"
            "sep", "september" -> "09"
            "okt", "oktober", "oct" -> "10"
            "nov", "november" -> "11"
            "des", "desember", "dec" -> "12"
            else -> "01"
        }
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))
        return sdf.format(Date())
    }
}