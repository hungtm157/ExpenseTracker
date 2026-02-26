package com.example.expensetracker.core.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment

/**
 * BaseFragment — lớp nền cho mọi Fragment trong ứng dụng.
 */
abstract class BaseFragment(@LayoutRes private val layoutResId: Int) : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(layoutResId, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        initListeners()
        initObservers()
    }

    /** Khởi tạo và ánh xạ View */
    protected open fun initViews(view: View) {}

    /** Gán sự kiện click, touch, v.v. */
    protected open fun initListeners() {}

    /** Lắng nghe dữ liệu / trạng thái từ Controller */
    protected open fun initObservers() {}
}
