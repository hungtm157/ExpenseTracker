import express from 'express';
import { createBudget, getBudgets, completeBudget } from '../controllers/budgetController.js';
import { requireAuth } from '../middlewares/authMiddleware.js';

const router = express.Router();

/**
 * @swagger
 * tags:
 *   name: Budgets
 *   description: Quản lý ngân sách chi tiêu
 */

/**
 * @swagger
 * /api/v1/budgets:
 *   post:
 *     summary: Tạo ngân sách mới
 *     tags: [Budgets]
 *     security:
 *       - BearerAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               category_id:
 *                 type: integer
 *               amount_limit:
 *                 type: number
 *               start_date:
 *                 type: string
 *                 format: date-time
 *               end_date:
 *                 type: string
 *                 format: date-time
 *               is_alert_enabled:
 *                 type: boolean
 *               alert_threshold:
 *                 type: number
 *     responses:
 *       201:
 *         description: Created
 */
router.post('/', requireAuth, createBudget);

/**
 * @swagger
 * /api/v1/budgets:
 *   get:
 *     summary: Lấy danh sách ngân sách của người dùng
 *     tags: [Budgets]
 *     security:
 *       - BearerAuth: []
 *     responses:
 *       200:
 *         description: Success
 */
router.get('/', requireAuth, getBudgets);

/**
 * @swagger
 * /api/v1/budgets/{id}/complete:
 *   patch:
 *     summary: Hoàn thành và chốt ngân sách
 *     tags: [Budgets]
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
 *         description: Success
 */
router.patch('/:id/complete', requireAuth, completeBudget);

export default router;
