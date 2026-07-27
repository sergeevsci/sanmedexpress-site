package ru.sanmedexpress.domain;

public enum OrderStatus {
    NEW("Новая"),
    IN_PROGRESS("В работе"),
    DONE("Выполнена"),
    CANCELLED("Отменена");

    private final String title;

    OrderStatus(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
