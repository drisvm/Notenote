package com.notenote.app.ads

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.notenote.app.BuildConfig
import com.notenote.app.NotenoteApplication
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object Ads {

    @Volatile
    private var initialized = false

    suspend fun initialize(activity: Activity) {
        if (initialized) return
        runCatching { ensureConsent(activity) }
        suspendCancellableCoroutine { cont ->
            MobileAds.initialize(activity) { cont.resume(Unit) }
        }
        initialized = true
    }

    private suspend fun ensureConsent(activity: Activity) = suspendCancellableCoroutine { cont ->
        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        var resumed = false
        fun finish() {
            if (!resumed) {
                resumed = true
                cont.resume(Unit)
            }
        }
        consentInformation.requestConsentInfoUpdate(
            activity,
            ConsentRequestParameters.Builder().build(),
            {
                if (consentInformation.isConsentFormAvailable) {
                    UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { finish() }
                } else {
                    finish()
                }
            },
            { finish() },
        )
    }
}

@Composable
fun NotenoteBannerAd(modifier: Modifier = Modifier) {
    val app = LocalContext.current.applicationContext as NotenoteApplication
    val adsRemoved by app.container.settingsRepository.adsRemoved.collectAsState(initial = false)
    if (adsRemoved) return

    val context = LocalContext.current
    val adView = remember {
        AdView(context).apply {
            setAdSize(AdSize.BANNER)
            adUnitId = BuildConfig.ADMOB_BANNER_UNIT_ID
        }
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center,
    ) {
        AndroidView(factory = { adView })
    }
    DisposableEffect(adView) {
        adView.loadAd(AdRequest.Builder().build())
        onDispose { adView.destroy() }
    }
}
