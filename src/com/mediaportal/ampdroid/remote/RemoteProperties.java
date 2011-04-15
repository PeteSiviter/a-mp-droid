package com.mediaportal.ampdroid.remote;

import org.codehaus.jackson.annotate.JsonProperty;

public class RemoteProperties {
   private RemotePropertiesUpdate[] mProperties;

   @JsonProperty("Tags")
   public void setProperties(RemotePropertiesUpdate[] properties) {
      mProperties = properties;
   }

   @JsonProperty("Tags")
   public RemotePropertiesUpdate[] getProperties() {
      return mProperties;
   }
}
