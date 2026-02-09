# Chat Service Database Schema

## 📊 Tổng Quan Các Tables

| # | Table | Mô tả | Records dự kiến |
|---|-------|-------|-----------------|
| 1 | **conversation** | Cuộc hội thoại (chat room) | ~10K-100K |
| 2 | **conversation_participant** | Thành viên trong hội thoại | ~50K-500K |
| 3 | **conversation_settings** | Cài đặt cho từng hội thoại | ~10K-100K |
| 4 | **message** | Tin nhắn | ~1M-100M ⚠️ |
| 5 | **message_attachment** | File đính kèm | ~100K-1M |
| 6 | **message_read_receipt** | Đã xem | ~5M-50M ⚠️ |
| 7 | **message_reaction** | Reactions (emoji) | ~500K-5M |
| 8 | **user_presence** | Trạng thái online | ~10K |
| 9 | **typing_indicator** | Đang gõ (ephemeral) | Redis only |
| 10 | **blocked_user** | Danh sách chặn | ~1K-10K |
| 11 | **message_report** | Báo cáo vi phạm | ~100-1K |

---

## 📋 Chi Tiết Từng Table

### 1️⃣ conversation
> Đại diện cho một cuộc hội thoại/phòng chat

```
┌─────────────────────────────────────────────────────────────────┐
│                        CONVERSATION                              │
├──────────────────────┬──────────────┬───────────────────────────┤
│ Field                │ Type         │ Description               │
├──────────────────────┼──────────────┼───────────────────────────┤
│ conversation_id (PK) │ UUID         │ ID duy nhất               │
│ conversation_type    │ ENUM         │ DIRECT/GROUP/ORDER/SHOP   │
│ name                 │ VARCHAR(100) │ Tên nhóm (nếu là group)   │
│ description          │ VARCHAR(500) │ Mô tả nhóm                │
│ avatar_url           │ VARCHAR(500) │ Ảnh đại diện nhóm         │
│ reference_type       │ VARCHAR(50)  │ ORDER/PRODUCT/SHOP        │
│ reference_id         │ VARCHAR(100) │ ID tham chiếu             │
│ last_message_preview │ VARCHAR(200) │ Preview tin nhắn cuối     │
│ last_message_at      │ TIMESTAMP    │ Thời gian tin nhắn cuối   │
│ last_message_sender  │ VARCHAR(100) │ Người gửi tin cuối        │
│ message_count        │ BIGINT       │ Tổng số tin nhắn          │
│ is_archived          │ BOOLEAN      │ Đã lưu trữ                │
│ is_pinned            │ BOOLEAN      │ Đã ghim                   │
│ created_by           │ VARCHAR(100) │ Người tạo                 │
│ created_at           │ TIMESTAMP    │ Ngày tạo                  │
│ updated_at           │ TIMESTAMP    │ Ngày cập nhật             │
└──────────────────────┴──────────────┴───────────────────────────┘

Indexes:
- idx_conv_type ON (conversation_type)
- idx_conv_reference ON (reference_type, reference_id)
- idx_conv_last_message ON (last_message_at DESC)
```

**Conversation Types trong SpringFood:**
- `DIRECT`: Chat 1-1 giữa Buyer và Seller
- `GROUP`: Nhóm chat nhiều người
- `ORDER_SUPPORT`: Chat liên quan đến đơn hàng cụ thể
- `SHOP_SUPPORT`: Hỗ trợ khách hàng của shop

---

### 2️⃣ conversation_participant
> Quản lý thành viên trong mỗi cuộc hội thoại

