package app.product.project.model.enums;

public enum RoleEnum {
    ADMIN("ADMIN", "Quản trị viên"),
    CANDIDATE("CANDIDATE", "Ứng viên"),
    EMPLOYER("EMPLOYER", "Nhà tuyển dụng");

    private final String value;
    private final String description;

    RoleEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static RoleEnum fromValue(String value) {
        for (RoleEnum role : RoleEnum.values()) {
            if (role.value.equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid role: " + value);
    }
}

