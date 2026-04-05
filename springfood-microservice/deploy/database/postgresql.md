# PostgreSQL Database Deployment - Neon

## Thông tin chung

**Platform:** Neon (Serverless PostgreSQL)  
**Project Name:** springfood  
**Project ID:** fragrant-shape-92803421  
**Branch:** production (Default)  
**Branch ID:** br-flat-pine-a184tt66  
**Region:** AWS Asia Pacific 1 (Singapore)

## Dashboard & Links

- **Console Dashboard:** https://console.neon.tech/app/projects/fragrant-shape-92803421
- **Branch Dashboard:** https://console.neon.tech/app/projects/fragrant-shape-92803421?branchId=br-flat-pine-a184tt66

## Thông tin Server

### Database Configuration
- **Database Name:** neondb
- **Owner:** neondb_owner
- **Host:** ep-green-dream-a1nsq7ax-pooler.ap-southeast-1.aws.neon.tech
- **Port:** 5432 (default PostgreSQL)
- **SSL Mode:** require
- **Channel Binding:** require

### Connection Details

#### JDBC Connection String (Spring Boot)
```
jdbc:postgresql://ep-green-dream-a1nsq7ax-pooler.ap-southeast-1.aws.neon.tech/neondb?sslmode=require
```

#### Native PostgreSQL Connection String
```
postgresql://neondb_owner:npg_gpK2ckWJPG4v@ep-green-dream-a1nsq7ax-pooler.ap-southeast-1.aws.neon.tech/neondb?sslmode=require&channel_binding=require
```

#### Environment Variables (.env)
```properties
DEFAULT_DATABASE_URL=jdbc:postgresql://ep-green-dream-a1nsq7ax-pooler.ap-southeast-1.aws.neon.tech/neondb?sslmode=require
DEFAULT_DATABASE_USERNAME=neondb_owner
DEFAULT_DATABASE_PASSWORD=npg_gpK2ckWJPG4v
```

## Resource Limits & Quotas

### Current Usage (as of March 15, 2026)

#### Branches
- **Used:** 1 / 10
- **Note:** Usage since Mar 15, 2025. Metrics may be delayed by an hour and are not updated for inactive projects.

#### Compute
- **Used:** 0 / 100 CU-hrs
- **CU (Compute Unit):** Đơn vị đo lường tài nguyên compute

#### Storage
- **Used:** 0 / 0.5 GB
- **Limit:** 512 MB (Free tier)

#### Network Transfer
- **Used:** 0 / 5 GB
- **Limit:** 5 GB (Free tier)

### Monitoring Metrics
- **Endpoint Status:** INACTIVE (hiện tại chưa có traffic)
- **Allocated CU:** Chưa có dữ liệu
- **RAM Usage:** Chưa có dữ liệu

## Thời gian hiệu lực

### Free Tier Limitations
- **Project Created:** March 15, 2026
- **Tier:** Free
- **Upgrade Required:** Khi vượt quá limits hoặc cần features nâng cao

### Auto-suspend
- Database sẽ tự động suspend sau một khoảng thời gian không hoạt động (để tiết kiệm resources)
- Tự động wake up khi có connection mới

## Branches

### Production Branch (Default)
- **Name:** production
- **Branch ID:** br-flat-pine-a184tt66
- **Status:** Active
- **Compute:** Primary - Active
- **Created by:** [User account]
- **Created at:** March 15, 2026

### Preview Workflow
- Hiện tại chưa có preview branches
- Có thể cài đặt integration để tự động tạo preview branches cho development reviews

## Security & Access

### SSL/TLS
- **Required:** Yes
- **Mode:** require
- **Certificate Verification:** Enabled

### Authentication
- **Method:** Password-based
- **Username:** neondb_owner
- **Password:** npg_gpK2ckWJPG4v (stored in .env)

### Network
- **Connection Pooling:** Enabled (via pooler endpoint)
- **Public Access:** Yes (với SSL required)
- **IP Whitelist:** Not configured (Free tier)

## Backup & Restore

### Automatic Backups
- Neon tự động backup theo point-in-time recovery
- Free tier: 7 days retention
- Có thể restore về bất kỳ thời điểm nào trong 7 ngày qua

### Manual Backup
- Sử dụng Neon Console: Backup & Restore section
- Export data qua SQL Editor hoặc pg_dump

## Integration & Tools

### Available Integrations
1. **Connection string** - Đã cấu hình
2. **Neon init** - CLI tool để setup local dev environment
3. **IDE extension** - VS Code và Cursor support
4. **MCP server** - Model Context Protocol integration

### Recommended Tools
- **SQL Editor:** Built-in trong Neon Console
- **Data Masking:** Available (BETA)
- **Auth:** Có thể tích hợp với Neon Auth
- **Data API:** REST API access to database

## Migration & Schema Management

### Current Setup
- **Tool:** Liquibase (configured in Spring Boot services)
- **Changelog Location:** `src/main/resources/config/liquibase/`
- **Auto-update:** Enabled on application startup

### Schema Versioning
- Mỗi service quản lý schema riêng của mình
- Liquibase changesets được version control trong Git

## Monitoring & Alerts

### Available Metrics
- Branch usage
- Compute usage (CU-hrs)
- Storage usage
- Network transfer
- Query performance (via SQL Editor)

### Alerts
- Có thể setup alerts khi gần đạt limits
- Email notifications cho critical events

## Cost & Billing

### Current Plan: Free Tier
- **Cost:** $0/month
- **Limits:**
  - 10 branches
  - 100 CU-hrs compute
  - 0.5 GB storage
  - 5 GB network transfer

### Upgrade Path
- **Launch:** $19/month - Increased limits
- **Scale:** $69/month - Production workloads
- **Business:** Custom pricing - Enterprise features

## Troubleshooting

### Common Issues

#### 1. Connection Timeout
- Kiểm tra SSL mode trong connection string
- Verify network connectivity
- Check if database is suspended (auto-wake có thể mất vài giây)

#### 2. Authentication Failed
- Verify username/password trong .env
- Ensure connection string format đúng (jdbc: prefix cho Spring Boot)

#### 3. SSL Certificate Error
- Ensure `sslmode=require` trong connection string
- Update JDBC driver nếu cần

### Support
- **Documentation:** https://neon.tech/docs
- **Community:** Discord, GitHub Discussions
- **Support Tickets:** Available in Console (paid plans)

## Next Steps

1. ✅ Database đã được deploy và configured
2. ⏳ Setup monitoring và alerts
3. ⏳ Configure backup strategy
4. ⏳ Review và optimize database schema
5. ⏳ Setup staging/preview branches cho development
6. ⏳ Consider upgrade plan khi production traffic tăng

---

**Last Updated:** March 15, 2026  
**Maintained by:** Development Team  
**Review Schedule:** Monthly
