import express from 'express';
import { requireAuth } from '../middlewares/authMiddleware.js';
import compressImageMiddleware from '../middlewares/compressImageMiddleware.js';

import {
    getTransactions,
    createTransaction,
    ocrScan,
    updateTransaction,
    deleteTransaction
} from '../controllers/transactionController.js';

const router = express.Router();

/**
 * @swagger
 * tags:
 *   name: Transactions
 *   description: Quản lý giao dịch
 */

/**
 * @swagger
 * /api/v1/transactions:
 *   get:
 *     summary: Lấy danh sách giao dịch
 *     description: Trả về danh sách giao dịch của người dùng hiện tại, hỗ trợ phân trang, lọc, tìm kiếm, ngày tháng.
 *     tags: [Transactions]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: query
 *         name: page
 *         schema:
 *           type: integer
 *           default: 1
 *         description: Trang hiện tại
 *       - in: query
 *         name: limit
 *         schema:
 *           type: integer
 *           default: 20
 *         description: Số record trên mỗi trang
 *       - in: query
 *         name: sort
 *         schema:
 *           type: string
 *           default: 'date_desc'
 *         description: 'Sắp xếp: date_asc, date_desc'
 *       - in: query
 *         name: search
 *         schema:
 *           type: string
 *         description: Tìm kiếm theo ghi chú
 *       - in: query
 *         name: type
 *         schema:
 *           type: string
 *           enum: [INCOME, EXPENSE]
 *         description: Lọc theo loại INCOME/EXPENSE
 *       - in: query
 *         name: wallet_id
 *         schema:
 *           type: integer
 *         description: Lọc theo ví cụ thể
 *       - in: query
 *         name: category_id
 *         schema:
 *           type: integer
 *         description: Lọc theo danh mục cụ thể
 *       - in: query
 *         name: from_date
 *         schema:
 *           type: string
 *         description: ISO date (YYYY-MM-DD), từ ngày...
 *       - in: query
 *         name: to_date
 *         schema:
 *           type: string
 *         description: ISO date (YYYY-MM-DD), đến ngày...
 *       - in: query
 *         name: source
 *         schema:
 *           type: string
 *           enum: [MANUAL, OCR_SCAN]
 *         description: Lọc theo nguồn tạo
 *     responses:
 *       200:
 *         description: Thành công
 */
router.get('/', requireAuth, getTransactions);

/**
 * @swagger
 * /api/v1/transactions:
 *   post:
 *     summary: Tạo giao dịch mới
 *     description: Tạo một giao dịch thu/chi thủ công
 *     tags: [Transactions]
 *     security:
 *       - bearerAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         multipart/form-data:
 *           schema:
 *             type: object
 *             required:
 *               - wallet_id
 *               - category_id
 *               - amount
 *               - transaction_date
 *             properties:
 *               wallet_id:
 *                 type: integer
 *               category_id:
 *                 type: integer
 *               amount:
 *                 type: number
 *               transaction_date:
 *                 type: string
 *                 format: date
 *               note:
 *                 type: string
 *               currency:
 *                 type: string
 *                 default: 'VND'
 *               receipt_image:
 *                 type: string
 *                 format: binary
 *     responses:
 *       201:
 *         description: Tạo giao dịch thành công
 *       400:
 *         description: Lỗi dữ liệu truyền vào
 */
// Note: Cho phép upload hình ảnh hóa đơn nếu có
router.post('/', requireAuth, compressImageMiddleware.uploadReceipt, createTransaction);

/**
 * @swagger
 * /api/v1/transactions/ocr-scan:
 *   post:
 *     summary: Quét hình ảnh hóa đơn
 *     description: (Placeholder) Tải lên ảnh, dùng dịch vụ OCR giả lập quét lấy thông tin tạo giao dịch
 *     tags: [Transactions]
 *     security:
 *       - bearerAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         multipart/form-data:
 *           schema:
 *             type: object
 *             required:
 *               - wallet_id
 *               - category_id
 *               - receipt_image
 *             properties:
 *               wallet_id:
 *                 type: integer
 *               category_id:
 *                 type: integer
 *               receipt_image:
 *                 type: string
 *                 format: binary
 *     responses:
 *       201:
 *         description: Quét và tạo thành công
 */
// Note: Quét OCR bắt buộc có ảnh
router.post('/ocr-scan', requireAuth, compressImageMiddleware.uploadReceipt, ocrScan);

/**
 * @swagger
 * /api/v1/transactions/{id}:
 *   patch:
 *     summary: Cập nhật thông tin giao dịch
 *     tags: [Transactions]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: integer
 *     requestBody:
 *       content:
 *         multipart/form-data:
 *           schema:
 *             type: object
 *             properties:
 *               wallet_id:
 *                 type: integer
 *               category_id:
 *                 type: integer
 *               amount:
 *                 type: number
 *               transaction_date:
 *                 type: string
 *                 format: date
 *               note:
 *                 type: string
 *               currency:
 *                 type: string
 *               status:
 *                 type: string
 *                 enum: [ACTIVATE, DISABLED]
 *               receipt_image:
 *                 type: string
 *                 format: binary
 *     responses:
 *       200:
 *         description: Cập nhật thành công
 *       403:
 *         description: Không có quyền thao tác
 *       404:
 *         description: Không tìm thấy giao dịch
 */
router.patch('/:id', requireAuth, compressImageMiddleware.uploadReceipt, updateTransaction);

/**
 * @swagger
 * /api/v1/transactions/{id}:
 *   delete:
 *     summary: Xóa giao dịch
 *     tags: [Transactions]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: integer
 *     responses:
 *       200:
 *         description: Đã xóa an toàn
 *       403:
 *         description: Không có quyền xóa
 *       404:
 *         description: Không tìm thấy giao dịch
 */
router.delete('/:id', requireAuth, deleteTransaction);

export default router;
