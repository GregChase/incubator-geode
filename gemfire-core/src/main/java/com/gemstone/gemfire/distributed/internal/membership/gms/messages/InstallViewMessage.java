package com.gemstone.gemfire.distributed.internal.membership.gms.messages;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.gemstone.gemfire.DataSerializer;
import com.gemstone.gemfire.distributed.internal.DistributionManager;
import com.gemstone.gemfire.distributed.internal.HighPriorityDistributionMessage;
import com.gemstone.gemfire.distributed.internal.membership.NetView;
import com.gemstone.gemfire.internal.InternalDataSerializer;

public class InstallViewMessage extends HighPriorityDistributionMessage {

  private NetView view;
  private Object credentials;
  private boolean preparing;
  private List<Integer> portsForMembers = Collections.<Integer>emptyList();

  public InstallViewMessage(NetView view, Object credentials) {
    this.view = view;
    this.preparing = false;
    this.credentials = credentials;
  }

  public InstallViewMessage(NetView view, Object credentials, boolean preparing) {
    this.view = view;
    this.preparing = preparing;
    this.credentials = credentials;
  }
  
  public InstallViewMessage() {
    // no-arg constructor for serialization
  }

  public NetView getView() {
    return view;
  }

  public Object getCredentials() {
    return credentials;
  }

  public boolean isPreparing() {
    return preparing;
  }

  @Override
  public int getDSFID() {
    return INSTALL_VIEW_MESSAGE;
  }

  @Override
  protected void process(DistributionManager dm) {
    throw new IllegalStateException("this message is not intended to execute in a thread pool");
  }

  private void writeListOfInteger(List<Integer> list, DataOutput out) throws IOException {
    int size;
    if (list == null) {
      size = -1;
    } else {
      size = list.size();
    }
    InternalDataSerializer.writeArrayLength(size, out);
    if (size > 0) {
      for (int i = 0; i < size; i++) {
        out.writeInt(list.get(i).intValue());
      }
    }
  }
  
  private List<Integer> readListOfInteger(DataInput in) throws IOException {
    int size = InternalDataSerializer.readArrayLength(in);
    if (size > 0) {
      List<Integer> list = new ArrayList<Integer>(size);
      for (int i = 0; i < size; i++) {
        list.add(Integer.valueOf(in.readInt()));
      }
      return list;
    }
    else if (size == 0) {
      return Collections.<Integer>emptyList();
    }
    else {
      return null;
    }
  }
  
  @Override
  public void toData(DataOutput out) throws IOException {
    super.toData(out);
    DataSerializer.writeObject(this.view, out);
    DataSerializer.writeObject(this.credentials, out);
    out.writeBoolean(preparing);
    writeListOfInteger(portsForMembers, out);
  }

  @Override
  public void fromData(DataInput in) throws IOException, ClassNotFoundException {
    super.fromData(in);
    this.view = DataSerializer.readObject(in);
    this.credentials = DataSerializer.readObject(in);
    this.preparing = in.readBoolean();
    this.portsForMembers = readListOfInteger(in);
  }

  @Override
  public String toString() {
    return "InstallViewMessage(preparing="+this.preparing+"; "+this.view
            +"; cred="+(credentials==null?"null": "not null")
            + "portsForMembers: " + portsForMembers
             +")";
  }

  public List<Integer> getPortsForMembers() {
    return this.portsForMembers;
  }

  public void setPortsForMembers(List<Integer> portsForMembers) {
    this.portsForMembers = portsForMembers;
  }
}