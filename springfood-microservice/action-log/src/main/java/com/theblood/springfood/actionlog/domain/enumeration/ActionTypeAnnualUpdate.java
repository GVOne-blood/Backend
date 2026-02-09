package com.theblood.springfood.actionlog.domain.enumeration;

public enum ActionTypeAnnualUpdate {
    CREATE("Cập nhật phiếu"),
    SUBMIT("Gửi phê duyệt"),
    REJECT("Từ chối phê duyệt"),
    APPROVE("Phê duyệt phiếu"),
    REQUEST_RECALL("Thu hồi yêu cầu"),
    RECALL_APPROVE("Thu hồi phê duyệt"),
    RECALL_REJECT("Thu hồi từ chối");

    private final String description;

    ActionTypeAnnualUpdate(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
