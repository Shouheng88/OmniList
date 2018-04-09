package me.shouheng.omnilist.listener;


import me.shouheng.omnilist.model.Attachment;

public interface OnAttachingFileListener {

    void onAttachingFileErrorOccurred(Attachment attachment);

    void onAttachingFileFinished(Attachment attachment);
}
