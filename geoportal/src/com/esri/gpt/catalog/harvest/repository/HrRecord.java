/* See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * Esri Inc. licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.gpt.catalog.harvest.repository;

import com.esri.gpt.framework.adhoc.AdHocEventFactoryList;
import com.esri.gpt.framework.adhoc.AdHocEventList;
import com.esri.gpt.catalog.harvest.clients.exceptions.HRConnectionException;
import com.esri.gpt.catalog.harvest.clients.exceptions.HRInvalidProtocolException;
import com.esri.gpt.catalog.harvest.protocols.HarvestProtocolAgp2Agp;
import com.esri.gpt.catalog.harvest.protocols.HarvestProtocolAgs2Agp;
import com.esri.gpt.catalog.harvest.protocols.HarvestProtocolNone;
import com.esri.gpt.catalog.management.MmdEnums.ApprovalStatus;
import com.esri.gpt.control.webharvest.DefaultIterationContext;
import com.esri.gpt.control.webharvest.IterationContext;
import com.esri.gpt.control.webharvest.protocol.Protocol;
import com.esri.gpt.control.webharvest.protocol.ProtocolInvoker;
import com.esri.gpt.framework.request.Record;
import com.esri.gpt.framework.resource.api.Native;
import com.esri.gpt.framework.resource.api.SourceUri;
import com.esri.gpt.framework.resource.common.CommonPublishable;
import com.esri.gpt.framework.resource.common.UrlUri;
import com.esri.gpt.framework.resource.query.QueryBuilder;
import com.esri.gpt.framework.robots.RobotsTxtMode;
import com.esri.gpt.framework.robots.RobotsTxtParser;
import com.esri.gpt.framework.util.LogUtil;
import com.esri.gpt.framework.util.ResourceXml;
import com.esri.gpt.framework.util.UuidUtil;
import com.esri.gpt.framework.util.Val;
import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;

/**
 * Harvest repository record.
 * 
 * <p/>
 * Represents single harvest repository definition. Usually, objects of this
 * class are stored within <code>HrRecords</code>. Object of the class 
 * <code>HrSelectRequest</code> is being used to read a list of records from the
 * database.
 * 
 * @see HrRecords
 * @see HrSelectRequest
 */
