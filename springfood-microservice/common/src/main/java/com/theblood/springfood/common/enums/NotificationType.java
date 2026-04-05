package com.theblood.springfood.common.enums;

import lombok.Getter;

import java.util.Map;

@Getter
public enum NotificationType {
    DOCUMENT_CREATED("Bạn có văn kiện tư liệu mới",
            "Nhấn vào để xem nội dung chi tiết.",
            "/api/documents/{id}"),
    FEEDBACK_CREATED("Đơn vị bạn có đóng góp ý kiến mới",
            "Nhấn vào để xem nội dung góp ý chi tiết.",
            "/api/feedback"),
    REQUEST_ATTENDANCE_PARTICIPANT_REQUEST_ABSENT("{pmFullName} đã gửi yêu cầu báo vắng",
            "Nhấn vào để xem lý do báo vắng và thực hiện xử lý.",
            "/pages/activity-support/{Id config/report}/detail?isConfig={configs/reports}"),

    //  === ORG_ACTIVITY_CONFIG ===
    ORG_ACTIVITY_CONFIG_CREATED("Bạn có cuộc sinh hoạt mới cần đăng ký",
            "[{orgName}] đã tạo mới một định hướng sinh hoạt. Vui lòng kiểm tra chi tiết!",
            "/pages/activity-support/{configId}/detail?isConfig=configs"),
    ORG_ACTIVITY_CONFIG_UPDATED("Bạn có cuộc sinh hoạt vừa thay đổi",
            "[{orgName}] đã sửa đổi một định hướng sinh hoạt. Vui lòng kiểm tra chi tiết!",
            "/pages/activity-support/{configId}/detail?isConfig=configs"),

    //  === ORG_ACTIVITY_REPORT_DETAIL_ ===
    ORG_ACTIVITY_REPORT_DETAIL_CREATE_CONCLUSION("{orgName} đã tạo báo cáo cho {activityTitle}",
            "",
            "/api/org-activity-report-details/conclusion-org-activity"),
    ORG_ACTIVITY_REPORT_DETAIL_CREATED("{orgName} đã tạo báo cáo cho {activityTitle}",
            "",
            "/api/org-activity-report-details/report-org-activity"),

    //  === ORG_ACTIVITY_REPORT ===
    ORG_ACTIVITY_REPORT_CREATED("Bạn được mời tham gia cuộc sinh hoạt mới",
            "",
            "/pages/activity-support/{reportId}/detail?isConfig=reports"),
    ORG_ACTIVITY_REPORT_REGISTER("Bạn được mời tham gia cuộc sinh hoạt mới",
            "",
            "/pages/activity-support/{reportId}/detail?isConfig=reports"),


    //  ===  REQUEST_ATTENDANCE_PARTICIPANT ===
    REQUEST_ATTENDANCE_PARTICIPANT_REQUEST_ACCEPTED("Đơn {requestType} cho {activityTitle} của bạn đã được phê duyệt",
            "",
            "/pages/activity-support/{reportId}/detail?isConfig=reports"),

    REQUEST_ATTENDANCE_PARTICIPANT_REQUEST_REJECTED("Đơn {requestType} cho {activityTitle} của bạn đã bị từ chối",
            "",
            "/pages/activity-support/{reportId}/detail?isConfig=reports"),
    ATTENDANCE_PROXY("Chi uỷ tại {orgName} đã điểm danh hộ bạn",
            "Bạn đã được điểm danh hộ cho {activityTitle}. Nhấn vào để xem chi tiếtt",
            "/pages/activity-support/{reportId}/detail?isConfig=reports"),

    ABSENCE_PROXY(
            "Chi uỷ tại {orgName} đã báo vắng hộ bạn",
            "Bạn đã được báo vắng hộ cho {activityTitle}. Nhấn vào để xem chi tiết",
            "/pages/activity-support/{reportId}/detail?isConfig=reports"),

    PARTICIPANT_LIST_CREATE_FEEDBACK(
            "{pmCurrentFullName} đã gửi đóng góp ý kiến",
            "Đảng viên đã gửi đóng góp ý kiến cho {activityTitle}",
            "/pages/activity-support/{reportId}/detail?isConfig=reports"),

    //  ===  PARTICIPANT_LIST ===
    ORG_ACTIVITY_REPORT_REMIND(
            "Bạn được nhắc nhở tham gia Sinh hoạt Chi bộ",
            "",
            "/pages/activity-support/{reportId}/detail?isConfig=reports"),
    ;


    private final String title;
    private final String body;
    private final String actionUrl;

    NotificationType(String title, String body, String actionUrl) {
        this.title = title;
        this.actionUrl = actionUrl;
        this.body = body;
    }

    private String build(Map<String, String> params, String content) {
        String result = content;
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                result = result.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        return result;
    }

    public String buildTitle(Map<String, String> params) {
        return build(params, title);
    }

    public String buildBody(Map<String, String> params) {
        return build(params, body);
    }

    public String buildActionUrl(Map<String, String> params) {
        return build(params, actionUrl);
    }
}


