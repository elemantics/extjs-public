/*
 * Ext GWT - Ext for GWT
 * Copyright(c) 2007, 2008, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */
package com.extjs.gxt.ui.client.event;

/**
 * Base class for all GXT events.
 * 
 * <p>
 * Note: For a given event, only the fields which are appropriate will be filled
 * in. The appropriate fields for each event are documented by the event source.
 * </p>
 */
public class BaseEvent {

  /**
   * The event type.
   */
  public int type;

  /**
   * The source object.
   */
  public Object source;

  /**
   * Depending on the event, a flag indicating whether the operation should be
   * allowed.
   */
  public boolean doit = true;
  
  public BaseEvent() {
    
  }
  
  public BaseEvent(Object source) {
    this.source = source;
  }

}
