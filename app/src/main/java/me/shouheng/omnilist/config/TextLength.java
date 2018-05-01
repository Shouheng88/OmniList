package me.shouheng.omnilist.config;

import android.support.annotation.IntegerRes;

import me.shouheng.omnilist.PalmApp;
import me.shouheng.omnilist.R;

/**
 * Created by wangshouheng on 2017/10/7. */
public enum TextLength {
    /* assignment */
    ASSIGNMENT_TITLE_LENGTH(R.integer.assignment_title_length),
    ASSIGNMENT_COMMENT_LENGTH(R.integer.assignment_comment_length),

    /* sub assignment */
    SUB_ASSIGNMENT_TITLE_LENGTH(R.integer.sub_assignment_title_length),
    SUB_ASSIGNMENT_CONTENT_LENGTH(R.integer.sub_assignment_content_length),

    /* category */
    CATEGORY_TITLE_LENGTH(R.integer.category_title_length),

    /* tags */
    TAG_SINGLE_LENGTH(R.integer.tag_single_length),
    TAGS_TOTAL_LENGTH(R.integer.tags_total_length),

    /* feedback */
    EMAIL_ADDRESS_LENGTH(R.integer.email_address_length),
    FEEDBACK_DETAILS_LENGTH(R.integer.feedback_details_length),

    /* security */
    SECURITY_QUESTION_LENGTH(R.integer.security_question_length),
    SECURITY_QUESTION_ANSWER_LENGTH(R.integer.security_question_answer_length),

    /* motto */
    MOTTO_LENGTH(R.integer.motto_length),

    /* timeline */
    TIMELINE_NAME_LENGTH(R.integer.timeline_name_length),

    /* attachment */
    MAX_ATTACHMENT_NUMBER(R.integer.max_attachment_number);

    @IntegerRes
    private final int length;

    TextLength(@IntegerRes int length) {
        this.length = length;
    }

    public int getLength() {
        return PalmApp.getContext().getResources().getInteger(length);
    }
}
