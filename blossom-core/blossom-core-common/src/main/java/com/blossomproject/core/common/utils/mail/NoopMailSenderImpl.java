package com.blossomproject.core.common.utils.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class NoopMailSenderImpl extends DeprecatedMailSenderImpl implements MailSender {
  private final static Logger LOGGER = LoggerFactory.getLogger(NoopMailSenderImpl.class);

  @Override
  public BlossomMailBuilder builder() {
    return new BlossomMailBuilderImpl() {

      @Override
      public BlossomMail build() {
        return new BlossomMail() {

          @Override
          public void send() {
            LOGGER.warn("A mail with recipient(s) '{To: '{}', CC: '{}', BCC: '{}'}' and subject '{}' was not sent because no java mail sender is configured", to, cc, bcc, mailSubject);
          }

          @Override
          public CompletableFuture<BlossomMail> asyncSend() {
            return CompletableFuture.completedFuture(this);
          }
        };
      }
    };
  }

}
