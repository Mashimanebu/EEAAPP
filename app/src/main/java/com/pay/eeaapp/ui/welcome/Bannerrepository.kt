package com.pay.eeaapp.ui.welcome

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class BannerImage(
    val id: String,
    val imageUrl: String,
    val caption: String? = null
)

object BannerRepository {

    // Seeded with the two EEA cover images you shared. Drop the matching
    // .jpg files into app/src/main/res/drawable/ (filenames must match exactly,
    // no extension in the reference below):
    //   eea_newsletter_jan_2024.jpg
    //   eea_state_of_environment_2020.jpg
    private val seedBanners = listOf(
        BannerImage(
            id = "seed_newsletter",
            imageUrl = "drawable/eea_newsletter_jan_2024",
            caption = "Environment Matters — January 2024 Newsletter"
        ),
        BannerImage(
            id = "seed_soe_report",
            imageUrl = "drawable/eea_state_of_environment_2020",
            caption = "Review & Update of the State of Environment Report — 2020"
        )
    )

    private val _banners = MutableStateFlow(seedBanners)
    val banners: StateFlow<List<BannerImage>> = _banners.asStateFlow()

    fun addBanner(imageUrl: String, caption: String?) {
        val trimmedUrl = imageUrl.trim()
        if (trimmedUrl.isEmpty()) return
        _banners.value = _banners.value + BannerImage(
            id = UUID.randomUUID().toString(),
            imageUrl = trimmedUrl,
            caption = caption?.trim()?.ifEmpty { null }
        )
    }

    fun removeBanner(id: String) {
        _banners.value = _banners.value.filterNot { it.id == id }
    }
}
