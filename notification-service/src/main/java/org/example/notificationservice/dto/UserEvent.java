package org.example.notificationservice.dto;

public class UserEvent {
    private String operation; // "CREATE" или "DELETE"
    private String email;

    // конструкторы, геттеры, сеттеры
    public UserEvent() {}

    public UserEvent(String operation, String email) {
        this.operation = operation;
        this.email = email;
    }

    public String getOperation() {
        return operation;
    }

    public String getEmail() {
        return email;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}