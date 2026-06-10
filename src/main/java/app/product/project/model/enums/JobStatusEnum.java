package app.product.project.model.enums;

public enum JobStatusEnum {
    DRAFT("DRAFT", "Bản nháp"),
    PENDING_APPROVAL("PENDING_APPROVAL", "Đang chờ phê duyệt"),
    APPROVED("APPROVED", "Đã được phê duyệt"),
    REJECTED("REJECTED", "Từ chối"),
    CLOSED("CLOSED", "Đã đóng");

    private final String value;
    private final String description;

    JobStatusEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static JobStatusEnum fromValue(String value) {
        for (JobStatusEnum status : JobStatusEnum.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid job status: " + value);
    }
}