```
┌─────────────────────────────────────────────────────────────────┐
│                 CONVERSATION_PARTICIPANT                         │
├──────────────────────┬──────────────┬───────────────────────────┤
│ Field                │ Type         │ Description               │
├──────────────────────┼──────────────┼───────────────────────────┤
│ participant_id (PK)  │ UUID         │ ID duy nhất               │
│ conversation_id (FK) │ UUID         │ → conversation            │
│ user_id              │ UUID         │ ID user từ Identity Svc   │
│ display_name         │ VARCHAR(100) │ Tên hiển thị (cached)     │
│ avatar_url           │ VARCHAR(500) │ Avatar (cached)           │
│ role                 │ ENUM         │ OWNER/ADMIN/MEMBER        │
│ status               │ ENUM         │ ACTIVE/LEFT/REMOVED/MUTED │
│ nickname             │ VARCHAR(50)  │ Biệt danh trong nhóm      │
│ last_read_message_id │ UUID         │ → message đã đọc cuối     │
│ last_read_at         │ TIMESTAMP    │ Thời gian đọc cuối        │
│ unread_count         │ INTEGER      │ Số tin chưa đọc           │
│ is_muted             │ BOOLEAN      │ Tắt thông báo             │
│ mute_until           │ TIMESTAMP    │ Tắt đến khi nào           │
│ is_pinned            │ BOOLEAN      │ Ghim conversation         │
│ pinned_at            │ TIMESTAMP    │ Thời gian ghim            │
│ joined_at            │ TIMESTAMP    │ Thời gian tham gia        │
│ left_at              │ TIMESTAMP    │ Thời gian rời đi          │
│ added_by             │ VARCHAR(100) │ Ai thêm vào               │
└──────────────────────┴──────────────┴───────────────────────────┘

Indexes:
- idx_participant_conv_user ON (conversation_id, user_id) UNIQUE
- idx_participant_user ON (user_id)
- idx_participant_unread ON (user_id, unread_count) WHERE unread_count > 0
```

---

### 3️⃣ message
> Tin nhắn - Table quan trọng nhất, cần optimize cao

```
┌─────────────────────────────────────────────────────────────────┐
│                          MESSAGE                                 │
├─────────────────────────┬──────────────┬────────────────────────┤
│ Field                   │ Type         │ Description            │
├─────────────────────────┼──────────────┼────────────────────────┤
│ message_id (PK)         │ UUID         │ ID duy nhất            │
│ conversation_id (FK)    │ UUID         │ → conversation         │
│ client_message_id       │ VARCHAR(100) │ ID từ client (dedup)   │
│ sender_id               │ UUID         │ User gửi               │
│ sender_name             │ VARCHAR(100) │ Tên người gửi (cached) │
│ sender_avatar           │ VARCHAR(500) │ Avatar (cached)        │
│ message_type            │ ENUM         │ TEXT/IMAGE/VIDEO/...   │
│ content                 │ TEXT         │ Nội dung tin nhắn      │
│ content_preview         │ VARCHAR(200) │ Preview (cho search)   │
│ reply_to_message_id     │ UUID         │ Trả lời tin nhắn nào   │
│ reply_to_preview        │ VARCHAR(200) │ Preview tin gốc        │
│ forwarded_from_msg_id   │ UUID         │ Chuyển tiếp từ         │
│ forwarded_from_conv_id  │ UUID         │ Từ conversation nào    │
│ reference_type          │ VARCHAR(50)  │ PRODUCT/ORDER          │
│ reference_id            │ VARCHAR(100) │ ID sản phẩm/đơn hàng   │
│ status                  │ ENUM         │ SENT/DELIVERED/READ    │
│ is_edited               │ BOOLEAN      │ Đã sửa                 │
│ edited_at               │ TIMESTAMP    │ Thời gian sửa          │
│ is_deleted              │ BOOLEAN      │ Đã xóa                 │
│ deleted_at              │ TIMESTAMP    │ Thời gian xóa          │
│ deleted_by              │ VARCHAR(100) │ Ai xóa                 │
│ reaction_count          │ INTEGER      │ Tổng reactions         │
│ created_at              │ TIMESTAMP    │ Thời gian tạo          │
│ updated_at              │ TIMESTAMP    │ Thời gian cập nhật     │
└─────────────────────────┴──────────────┴────────────────────────┘

Indexes:
- idx_msg_conv_created ON (conversation_id, created_at DESC)  -- Quan trọng nhất!
- idx_msg_sender ON (sender_id, created_at DESC)
- idx_msg_client_id ON (client_message_id) -- Deduplication
- idx_msg_reply ON (reply_to_message_id)
```

