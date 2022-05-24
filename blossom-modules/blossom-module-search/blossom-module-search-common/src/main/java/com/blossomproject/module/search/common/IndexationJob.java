package com.blossomproject.module.search.common;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;

/**
 * Created by Maël Gargadennnec on 09/05/2017.
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public abstract class IndexationJob implements Job {

  @Override
  public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
    getIndexationEngine().indexFull();
  }

  protected abstract IndexationEngine getIndexationEngine();
}
