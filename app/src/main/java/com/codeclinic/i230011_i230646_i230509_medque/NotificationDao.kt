package com.codeclinic.i230011_i230646_i230509_medque

import androidx.room.*

@Dao
interface NotificationDao {

    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getAllNotifications(userId: Int): List<NotificationEntity>

    @Query("SELECT * FROM notifications WHERE userId = :userId AND isRead = 0")
    suspend fun getUnreadNotifications(userId: Int): List<NotificationEntity>

    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0")
    suspend fun getUnreadCount(userId: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notifications: List<NotificationEntity>)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :notificationId AND userId = :userId")
    suspend fun markAsRead(notificationId: Int, userId: Int)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: Int)

    @Query("DELETE FROM notifications WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: Int)

    @Query("DELETE FROM notifications WHERE syncedAt < :timestamp")
    suspend fun deleteOldNotifications(timestamp: Long)
}