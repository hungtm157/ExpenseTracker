package com.example.expensetracker.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.expensetracker.data.local.AppPreferences
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object AdManager {

    private const val TAG = "AdManager"
    
    // Test Ad Unit ID for Interstitial Ads
    private const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    
    private var interstitialAd: InterstitialAd? = null
    private var isAdLoading = false

    /**
     * Khởi tạo MobileAds và bắt đầu tải quảng cáo.
     * Nên được gọi ở Application hoặc Activity đầu tiên (VD: Splash/MainActivity).
     */
    fun init(context: Context) {
        MobileAds.initialize(context) { initializationStatus ->
            Log.d(TAG, "AdMob initialized: $initializationStatus")
            loadInterstitialAd(context)
        }
    }

    /**
     * Tải trước Interstitial Ad để sẵn sàng hiển thị.
     */
    fun loadInterstitialAd(context: Context) {
        // Tránh tải nhiều lần cùng lúc
        if (isAdLoading || interstitialAd != null) {
            return
        }

        isAdLoading = true
        val adRequest = AdRequest.Builder().build()
        
        InterstitialAd.load(
            context,
            INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e(TAG, "Ad failed to load: ${adError.message}")
                    interstitialAd = null
                    isAdLoading = false
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Ad was loaded successfully.")
                    interstitialAd = ad
                    isAdLoading = false
                }
            }
        )
    }

    /**
     * Hiển thị quảng cáo nếu người dùng không phải PREMIUM.
     * @param activity: Activity hiện tại để show quảng cáo
     * @param onAdDismissed: Callback được gọi khi quảng cáo đóng lại HOẶC bỏ qua quảng cáo (bởi PREMIUM hoặc lỗi).
     */
    fun showInterstitialAd(activity: Activity, onAdDismissed: () -> Unit) {
        val prefs = AppPreferences(activity)
        
        // Nếu là PREMIUM, bỏ qua quảng cáo ngay lập tức
        if (prefs.isPremium) {
            Log.d(TAG, "User is PREMIUM. Skipping Ad.")
            onAdDismissed()
            return
        }

        if (interstitialAd != null) {
            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Ad was dismissed.")
                    // Đóng quảng cáo xong thì xóa instance cũ và tải quảng cáo mới
                    interstitialAd = null
                    loadInterstitialAd(activity)
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "Ad failed to show: ${adError.message}")
                    interstitialAd = null
                    // Bị lỗi thì cho đi tiếp luôn
                    onAdDismissed()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Ad showed fullscreen content.")
                    // Đã show, tránh show lại cái cũ (mặc dù AdMob tự hủy)
                    interstitialAd = null
                }
            }
            interstitialAd?.show(activity)
        } else {
            Log.d(TAG, "The interstitial ad wasn't ready yet.")
            // Quảng cáo chưa tải xong thì cho đi tiếp để không gây kẹt màn hình
            onAdDismissed()
            
            // Và thử tải lại
            loadInterstitialAd(activity)
        }
    }
}
