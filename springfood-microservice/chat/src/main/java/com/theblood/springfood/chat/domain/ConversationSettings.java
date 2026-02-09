package com.theblood.springfood.chat.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.domain.Persistable;

/**
 * ConversationSettings - Per-conversation settings
 */
@Entity
@Table(name = "conversation_settings")
@JsonIgnoreProperties(value = { "new", "id" })
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ConversationSettings extends AbstractAuditingEntity<String> implements Serializable, Persistable<String> {

    private static final long serialVersionUID = 1L;

    @Size(max = 50)
    @Id
    @UuidGenerator
    @Column(name = "settings_id", length = 50, nullable = false)
    private String settingsId;

    @Column(name = "only_admin_can_send")
    private Integer onlyAdminCanSend;

    @Column(name = "only_admin_can_add_members")
    private Integer onlyAdminCanAddMembers;

    @Column(name = "auto_delete_days")
    private Integer autoDeleteDays;

    @Column(name = "allow_reactions")
    private Integer allowReactions;

    @Column(name = "allow_replies")
    private Integer allowReplies;

    @Column(name = "allow_attachments")
    private Integer allowAttachments;

    @Column(name = "max_attachment_size_mb")
    private Integer maxAttachmentSizeMb;

    @Size(max = 500)
    @Column(name = "allowed_file_types", length = 500)
    private String allowedFileTypes;

    @Column(name = "show_read_receipts")
    private Integer showReadReceipts;

    @Column(name = "show_typing_indicators")
    private Integer showTypingIndicators;

    @org.springframework.data.annotation.Transient
    @Transient
    private boolean isPersisted;

    @JsonIgnoreProperties(value = { "settings", "participants", "messages" }, allowSetters = true)
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "settings")
    private Conversation conversation;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public String getSettingsId() {
        return this.settingsId;
    }

    public ConversationSettings settingsId(String settingsId) {
        this.setSettingsId(settingsId);
        return this;
    }

    public void setSettingsId(String settingsId) {
        this.settingsId = settingsId;
    }

    public Integer getOnlyAdminCanSend() {
        return this.onlyAdminCanSend;
    }

    public ConversationSettings onlyAdminCanSend(Integer onlyAdminCanSend) {
        this.setOnlyAdminCanSend(onlyAdminCanSend);
        return this;
    }

    public void setOnlyAdminCanSend(Integer onlyAdminCanSend) {
        this.onlyAdminCanSend = onlyAdminCanSend;
    }

    public Integer getOnlyAdminCanAddMembers() {
        return this.onlyAdminCanAddMembers;
    }

    public ConversationSettings onlyAdminCanAddMembers(Integer onlyAdminCanAddMembers) {
        this.setOnlyAdminCanAddMembers(onlyAdminCanAddMembers);
        return this;
    }

    public void setOnlyAdminCanAddMembers(Integer onlyAdminCanAddMembers) {
        this.onlyAdminCanAddMembers = onlyAdminCanAddMembers;
    }

    public Integer getAutoDeleteDays() {
        return this.autoDeleteDays;
    }

    public ConversationSettings autoDeleteDays(Integer autoDeleteDays) {
        this.setAutoDeleteDays(autoDeleteDays);
        return this;
    }

    public void setAutoDeleteDays(Integer autoDeleteDays) {
        this.autoDeleteDays = autoDeleteDays;
    }

    public Integer getAllowReactions() {
        return this.allowReactions;
    }

    public ConversationSettings allowReactions(Integer allowReactions) {
        this.setAllowReactions(allowReactions);
        return this;
    }

    public void setAllowReactions(Integer allowReactions) {
        this.allowReactions = allowReactions;
    }

    public Integer getAllowReplies() {
        return this.allowReplies;
    }

    public ConversationSettings allowReplies(Integer allowReplies) {
        this.setAllowReplies(allowReplies);
        return this;
    }

    public void setAllowReplies(Integer allowReplies) {
        this.allowReplies = allowReplies;
    }

    public Integer getAllowAttachments() {
        return this.allowAttachments;
    }

    public ConversationSettings allowAttachments(Integer allowAttachments) {
        this.setAllowAttachments(allowAttachments);
        return this;
    }

    public void setAllowAttachments(Integer allowAttachments) {
        this.allowAttachments = allowAttachments;
    }

    public Integer getMaxAttachmentSizeMb() {
        return this.maxAttachmentSizeMb;
    }

    public ConversationSettings maxAttachmentSizeMb(Integer maxAttachmentSizeMb) {
        this.setMaxAttachmentSizeMb(maxAttachmentSizeMb);
        return this;
    }

    public void setMaxAttachmentSizeMb(Integer maxAttachmentSizeMb) {
        this.maxAttachmentSizeMb = maxAttachmentSizeMb;
    }

    public String getAllowedFileTypes() {
        return this.allowedFileTypes;
    }

    public ConversationSettings allowedFileTypes(String allowedFileTypes) {
        this.setAllowedFileTypes(allowedFileTypes);
        return this;
    }

    public void setAllowedFileTypes(String allowedFileTypes) {
        this.allowedFileTypes = allowedFileTypes;
    }

    public Integer getShowReadReceipts() {
        return this.showReadReceipts;
    }

    public ConversationSettings showReadReceipts(Integer showReadReceipts) {
        this.setShowReadReceipts(showReadReceipts);
        return this;
    }

    public void setShowReadReceipts(Integer showReadReceipts) {
        this.showReadReceipts = showReadReceipts;
    }

    public Integer getShowTypingIndicators() {
        return this.showTypingIndicators;
    }

    public ConversationSettings showTypingIndicators(Integer showTypingIndicators) {
        this.setShowTypingIndicators(showTypingIndicators);
        return this;
    }

    public void setShowTypingIndicators(Integer showTypingIndicators) {
        this.showTypingIndicators = showTypingIndicators;
    }

    @PostLoad
    @PostPersist
    public void updateEntityState() {
        this.setIsPersisted();
    }

    @Override
    public String getId() {
        return this.settingsId;
    }

    @Override
    public void setId(String id) {
        this.settingsId = id;
    }

    @org.springframework.data.annotation.Transient
    @Transient
    @Override
    public boolean isNew() {
        return !this.isPersisted;
    }

    public ConversationSettings setIsPersisted() {
        this.isPersisted = true;
        return this;
    }

    public Conversation getConversation() {
        return this.conversation;
    }

    public void setConversation(Conversation conversation) {
        if (this.conversation != null) {
            this.conversation.setSettings(null);
        }
        if (conversation != null) {
            conversation.setSettings(this);
        }
        this.conversation = conversation;
    }

    public ConversationSettings conversation(Conversation conversation) {
        this.setConversation(conversation);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConversationSettings)) {
            return false;
        }
        return getSettingsId() != null && getSettingsId().equals(((ConversationSettings) o).getSettingsId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ConversationSettings{" +
            "settingsId=" + getSettingsId() +
            ", onlyAdminCanSend=" + getOnlyAdminCanSend() +
            ", onlyAdminCanAddMembers=" + getOnlyAdminCanAddMembers() +
            ", autoDeleteDays=" + getAutoDeleteDays() +
            ", allowReactions=" + getAllowReactions() +
            ", allowReplies=" + getAllowReplies() +
            ", allowAttachments=" + getAllowAttachments() +
            ", maxAttachmentSizeMb=" + getMaxAttachmentSizeMb() +
            ", allowedFileTypes='" + getAllowedFileTypes() + "'" +
            ", showReadReceipts=" + getShowReadReceipts() +
            ", showTypingIndicators=" + getShowTypingIndicators() +
            "}";
    }
}
