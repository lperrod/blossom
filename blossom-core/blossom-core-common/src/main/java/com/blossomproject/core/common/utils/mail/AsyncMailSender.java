package com.blossomproject.core.common.utils.mail;

import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.CompletableFuture;

/**
 * Enable sending a BlossomMail asynchronously for a standard MailSender.
 * This is to circumvent the limitation that AOP cannot be applied inside the
 * calling service, and calling methods on BlossomMail are not Spring-managed
 *
 * @author rlejolivet
 */
public interface AsyncMailSender {

  @Async
  CompletableFuture<BlossomMail> asyncSend(BlossomMail mail);

}
