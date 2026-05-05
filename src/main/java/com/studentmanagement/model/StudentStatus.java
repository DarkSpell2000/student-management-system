package com.studentmanagement.model;

/**
 * Статус студента в учебном процессе.
 * Хранится в БД как строка (EnumType.STRING) — устойчиво к изменению порядка значений.
 */
public enum StudentStatus {
    /** Активный студент, учится. */
    ACTIVE,

    /** Академический отпуск. */
    ACADEMIC,

    /** Отчислен. */
    EXPELLED,

    /** Окончил обучение. */
    GRADUATE
}