**Message Types:**
- `TEXT`: Tin nhắn văn bản thông thường
- `IMAGE`: Ảnh
- `VIDEO`: Video
- `FILE`: Tài liệu
- `AUDIO`: Tin nhắn thoại
- `LOCATION`: Vị trí
- `STICKER`: Sticker
- `SYSTEM`: Tin hệ thống (joined/left)
- `ORDER_CARD`: Card thông tin đơn hàng
- `PRODUCT_CARD`: Card chia sẻ sản phẩm

---

### 4️⃣ message_attachment
> File đính kèm với tin nhắn

```
┌─────────────────────────────────────────────────────────────────┐
│                    MESSAGE_ATTACHMENT                            │
├──────────────────────┬──────────────┬───────────────────────────┤
│ Field                │ Type         │ Description               │
├──────────────────────┼──────────────┼───────────────────────────┤
│ attachment_id (PK)   │ UUID         │ ID duy nhất               │
│ message_id (FK)      │ UUID         │ → message                 │
│ media_id             │ UUID         │ → Media Service           │
│ attachment_type      │ ENUM         │ IMAGE/VIDEO/AUDIO/DOC     │
│ file_name            │ VARCHAR(255) │ Tên file gốc              │
│ file_size            │ BIGINT       │ Kích thước (bytes)        │
│ mime_type            │ VARCHAR(100) │ MIME type                 │
│ url                  │ VARCHAR(1000)│ URL download              │
│ thumbnail_url        │ VARCHAR(1000)│ URL thumbnail             │
│ width                │ INTEGER      │ Chiều rộng (px)           │
│ height               │ INTEGER      │ Chiều cao (px)            │
│ duration             │ INTEGER      │ Thời lượng (seconds)      │
│ display_order        │ INTEGER      │ Thứ tự hiển thị           │
│ created_at           │ TIMESTAMP    │ Thời gian tạo             │
└──────────────────────┴──────────────┴───────────────────────────┘
```

---

### 5️⃣ message_read_receipt
> Theo dõi ai đã đọc tin nhắn nào

```
┌─────────────────────────────────────────────────────────────────┐
│                   MESSAGE_READ_RECEIPT                           │
├──────────────────────┬──────────────┬───────────────────────────┤
│ Field                │ Type         │ Description               │
├──────────────────────┼──────────────┼───────────────────────────┤
│ receipt_id (PK)      │ UUID         │ ID duy nhất               │
│ message_id (FK)      │ UUID         │ → message                 │
│ user_id              │ UUID         │ Người đã đọc              │
│ read_at              │ TIMESTAMP    │ Thời gian đọc             │
│ device_type          │ VARCHAR(50)  │ web/ios/android           │
└──────────────────────┴──────────────┴───────────────────────────┘

Indexes:
- idx_receipt_msg_user ON (message_id, user_id) UNIQUE
```

---

### 6️⃣ message_reaction
> Emoji reactions trên tin nhắn

```
┌─────────────────────────────────────────────────────────────────┐
│                    MESSAGE_REACTION                              │
├──────────────────────┬──────────────┬───────────────────────────┤
│ Field                │ Type         │ Description               │
├──────────────────────┼──────────────┼───────────────────────────┤
│ reaction_id (PK)     │ UUID         │ ID duy nhất               │
│ message_id (FK)      │ UUID         │ → message                 │
│ user_id              │ UUID         │ Người reaction            │
│ emoji                │ VARCHAR(50)  │ Emoji code (thumbs_up)    │
│ emoji_display        │ VARCHAR(20)  │ Emoji display (👍)        │
│ created_at           │ TIMESTAMP    │ Thời gian                 │
└──────────────────────┴──────────────┴───────────────────────────┘

Indexes:
- idx_reaction_msg_user_emoji ON (message_id, user_id, emoji) UNIQUE
```

---

### 7️⃣ user_presence
> Trạng thái online/offline (thường lưu Redis)

