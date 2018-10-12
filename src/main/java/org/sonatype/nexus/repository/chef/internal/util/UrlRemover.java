package org.sonatype.nexus.repository.chef.internal.util;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public interface UrlRemover
{
  void remove(JsonReader reader, JsonWriter writer);
}
