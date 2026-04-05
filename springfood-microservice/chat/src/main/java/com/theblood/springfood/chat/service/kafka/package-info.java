/**
 * Kafka consumers for chat message distribution and persistence.
 * 
 * <p>This package contains Kafka consumers that handle:
 * <ul>
 *   <li>Broadcasting messages to WebSocket clients (ChatMessageBroadcastConsumer)</li>
 *   <li>Persisting messages to database (ChatMessagePersistenceConsumer)</li>
 *   <li>Processing read receipts (ReadReceiptConsumer)</li>
 * </ul>
 * 
 * <p>Architecture:
 * <ul>
 *   <li><b>Broadcasting</b>: Each instance has unique consumer group ID for fan-out pattern</li>
 *   <li><b>Persistence</b>: Shared consumer group ID for load-balanced batch processing</li>
 * </ul>
 */
package com.theblood.springfood.chat.service.kafka;
