package com.example.expensetracker.features.profile

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.example.expensetracker.App
import com.example.expensetracker.R
import com.example.expensetracker.core.base.BaseActivity
import com.example.expensetracker.core.network.ApiClient
import com.example.expensetracker.core.network.ApiService
import com.example.expensetracker.data.local.AppPreferences
import com.example.expensetracker.data.models.UserProfileResponse
import com.example.expensetracker.data.repository.AuthRepository
import com.example.expensetracker.features.auth.login.LoginActivity

class ProfileActivity : BaseActivity(R.layout.activity_profile), ProfileListener {

    private lateinit var btnBack: ImageView
    private lateinit var ivAvatar: ImageView
    private lateinit var btnEditAvatar: ImageView
    private lateinit var layoutNameShow: LinearLayout
    private lateinit var tvFullName: TextView
    private lateinit var btnEditName: ImageView
    private lateinit var layoutNameEdit: LinearLayout
    private lateinit var etFullName: android.widget.EditText
    private lateinit var btnCancelName: TextView
    private lateinit var btnSaveName: TextView
    private lateinit var layoutAccountType: LinearLayout
    private lateinit var tvAccountType: TextView
    private lateinit var tvEmail: TextView
    private lateinit var btnChangePassword: LinearLayout
    private lateinit var layoutPremiumBanner: LinearLayout
    private lateinit var btnUpgradePremium: View
    private lateinit var btnLogout: LinearLayout
    private lateinit var progressBar: ProgressBar

    private lateinit var controller: ProfileController
    private lateinit var prefs: AppPreferences



    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            controller.updateAvatar(this, it)
        }
    }

    override fun initViews() {
        App.instance.preferences.authToken
        btnBack = findViewById(R.id.btnBack)
        ivAvatar = findViewById(R.id.ivAvatar)
        btnEditAvatar = findViewById(R.id.btnEditAvatar)
        layoutNameShow = findViewById(R.id.layoutNameShow)
        tvFullName = findViewById(R.id.tvFullName)
        btnEditName = findViewById(R.id.btnEditName)
        layoutNameEdit = findViewById(R.id.layoutNameEdit)
        etFullName = findViewById(R.id.etFullName)
        btnCancelName = findViewById(R.id.btnCancelName)
        btnSaveName = findViewById(R.id.btnSaveName)
        layoutAccountType = findViewById(R.id.layoutAccountType)
        tvAccountType = findViewById(R.id.tvAccountType)
        tvEmail = findViewById(R.id.tvEmail)
        btnChangePassword = findViewById(R.id.btnChangePassword)
        layoutPremiumBanner = findViewById(R.id.layoutPremiumBanner)
        btnUpgradePremium = findViewById(R.id.btnUpgradePremium)
        btnLogout = findViewById(R.id.btnLogout)
        progressBar = findViewById(R.id.progressBar)

        prefs = AppPreferences(this)
        val apiService = ApiClient.create(ApiService::class.java)
        val repository = AuthRepository(apiService)
        controller = ProfileController(repository, this)

        // Tạm ẩn các components cho đến khi tải xong
        tvFullName.text = "Đang tải..."
        tvEmail.text = "..."
        layoutPremiumBanner.visibility = View.GONE

        // Tải profile ngay khi mở màn
        controller.fetchProfile()
    }

    override fun initListeners() {
        btnBack.setOnClickListener { finish() }

        btnEditAvatar.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnEditName.setOnClickListener {
            layoutNameShow.visibility = View.GONE
            layoutNameEdit.visibility = View.VISIBLE
            etFullName.setText(tvFullName.text)
            etFullName.requestFocus()
        }

        btnCancelName.setOnClickListener {
            layoutNameEdit.visibility = View.GONE
            layoutNameShow.visibility = View.VISIBLE
        }

        btnSaveName.setOnClickListener {
            val newName = etFullName.text.toString().trim()
            if (newName.isEmpty()) {
                etFullName.error = "Tên không được để trống"
                return@setOnClickListener
            }
            controller.updateName(newName)
        }

        btnChangePassword.setOnClickListener {
            Toast.makeText(this, "Chuyển sang màn hình Đổi mật khẩu", Toast.LENGTH_SHORT).show()
        }

        btnUpgradePremium.setOnClickListener {
            Toast.makeText(this, "Chuyển sang màn hình Nâng cấp giao diện", Toast.LENGTH_SHORT).show()
        }

        btnLogout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Xác nhận đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                .setPositiveButton("Đăng xuất") { _, _ ->
                    // Clear user prefs
                    prefs.clear()
                    Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show()
                    
                    // Navigate to Login
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("Hủy", null)
                .show()
        }
    }

    override fun onLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onProfileLoaded(profile: UserProfileResponse) {
        tvFullName.text = profile.fullName
        tvEmail.text = profile.email

        // Load Avatar
        val fullIconUrl = if (!profile.avatar.isNullOrEmpty()) {
            if (profile.avatar.startsWith("https")) profile.avatar else ApiClient.BASE_URL + profile.avatar
        } else null

        Log.d("AVATAR: ","$fullIconUrl")

        Glide.with(this)
            .load(fullIconUrl)
            .placeholder(R.drawable.ic_logo_splash)
            .error(R.drawable.ic_logo_splash)
            .circleCrop()
            .into(ivAvatar)

        // Account Type Logic
        if (profile.type == "PREMIUM") {
            tvAccountType.text = "Gói Premium"
            tvAccountType.setTextColor(getColor(R.color.white))
            layoutAccountType.setBackgroundResource(R.drawable.bg_button_orange) // Reusing simple corner background if possible
            // Ẩn banner nâng cấp
            layoutPremiumBanner.visibility = View.GONE
        } else {
            // Free Tier
            tvAccountType.text = "Miễn phí"
            tvAccountType.setTextColor(getColor(R.color.text_secondary))
            // Hiện banner nâng cấp
            layoutPremiumBanner.visibility = View.VISIBLE
        }
    }

    override fun onError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        // Cân nhắc back về màn trước hoặc hiện nút retry nếu lỗi nặng
    }

    override fun onNameUpdated(profile: UserProfileResponse) {
        Toast.makeText(this, "Cập nhật tên thành công", Toast.LENGTH_SHORT).show()
        
        // Cập nhật text mới
        tvFullName.text = profile.fullName
        
        // Trở về mode View
        layoutNameEdit.visibility = View.GONE
        layoutNameShow.visibility = View.VISIBLE
    }

    override fun onNameUpdateError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onAvatarUpdated(profile: UserProfileResponse) {
        Toast.makeText(this, "Cập nhật ảnh đại diện thành công", Toast.LENGTH_SHORT).show()
        
        // Cập nhật Avatar mới ngay lập tức
        val fullIconUrl = if (!profile.avatar.isNullOrEmpty()) {
            if (profile.avatar.startsWith("https")) profile.avatar else ApiClient.BASE_URL + profile.avatar
        } else null

        Glide.with(this)
            .load(fullIconUrl)
            .placeholder(R.drawable.ic_logo_splash)
            .error(R.drawable.ic_logo_splash)
            .circleCrop()
            .into(ivAvatar)
    }

    override fun onAvatarUpdateError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