```
┌─────────────────────────────────────────────────────────────────┐
│                      USER_PRESENCE                               │
├──────────────────────┬──────────────┬───────────────────────────┤
│ Field                │ Type         │ Description               │
├──────────────────────┼──────────────┼───────────────────────────┤
│ user_id (PK)         │ UUID         │ User ID                   │
│ status               │ ENUM         │ ONLINE/AWAY/BUSY/OFFLINE  │
│ status_message       │ VARCHAR(100) │ Custom status             │
│ last_seen_at         │ TIMESTAMP    │ Lần cuối online           │
│ active_conv_id       │ UUID         │ Đang xem conversation nào │
│ device_type          │ VARCHAR(50)  │ web/ios/android           │
│ device_id            │ VARCHAR(100) │ Device identifier         │
│ session_id           │ VARCHAR(100) │ WebSocket session         │
│ last_activity_at     │ TIMESTAMP    │ Hoạt động cuối            │
└──────────────────────┴──────────────┴───────────────────────────┘
```

---

### 8️⃣ blocked_user
> Danh sách chặn người dùng

```
┌─────────────────────────────────────────────────────────────────┐
│                       BLOCKED_USER                               │
├──────────────────────┬──────────────┬───────────────────────────┤
│ Field                │ Type         │ Description               │
├──────────────────────┼──────────────┼───────────────────────────┤
│ block_id (PK)        │ UUID         │ ID duy nhất               │
│ blocker_id           │ UUID         │ Người chặn                │
│ blocked_user_id      │ UUID         │ Người bị chặn             │
│ reason               │ VARCHAR(500) │ Lý do (optional)          │
│ created_at           │ TIMESTAMP    │ Thời gian chặn            │
└──────────────────────┴──────────────┴───────────────────────────┘

Indexes:
- idx_blocked_pair ON (blocker_id, blocked_user_id) UNIQUE
```

---

### 9️⃣ message_report
> Báo cáo tin nhắn vi phạm

```
┌─────────────────────────────────────────────────────────────────┐
│                      MESSAGE_REPORT                              │
├──────────────────────┬──────────────┬───────────────────────────┤
│ Field                │ Type         │ Description               │
├──────────────────────┼──────────────┼───────────────────────────┤
│ report_id (PK)       │ UUID         │ ID duy nhất               │
│ message_id           │ UUID         │ Tin nhắn bị báo cáo       │
│ reporter_id          │ UUID         │ Người báo cáo             │
│ reason               │ ENUM         │ SPAM/HARASSMENT/...       │
│ details              │ VARCHAR(1000)│ Chi tiết                  │
│ status               │ ENUM         │ PENDING/REVIEWED/RESOLVED │
│ reviewed_by          │ VARCHAR(100) │ Moderator                 │
│ reviewed_at          │ TIMESTAMP    │ Thời gian review          │
│ review_notes         │ VARCHAR(1000)│ Ghi chú                   │
│ action_taken         │ VARCHAR(500) │ Hành động đã thực hiện    │
│ created_at           │ TIMESTAMP    │ Thời gian tạo             │
│ updated_at           │ TIMESTAMP    │ Cập nhật                  │
└──────────────────────┴──────────────┴───────────────────────────┘
```

---

### 🔟 conversation_settings
> Cài đặt cho mỗi conversation

```
┌─────────────────────────────────────────────────────────────────┐
│                  CONVERSATION_SETTINGS                           │
├───────────────────────────┬──────────────┬──────────────────────┤
│ Field                     │ Type         │ Description          │
├───────────────────────────┼──────────────┼──────────────────────┤
│ settings_id (PK)          │ UUID         │ ID duy nhất          │
│ conversation_id (FK)      │ UUID         │ → conversation       │
│ only_admin_can_send       │ BOOLEAN      │ Chỉ admin gửi được   │
│ only_admin_can_add        │ BOOLEAN      │ Chỉ admin thêm người │
│ auto_delete_days          │ INTEGER      │ Tự xóa sau X ngày    │
│ allow_reactions           │ BOOLEAN      │ Cho phép reactions   │
│ allow_replies             │ BOOLEAN      │ Cho phép reply       │
│ allow_attachments         │ BOOLEAN      │ Cho phép đính kèm    │
│ max_attachment_size_mb    │ INTEGER      │ Kích thước tối đa    │
│ allowed_file_types        │ VARCHAR(500) │ Loại file cho phép   │
│ show_read_receipts        │ BOOLEAN      │ Hiện đã xem          │
│ show_typing_indicators    │ BOOLEAN      │ Hiện đang gõ         │
└───────────────────────────┴──────────────┴──────────────────────┘
```

