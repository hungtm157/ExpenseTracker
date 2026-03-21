package com.example.expensetracker.features.intro

import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import com.example.expensetracker.App
import com.example.expensetracker.R
import com.example.expensetracker.core.base.BaseActivity
import com.example.expensetracker.features.auth.login.LoginActivity

/**
 * IntroSliderActivity — màn hình Intro Slider 4 trang.
 * Hiển thị lần đầu khi người dùng mở app. Sau khi hoàn tất → LoginActivity.
 */
class IntroSliderActivity : BaseActivity(R.layout.activity_intro_slider) {

    private lateinit var viewPager: ViewPager2
    private lateinit var dotsContainer: LinearLayout
    private lateinit var btnNext: Button
    private lateinit var tvSkip: TextView

    private lateinit var adapter: IntroPagerAdapter
    private val slides = buildSlides()

    // Màu active dot cho từng trang
    private val dotColors = intArrayOf(
        Color.parseColor("#26C6A2"),
        Color.parseColor("#42B8F5"),
        Color.parseColor("#F06292"),
        Color.parseColor("#FFB74D")
    )

    // Drawable button cho từng trang
    private val buttonBgRes = intArrayOf(
        R.drawable.bg_intro_button_green,
        R.drawable.bg_intro_button_blue,
        R.drawable.bg_intro_button_purple,
        R.drawable.bg_intro_button_orange
    )

    override fun initViews() {
        // Nếu đã xem intro rồi → bỏ qua
        if (App.instance.preferences.hasSeenIntro) {
            goToLogin()
            return
        }

        viewPager = findViewById(R.id.viewPager)
        dotsContainer = findViewById(R.id.dotsContainer)
        btnNext = findViewById(R.id.btnNext)
        tvSkip = findViewById(R.id.tvSkip)

        adapter = IntroPagerAdapter(slides)
        viewPager.adapter = adapter

        // post{} để đảm bảo dotsContainer đã hoàn thành layout
        // trước khi add views vào — tránh dots không hiện ở trang đầu
        dotsContainer.post {
            setupDots(0)
            updateButton(0)
        }
    }

    override fun initListeners() {
        if (isFinishing) return

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                setupDots(position)
                updateButton(position)
            }
        })

        btnNext.setOnClickListener {
            val current = viewPager.currentItem
            if (current < slides.size - 1) {
                viewPager.currentItem = current + 1
            } else {
                finishIntro()
            }
        }

        tvSkip.setOnClickListener {
            finishIntro()
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private fun setupDots(currentPage: Int) {
        dotsContainer.removeAllViews()
        val density = resources.displayMetrics.density

        for (i in slides.indices) {
            val dot = View(this)
            val params: LinearLayout.LayoutParams

            if (i == currentPage) {
                // Active dot: pill shape (wide)
                params = LinearLayout.LayoutParams(
                    (28 * density).toInt(),
                    (8 * density).toInt()
                )
                dot.setBackgroundResource(R.drawable.bg_intro_dot_active)
                dot.background?.setColorFilter(
                    dotColors[currentPage],
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            } else {
                // Inactive dot: small circle
                params = LinearLayout.LayoutParams(
                    (8 * density).toInt(),
                    (8 * density).toInt()
                )
                dot.setBackgroundResource(R.drawable.bg_intro_dot_inactive)
            }

            params.setMargins(
                (4 * density).toInt(), 0,
                (4 * density).toInt(), 0
            )
            dot.layoutParams = params
            dotsContainer.addView(dot)
        }
    }

    private fun updateButton(position: Int) {
        btnNext.background = getDrawable(buttonBgRes[position])
        val isLast = position == slides.size - 1
        btnNext.text = if (isLast) "Bắt đầu ngay" else "Tiếp theo  ›"
        // Ẩn Bỏ qua ở trang cuối
        tvSkip.visibility = if (isLast) View.INVISIBLE else View.VISIBLE
    }

    private fun finishIntro() {
        App.instance.preferences.hasSeenIntro = true
        goToLogin()
    }

    private fun goToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    // ─── Data ─────────────────────────────────────────────────────────────────

    private fun buildSlides(): List<SlideModel> = listOf(
        SlideModel(
            iconRes = R.drawable.ic_intro_wallet,
            iconBgRes = R.drawable.bg_intro_icon_green,
            buttonBgRes = R.drawable.bg_intro_button_green,
            dotColor = Color.parseColor("#26C6A2"),
            title = "Chào mừng đến Monety",
            description = "Ứng dụng quản lý chi tiêu cá nhân thông minh, giúp bạn kiểm soát tài chính một cách dễ dàng"
        ),
        SlideModel(
            iconRes = R.drawable.ic_intro_chart,
            iconBgRes = R.drawable.bg_intro_icon_blue,
            buttonBgRes = R.drawable.bg_intro_button_blue,
            dotColor = Color.parseColor("#42B8F5"),
            title = "Ghi nhận thu chi dễ dàng",
            description = "Thêm giao dịch nhanh chóng, phân loại theo danh mục và theo dõi dòng tiền của bạn"
        ),
        SlideModel(
            iconRes = R.drawable.ic_intro_target,
            iconBgRes = R.drawable.bg_intro_icon_purple,
            buttonBgRes = R.drawable.bg_intro_button_purple,
            dotColor = Color.parseColor("#F06292"),
            title = "Theo dõi ngân sách thông minh",
            description = "Đặt giới hạn chi tiêu, nhận cảnh báo khi vượt ngưỡng và kiểm soát tài chính tốt hơn"
        ),
        SlideModel(
            iconRes = R.drawable.ic_intro_piggy,
            iconBgRes = R.drawable.bg_intro_icon_orange,
            buttonBgRes = R.drawable.bg_intro_button_orange,
            dotColor = Color.parseColor("#FFB74D"),
            title = "Kế hoạch tiết kiệm hiệu quả",
            description = "Tạo mục tiêu tiết kiệm, theo dõi tiến độ và đạt được ước mơ tài chính của bạn",
            isLastSlide = true
        )
    )
}
