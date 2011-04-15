package com.mediaportal.ampdroid.remote;

import org.codehaus.jackson.annotate.JsonProperty;

public class RemotePropertiesUpdate {
   private String mTag;
   private String mValue;

   @JsonProperty("Tag")
   public String getTag() {
      return mTag;
   }

   @JsonProperty("Tag")
   public void setTag(String _tag) {
      mTag = _tag;
   }

   @JsonProperty("Value")
   public String getValue() {
      return mValue;
   }

   @JsonProperty("Value")
   public void setValue(String _value) {
      mValue = _value;
   }

}
