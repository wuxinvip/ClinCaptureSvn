package org.akaza.openclinica.domain.datamap;

import org.akaza.openclinica.domain.DataMapDomainObject;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.user.UserAccount;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;
import java.util.List;

/**
 * Subject.
 */
@Entity
@Table(name = "subject")
@GenericGenerator(name = "id-generator", strategy = "native", parameters = {@Parameter(name = "sequence", value = "subject_subject_id_seq")})
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Subject extends DataMapDomainObject {

    private int subjectId;
    private UserAccount userAccount;
    private Subject subjectByMotherId;
    private Status status;
    private Subject subjectByFatherId;
    private Date dateOfBirth;
    private Character gender;
    private String uniqueIdentifier;
    private Date dateCreated;
    private Date dateUpdated;
    private Integer updateId;
    private Boolean dobCollected;

    private List<StudySubject> studySubjects;
    private List<DnSubjectMap> dnSubjectMaps;

    @Id
    @Column(name = "subject_id", unique = true, nullable = false)
    @GeneratedValue(generator = "id-generator")

    public int getSubjectId() {
        return this.subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
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
    @JoinColumn(name = "mother_id")
    public Subject getSubjectByMotherId() {
        return this.subjectByMotherId;
    }

    public void setSubjectByMotherId(Subject subjectByMotherId) {
        this.subjectByMotherId = subjectByMotherId;
    }

    @Type(type = "status")
    @Column(name = "status_id")
    public Status getStatus() {
        if (status != null) {
            return status;
        } else
            return Status.AVAILABLE;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "father_id")
    public Subject getSubjectByFatherId() {
        return this.subjectByFatherId;
    }

    public void setSubjectByFatherId(Subject subjectByFatherId) {
        this.subjectByFatherId = subjectByFatherId;
    }

    @Temporal(TemporalType.DATE)
    @Column(name = "date_of_birth", length = 4)
    public Date getDateOfBirth() {
        return this.dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    @Column(name = "gender", length = 1)
    public Character getGender() {
        return this.gender;
    }

    public void setGender(Character gender) {
        this.gender = gender;
    }

    @Column(name = "unique_identifier")
    public String getUniqueIdentifier() {
        return this.uniqueIdentifier;
    }

    public void setUniqueIdentifier(String uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
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

    @Column(name = "dob_collected")
    public Boolean getDobCollected() {
        return this.dobCollected;
    }

    public void setDobCollected(Boolean dobCollected) {
        this.dobCollected = dobCollected;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "subject")
    public List<StudySubject> getStudySubjects() {
        return this.studySubjects;
    }

    public void setStudySubjects(List<StudySubject> studySubjects) {
        this.studySubjects = studySubjects;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "subject")
    public List<DnSubjectMap> getDnSubjectMaps() {
        return this.dnSubjectMaps;
    }

    public void setDnSubjectMaps(List<DnSubjectMap> dnSubjectMaps) {
        this.dnSubjectMaps = dnSubjectMaps;
    }

}
