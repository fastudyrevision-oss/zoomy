package com.zoomy.app.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

class ConsentManager(private val context: Context) {
    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(context)

    fun requestConsentAndInit(activity: Activity, onConsentCompleted: () -> Unit) {
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    initializeMobileAdsIfAllowed()
                    onConsentCompleted()
                }
            },
            { requestConsentError ->
                initializeMobileAdsIfAllowed()
                onConsentCompleted()
            }
        )

        if (consentInformation.canRequestAds()) {
            initializeMobileAdsIfAllowed()
        }
    }

    private fun initializeMobileAdsIfAllowed() {
        if (consentInformation.canRequestAds()) {
            val reqConfig = RequestConfiguration.Builder()
                .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)
                .build()
            MobileAds.setRequestConfiguration(reqConfig)
            MobileAds.initialize(context) {}
        }
    }

    fun isPrivacyOptionsRequired(): Boolean {
        return consentInformation.privacyOptionsRequirementStatus ==
                ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
    }

    fun showPrivacyOptionsForm(activity: Activity, onDismissed: () -> Unit) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) {
            onDismissed()
        }
    }
}
