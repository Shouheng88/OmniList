package me.shouheng.omnilist.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;
import java.util.List;

import me.shouheng.omnilist.model.enums.AssignmentType;
import me.shouheng.omnilist.model.enums.Priority;
import me.shouheng.omnilist.model.enums.Status;
import me.shouheng.omnilist.provider.annotation.Column;
import me.shouheng.omnilist.provider.annotation.Table;
import me.shouheng.omnilist.provider.schema.AssignmentSchema;
import me.shouheng.omnilist.utils.TimeUtils;

/**
 * Created by wangshouheng on 2017/3/13. */
@Table(name = AssignmentSchema.TABLE_NAME)
public class Assignment extends Model implements Parcelable {

    @Column(name = AssignmentSchema.ASSIGNMENT_TYPE)
    private AssignmentType assignmentType;

    @Column(name = AssignmentSchema.CATEGORY_CODE)
    private long categoryCode;

    @Column(name = AssignmentSchema.NAME)
    private String name;

    @Column(name = AssignmentSchema.COMMENT)
    private String comment;

    @Column(name = AssignmentSchema.TAGS)
    private String tags;

    @Column(name = AssignmentSchema.START_TIME)
    private Date startTime;

    @Column(name = AssignmentSchema.END_TIME)
    private Date endTime;

    @Column(name = AssignmentSchema.COMPLETED_TIME)
    private Date completeTime;

    @Column(name = AssignmentSchema.PROGRESS)
    private int progress;

    @Column(name = AssignmentSchema.PRIORITY)
    private Priority priority;

    @Column(name = AssignmentSchema.ASSIGNMENT_ORDER)
    private int assignmentOrder;

    // region Android端字段，不计入数据库
    private boolean changed;

    /**
     * 用于记录是否在本次操作中完成了该任务 */
    private boolean completeThisTime;

    /**
     * 标记记录在本次设置成了未完成的状态 */
    private boolean inCompletedThisTime;

    /**
     * 任务所具有的附件的数量 */
    private int attachments;

    /**
     * 任务所具有的闹钟的数量 */
    private int alarms;

    /**
     * 标记当前的任务是否被选中 */
    private boolean isSelected;

    private List<SubAssignment> subAssignments;

    public List<SubAssignment> getSubAssignments() {
        return subAssignments;
    }

    public void setSubAssignments(List<SubAssignment> subAssignments) {
        this.subAssignments = subAssignments;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public int getAttachments() {
        return attachments;
    }

    public void setAttachments(int attachments) {
        this.attachments = attachments;
    }

    public int getAlarms() {
        return alarms;
    }

    public void setAlarms(int alarms) {
        this.alarms = alarms;
    }

    public boolean isCompleteThisTime() {
        return completeThisTime;
    }

    public void setCompleteThisTime(boolean completeThisTime) {
        this.completeThisTime = completeThisTime;
    }

    public boolean isInCompletedThisTime() {
        return inCompletedThisTime;
    }

    public void setInCompletedThisTime(boolean inCompletedThisTime) {
        this.inCompletedThisTime = inCompletedThisTime;
    }

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    // endregion

    public Assignment() {}

    private Assignment(Parcel in) {
        setId(in.readLong());
        setCode(in.readLong());
        setUserId(in.readLong());
        setAddedTime(new Date(in.readLong()));
        setLastModifiedTime(new Date(in.readLong()));
        setLastSyncTime(new Date(in.readLong()));
        setStatus(Status.getStatusById(in.readInt()));

        categoryCode = in.readLong();
        name = in.readString();
        comment = in.readString();
        tags = in.readString();
        startTime = new Date(in.readLong());
        endTime = new Date(in.readLong());
        completeTime = new Date(in.readLong());
        progress = in.readInt();
        priority = Priority.getTypeById(in.readInt());
        assignmentOrder = in.readInt();
        assignmentType = AssignmentType.getTypeById(in.readInt());
    }

    public static final Creator<Assignment> CREATOR = new Creator<Assignment>() {
        @Override
        public Assignment createFromParcel(Parcel in) {
            return new Assignment(in);
        }

        @Override
        public Assignment[] newArray(int size) {
            return new Assignment[size];
        }
    };

    public long getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(long categoryCode) {
        this.categoryCode = categoryCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public int getAssignmentOrder() {
        return assignmentOrder;
    }

    public void setAssignmentOrder(int assignmentOrder) {
        this.assignmentOrder = assignmentOrder;
    }

    public AssignmentType getAssignmentType() {
        return assignmentType;
    }

    public void setAssignmentType(AssignmentType assignmentType) {
        this.assignmentType = assignmentType;
    }

    public Date getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(Date completeTime) {
        this.completeTime = completeTime;
    }

    @Override
    public String toString() {
        return "Assignment{" +
                "categoryCode=" + categoryCode +
                ", name='" + name + '\'' +
                ", comment='" + comment + '\'' +
                ", tags='" + tags + '\'' +
                ", startTime=" + TimeUtils.formatDate(startTime, TimeUtils.DateFormat.YYYY_MMM_dd_E_hh_mm_a) +
                ", endTime=" + TimeUtils.formatDate(endTime, TimeUtils.DateFormat.YYYY_MMM_dd_E_hh_mm_a) +
                ", completeTime=" + TimeUtils.formatDate(completeTime, TimeUtils.DateFormat.YYYY_MMM_dd_E_hh_mm_a) +
                ", progress=" + progress +
                ", priority=" + priority +
                ", assignmentOrder=" + assignmentOrder +
                ", assignmentType=" + (assignmentType == null ? null : assignmentType.name()) +
                ", changed=" + changed +
                "} " + super.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(getId());
        dest.writeLong(getCode());
        dest.writeLong(getUserId());
        dest.writeLong(getAddedTime().getTime());
        dest.writeLong(getLastModifiedTime().getTime());
        dest.writeLong(getLastSyncTime().getTime());
        dest.writeInt(getStatus().id);

        dest.writeLong(categoryCode);
        dest.writeString(name);
        dest.writeString(comment);
        dest.writeString(tags);
        dest.writeLong(startTime.getTime());
        dest.writeLong(endTime.getTime());
        dest.writeLong(completeTime.getTime());
        dest.writeInt(progress);
        dest.writeInt(priority.id);
        dest.writeInt(assignmentOrder);
        dest.writeInt(assignmentType.id);
    }
}
