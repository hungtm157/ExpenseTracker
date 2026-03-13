import express from 'express';
import { requireAuth } from '../middlewares/authMiddleware.js';
import {
    getWallets,
    createWallet,
    updateWallet,
    deleteWallet
} from '../controllers/walletController.js';

const router = express.Router();

/**
 * @swagger
 * tags:
 *   name: Wallets
 *   description: API Quản lý Ví Tiền Của Người Dùng
 */

/**
 * @swagger
 * /api/v1/wallets:
 *   get:
 *     summary: Lấy danh sách ví hiện tại
 *     description: Lọc dữ liệu ví theo params tìm kiếm và phân trang
 *     tags: [Wallets]
 *     security:
 *       - BearerAuth: []
 *     parameters:
 *       - in: query
 *         name: page
 *         schema:
 *           type: integer
 *           default: 1
 *         description: Số thứ tự trang
 *       - in: query
 *         name: limit
 *         schema:
 *           type: integer
 *           default: 20
 *         description: Số lượng kết quả
 *       - in: query
 *         name: sort
 *         schema:
 *           type: string
 *           enum: [name_asc, name_desc]
 *         description: Sắp xếp theo tên
 *       - in: query
 *         name: search
 *         schema:
 *           type: string
 *         description: Tìm kiếm tên ví
 *       - in: query
 *         name: type
 *         schema:
 *           type: string
 *           enum: [CASH, BANK_ACCOUNT, E_WALLET]
 *         description: Loại hệ thống ví (Tiền mặt, ngân hàng..)
 *     responses:
 *       200:
 *         description: Thành công
 *         content:
 *           application/json:
 *             example:
 *               status: 200
 *               message: "Success"
 *               data:
 *                 items: []
 *                 total: 50
 *                 page: 1
 *                 limit: 20
 *                 totalPages: 3
 *       401:
 *         description: Không có quyền truy cập
 */
router.get('/', requireAuth, getWallets);

/**
 * @swagger
 * /api/v1/wallets:
 *   post:
 *     summary: Tạo 1 ví tính toán mới
 *     tags: [Wallets]
 *     security:
 *       - BearerAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required:
 *               - name
 *               - type
 *             properties:
 *               name:
 *                 type: string
 *                 example: "Ví Chi Tiêu Vietcombank"
 *               type:
 *                 type: string
 *                 enum: [CASH, BANK_ACCOUNT, E_WALLET]
 *                 example: BANK_ACCOUNT
 *               balance:
 *                 type: number
 *                 example: 5000000
 *               currency:
 *                 type: string
 *                 example: VND
 *     responses:
 *       201:
 *         description: Bản ghi ví được tạo mới xong
 *         content:
 *           application/json:
 *             example:
 *               id: 1
 *               name: "Ví Chi Tiêu Vietcombank"
 *               balance: 5000000
 *               currency: VND
 *               status: ACTIVATE
 *       400:
 *         description: Vướng lỗi định dạng hoặc trùng lặp name
 *         content:
 *           application/json:
 *             example:
 *               name: Tên ví đã tồn tại
 */
router.post('/', requireAuth, createWallet);

/**
 * @swagger
 * /api/v1/wallets/{id}:
 *   patch:
 *     summary: Sửa thông tin ví
 *     tags: [Wallets]
 *     security:
 *       - BearerAuth: []
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: integer
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               name:
 *                 type: string
 *               type:
 *                 type: string
 *                 enum: [CASH, BANK_ACCOUNT, E_WALLET]
 *               balance:
 *                 type: number
 *               currency:
 *                 type: string
 *               status:
 *                 type: string
 *                 enum: [ACTIVATE, DISABLED]
 *     responses:
 *       200:
 *         description: Đổi số liệu ví thành công
 *       400:
 *         description: Tên ví thay đổi đè trùng lặp mới
 *       403:
 *         description: Bạn không cấu hình được ví của người khác
 *       404:
 *         description: ID sai
 */
router.patch('/:id', requireAuth, updateWallet);

/**
 * @swagger
 * /api/v1/wallets/{id}:
 *   delete:
 *     summary: Khử xoá bỏ ví tiền vĩnh viễn
 *     tags: [Wallets]
 *     security:
 *       - BearerAuth: []
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: integer
 *     responses:
 *       200:
 *         description: Xóa dữ liệu sạch
 *         content:
 *           application/json:
 *             example:
 *               message: Xóa ví thành công
 *       403:
 *         description: Lỗi khác chủ nhân
 *       404:
 *         description: Không tìm ra ID ví phù hợp
 */
// (Note: Cấu trúc DELETE HTTP để xóa vĩnh viễn từ CSDL theo ID params)
router.delete('/:id', requireAuth, deleteWallet);

export default router;
