package com.blossomproject.core.common.utils.mail;

import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.CompletableFuture;

public class AsyncMailSenderImpl implements AsyncMailSender {

  @Override
  @Async
  public CompletableFuture<BlossomMail> asyncSend(BlossomMail mail) {
    try {
      mail.send();
    } catch (Exception e) {
      return CompletableFuture.failedFuture(e);
    }
    return CompletableFuture.completedFuture(mail);
  }

}
