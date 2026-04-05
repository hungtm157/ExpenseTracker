package com.example.expensetracker.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * ImageUtils — Cung cấp các tiện ích xử lý ảnh (nén, xoay, thay đổi kích thước).
 */
object ImageUtils {
    private const val TAG = "ImageUtils"
    private const val MAX_WIDTH = 1080
    private const val MAX_HEIGHT = 1920
    private const val COMPRESSION_QUALITY = 80 // 0-100

    /**
     * Nén file ảnh để giảm dung lượng trước khi gửi lên Server.
     * @param context Context để truy cập cache directory
     * @param originalFile File ảnh gốc
     * @return File ảnh đã được nén (lưu trong cache)
     */
    fun compressImage(context: Context, originalFile: File): File {
        val originalSize = originalFile.length()
        Log.d(TAG, "compressImage: Kích thước gốc = ${originalSize / 1024} KB")

        try {
            // 1. Decode với sample size để giảm độ phân giải ban đầu (nếu ảnh quá to)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(originalFile.absolutePath, options)

            options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT)
            options.inJustDecodeBounds = false

            val bitmap = BitmapFactory.decodeFile(originalFile.absolutePath, options)
                ?: return originalFile // Trả về file gốc nếu không decode được

            // 2. Xử lý xoay ảnh dựa trên EXIF (nếu có)
            val rotatedBitmap = rotateImageIfRequired(bitmap, originalFile.absolutePath)

            // 3. Lưu vào file tạm trong cache
            val compressedFile = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
            val out = FileOutputStream(compressedFile)
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, out)
            out.flush()
            out.close()

            // Giải phóng bộ nhớ bitmap
            if (rotatedBitmap != bitmap) bitmap.recycle()
            rotatedBitmap.recycle()

            Log.d(TAG, "compressImage: Kích thước sau nén = ${compressedFile.length() / 1024} KB")
            return compressedFile

        } catch (e: Exception) {
            Log.e(TAG, "compressImage: Lỗi khi nén ảnh", e)
            return originalFile
        }
    }

    /**
     * Tính toán tỷ lệ lấy mẫu (inSampleSize) để giảm độ phân giải ảnh.
     */
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    /**
     * Xoay ảnh nếu thông tin EXIF cho biết ảnh bị nghiêng (thường do camera điện thoại).
     */
    private fun rotateImageIfRequired(img: Bitmap, path: String): Bitmap {
        val ei = ExifInterface(path)
        val orientation: Int = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(img, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img, 270f)
            else -> img
        }
    }

    private fun rotateImage(img: Bitmap, degree: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree)
        val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
        img.recycle()
        return rotatedImg
    }
}
