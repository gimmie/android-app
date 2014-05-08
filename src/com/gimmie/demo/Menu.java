package com.gimmie.demo;

public class Menu {

  public static final String TYPE_HEADER = "header";
  public static final String TYPE_ACTION = "action";

  private String mName;
  private String mType;
  private ListAction mAction;

  public Menu(String name, String type) {
    mName = name;
    mType = type;
  }

  public Menu(String name, String type, ListAction action) {
    mName = name;
    mType = type;
    mAction = action;
  }

  public String getName() {
    return mName;
  }

  public void setName(String name) {
    mName = name;
  }

  public String getType() {
    return mType;
  }

  public ListAction getAction() {
    return mAction;
  }

}
