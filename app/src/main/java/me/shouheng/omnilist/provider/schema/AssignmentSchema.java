package me.shouheng.omnilist.provider.schema;

/**
 * Created by WngShhng on 2017/12/10.*/
public interface AssignmentSchema extends BaseSchema {
    String TABLE_NAME = "gt_assignment";

    String CATEGORY_CODE = "category_code";
    String NAME = "name";
    String COMMENT = "comment";
    String TAGS = "tags";
    String START_TIME = "start_time";
    String END_TIME = "end_time";
    String COMPLETED_TIME = "completed_time";
    String PROGRESS = "progress";
    String PRIORITY = "priority";
    String ASSIGNMENT_ORDER = "assignment_order";
    String ASSIGNMENT_TYPE = "type";

    String ATTACHMENT_NUMBER = "attachments_number";
    String ALARM_NUMBER = "alarms_number";
}
