package com.aptoide.uploader.apps;

import java.util.ArrayList;
import java.util.List;

class QueueManager {
  private final int queueLimit;
  private List<String> uploadMd5s;

  QueueManager(int queueLimit) {
    this.uploadMd5s = new ArrayList<>();
    this.queueLimit = queueLimit;
  }

  List<UploadDraft> applyQueue(List<UploadDraft> uploadDrafts) {
    List<UploadDraft> output = new ArrayList<>();
    for (UploadDraft uploadDraft : uploadDrafts) {
      if (uploadMd5s.size() < queueLimit) {
        uploadMd5s.add(uploadDraft.getMd5());
        output.add(uploadDraft);
      }
    }
    return output;
  }

  public void remove(UploadDraft uploadDraft) {
    uploadMd5s.remove(uploadDraft.getMd5());
  }
}