public class HrRecord extends Record {

// class variables =============================================================
// instance variables ==========================================================
/** Repository local id. Autogenerated. */
private int _localId = 0;
/** Repository unique id. Default: empty string.*/
private String _uuid = "";
/** Owner (user) local id. Default: -1*/
private int _ownerId = -1;
/** Date of the first input. Default: current date.*/
private Date _inputDate = new Date();
/** Date of the most recent update. Default: current date. */
private Date _updateDate = new Date();
/** Name of the repository. Default: empty string. */
private String _name = "";
/** Host server url. Default: empty string.*/
private String _hostUrl = "";
/** Theme lookup switch flag. Deafult: <code>false</code>.*/
private boolean _useThemeLookup = false;
/** Harvest frequency. Default: <code>Skip</code>.*/
private HarvestFrequency _harvestFrequency = HarvestFrequency.Skip;
/** Sending email notificiation switch flag. Default: <code>false</code>.*/
private boolean _sendNotification = false;
/** Harvest protocol. Default: <code>HarvestProtocolNone</code>*/
private Protocol _harvestProtocol = new HarvestProtocolNone();
/** Last harvest date. Defaul: <code>null</code> */
private Date _lastHarvestDate;
/** Recent job status. Default: {@link RecentJobStatus#Unavailable} */
private RecentJobStatus _recentJobStatus = RecentJobStatus.Unavailable;
/** findable */
private boolean findable;
/** searchable */
private boolean searchable;
/** synchronizable */
private boolean synchronizable;
/** approval status */
private ApprovalStatus approvalStatus = ApprovalStatus.defaultValue();
/** last sync date */
private Date lastSyncDate;

private boolean searchRequiresLogin;



// constructors ================================================================
// properties ==================================================================
/**
 * Gets local id.
 * Local id is an identity number generated by database.
 * @return local id
 */
public int getLocalId() {
  return _localId;
}

/**
 * Sets local id.
 * Local id is an identity number generated by database.
 * @param localId local id
 */
public void setLocalId(int localId) {
  _localId = localId;
}

/**
 * Gets harvest repository UUID.
 * @return harvest UUID
 */
public String getUuid() {
  return _uuid;
}

/**
 * Sets harvest repository UUID.
 * 
 * <p/>
 * If the <code>uuid</code> argument is invald, UUID becomes an empty 
 * string.
 * 
 * @param uuid harvest UUID
 */
public void setUuid(String uuid) {
  _uuid = UuidUtil.isUuid(uuid) ? uuid : "";
}

/**
 * Gets owner user local id.
 * @return owner user local id
 */
public int getOwnerId() {
  return _ownerId;
}

/**
 * Sets owner user local id.
 * @param ownerId owner user local id
 */
public void setOwnerId(int ownerId) {
  _ownerId = ownerId;
}


/**
 * Gets the search requires login.
 * NOTE: custom for NGA
 * @return the search requires login
 */
public boolean getSearchRequiresLogin() {
	return searchRequiresLogin;
}

/**
 * Sets the search requires login.
 * NOTE: custom for NGA
 * @param searchRequiresLogin the new search requires login
 */
public void setSearchRequiresLogin(boolean searchRequiresLogin) {
	this.searchRequiresLogin = searchRequiresLogin;
}

/**
 * Gets input date.
 * @return input date
 */
public Date getInputDate() {
  return _inputDate;
}

/**
 * Sets input date.
 * 
 * <p/>
 * If the <code>inputDate</code> is <code>null</code> than input date is set
 * to current date.
 * 
 * @param inputDate input date.
 */
public void setInputDate(Date inputDate) {
  _inputDate = inputDate != null ? inputDate : new Date();
}

/**
 * Gets update date.
 * @return update date
 */
public Date getUpdateDate() {
  return _updateDate;
}

/**
 * Sets update date.
 * If the <code>lastHarvestDate</code> is <code>null</code> than update date is
 * set to current date.
 * @param updateDate update date
 */
public void setUpdateDate(Date updateDate) {
  _updateDate = updateDate != null ? updateDate : new Date();
}

/**
 * Gets name.
 * @return name
 */
public String getName() {
  return _name;
}

/**
 * Sets name.
 * @param name name
 */
public void setName(String name) {
  _name = Val.chkStr(name);
}

/**
 * Gets host url.
 * @return host url
 */
public String getHostUrl() {
  if (getProtocol() instanceof HarvestProtocolAgp2Agp) {
    return ((HarvestProtocolAgp2Agp)getProtocol()).getHostUrl();
  } else if (getProtocol() instanceof HarvestProtocolAgs2Agp) {
    return ((HarvestProtocolAgs2Agp)getProtocol()).getHostUrl();
  } else {
    return _hostUrl;
  }
}

/**
 * Sets host url.
 * @param hostUrl host url
 */
public void setHostUrl(String hostUrl) {
  _hostUrl = Val.chkStr(hostUrl);
}

/**
 * Gets short version of host url.
 * @return short version of host url
 */
public String getHostUrlShort() {
  String hostUrlShort = getHostUrl();
  int slashSearchStart = 0;
  int protocolEndIndex = hostUrlShort.indexOf("://");
  if (protocolEndIndex != -1) {
    slashSearchStart = protocolEndIndex + "://".length();
  }
  int slashPosition = hostUrlShort.indexOf("/", slashSearchStart);
  if (slashPosition != -1) {
    hostUrlShort = hostUrlShort.substring(0, slashPosition);
  }
  return hostUrlShort;
}

/**
 * Gets harvest protocol.
 * @return harvest protocol
 */
public Protocol getProtocol() {
  return _harvestProtocol;
}

/**
 * Sets protocol.
 * @param protocol protocol
 */
public void setProtocol(Protocol protocol) {
  this._harvestProtocol = protocol;
}

/**
 * Gets harvest frequency.
 * @return harvest frequency
 */
public HarvestFrequency getHarvestFrequency() {
  return _harvestFrequency;
}

/**
 * Sets harvest frequency.
 * @param harvestFrequency harvest frequency
 */
public void setHarvestFrequency(HarvestFrequency harvestFrequency) {
  _harvestFrequency = harvestFrequency;
}

/**
 * Gets sending notification switch flag.
 * @return sending notification switch flag
 */
public boolean getSendNotification() {
  return _sendNotification;
}

/**
 * Sets sending notification switch flag.
 * @param sendNotification sending notification switch flag
 */
public void setSendNotification(boolean sendNotification) {
  _sendNotification = sendNotification;
}

/**
 * Gets last harvest date.
 * Last harvest date might be <code>null</code> if no harvest performed on
 * the repository.
 * @return last harvest date
 */
public Date getLastHarvestDate() {
  return _lastHarvestDate;
}

/**
 * Sets last harvest date.
 * @param lastHarvestDate last harvest date
 */
public void setLastHarvestDate(Date lastHarvestDate) {
  _lastHarvestDate = lastHarvestDate;
}

/**
 * Gets next harvest date.
 * @return next harvest date or <code>null</code> harvesting not due yet
 */
public Date getNextHarvestDate() {
  Date lastHarvestDate = getLastHarvestDate();
  HarvestFrequency harvestFrequency = getHarvestFrequency();
  switch (harvestFrequency) {
    case AdHoc:
      try {
        AdHocEventList eventList = getAdHocEventList();
        if (eventList!=null) {
          return eventList.getNextHarvestDate(lastHarvestDate);
        } else {
          return null;
        }
      } catch (ParseException ex) {
        return null;
      }
    default:
      return harvestFrequency.getDueDate(lastSyncDate);
  }
}

/**
 * Gets ad-hoc event list.
 * @return ad-hoc event list
 * @throws ParseException if parsing "ad-hoc" attribute fails
 */
public AdHocEventList getAdHocEventList() throws ParseException {
  return AdHocEventFactoryList.getInstance().parse(Val.chkStr(getProtocol().getAdHoc()));
}

/**
 * Sets ad-hoc event list.
 * @param eventList event list
 */
public void setAdHocEventList(AdHocEventList eventList) {
  getProtocol().setAdHoc(eventList.getCodes());
}

/**
 * Clears ad-hoc event list.
 */
public void clearAdHocEventList() {
  getProtocol().getAttributeMap().remove("ad-hoc");
}

/**
 * Gets recent job status.
 * @return recent job status
 */
public RecentJobStatus getRecentJobStatus() {
  return _recentJobStatus;
}

/**
 * Sets recent job status.
 * @param recentJobStatus recent job status
 */
/* default */ void setRecentJobStatus(RecentJobStatus recentJobStatus) {
  _recentJobStatus = recentJobStatus;
}

/**
 * Checks if record is findable.
 * @return <code>true</code> if record is findable
 */
public boolean getFindable() {
  return findable;
}

/**
 * Sets record is findable.
 * @param findable <code>true</code> to make record is findable
 */
public void setFindable(boolean findable) {
  this.findable = findable;
}

/**
 * Checks if records is synchronizable.
 * @return <code>true</code> if records is synchronizable
 */
public boolean getSynchronizable() {
  return synchronizable;
}

/**
 * Sets records is synchronizable.
 * @param synchronizable <code>true</code> to make records is synchronizable
 */
public void setSynchronizable(boolean synchronizable) {
  this.synchronizable = synchronizable;
}

/**
 * Checks if records is searchable.
 * @return <code>true</code> if records is searchable
 */
public boolean getSearchable() {
  return searchable;
}

/**
 * Sets records is searchable.
 * @param searchable <ocde>true</code> to make records is searchable
 */
public void setSearchable(boolean searchable) {
  this.searchable = searchable;
}

/**
 * Gets approval status.
 * @return approval status
 */
public ApprovalStatus getApprovalStatus() {
  return approvalStatus;
}

/**
 * Sets approval status.
 * @param approvalStatus approval status
 */
public void setApprovalStatus(ApprovalStatus approvalStatus) {
  this.approvalStatus = approvalStatus!=null? approvalStatus: ApprovalStatus.defaultValue();
}

/**
 * Gets last synchronization date.
 * This is a date when last successful synchronization has been started
 * @return last synchronization date
 */
public Date getLastSyncDate() {
  return lastSyncDate;
}

/**
 * Sets last synchronization date.
 * This is a date when last successful synchronization has been started
 * @param lastSyncDate last synchronization date
 */
public void setLastSyncDate(Date lastSyncDate) {
  this.lastSyncDate = lastSyncDate;
}

// methods =====================================================================
/**
 * Creates string representation of the site.
 * @return string representation of the site
 */
@Override
public String toString() {
  StringBuilder sb = new StringBuilder();
    sb.append("Uuid:").append(_uuid);
    sb.append(" protocol:").append(_harvestProtocol.getKind());
    sb.append(" name:").append(_name);
    sb.append(" url:").append(_hostUrl);
    sb.append(" input date:").append(_inputDate);
    sb.append(" update date:").append(_updateDate);
    sb.append(" harvest date:").append(_lastHarvestDate != null ? _lastHarvestDate : "none");
    sb.append(" job status:").append(_recentJobStatus.toString());
  return sb.toString();
}

/**
 * Checks connection to the remote host.
 * @throws HRInvalidProtocolException when protocol attributes are invalid
 * @throws HRConnectionException if connecting remote repository failed
 * @deprecated replaced with {@link com.esri.gpt.control.webharvest.validator.ValidatorFactory}
 */
@Deprecated
public void checkConnection()
    throws HRInvalidProtocolException, HRConnectionException {
  try {
    Protocol protocol = getProtocol();
    String hostUrl = getHostUrl();
    ProtocolInvoker.ping(protocol, hostUrl);
  } catch (Exception ex) {
    if (ex instanceof HRInvalidProtocolException) {
      throw (HRInvalidProtocolException) ex;
    }
    if (ex instanceof HRConnectionException) {
      throw (HRConnectionException) ex;
    }
    throw new HRConnectionException("Protocol connection exception", ex);
  }
}

/**
 * Checks if is harvest due now.
 * @return <code>true</code> if harvest is due now
 */
public boolean getIsHarvestDue() {
  Date dueDate = getNextHarvestDate();
  return dueDate != null ? !(new Date().before(dueDate)) : false;
}

/**
 * Creates new query builder.
 * @param iterationContext iteration context (can be <code>null</code>)
 * @return query builder or <code>null</code> if no protocol
 */
public QueryBuilder newQueryBuilder(IterationContext iterationContext) {
  if (iterationContext==null) {
    iterationContext = new DefaultIterationContext(
            RobotsTxtParser.getDefaultInstance().parseRobotsTxt(
                    getRobotsTxtMode(),
                    getHostUrl()
            )
    );
  }
  return getProtocol() != null ? getProtocol().newQueryBuilder(iterationContext, getHostUrl()) : null;
}

/**
 * Gets robots.txt mode.
 * @return robots.txt mode.
 */
public RobotsTxtMode getRobotsTxtMode() {
  return ProtocolInvoker.getRobotsTxtMode(this.getProtocol());
}

// custom types ================================================================

/**
 * Harvest frequency.
 */
public enum HarvestFrequency {

/** monthly harvest. */
Monthly {

@Override
public Date getDueDate(Date lastDate) {
  if (lastDate == null) {
    return new Date();
  }
  Calendar due = Calendar.getInstance();
  due.setTime(lastDate);
  due.add(Calendar.MONTH, 1);
  return due.getTime();
}
},
/** once every two weeks harvest. */
BiWeekly {

@Override
public Date getDueDate(Date lastDate) {
  if (lastDate == null) {
    return new Date();
  }
  Calendar due = Calendar.getInstance();
  due.setTime(lastDate);
  due.add(Calendar.WEEK_OF_YEAR, 2);
  return due.getTime();
}
},
/** once a week harvest. */
Weekly {

@Override
public Date getDueDate(Date lastDate) {
  if (lastDate == null) {
    return new Date();
  }
  Calendar due = Calendar.getInstance();
  due.setTime(lastDate);
  due.add(Calendar.WEEK_OF_YEAR, 1);
  return due.getTime();
}
},
/** once a day. */
Dayly {

@Override
public Date getDueDate(Date lastDate) {
  if (lastDate == null) {
    return new Date();
  }
  Calendar due = Calendar.getInstance();
  due.setTime(lastDate);
  due.add(Calendar.DAY_OF_YEAR, 1);
  return due.getTime();
}
},
/** once a hour. */
Hourly {

@Override
public Date getDueDate(Date lastDate) {
  if (lastDate == null) {
    return new Date();
  }
  Calendar due = Calendar.getInstance();
  due.setTime(lastDate);
  due.add(Calendar.HOUR_OF_DAY, 1);
  return due.getTime();
}
},
/** one harvest only scheduled. */
Once {

@Override
public Date getDueDate(Date lastDate) {
  if (lastDate == null) {
    return new Date();
  }
  return null;
}
},
/** do not perform scheduled harvests. */
Skip,
/** do ad-hoc scheduled harvest. */
AdHoc;

/**
 * Checks value of the frequency.
 * @param frequency value of frequency to parseSingle
 * @return parsed frequency
 */
public static HarvestFrequency checkValueOf(String frequency) {
  frequency = Val.chkStr(frequency);
  for (HarvestFrequency f : values()) {
    if (f.name().equalsIgnoreCase(frequency)) {
      return f;
    }
  }
  LogUtil.getLogger().log(Level.SEVERE, "Invalid HarvestFrequency value: {0}", frequency);
  return HarvestFrequency.Skip;
}

/**
 * Gets due date.
 * @param lastDate date of the last harvest
 * @return due date of the next harvest or <code>null</code> if no next harvest 
 * required
 */
public Date getDueDate(Date lastDate) {
  return null;
}
}

/**
 * Harvest job status.
 */
public enum RecentJobStatus {

/** Unavailable; no job has been submitted yet. */
Unavailable(-1),
/** Job has just been submitted. */
Submited(0),
/** Job is running (currently being processed). */
Running(1),
/** Job has been completed. */
Completed(2),
/** job has been canceled */
Canceled(3);
/** Integer id */
private int _intId;

/**
 * Creates instance of the enum
 * @param intId integer id
 */
RecentJobStatus(int intId) {
  _intId = intId;
}

/**
 * Gets integer id.
 * @return integer id
 */
public int getIntId() {
  return _intId;
}

/**
 * Checks recent job status.
 * @param name status name.
 * @return status or <code>Unavailable</code> if unknown status.
 */
public static RecentJobStatus checkValueOf(String name) {
  name = Val.chkStr(name);
  for (RecentJobStatus s : values()) {
    if (s.name().equalsIgnoreCase(name)) {
      return s;
    }
  }
  LogUtil.getLogger().log(Level.SEVERE, "Invalid JobStatus value: {0}", name);
  return RecentJobStatus.Unavailable;
}

/**
 * Checks job integer id.
 * @param intId integer id
 * @return status or <code>Unavailable</code> if invalid integer id
 */
public static RecentJobStatus checkValueOf(int intId) {
  for (RecentJobStatus s : values()) {
    if (s.getIntId() == intId) {
      return s;
    }
  }
  LogUtil.getLogger().log(Level.SEVERE, "Invalid JobStatus integer id: {0}", intId);
  return RecentJobStatus.Unavailable;
}
}

/**
 * Generates temporary native resource.
 * @return native resource
 * @throws IllegalArgumentException if title not provided
 */
public Native generateNativeResource() throws IllegalArgumentException {
  return new NativeImpl(new ResourceXml().makeSimpleResourceXml(
    getName().length()>0? getName(): getHostUrl(), getHostUrl()
  ));
}

/**
 * Native implementation.
 */
private class NativeImpl extends CommonPublishable implements Native {
    private String content;

    public NativeImpl(String content) {
      this.content = content;
    }

    @Override
    public SourceUri getSourceUri() {
      return new UrlUri(getHostUrl());
    }

    @Override
    public String getContent() throws IOException {
      return content;
    }
}
}
