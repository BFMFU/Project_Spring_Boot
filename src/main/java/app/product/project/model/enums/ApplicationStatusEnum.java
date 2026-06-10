package app.product.project.model.enums;

public enum ApplicationStatusEnum {
    PENDING("PENDING", "Ứng viên nộp hồ sơ"),
    REVIEWING("REVIEWING", "Nhà tuyển dụng mở xem CV"),
    INTERVIEWING("INTERVIEWING", "Đặt yêu cầu sơ loại - Hẹn phỏng vấn"),
    ACCEPTED("ACCEPTED", "Trúng tuyển - Gửi Offer"),
    REJECTED("REJECTED", "Không phù hợp / Trượt phỏng vấn / Từ chối lời đề nghị");

    private final String value;
    private final String description;

    ApplicationStatusEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static ApplicationStatusEnum fromValue(String value) {
        for (ApplicationStatusEnum status : ApplicationStatusEnum.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid application status: " + value);
    }
}