---

## 📐 Entity Relationship Diagram

```
┌─────────────────────┐
│    conversation     │
│                     │
│  conversation_id PK │◄───────────────────────────────────────────┐
│  conversation_type  │                                            │
│  name               │         ┌──────────────────────────┐       │
│  ...                │         │ conversation_participant │       │
└─────────┬───────────┘         │                          │       │
          │                     │  participant_id PK       │       │
          │ 1                   │  conversation_id FK ─────┼───────┘
          │                     │  user_id                 │
          │         ┌───────────│  role                    │
          │         │           │  status                  │
          ▼         │           │  last_read_message_id FK─┼───┐
    ┌─────────────┐ │           │  unread_count            │   │
    │   message   │ │           └──────────────────────────┘   │
    │             │ N                                          │
    │ message_id PK◄───────────────────────────────────────────┘
    │ conversation_id FK        ┌──────────────────────────┐
    │ sender_id   │             │  message_attachment      │
    │ content     │             │                          │
    │ message_type│  1          │  attachment_id PK        │
    │ ...         │───────────N─│  message_id FK           │
    └─────┬───────┘             │  media_id                │
          │                     │  ...                     │
          │                     └──────────────────────────┘
          │ 1
          │                     ┌──────────────────────────┐
          │                     │  message_read_receipt    │
          │                     │                          │
          ├───────────────────N─│  receipt_id PK           │
          │                     │  message_id FK           │
          │                     │  user_id                 │
          │                     │  read_at                 │
          │                     └──────────────────────────┘
          │
          │                     ┌──────────────────────────┐
          │                     │  message_reaction        │
          │                     │                          │
          └───────────────────N─│  reaction_id PK          │
                                │  message_id FK           │
                                │  user_id                 │
                                │  emoji                   │
                                └──────────────────────────┘
```

---

## 🚀 Redis Keys Structure

```
# User Presence
presence:user:{userId}          → { status, lastSeen, deviceType }

# Typing Indicators  
typing:{conversationId}         → SET [ userId1, userId2, ... ]

# Unread Count Cache
unread:{userId}:{conversationId} → INTEGER

# Recent Messages Cache
messages:{conversationId}:recent → LIST [ messageId1, messageId2, ... ]

# Online Users in Conversation
online:{conversationId}          → SET [ userId1, userId2, ... ]

# User Sessions (WebSocket)
session:{userId}                 → { sessionId, deviceType, connectedAt }
```

---

## 📚 Recommended Indexes (SQL)

```sql
-- High-priority indexes
CREATE INDEX idx_msg_conv_created ON message(conversation_id, created_at DESC);
CREATE INDEX idx_participant_user ON conversation_participant(user_id);
CREATE UNIQUE INDEX idx_participant_conv_user ON conversation_participant(conversation_id, user_id);
CREATE UNIQUE INDEX idx_receipt_msg_user ON message_read_receipt(message_id, user_id);
CREATE UNIQUE INDEX idx_reaction_unique ON message_reaction(message_id, user_id, emoji);
CREATE UNIQUE INDEX idx_blocked_pair ON blocked_user(blocker_id, blocked_user_id);

-- Partial indexes for performance
CREATE INDEX idx_participant_unread ON conversation_participant(user_id, unread_count) 
    WHERE unread_count > 0;
CREATE INDEX idx_conv_active ON conversation(last_message_at DESC) 
    WHERE is_archived = FALSE;
```

---

## 📅 Data Retention Strategy

| Table | Retention | Notes |
|-------|-----------|-------|
| conversation | Forever | Soft delete only |
| message | 2 years | Archive to cold storage |
| message_read_receipt | 30 days | Can be regenerated |
| message_reaction | 1 year | Less critical |
| typing_indicator | Real-time only | Redis, ephemeral |
| user_presence | Real-time + 7 days history | |

