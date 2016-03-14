 package org.akaza.openclinica.domain.datamap;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.akaza.openclinica.domain.DataMapDomainObject;
import org.akaza.openclinica.domain.user.UserAccount;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

/**
 * DiscrepancyNote.
 */
@Entity
@Table(name = "discrepancy_note")
@GenericGenerator(name = "id-generator", strategy = "native", parameters = {@Parameter(name = "sequence_name", value = "discrepancy_note_discrepancy_note_id_seq")})
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class DiscrepancyNote extends DataMapDomainObject {

    private static final long serialVersionUID = 1L;
    private int discrepancyNoteId;
    private UserAccount userAccountByOwnerId;
    private DiscrepancyNoteType discrepancyNoteType;
    private UserAccount userAccount;
    private Study study;
    private ResolutionStatus resolutionStatus;
    private String description;
    private String detailedNotes;
    private Date dateCreated;
    private String entityType;
    private List<DnStudyEventMap> dnStudyEventMaps;
    private List<DnEventCrfMap> dnEventCrfMaps;
    private List<DnItemDataMap> dnItemDataMaps;
    private List<DnStudySubjectMap> dnStudySubjectMaps;
    private List<DnSubjectMap> dnSubjectMaps;
    private DiscrepancyNote parentDiscrepancyNote;
    private List<DiscrepancyNote> childDiscrepancyNotes;

    @Id
    @Column(name = "discrepancy_note_id", unique = true, nullable = false)
    @GeneratedValue(generator = "id-generator")
    public int getDiscrepancyNoteId() {
        return this.discrepancyNoteId;
    }

    public void setDiscrepancyNoteId(int discrepancyNoteId) {
        this.discrepancyNoteId = discrepancyNoteId;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    public UserAccount getUserAccountByOwnerId() {
        return this.userAccountByOwnerId;
    }

    public void setUserAccountByOwnerId(UserAccount userAccountByOwnerId) {
        this.userAccountByOwnerId = userAccountByOwnerId;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discrepancy_note_type_id")
    public DiscrepancyNoteType getDiscrepancyNoteType() {
        return this.discrepancyNoteType;
    }

    public void setDiscrepancyNoteType(DiscrepancyNoteType discrepancyNoteType) {
        this.discrepancyNoteType = discrepancyNoteType;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id")
    public UserAccount getUserAccount() {
        return this.userAccount;
    }

    public void setUserAccount(
            UserAccount userAccount) {
        this.userAccount = userAccount;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id")
    public Study getStudy() {
        return this.study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolution_status_id")
    public ResolutionStatus getResolutionStatus() {
        return this.resolutionStatus;
    }

    public void setResolutionStatus(ResolutionStatus resolutionStatus) {
        this.resolutionStatus = resolutionStatus;
    }

    @Column(name = "description")
    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name = "detailed_notes", length = 1000)
    public String getDetailedNotes() {
        return this.detailedNotes;
    }

    public void setDetailedNotes(String detailedNotes) {
        this.detailedNotes = detailedNotes;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_created", length = 4)
    public Date getDateCreated() {
        return this.dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    @Column(name = "entity_type", length = 30)
    public String getEntityType() {
        return this.entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_dn_id")
    public DiscrepancyNote getParentDiscrepancyNote() {
        return this.parentDiscrepancyNote;
    }

    public void setParentDiscrepancyNote(DiscrepancyNote parentDiscrepancyNote) {
        this.parentDiscrepancyNote = parentDiscrepancyNote;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "parentDiscrepancyNote")
    public List<DiscrepancyNote> getChildDiscrepancyNotes() {
        return this.childDiscrepancyNotes;
    }

    public void setChildDiscrepancyNotes(List<DiscrepancyNote> childDiscrepancyNotes) {
        this.childDiscrepancyNotes = childDiscrepancyNotes;
    }


    @OneToMany(fetch = FetchType.LAZY, mappedBy = "discrepancyNote")
    public List<DnStudyEventMap> getDnStudyEventMaps() {
        return this.dnStudyEventMaps;
    }

    public void setDnStudyEventMaps(List<DnStudyEventMap> dnStudyEventMaps) {
        this.dnStudyEventMaps = dnStudyEventMaps;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "discrepancyNote")
    public List<DnEventCrfMap> getDnEventCrfMaps() {
        return this.dnEventCrfMaps;
    }

    public void setDnEventCrfMaps(List<DnEventCrfMap> dnEventCrfMaps) {
        this.dnEventCrfMaps = dnEventCrfMaps;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "discrepancyNote")
    public List<DnItemDataMap> getDnItemDataMaps() {
        return this.dnItemDataMaps;
    }

    public void setDnItemDataMaps(List<DnItemDataMap> dnItemDataMaps) {
        this.dnItemDataMaps = dnItemDataMaps;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "discrepancyNote")
    public List<DnStudySubjectMap> getDnStudySubjectMaps() {
        return this.dnStudySubjectMaps;
    }

    public void setDnStudySubjectMaps(List<DnStudySubjectMap> dnStudySubjectMaps) {
        this.dnStudySubjectMaps = dnStudySubjectMaps;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "discrepancyNote")
    public List<DnSubjectMap> getDnSubjectMaps() {
        return this.dnSubjectMaps;
    }

    public void setDnSubjectMaps(List<DnSubjectMap> dnSubjectMaps) {
        this.dnSubjectMaps = dnSubjectMaps;
    }
}
