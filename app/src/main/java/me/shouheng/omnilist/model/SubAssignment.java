package me.shouheng.omnilist.model;


import me.shouheng.omnilist.model.enums.SubAssignmentType;
import me.shouheng.omnilist.provider.annotation.Column;
import me.shouheng.omnilist.provider.annotation.Table;
import me.shouheng.omnilist.provider.schema.SubAssignmentSchema;

/**
 * Created by wangshouheng on 2017/3/13. */
@Table(name = SubAssignmentSchema.TABLE_NAME)
public class SubAssignment extends Model {

    @Column(name = SubAssignmentSchema.PARENT_CODE)
    private long assignmentCode;

    @Column(name = SubAssignmentSchema.CONTENT)
    private String content;

    @Column(name = SubAssignmentSchema.COMPLETED)
    private boolean completed;

    @Column(name = SubAssignmentSchema.SUB_ASSIGNMENT_ORDER)
    private int subAssignmentOrder;

    @Column(name = SubAssignmentSchema.SUB_ASSIGNMENT_TYPE)
    private SubAssignmentType subAssignmentType;

    // region Android端字段，不计入数据库

    /**
     * 不计入数据库，用于记录是否在本次操作中完成了该任务 */
    private boolean completeThisTime;

    /**
     * 不计入数据库，标记记录在本次设置成了未完成的状态 */
    private boolean inCompletedThisTime;

    /**
     * 不计入数据库，用于标记记录的内容是否被更改 */
    private boolean contentChanged;

    public boolean isInCompletedThisTime() {
        return inCompletedThisTime;
    }

    public void setInCompletedThisTime(boolean inCompletedThisTime) {
        this.inCompletedThisTime = inCompletedThisTime;
    }

    public boolean isContentChanged() {
        return contentChanged;
    }

    public void setContentChanged(boolean contentChanged) {
        this.contentChanged = contentChanged;
    }

    public boolean isCompleteThisTime() {
        return completeThisTime;
    }

    public void setCompleteThisTime(boolean completeThisTime) {
        this.completeThisTime = completeThisTime;
    }

    // endregion

    public long getAssignmentCode() {
        return assignmentCode;
    }

    public void setAssignmentCode(long assignmentCode) {
        this.assignmentCode = assignmentCode;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public int getSubAssignmentOrder() {
        return subAssignmentOrder;
    }

    public void setSubAssignmentOrder(int subAssignmentOrder) {
        this.subAssignmentOrder = subAssignmentOrder;
    }

    public SubAssignmentType getSubAssignmentType() {
        return subAssignmentType;
    }

    public void setSubAssignmentType(SubAssignmentType subAssignmentType) {
        this.subAssignmentType = subAssignmentType;
    }

    @Override
    public String toString() {
        return "SubAssignment{" +
                "assignmentCode=" + assignmentCode +
                ", content='" + content + '\'' +
                ", completed=" + completed +
                ", subAssignmentOrder=" + subAssignmentOrder +
                ", subAssignmentType=" + subAssignmentType +
                ", completeThisTime=" + completeThisTime +
                ", inCompletedThisTime=" + inCompletedThisTime +
                ", contentChanged=" + contentChanged +
                ", id=" + id +
                ", code=" + code +
                ", userId=" + userId +
                ", addedTime=" + addedTime +
                ", lastModifiedTime=" + lastModifiedTime +
                ", lastSyncTime=" + lastSyncTime +
                ", status=" + status +
                '}';
    }
}
