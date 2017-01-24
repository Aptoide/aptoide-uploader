/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/04/2016.
 */

package pt.caixamagica.aptoide.uploader;

/**
 * Created by neuro on 04-02-2015.
 */
public abstract class PriorityRunnable implements Runnable {

  public long priority = System.currentTimeMillis();
}
