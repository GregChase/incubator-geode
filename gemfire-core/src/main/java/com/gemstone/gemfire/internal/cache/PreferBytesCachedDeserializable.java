/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gemstone.gemfire.internal.cache;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.gemstone.gemfire.DataSerializer;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.internal.DataSerializableFixedID;
import com.gemstone.gemfire.internal.Version;
import com.gemstone.gemfire.internal.i18n.LocalizedStrings;
import com.gemstone.gemfire.internal.lang.StringUtils;

/**
 * This cache deserializable always keeps its byte[] in serialized form.
 * You can ask it for its Object in which case it always has to deserialize.
 * So it "prefers serialization (aka bytes)".
 *
 * @author Darrel
 * @since 5.0.2
 *
 */
public final class PreferBytesCachedDeserializable implements CachedDeserializable, DataSerializableFixedID {
  
  
  /**
   * empty constructor for serialization only
   */
  public PreferBytesCachedDeserializable() {
  }
  
  
  /** The cached value */
  private byte[] value;
  
  /**
   * +PER_OBJECT_OVERHEAD for VMCachedDeserializable object
   * +4 for value field
   */
  static final int MEM_OVERHEAD = PER_OBJECT_OVERHEAD + 4;

  /** 
   * Creates a new instance of <code>PreferBytesCachedDeserializable</code>.
   *
   * Note that, in general, instances of this class should be obtained
   * via {@link CachedDeserializableFactory}.
   */
  PreferBytesCachedDeserializable(byte[] serializedValue) {
    this.value = serializedValue;
    if (serializedValue == null)
      throw new NullPointerException(LocalizedStrings.PreferBytesCachedDeserializable_VALUE_MUST_NOT_BE_NULL.toLocalizedString());
  }

  public PreferBytesCachedDeserializable(Object object) {
    this.value = EntryEventImpl.serialize(object);
  }

  public Object getDeserializedValue(Region r, RegionEntry re) {
    return EntryEventImpl.deserialize(this.value);
  }
  
  public Object getDeserializedForReading() {
    return getDeserializedValue(null, null);
  }
  public Object getDeserializedWritableCopy(Region r, RegionEntry re) {
    return getDeserializedValue(r, re);
  }

  /**
   * Return the serialized value as a byte[]
   */
  public byte[] getSerializedValue() {
    return this.value;
  }
  
  public void fillSerializedValue(BytesAndBitsForCompactor wrapper, byte userBits) {
    wrapper
      .setData(this.value, userBits, this.value.length, 
               false /* Not Reusable as it refers to underlying value */);
  }


  /**
   * Return current value regardless of whether it is serialized or
   * deserialized: if it was serialized than it is a byte[], otherwise it is not
   * a byte[].
   */
  public Object getValue() {
    return this.value;
  }

  public int getSizeInBytes() {
    return MEM_OVERHEAD + CachedDeserializableFactory.getByteSize(this.value);
  }
  
  public int getValueSizeInBytes() {
    return CachedDeserializableFactory.getByteSize(this.value);
  }

  public int getDSFID() {
    return PREFER_BYTES_CACHED_DESERIALIZABLE;
  }

  public void fromData(DataInput in) throws IOException, ClassNotFoundException {
    this.value = DataSerializer.readByteArray(in);
  }

  public void toData(DataOutput out) throws IOException {
    DataSerializer.writeByteArray(this.value, out);
  }
  
  String getShortClassName() {
    String cname = getClass().getName();
    return cname.substring(getClass().getPackage().getName().length()+1);
  }

  @Override
  public String toString() {
    return getShortClassName()+"@"+this.hashCode();
  }

  public void writeValueAsByteArray(DataOutput out) throws IOException {
    toData(out);
  }

  public String getStringForm() {
    try {
      return StringUtils.forceToString(getDeserializedForReading());
    } catch (RuntimeException ex) {
      return "Could not convert object to string because " + ex;
    }
  }

  @Override
  public Version[] getSerializationVersions() {
    // TODO Auto-generated method stub
    return null;
  }

}
