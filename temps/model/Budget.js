import { EntitySchema } from 'typeorm';

// Định nghĩa model (bảng) Budget sử dụng TypeORM
// (Note: Cấu hình các cột cho bảng budgets - quản lý hạn mức chi tiêu)
export const Budget = new EntitySchema({
    name: 'Budget',
    tableName: 'budgets',
    columns: {
        id: {
            primary: true,
            type: 'int',
            generated: true,
        },
        user_id: {
            type: 'int',
            nullable: false,
        },
        category_id: {
            type: 'int',
            nullable: false,
        },
        amount_limit: {
            type: 'decimal',
            precision: 18,
            scale: 2,
            nullable: false, // Hạn mức chi tiêu tối đa
        },
        start_date: {
            type: 'timestamp',
            nullable: false,
        },
        end_date: {
            type: 'timestamp',
            nullable: false,
        },
        status: {
            type: 'enum',
            enum: ['ACTIVE', 'COMPLETED', 'CANCELLED'],
            default: 'ACTIVE',
        },
        final_spent_amount: {
            type: 'decimal',
            precision: 18,
            scale: 2,
            nullable: true, // Số tiền thực tế đã chi (chốt khi COMPLETED)
        },
        result_status: {
            type: 'enum',
            enum: ['UNDER_BUDGET', 'EXACT', 'OVER_BUDGET'],
            nullable: true, // Kết quả ngân sách (chốt khi COMPLETED)
        },
        is_alert_enabled: {
            type: 'boolean',
            default: true,
        },
        alert_threshold: {
            type: 'decimal',
            precision: 3,
            scale: 2,
            default: 0.8, // Ngưỡng thông báo (ví dụ 0.8 = 80%)
        },
        completion_date: {
            type: 'timestamp',
            nullable: true, // Ngày hoàn thành ngân sách
        },
        createdAt: {
            type: 'timestamp',
            createDate: true,
        },
        updatedAt: {
            type: 'timestamp',
            updateDate: true,
        },
    },
    relations: {
        user: {
            target: 'User',
            type: 'many-to-one',
            joinColumn: { name: 'user_id' },
            onDelete: 'CASCADE',
        },
        category: {
            target: 'Category',
            type: 'many-to-one',
            joinColumn: { name: 'category_id' },
            onDelete: 'CASCADE',
        },
    },
});
