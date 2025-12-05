# MedQue Upload Reports - Quick Start Guide

## 🚀 Quick Setup (3 Steps)

### Step 1: Create Database Table
1. Open phpMyAdmin: http://localhost/phpmyadmin
2. Select `medque_db` database
3. Click "SQL" tab
4. Copy and paste this SQL:

```sql
CREATE TABLE IF NOT EXISTS reports (
    id INT(11) AUTO_INCREMENT PRIMARY KEY,
    user_id INT(11) NOT NULL,
    report_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size VARCHAR(50) NOT NULL,
    file_type VARCHAR(100) NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
```

### Step 2: Create Upload Directory
Create this folder structure:
```
c:\xampp\htdocs\medque_app\uploads\reports\
```

### Step 3: Test It!
1. Start XAMPP (Apache + MySQL)
2. Open in browser: http://localhost/medque_app/test_reports.html
3. Upload a test file with user_id = 1
4. Run your Android app and test the Upload Reports screen

## 📁 Files Created

### PHP APIs (in c:\xampp\htdocs\medque_app\)
- ✅ `upload_report.php` - Upload files
- ✅ `get_reports.php` - Get user's reports
- ✅ `delete_report.php` - Delete reports
- ✅ `test_reports.html` - Test page for APIs
- ✅ `create_reports_table.sql` - Database setup script

### Android Files (Updated)
- ✅ `Upload_reports.kt` - Now fully dynamic with file picker and API integration

## 🎯 What Changed

### Before (Static/Hardcoded)
- ❌ Fake file name: "Lab_Report_Jan_2024.pdf"
- ❌ Fake file size: "1.2 MB"
- ❌ No actual file upload
- ❌ No database storage
- ❌ Submit button just goes back to Home

### After (Dynamic)
- ✅ Real file picker to select files
- ✅ Shows actual file name and size
- ✅ Uploads file to server
- ✅ Stores in database with user_id
- ✅ Files saved in uploads/reports/
- ✅ Remove file option before upload
- ✅ Success/error messages

## 🔧 Testing Instructions

### Test from Browser (Quick Test)
1. Open: http://localhost/medque_app/test_reports.html
2. Enter user_id: 1 (or any existing user ID from your users table)
3. Select a file (PDF, image, or document)
4. Click "Upload Report"
5. See success message
6. Click "Get Reports" to see all uploaded files
7. Check `c:\xampp\htdocs\medque_app\uploads\reports\` folder

### Test from Android App
1. Run the app on emulator or device
2. Login with valid credentials
3. Navigate to Upload Reports screen
4. Tap the upload area
5. Select a file from your device
6. See file name and size appear
7. Tap "Submit Report"
8. Get success message
9. Verify in phpMyAdmin that record was created

## 🔑 Important Notes

### For Android Emulator
URL in code is already correct:
```kotlin
val url = "http://10.0.2.2/medque_app/upload_report.php"
```

### For Real Android Device
1. Find your computer's IP address:
   - Open Command Prompt (Windows)
   - Type: `ipconfig`
   - Look for "IPv4 Address" (e.g., 192.168.1.100)

2. Update URL in `Upload_reports.kt`:
```kotlin
val url = "http://192.168.1.100/medque_app/upload_report.php"
```

3. Make sure phone and computer are on same WiFi network

### User ID Storage
The app gets user_id from SharedPreferences. Make sure your login screen saves it:
```kotlin
val sharedPreferences = getSharedPreferences("MedQuePrefs", MODE_PRIVATE)
sharedPreferences.edit().putInt("user_id", userId).apply()
```

## 📊 Database Structure

```
reports table:
├── id (Primary Key, Auto-increment)
├── user_id (Foreign Key → users.id)
├── report_name (Original filename)
├── file_path (Server file path)
├── file_size (Formatted size)
├── file_type (MIME type)
└── uploaded_at (Timestamp)
```

## 🎨 Supported File Types
- PDF (.pdf)
- Images (.jpg, .jpeg, .png)
- Word Documents (.doc, .docx)
- Maximum size: 10MB

## ❓ Troubleshooting

### "Connection failed" error
- ✅ Start XAMPP (Apache + MySQL)
- ✅ Check if localhost/medque_app/ is accessible in browser

### "User not logged in" error
- ✅ Make sure user_id is saved in SharedPreferences during login

### "Failed to upload file" error
- ✅ Check if uploads/reports/ folder exists
- ✅ Verify folder has write permissions

### File not appearing
- ✅ Check uploads/reports/ folder manually
- ✅ Verify database has new record in reports table

## 🎉 Success Checklist
- [ ] Database table created
- [ ] uploads/reports/ folder exists
- [ ] XAMPP is running
- [ ] Test page uploads successfully
- [ ] Android app can pick files
- [ ] Android app uploads successfully
- [ ] Files appear in uploads/reports/
- [ ] Database records created

## 📞 Support
If you face any issues:
1. Check test_reports.html works first (browser test)
2. Verify database table exists in phpMyAdmin
3. Check uploads folder permissions
4. Ensure XAMPP services are running
5. Verify API URL matches your setup (emulator vs device)

---
**Project**: MedQue
**Feature**: Dynamic Upload Reports
**Date**: December 2025
