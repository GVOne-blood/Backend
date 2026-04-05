# Apache Kafka Deployment - Aiven

## Thông tin chung

**Platform:** Aiven (Managed Kafka)  
**Service Name:** springfood  
**Project ID:** dta150904-42aa  
**Account ID:** a4fa904a4599  
**Tier:** Free Tier (Startup-2)  
**Region:** Asia Pacific  
**Cloud Provider:** AWS

## Dashboard & Links

- **Console Dashboard:** https://console.aiven.io/account/a4fa904a4599/project/dta150904-42aa/services/springfood/overview
- **Documentation:** https://docs.aiven.io/docs/products/kafka

## Thông tin Server

### Connection Details
- **Service URI:** springfood-dta150904-42aa.k.aivencloud.com:24392
- **Host:** springfood-dta150904-42aa.k.aivencloud.com
- **Port:** 24392
- **Authentication Method:** Client Certificate (SSL/TLS)
- **Security Protocol:** SSL

### Network Configuration
- **Location:** Asia Pacific
- **Deployment Model:** Public Internet
- **IP Address Allowlist:** Open to all ⚠️

## SSL Certificates

Certificates được lưu tại: `deploy/messaging/certs/`
- Access Key (Private Key): `kafka-access-key.pem`
- Access Certificate: `kafka-access-cert.pem`
- CA Certificate: Download từ Aiven Console

**⚠️ Security:** Certificates không được commit vào Git (đã thêm vào .gitignore)

## Resource Limits

### Free Tier Specifications
- **Plan:** Startup-2
- **Storage:** Limited
- **Throughput:** Limited MB/s
- **Retention:** Default 7 days
- **Max Connections:** Limited

## Thời gian hiệu lực

- **Created:** March 15, 2026
- **Plan:** Free Tier
- **Upgrade Required:** Khi cần higher throughput hoặc storage

---

**Last Updated:** March 15, 2026  
**Maintained by:** Development Team
