package org.akaza.openclinica.domain.datamap;

import org.akaza.openclinica.domain.DataMapDomainObject;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.user.UserAccount;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * SubjectGroupMap.
 */
@Entity
@Table(name = "subject_group_map")
@GenericGenerator(name = "id-generator", strategy = "native", parameters = {@Parameter(name = "sequence", value = "subject_group_map_subject_group_map_id_seq")})
public class SubjectGroupMap extends DataMapDomainObject {

    private static final long serialVersionUID = 1L;
    private int subjectGroupMapId;
    private UserAccount userAccount;
    private StudySubject studySubject;
    private StudyGroupClass studyGroupClass;
    private Status status;
    private StudyGroup studyGroup;
    private Date dateCreated;
    private Date dateUpdated;
    private Integer updateId;
    private String notes;

    @Id
    @Column(name = "subject_group_map_id", unique = true, nullable = false)
    public int getSubjectGroupMapId() {
        return this.subjectGroupMapId;
    }

    public void setSubjectGroupMapId(int subjectGroupMapId) {
        this.subjectGroupMapId = subjectGroupMapId;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    public UserAccount getUserAccount() {
        return this.userAccount;
    }

    public void setUserAccount(UserAccount userAccount) {
        this.userAccount = userAccount;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_subject_id")
    public StudySubject getStudySubject() {
        return this.studySubject;
    }

    public void setStudySubject(StudySubject studySubject) {
        this.studySubject = studySubject;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_group_class_id")
    public StudyGroupClass getStudyGroupClass() {
        return this.studyGroupClass;
    }

    public void setStudyGroupClass(StudyGroupClass studyGroupClass) {
        this.studyGroupClass = studyGroupClass;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_group_id")
    public StudyGroup getStudyGroup() {
        return this.studyGroup;
    }

    public void setStudyGroup(StudyGroup studyGroup) {
        this.studyGroup = studyGroup;
    }

    @Temporal(TemporalType.DATE)
    @Column(name = "date_created", length = 4)
    public Date getDateCreated() {
        return this.dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    @Temporal(TemporalType.DATE)
    @Column(name = "date_updated", length = 4)
    public Date getDateUpdated() {
        return this.dateUpdated;
    }

    public void setDateUpdated(Date dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    @Column(name = "update_id")
    public Integer getUpdateId() {
        return this.updateId;
    }

    public void setUpdateId(Integer updateId) {
        this.updateId = updateId;
    }

    @Column(name = "notes")
    public String getNotes() {
        return this.notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

}
