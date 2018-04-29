package me.shouheng.omnilist.provider.schema;

/**
 * Created by WngShhng on 2017/12/10.*/
public interface SubAssignmentSchema extends BaseSchema {
    String TABLE_NAME = "gt_sub_assignment";

    String PARENT_CODE = "parent_code";
    String CONTENT = "content";
    String COMPLETED = "completed";
    String SUB_ASSIGNMENT_ORDER = "sub_assignment_order";
    String SUB_ASSIGNMENT_TYPE = "sub_assignment_type";
    String PORTRAIT = "portrait";
}
