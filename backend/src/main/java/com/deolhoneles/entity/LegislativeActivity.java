package com.deolhoneles.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "legislative_activity")
public class LegislativeActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String subtitle;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String author;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(name = "vote_date", nullable = false)
    private LocalDate voteDate;

    @Column(name = "external_id", unique = true, length = 100)
    private String externalId;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "vote_round", length = 500)
    private String voteRound;

    @Column(name = "source_proposal_id", length = 100)
    private String sourceProposalId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public LocalDate getVoteDate() { return voteDate; }
    public void setVoteDate(LocalDate voteDate) { this.voteDate = voteDate; }

    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getVoteRound() { return voteRound; }
    public void setVoteRound(String voteRound) { this.voteRound = voteRound; }

    public String getSourceProposalId() { return sourceProposalId; }
    public void setSourceProposalId(String sourceProposalId) { this.sourceProposalId = sourceProposalId; }
}
